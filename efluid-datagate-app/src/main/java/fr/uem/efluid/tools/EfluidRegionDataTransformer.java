package fr.uem.efluid.tools;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.utils.ApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.uem.efluid.utils.ErrorType.TRANSFORMER_EFUID_NO_SITE;
import static fr.uem.efluid.utils.ErrorType.TRANSFORMER_EFUID_WRONG_REGION_DATA;

/**
 * Transformer for region-scoped select of values at import
 *
 * @author elecomte
 * @version 1
 * @since v2.1.16
 */
@Component
public class EfluidRegionDataTransformer extends Transformer<EfluidRegionDataTransformer.Config, EfluidRegionDataTransformer.Runner> {

    @Autowired
    public EfluidRegionDataTransformer(ManagedValueConverter converter, TransformerValueProvider provider) {
        super(converter, provider);
    }

    @Override
    public String getName() {
        return "EFLUID_REGION_SUPPORT";
    }

    @Override
    protected EfluidRegionDataTransformer.Config newConfig() {
        return new EfluidRegionDataTransformer.Config();
    }

    @Override
    protected EfluidRegionDataTransformer.Runner runner(EfluidRegionDataTransformer.Config config, DictionaryEntry dict) {
        return new EfluidRegionDataTransformer.Runner(getValueProvider(), config, dict);
    }

    public static class Config extends Transformer.TransformerConfig {

        private static final String CSV_SEPARATOR = ";";

        private static final String[] SOURCE_COLS = {"DIR", "TABNAME", "OP", "COLS_PK"
                , "SRC_ID1", "SRC_ID2", "SRC_ID3", "SRC_ID4", "SRC_ID5"};

        private static final String SOURCE_QUERY =
                "SELECT " + String.join(", ", SOURCE_COLS) + " FROM TRECOPIEPARAMREFERENTIELDIR";

        private static final String GET_REGION_QUERY =
                "SELECT SITE FROM TAPPLICATIONINFO WHERE PROJET = ?";

        private String project;

        @JsonIgnore
        private transient Map<String, List<RegionParameter>> parameters = new HashMap<>();

        public String getProject() {
            return this.project;
        }

        public void setProject(String project) {
            this.project = project;
        }

        Map<String, List<RegionParameter>> getParameters() {
            return this.parameters;
        }

        @Override
        void populateDefault() {
            super.populateDefault();
            this.project = "project";
        }

        /**
         * Load the source table as a CSV file and keep it in-memory (requires to load the whole table ...)
         *
         * @param managedSource for managed database attachment loading
         * @return loaded content of table TRECOPIEPARAMREFERENTIELDIR in CSV format
         */
        @Override
        public byte[] exportAttachmentPackageData(JdbcTemplate managedSource) {

            // TODO : do not load in memory but prepare an export source for low memory use at export

            StringBuilder csvInMemory = new StringBuilder();

            // Header
            csvInMemory.append(String.join(CSV_SEPARATOR, SOURCE_COLS)).append("\n");

            // Content
            managedSource.query(SOURCE_QUERY, (rs) -> {
                appendRowToCsv(rs, csvInMemory);
            });

            // Keep data of CSV
            return csvInMemory.toString().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public void importAttachmentPackageData(byte[] attachmentPackageData, JdbcTemplate managedSource, ManagedValueConverter valueConverter) {
            String site = managedSource.query(GET_REGION_QUERY, new Object[]{this.project}, (rs) -> {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            });

            if (site == null) {
                throw new ApplicationException(TRANSFORMER_EFUID_NO_SITE, "no site found with query "
                        + GET_REGION_QUERY + "on specified project " + this.project);
            }

            boolean header = true;
            try (InputStream is = new ByteArrayInputStream(attachmentPackageData)) {
                try (Scanner s = new Scanner(is)) {

                    // Parse CSV data
                    while (s.hasNextLine()) {
                        if (!header) {
                            RegionParameter param = new RegionParameter(valueConverter, s.nextLine().split(CSV_SEPARATOR));
                            if (site.equals(param.getSite())) {
                                this.parameters
                                        .computeIfAbsent(param.getTable(), k -> new ArrayList<>())
                                        .add(param);
                            }
                        } else {
                            header = false;
                        }
                    }
                }
            } catch (IOException e) {
                throw new ApplicationException(TRANSFORMER_EFUID_WRONG_REGION_DATA, "cannot read attachment CSV" +
                        " with region config on specified project " + this.project, e);
            }
        }

        private static void appendRowToCsv(ResultSet row, StringBuilder builder) throws SQLException {

            for (int i = 1; i < SOURCE_COLS.length; i++) {
                builder.append(row.getString(i)).append(';');
            }

            // Last with new line for easier use
            builder.append(row.getString(SOURCE_COLS.length)).append('\n');
        }

    }

    public static class Runner extends Transformer.TransformerRunner<EfluidRegionDataTransformer.Config> {

        private final Map<IndexAction, Set<String>> matchingKeys;

        public Runner(TransformerValueProvider provider, Config config, DictionaryEntry dict) {
            super(provider, config, dict);
            List<RegionParameter> params = config.getParameters().get(dict.getTableName());
            this.matchingKeys = params != null
                    ? params.stream().collect(Collectors.groupingBy(RegionParameter::getOp, Collectors.mapping(RegionParameter::getKey, Collectors.toSet())))
                    : null;
        }

        @Override
        public void transform(IndexAction action, String key, List<Value> values) {

            // Keep only matching lines, other are dropped
            if (!this.matchingKeys.get(action).contains(key)) {
                values.clear();
            }
        }

        @Override
        public boolean test(PreparedIndexEntry preparedIndexEntry) {
            return this.matchingKeys != null;
        }
    }

    private static class RegionParameter {

        private final String site;
        private final String table;
        private final IndexAction op;
        private final String key;
        // "DIR", "TABNAME", "OP", "COLS_PK", "SRC_ID1", "SRC_ID2", "SRC_ID3", "SRC_ID4", "SRC_ID5"

        RegionParameter(ManagedValueConverter valueConverter, String[] row) {
            this.site = row[0].trim();
            this.table = row[1].trim();
            this.op = EfluidActionToDatagateAction(row[2].trim());
            //  + SOURCE + DEST
            long pkCount = Stream.of(row[3].trim().split("\\+ "))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .count();
            StringBuilder keyBuild = new StringBuilder();
            for (int i = 0; i < pkCount; i++) {
                valueConverter.appendExtractedKeyValue(keyBuild, row[i + 4].trim());
            }
            this.key = keyBuild.toString();
        }

        public String getSite() {
            return this.site;
        }

        public String getTable() {
            return this.table;
        }

        public IndexAction getOp() {
            return this.op;
        }

        public String getKey() {
            return this.key;
        }

        private static IndexAction EfluidActionToDatagateAction(String action) {
            if ("INS".equals(action)) {
                return IndexAction.ADD;
            }
            return IndexAction.UPDATE;
        }

    }
}
