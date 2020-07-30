package fr.uem.efluid.model.repositories.impls;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.ContentLine;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.tools.ManagedValueConverter;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import fr.uem.efluid.utils.IntSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * <p>
 * Standardized model for managed database value extractor. RS process is streamed for lower memory use.
 * Each content can be easily processed as <tt>ContentLine</tt>
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.8
 */
public abstract class InternalExtractor<T> implements ResultSetExtractor<Stream<ContentLine>> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(InternalExtractor.class);

    private final DictionaryEntry parameterEntry;
    private final ManagedValueConverter valueConverter;

    private final boolean useLabelForColNames;

    /**
     * @param parameterEntry
     */
    public InternalExtractor(DictionaryEntry parameterEntry, ManagedValueConverter valueConverter, boolean useLabelForColNames) {
        super();
        this.parameterEntry = parameterEntry;
        this.valueConverter = valueConverter;
        this.useLabelForColNames = useLabelForColNames;
    }

    /**
     * Init a stream on content
     *
     * @param rs
     * @return
     * @throws SQLException
     * @throws DataAccessException
     * @see ResultSetExtractor#extractData(ResultSet)
     */
    @Override
    public Stream<ContentLine> extractData(ResultSet rs) throws SQLException, DataAccessException {

        final AtomicInteger totalSize = new AtomicInteger(1);

        final Set<String> keyNames = this.parameterEntry.keyNames().map(String::toUpperCase).collect(Collectors.toSet());
        ResultSetMetaData meta = rs.getMetaData();

        // Prepare data definition from metadata
        final int count = meta.getColumnCount();
        final String[] columnNames = new String[count];
        final ColumnType[] columnType = new ColumnType[count];
        final int[] nativeTypes = new int[count];
        final IntSet keyPos = new IntSet();

        // Identify columns from prepared
        for (int i = 0; i < count; i++) {
            String colname = extractColumnName(meta, i);
            if (keyNames.contains(colname)) {
                keyPos.add(i);
            }
            columnNames[i] = colname;

            int nativeType = meta.getColumnType(i + 1);
            nativeTypes[i] = nativeType;
            columnType[i] = ColumnType.forJdbcType(nativeType);
        }

        return StreamSupport.stream(
                new Spliterators.AbstractSpliterator<ContentLine>(Long.MAX_VALUE, Spliterator.ORDERED) {

                    @Override
                    public boolean tryAdvance(Consumer<? super ContentLine> action) {
                        try {
                            if (!rs.next()) return false;

                            T payload = initLineHolder();
                            StringBuilder keyValue = new StringBuilder();
                            for (int i = 0; i < count; i++) {
                                try {
                                    if (!keyPos.contains(i)) {
                                        // Internally specified extractor process
                                        appendProcessValue(InternalExtractor.this.valueConverter, payload, columnType[i], nativeTypes[i], columnNames[i], i + 1, rs);

                                    } else {
                                        InternalExtractor.this.valueConverter.appendExtractedKeyValue(keyValue, rs.getString(i + 1));
                                    }

                                } catch (SQLException s) {
                                    throw new DataRetrievalFailureException("Cannot process append on value #" + i + " from dataset, of type "
                                            + nativeTypes[i] + " at column " + columnNames[i] + ". Identified as internal type " + columnType[i], s);
                                }
                            }

                            ExtractedContentLine line = new ExtractedContentLine(keyValue.toString(), getFinalizedPayload(InternalExtractor.this.valueConverter, payload));

                            // Only on debug : alert on large data load
                            if (LOGGER.isDebugEnabled()) {
                                if (totalSize.get() % 100000 == 0) {
                                    LOGGER.debug("Large data extraction - table \"{}\" - extracted {} items", InternalExtractor.this.parameterEntry.getTableName(), totalSize);
                                }
                                totalSize.incrementAndGet();
                            }

                            action.accept(line);
                            return true;
                        } catch (SQLException ex) {
                            throw new ApplicationException(ErrorType.EXTRACTION_ERROR, ex);
                        }
                    }
                }, false).onClose(() -> {
            try {
                rs.close();
            } catch (SQLException t) {
                throw new DataRetrievalFailureException("Cannot process closure of RS", t);
            }
        });
    }

    /**
     * <p>
     * As the holder for a line payload before finalization depends on internal extraction
     * process, this method inits the required holder. Called at each line
     * </p>
     *
     * @return
     */
    protected abstract T initLineHolder();

    /**
     * <p>
     * From current converter, process completion of value payload. Called at each column
     * except key col
     * </p>
     *
     * @param currentValueConverter
     * @param lineHolder
     * @param type
     * @param nativeJdbcType
     * @param columnName
     * @param colPosition
     * @param rs
     * @throws DataAccessException
     */
    protected abstract void appendProcessValue(
            ManagedValueConverter currentValueConverter,
            T lineHolder,
            ColumnType type,
            int nativeJdbcType,
            String columnName,
            int colPosition,
            ResultSet rs) throws SQLException;

    /**
     * <p>
     * Finalize the holder for string payload generate. As the holder and the payload
     * models can depends on extractor process, this abstract method allows to customize
     * the completion of a line. Called after each line
     * </p>
     *
     * @param currentValueConverter
     * @param lineHolder
     * @return
     */
    protected abstract String getFinalizedPayload(ManagedValueConverter currentValueConverter, T lineHolder);

    /**
     * Regarding the database type, the behavior of "label" may be different. Can switch between name or label
     *
     * @param meta
     * @param index
     * @return
     * @throws SQLException
     */
    private String extractColumnName(ResultSetMetaData meta, int index) throws SQLException {
        if (this.useLabelForColNames) {
            return meta.getColumnLabel(index + 1).toUpperCase();
        }
        return meta.getColumnName(index + 1).toUpperCase();
    }

    /**
     * One line of content extracted in a stream
     */
    private static class ExtractedContentLine implements ContentLine {

        private final String key;
        private final String payload;

        public ExtractedContentLine(String key, String payload) {
            this.key = key;
            this.payload = payload;
        }

        @Override
        public String getKeyValue() {
            return this.key;
        }

        @Override
        public String getPayload() {
            return this.payload;
        }
    }
}