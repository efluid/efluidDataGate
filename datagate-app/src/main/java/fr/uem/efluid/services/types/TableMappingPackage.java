package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.entities.TableMapping;

/**
 * @author elecomte
 * @since v2.0.0
 * @version 1
 */
public class TableMappingPackage extends TableMappingExportPackage<TableMapping> {

	/**
	 * @param name
	 * @param exportDate
	 */
	public TableMappingPackage(String name, LocalDateTime exportDate) {
		super(name, exportDate);
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.SharedPackage#initContent()
	 */
	@Override
	protected TableMapping initContent() {
		return new TableMapping();
	}
}
