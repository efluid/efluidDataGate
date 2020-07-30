package fr.uem.efluid.model.repositories;

import fr.uem.efluid.model.ContentLine;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.utils.ApplicationException;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * <p>
 * Access to raw data of parameters : can read from Parameter source table or regenerate
 * from existing index. Managed sources are specified as <code>DictionaryEntry</code>. Can
 * also process a stale <code>DictionaryEntry</code> and extract only for testing the
 * resulting content in actually managed database.
 * </p>
 * <p>
 * This is a main feature for the application : extracting data from managed database
 * </p>
 *
 * @author elecomte
 * @version 4
 * @since v0.0.1
 */
public interface ManagedExtractRepository {

    /**
     * <p>
     * Get real content from managed parameters tables
     * </p>
     *
     * @param parameterEntry table identifier for extraction
     * @param lobs           where the extracted lob values will be stored
     * @param project        working project
     * @return stream on content line rendering (key - payload map)
     */
    Extraction extractCurrentContent(DictionaryEntry parameterEntry, Map<String, byte[]> lobs, Project project);

    /**
     * <p>
     * For content testing on a temporary dictionaryEntry. Extract the corresponding
     * content from managed database. Will ignore links
     * </p>
     * <p>
     * Provides table content into initialized tableData list
     * </p>
     *
     * @param parameterEntry stale or existing details on a DictionarayEntry to extract
     * @param tableData      holder of required load <code>items</code> for corresponding query
     * @param limit          nbr of loaded items for <code>tableData</code>
     * @return the total count of items for query. Only <code>limit</code> items will be
     * specified into <code>tableData</code>
     */
    long testCurrentContent(DictionaryEntry parameterEntry, List<List<String>> tableData, long limit);

    /**
     * <p>
     * Get the content which should be extracted if unchecked joins was valid
     * </p>
     * <p>Must be "finalized" once completed</p>
     *
     * @param parameterEntry table identifier for extraction
     * @param project        working project
     * @return extracted missing content processing stream (with content lines)
     */
    Extraction extractCurrentMissingContentWithUncheckedJoins(DictionaryEntry parameterEntry, Project project);

    /**
     * <p>
     * For the specified table, get the current count of result with standard select
     * criteria and with unchecked join. This allows to compare if their is count
     * differences between extraction and existing values, to check if their is some
     * unmatched join condition (this situation occurs a lot as EFLUID is mostly using
     * unspecified foreign key)
     * </p>
     *
     * @param parameterEntry table identifier for extraction
     * @param project        working project
     * @return current count with unchecked join
     */
    int countCurrentContentWithUncheckedJoins(DictionaryEntry parameterEntry, Project project);

    /**
     * <p>Definition of the pointer to a content extraction, as a stream of <tt>ContentLine</tt> and any required source</p>
     * <p>Extraction is transactional and requires to be closed after all transformation, in a finally bloc, or in automatic
     * closure in a "try-with-resource" statement</p>
     *
     * @author elecomte
     * @version 1
     * @since v2.0.7
     */
    interface Extraction extends AutoCloseable {

        /**
         * Extraction content, as a prepared stream
         *
         * @return stream to content
         */
        Stream<ContentLine> stream();

        @Override
        void close() throws ApplicationException;
    }
}
