package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.shared.ExportAwareDictionaryEntry;
import fr.uem.efluid.model.shared.ExportAwareFunctionalDomain;
import fr.uem.efluid.model.shared.ExportAwareProject;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public abstract class DictionaryExportPackage<D extends ExportAwareDictionaryEntry<? extends ExportAwareFunctionalDomain<? extends ExportAwareProject>>>
		extends SharedPackage<D> {

	public static final String DICT_EXPORT = "full-dictionary";
	public static final String PARTIAL_DICT_EXPORT = "partial-dictionary";

	/**
	 * @param name
	 * @param exportDate
	 */
	public DictionaryExportPackage(String name, LocalDateTime exportDate) {
		super(name, exportDate);
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.SharedPackage#getVersion()
	 */
	@Override
	public final String getVersion() {
		return "1";
	}

}
