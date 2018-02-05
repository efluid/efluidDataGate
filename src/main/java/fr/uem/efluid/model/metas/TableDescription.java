package fr.uem.efluid.model.metas;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Describes a table which can be specified in the dictionary as a parameter table from
 * the managed database.
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class TableDescription {

	/**
	 * <p>
	 * Can be used as reference on identified missing table
	 * </p>
	 */
	public static final TableDescription MISSING = new TableDescription() {
		@Override
		public String getName() {
			return "nMn";
		}
	};

	private String name;
	private Set<ColumnDescription> columns = new HashSet<>();
	private boolean view;

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
	 * @return the columns
	 */
	public Set<ColumnDescription> getColumns() {
		return this.columns;
	}

	/**
	 * @param columns
	 *            the columns to set
	 */
	public void setColumns(Set<ColumnDescription> columns) {
		this.columns = columns;
	}

	/**
	 * @return the view
	 */
	public boolean isView() {
		return this.view;
	}

	/**
	 * @param view
	 *            the view to set
	 */
	public void setView(boolean view) {
		this.view = view;
	}

}
