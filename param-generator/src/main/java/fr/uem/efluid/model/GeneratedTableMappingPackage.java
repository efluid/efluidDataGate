package fr.uem.efluid.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import fr.uem.efluid.services.types.TableLinkExportPackage;
import fr.uem.efluid.services.types.TableMappingExportPackage;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class GeneratedTableMappingPackage extends TableMappingExportPackage<ParameterMappingDefinition> {

	private static final String RELOADABLE_TYPE = "fr.uem.efluid.services.types.TableMappingPackage";

	/**
	 * @param allMappings
	 */
	public GeneratedTableMappingPackage(Collection<ParameterMappingDefinition> allMappings) {
		super(TableLinkExportPackage.LINKS_EXPORT, LocalDateTime.now());
		initWithContent(allMappings.stream().sorted(Comparator.comparing(ParameterMappingDefinition::getTableTo))
				.collect(Collectors.toList()));
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.SharedPackage#initContent()
	 */
	@Override
	protected ParameterMappingDefinition initContent() {
		return new ParameterMappingDefinition();
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