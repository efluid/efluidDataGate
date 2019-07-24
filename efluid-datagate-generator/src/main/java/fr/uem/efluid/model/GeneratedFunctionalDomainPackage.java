package fr.uem.efluid.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import fr.uem.efluid.services.types.FunctionalDomainExportPackage;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class GeneratedFunctionalDomainPackage extends FunctionalDomainExportPackage<ParameterDomainDefinition> {

	private static final String RELOADABLE_TYPE = "fr.uem.efluid.services.types.FunctionalDomainPackage";

	/**
	 * @param name
	 * @param exportDate
	 */
	public GeneratedFunctionalDomainPackage(Collection<ParameterDomainDefinition> allDomains) {
		super(FunctionalDomainExportPackage.DOMAINS_EXPORT, LocalDateTime.now());
		initWithContent(allDomains.stream().sorted(Comparator.comparing(ParameterDomainDefinition::getName))
				.collect(Collectors.toList()));
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.SharedPackage#initContent()
	 */
	@Override
	protected ParameterDomainDefinition initContent() {
		return new ParameterDomainDefinition();
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