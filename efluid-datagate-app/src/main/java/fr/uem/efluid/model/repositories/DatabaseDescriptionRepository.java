package fr.uem.efluid.model.repositories;

import fr.uem.efluid.model.metas.TableDescription;
import fr.uem.efluid.utils.ApplicationException;

import java.util.Collection;

/**
 * <p>
 * Provider of all table descriptions for Managed database. Implements can use various
 * stategy to get access to table descriptions, and use a cache.
 * </p>
 *
 * @author elecomte
 * @version 3
 * @since v0.0.1
 */
public interface DatabaseDescriptionRepository {

    /**
     * @return completed TableDescription for the managed database.
     */
    Collection<TableDescription> getTables() throws ApplicationException;

    boolean isTableExists(String tableName);

    /**
     * <p>
     * Check if for specified table, the given column combination has a unique value
     * </p>
     */
    boolean isColumnSetHasUniqueValue(String tableName, Collection<String> colNames, String filterClause);

    /**
     * Check if the specified filterClause can apply on specified table. Run a basic select query using the filter, and check if the
     * qury can run without error
     *
     * @param tableName
     * @param filterClause
     * @return
     */
    boolean isFilterCanApply(String tableName, String filterClause);

    /**
     * Force refresh on cached data if any
     */
    void refreshAll();

    /**
     * Force refresh on one specified table only
     *
     * @param tablename
     */
    void refreshTable(String tablename);
}
