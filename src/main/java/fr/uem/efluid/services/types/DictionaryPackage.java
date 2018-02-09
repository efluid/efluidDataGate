package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.services.ExportImportService.ExportImportPackage;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class DictionaryPackage extends ExportImportPackage<DictionaryEntry> {

	/**
	 * @param name
	 * @param exportDate
	 */
	public DictionaryPackage(String name, LocalDateTime exportDate) {
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
	protected DictionaryEntry initContent() {
		return new DictionaryEntry();
	}

}
