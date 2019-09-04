package fr.uem.efluid.services.types;

import java.util.List;

/**
 * <p>
 * DTO for version diff. Hold details in the dictionary content hierarchy.
 * Keeps a kind of hierarchy on identified changes
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v2.0.0
 */
public class VersionCompare {

    private final VersionData one;

    private final VersionData two;

    public List<DomainChanges> domainChanges;

    public VersionCompare(VersionData one, VersionData two) {
        this.one = one;
        this.two = two;
    }

    public VersionData getOne() {
        return one;
    }

    public VersionData getTwo() {
        return two;
    }

    public List<DomainChanges> getDomainChanges() {
        return domainChanges;
    }

    public void setDomainChanges(List<DomainChanges> domainChanges) {
        this.domainChanges = domainChanges;
    }

    /**
     * Common definition for a set of changes identified on one item in a version.
     */
    public interface Changes {

        String getName();

        ChangeType getChangeType();
    }

    /**
     * Change details for a domain. List all table changes
     */
    public static class DomainChanges implements Changes {

        private ChangeType changeType;

        private String name;

        private List<DictionaryTableChanges> tableChanges;

        @Override
        public ChangeType getChangeType() {
            return changeType;
        }

        public void setChangeType(ChangeType changeType) {
            this.changeType = changeType;
        }

        @Override
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getUnmodifiedTableCount() {
            return this.tableChanges.stream()
                    .filter(c -> c.getChangeType() == ChangeType.UNCHANGED)
                    .count();
        }

        public List<DictionaryTableChanges> getTableChanges() {
            return tableChanges;
        }

        public void setTableChanges(List<DictionaryTableChanges> tableChanges) {
            this.tableChanges = tableChanges;
        }
    }

    /**
     * Details on one table, from a top level domain
     */
    public static class DictionaryTableChanges implements Changes {

        private ChangeType changeType;

        private String name;
        private String tableName;
        private String filter;

        private String tableNameChange;
        private String nameChange;
        private String filterChange;

        private List<ColumnChanges> columnChanges;


        @Override
        public ChangeType getChangeType() {
            return changeType;
        }

        public void setChangeType(ChangeType changeType) {
            this.changeType = changeType;
        }

        @Override
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getFilter() {
            return filter;
        }

        public void setFilter(String filter) {
            this.filter = filter;
        }

        public String getTableNameChange() {
            return tableNameChange;
        }

        public void setTableNameChange(String tableNameChange) {
            this.tableNameChange = tableNameChange;
        }

        public String getNameChange() {
            return nameChange;
        }

        public void setNameChange(String nameChange) {
            this.nameChange = nameChange;
        }

        public String getFilterChange() {
            return filterChange;
        }

        public void setFilterChange(String filterChange) {
            this.filterChange = filterChange;
        }

        public List<ColumnChanges> getColumnChanges() {
            return columnChanges;
        }

        public void setColumnChanges(List<ColumnChanges> columnChanges) {
            this.columnChanges = columnChanges;
        }
    }

    /**
     * Details for one column in a table
     */
    public static class ColumnChanges implements Changes {

        private ChangeType changeType;
        private String name;
        private String type;
        private String link;

        private String linkChange;

        @Override
        public ChangeType getChangeType() {
            return changeType;
        }

        public void setChangeType(ChangeType changeType) {
            this.changeType = changeType;
        }

        @Override
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getLinkChange() {
            return linkChange;
        }

        public void setLinkChange(String linkChange) {
            this.linkChange = linkChange;
        }
    }

    /**
     * Definition of the change identified for one type of item in a version compare process
     */
    public enum ChangeType {
        ADDED, REMOVED, MODIFIED, UNCHANGED
    }
}
