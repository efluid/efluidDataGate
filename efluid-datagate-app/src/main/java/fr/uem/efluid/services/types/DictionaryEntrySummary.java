package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.DictionaryEntry;

import java.util.UUID;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public final class DictionaryEntrySummary implements Comparable<DictionaryEntrySummary> {

    private final UUID uuid;

    private final UUID domainUuid;

    private final String domainName;

    private final String name;

    private final String query;

    private final String tableName;

    private final String keyName;

    private boolean canDelete;

    private DictionaryEntrySummary(UUID uuid, UUID domainUuid, String domainName, String name, String tableName, String keyName, String query) {
        super();
        this.uuid = uuid;
        this.domainUuid = domainUuid;
        this.domainName = domainName;
        this.name = name;
        this.tableName = tableName;
        this.keyName = keyName;
        this.query = query;
    }

    /**
     * @return the uuid
     */
    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * @return the domainUuid
     */
    public UUID getDomainUuid() {
        return this.domainUuid;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the query
     */
    public String getQuery() {
        return this.query;
    }

    /**
     * @return the domainName
     */
    public String getDomainName() {
        return this.domainName;
    }

    public String getTableName() {
        return this.tableName;
    }

    public String getKeyName() {
        return this.keyName;
    }

    /**
     * @return the canDelete
     */
    public boolean isCanDelete() {
        return this.canDelete;
    }

    /**
     * @param canDelete the canDelete to set
     */
    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    /**
     * @param o
     * @return
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(DictionaryEntrySummary o) {

        int dom = this.getDomainName().compareTo(o.getDomainName());

        if (dom != 0) {
            return dom;
        }

        return this.getName().compareTo(o.getName());
    }

    /**
     * @param entity
     * @return
     */
    public static DictionaryEntrySummary fromEntity(DictionaryEntry entity, String selectQuery) {
        return new DictionaryEntrySummary(
                entity.getUuid(),
                entity.getDomain().getUuid(),
                entity.getDomain().getName(),
                entity.getParameterName(),
                entity.getTableName(),
                entity.getKeyName(),
                selectQuery);
    }
}