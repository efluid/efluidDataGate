package fr.uem.efluid.services.types;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.metas.ColumnDescription;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * Details TO for Dependencies and FK update / rendering for a dictionary entry
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
public class DictionaryEntryEditData {

    private UUID uuid;

    @NotNull
    private UUID domainUuid;

    @NotNull
    private String name;

    @NotNull
    private String table;

    @NotNull
    private String where;

    @NotEmpty
    private List<ColumnEditData> columns;

    private boolean missingTable;

    /**
     * @return the uuid
     */
    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the domainUuid
     */
    public UUID getDomainUuid() {
        return this.domainUuid;
    }

    /**
     * @return the table
     */
    public String getTable() {
        return this.table;
    }

    /**
     * @param table the table to set
     */
    public void setTable(String table) {
        this.table = table;
    }

    /**
     * @param domainUuid the domainUuid to set
     */
    public void setDomainUuid(UUID domainUuid) {
        this.domainUuid = domainUuid;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the where
     */
    public String getWhere() {
        return this.where;
    }

    /**
     * @param where the where to set
     */
    public void setWhere(String where) {
        this.where = where;
    }

    /**
     * @return the columns
     */
    public List<ColumnEditData> getColumns() {
        return this.columns;
    }

    /**
     * @param columns the columns to set
     */
    public void setColumns(List<ColumnEditData> columns) {
        this.columns = columns;
    }

    /**
     * @return the missingTable
     */
    public boolean isMissingTable() {
        return this.missingTable;
    }

    /**
     * @param missingTable the missingTable to set
     */
    public void setMissingTable(boolean missingTable) {
        this.missingTable = missingTable;
    }

    /**
     * @param entity
     * @return
     */
    public static DictionaryEntryEditData fromEntity(DictionaryEntry entity) {

        DictionaryEntryEditData edit = new DictionaryEntryEditData();

        edit.setUuid(entity.getUuid());
        edit.setDomainUuid(entity.getDomain().getUuid());
        edit.setName(entity.getParameterName());
        edit.setTable(entity.getTableName());
        edit.setWhere(entity.getWhereClause());

        return edit;
    }

    /**
     * <p>
     * One column desc edit for current DictionaryEntry
     * </p>
     *
     * @author elecomte
     * @version 1
     * @since v0.0.1
     */
    public static final class ColumnEditData implements Comparable<ColumnEditData> {

        @NotNull
        private String name;

        private String displayName;

        @NotNull
        private ColumnType type;

        private boolean key;

        private String foreignKeyTable;

        private String foreignKeyColumn;

        private boolean selected;

        /**
         *
         */
        public ColumnEditData() {
            super();
        }

        /**
         * @return the displayName
         */
        public String getDisplayName() {
            return this.displayName;
        }

        /**
         * @param displayName the displayName to set
         */
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        /**
         * @return the name
         */
        public String getName() {
            return this.name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the selected
         */
        public boolean isSelected() {
            return this.selected;
        }

        /**
         * @param selected the selected to set
         */
        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        /**
         * @return the type
         */
        public ColumnType getType() {
            return this.type;
        }

        /**
         * @param type the type to set
         */
        public void setType(ColumnType type) {
            this.type = type;
        }

        /**
         * @return the key
         */
        public boolean isKey() {
            return this.key;
        }

        /**
         * @param key the key to set
         */
        public void setKey(boolean key) {
            this.key = key;
        }

        /**
         * @return the foreignKeyTable
         */
        public String getForeignKeyTable() {
            return this.foreignKeyTable;
        }

        /**
         * @param foreignKeyTable the foreignKeyTable to set
         */
        public void setForeignKeyTable(String foreignKeyTable) {
            this.foreignKeyTable = foreignKeyTable;
        }

        /**
         * @return the foreignKeyColumn
         */
        public String getForeignKeyColumn() {
            return this.foreignKeyColumn;
        }

        /**
         * @param foreignKeyColumn the foreignKeyColumn to set
         */
        public void setForeignKeyColumn(String foreignKeyColumn) {
            this.foreignKeyColumn = foreignKeyColumn;
        }

        /**
         * @param o
         * @return
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(ColumnEditData o) {

            // Avoid error on imported dic without real ref

            if (this.getType() == null || o.getType() == null) {
                return -1;
            }

            // Priority on columns of type generated

            if (this.getType().isPk() && !o.getType().isPk()) {
                return -1;
            }

            if (!this.getType().isPk() && o.getType().isPk()) {
                return 1;
            }

            return this.name.compareTo(o.getName());
        }

        /**
         * <p>
         * Standard edit : full content
         * </p>
         *
         * @param col
         * @param selecteds
         * @param keyNames
         * @param linkUpdate from existing link has priority over col foreignKeyTable
         * @return
         */
        public static ColumnEditData fromColumnDescription(
                ColumnDescription col,
                Collection<String> selecteds,
                List<String> keyNames,
                LinkUpdateFollow linkUpdate) {

            ColumnEditData editData = new ColumnEditData();

            if (linkUpdate == null) {
                editData.setForeignKeyTable(col.getForeignKeyTable());
                editData.setForeignKeyColumn(col.getForeignKeyColumn());
            } else {
                editData.setForeignKeyTable(linkUpdate.getLink().getTableTo());
                editData.setForeignKeyColumn(linkUpdate.getLink().getColumnTo(linkUpdate.getIndexAndIncr()));
            }
            editData.setName(col.getName());
            editData.setType(col.getType());
            if (selecteds != null) {
                // Excludes selected from name
                if (keyNames.contains(col.getName())) {
                    editData.setKey(true);
                } else {
                    editData.setSelected(selecteds.contains(col.getName()));
                }
            }
            return editData;
        }

        /**
         * <p>
         * Edit on missing / not created yet table (possible situation when importing
         * external dictionary), simply simulate column from selected values.
         * </p>
         *
         * @param selected
         * @param keyNames
         * @param keyTypes
         * @param linkUpdate
         * @return
         */
        public static ColumnEditData fromSelecteds(
                String selected,
                List<String> keyNames,
                List<ColumnType> keyTypes,
                LinkUpdateFollow linkUpdate) {

            ColumnEditData editData = new ColumnEditData();
            editData.setName(selected);
            editData.setType(ColumnType.UNKNOWN);

            // Specify if key
            for (int i = 0; i < keyNames.size(); i++) {
                if (selected.equals(keyNames.get(i))) {
                    editData.setKey(true);
                    editData.setType(keyTypes.get(i));
                    break;
                }
            }

            editData.setSelected(true);

            if (linkUpdate != null) {
                editData.setForeignKeyTable(linkUpdate.getLink().getTableTo());
                editData.setForeignKeyColumn(linkUpdate.getLink().getColumnTo(linkUpdate.getIndexAndIncr()));
            }
            return editData;
        }

        public static enum ReferenceType {
            LINK,
            MAPPING;
        }

    }

}
