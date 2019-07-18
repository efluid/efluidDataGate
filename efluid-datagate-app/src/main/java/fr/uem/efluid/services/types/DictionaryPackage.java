package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.entities.DictionaryEntry;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class DictionaryPackage extends DictionaryExportPackage<DictionaryEntry> {

	/**
	 * @param name
	 * @param exportDate
	 */
	public DictionaryPackage(String name, LocalDateTime exportDate) {
		super(name, exportDate);
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.SharedPackage#initContent()
	 */
	@Override
	protected DictionaryEntry initContent() {
		return new DictionaryEntry();
	}

}
