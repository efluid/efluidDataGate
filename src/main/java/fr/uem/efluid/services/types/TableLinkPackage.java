package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.entities.TableLink;
import fr.uem.efluid.services.ExportImportService.ExportImportPackage;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class TableLinkPackage extends ExportImportPackage<TableLink> {

	/**
	 * @param name
	 * @param exportDate
	 */
	public TableLinkPackage(String name, LocalDateTime exportDate) {
		super(name, exportDate);
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.ExportImportPackage#getVersion()
	 */
	@Override
	public String getVersion() {
		return "1";
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.ExportImportPackage#initContent()
	 */
	@Override
	protected TableLink initContent() {
		return new TableLink();
	}

}
