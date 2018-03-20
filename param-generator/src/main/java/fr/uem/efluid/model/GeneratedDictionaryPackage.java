package fr.uem.efluid.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import fr.uem.efluid.services.types.DictionaryExportPackage;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class GeneratedDictionaryPackage extends DictionaryExportPackage<ParameterTableDefinition> {

	private static final String RELOADABLE_TYPE = "fr.uem.efluid.services.types.DictionaryPackage";

	/**
	 * @param name
	 * @param exportDate
	 */
	public GeneratedDictionaryPackage(Collection<ParameterTableDefinition> allTables) {
		super(DictionaryExportPackage.DICT_EXPORT, LocalDateTime.now());
		initWithContent(allTables.stream().sorted(Comparator.comparing(ParameterTableDefinition::getParameterName))
				.collect(Collectors.toList()));
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.SharedPackage#initContent()
	 */
	@Override
	protected ParameterTableDefinition initContent() {
		return new ParameterTableDefinition();
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.SharedPackage#getReloadableTypeName()
	 */
	@Override
	public String getReloadableTypeName() {
		// Allows reload at import from app
		return RELOADABLE_TYPE;
	}

}