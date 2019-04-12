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
 * @version 2
 * @since v0.0.1
 */
public interface DatabaseDescriptionRepository {

    /**
     * @return completed TableDescription for the managed database.
     * @throws ApplicationException
     */
    Collection<TableDescription> getTables() throws ApplicationException;

    /**
     * @param tableName
     * @return
     */
    boolean isTableExists(String tableName);

    /**
     * <p>
     * Check if for specified table, the given column combination has a unique value
     * </p>
     *
     * @param tableName
     * @param colNames
     * @param filterClause
     * @return
     */
    boolean isColumnSetHasUniqueValue(String tableName, Collection<String> colNames, String filterClause);

    /**
     * Force refresh on cached data if any
     */
    void refreshAll();
}
