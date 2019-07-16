package fr.uem.efluid.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import fr.uem.efluid.services.types.TableLinkExportPackage;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class GeneratedTableLinkPackage extends TableLinkExportPackage<ParameterLinkDefinition> {

	private static final String RELOADABLE_TYPE = "fr.uem.efluid.services.types.TableLinkPackage";

	/**
	 * @param name
	 * @param exportDate
	 */
	public GeneratedTableLinkPackage(Collection<ParameterLinkDefinition> allLinks) {
		super(TableLinkExportPackage.LINKS_EXPORT, LocalDateTime.now());
		initWithContent(allLinks.stream().sorted(Comparator.comparing(ParameterLinkDefinition::getTableTo))
				.collect(Collectors.toList()));
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.SharedPackage#initContent()
	 */
	@Override
	protected ParameterLinkDefinition initContent() {
		return new ParameterLinkDefinition();
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