package fr.uem.efluid.services.types;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.TableLink;
import fr.uem.efluid.model.metas.ColumnDescription;

/**
 * <p>
 * Details TO for Dependencies and FK update / rendering for a dictionary entry
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
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
	 * @param uuid
	 *            the uuid to set
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
	 * @param table
	 *            the table to set
	 */
	public void setTable(String table) {
		this.table = table;
	}

	/**
	 * @param domainUuid
	 *            the domainUuid to set
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
	 * @param name
	 *            the name to set
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
	 * @param where
	 *            the where to set
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
	 * @param columns
	 *            the columns to set
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
	 * @param missingTable
	 *            the missingTable to set
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
	 * @since v0.0.1
	 * @version 1
	 */
	public static final class ColumnEditData implements Comparable<ColumnEditData> {

		@NotNull
		private String name;

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
		 * @return the name
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * @param name
		 *            the name to set
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
		 * @param selected
		 *            the selected to set
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
		 * @param type
		 *            the type to set
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
		 * @param key
		 *            the key to set
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
		 * @param foreignKeyTable
		 *            the foreignKeyTable to set
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
		 * @param foreignKeyColumn
		 *            the foreignKeyColumn to set
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

			// Priority on columns of type generated

			if (this.getType() == ColumnType.PK && o.getType() != ColumnType.PK) {
				return -1;
			}

			if (this.getType() != ColumnType.PK && o.getType() == ColumnType.PK) {
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
		 * @param linkedTable
		 *            from existing link has priority over col foreignKeyTable
		 * @return
		 */
		public static ColumnEditData fromColumnDescription(ColumnDescription col, Collection<String> selecteds, String keyValue,
				TableLink link) {
			ColumnEditData editData = new ColumnEditData();
			if (link == null) {
				editData.setForeignKeyTable(col.getForeignKeyTable());
				editData.setForeignKeyColumn(col.getForeignKeyColumn());
			} else {
				editData.setForeignKeyTable(link.getTableTo());
				editData.setForeignKeyColumn(link.getColumnTo());
			}
			editData.setName(col.getName());
			editData.setType(col.getType());
			if (selecteds != null) {
				// Excludes selected from name
				if (col.getName().equals(keyValue)) {
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
		 * @param keyname
		 * @param linkedTable
		 *            from existing link
		 * @return
		 */
		public static ColumnEditData fromSelecteds(String selected, String keyname, ColumnType keyType, TableLink link) {
			ColumnEditData editData = new ColumnEditData();
			editData.setName(selected);
			if (selected.equals(keyname)) {
				editData.setKey(true);
				editData.setType(keyType);
			} else {
				editData.setSelected(true);
			}
			if (link != null) {
				editData.setForeignKeyTable(link.getTableTo());
				editData.setForeignKeyColumn(link.getColumnTo());
			}
			return editData;
		}
	}

}
