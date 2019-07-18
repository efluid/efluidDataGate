package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.entities.TableLink;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class TableLinkPackage extends TableLinkExportPackage<TableLink> {

	/**
	 * @param name
	 * @param exportDate
	 */
	public TableLinkPackage(String name, LocalDateTime exportDate) {
		super(name, exportDate);
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.SharedPackage#initContent()
	 */
	@Override
	protected TableLink initContent() {
		return new TableLink();
	}

}
