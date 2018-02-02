package fr.uem.efluid.services.types;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import fr.uem.efluid.model.entities.DictionaryEntry;
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

	private UUID domainUuid;

	private String name;

	private String table;

	private String where;

	private List<ColumnEditData> columns;

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

		private String name;
		private String type;
		private boolean primaryKey;
		private String foreignKeyTable;
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
		public String getType() {
			return this.type;
		}

		/**
		 * @param type
		 *            the type to set
		 */
		public void setType(String type) {
			this.type = type;
		}

		/**
		 * @return the primaryKey
		 */
		public boolean isPrimaryKey() {
			return this.primaryKey;
		}

		/**
		 * @param primaryKey
		 *            the primaryKey to set
		 */
		public void setPrimaryKey(boolean primaryKey) {
			this.primaryKey = primaryKey;
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
		 * @param o
		 * @return
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(ColumnEditData o) {
			return this.name.compareTo(o.getName());
		}

		/**
		 * @param col
		 * @return
		 */
		public static ColumnEditData fromColumnDescription(ColumnDescription col, Collection<String> selecteds) {
			ColumnEditData editData = new ColumnEditData();
			editData.setForeignKeyTable(col.getForeignKeyTable());
			editData.setName(col.getName());
			editData.setType(col.isBinary() ? "binary" : "atomic");
			editData.setPrimaryKey(col.isPrimaryKey());
			if (selecteds != null) {
				editData.setSelected(selecteds.contains(col.getName()));
			}
			return editData;
		}
	}

}
