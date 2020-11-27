package fr.uem.efluid.model;

import fr.uem.efluid.services.types.FunctionalDomainExportPackage;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class GeneratedFunctionalDomainPackage extends FunctionalDomainExportPackage<ParameterDomainDefinition> {

    private static final String RELOADABLE_TYPE = "fr.uem.efluid.services.types.FunctionalDomainPackage";

    /**
     * @param allDomains all included entities in package
     */
    public GeneratedFunctionalDomainPackage(Collection<ParameterDomainDefinition> allDomains) {
        super(FunctionalDomainExportPackage.DOMAINS_EXPORT, LocalDateTime.now());
        from(allDomains.stream().sorted(Comparator.comparing(ParameterDomainDefinition::getName)));
    }

    /**
     * @see fr.uem.efluid.services.types.SharedPackage#initContent()
     */
    @Override
    protected ParameterDomainDefinition initContent() {
        return new ParameterDomainDefinition();
    }

    /**
     * @see fr.uem.efluid.services.types.SharedPackage#getReloadableTypeName()
     */
    @Override
    public String getReloadableTypeName() {
        // Allows reload at import from app
        return RELOADABLE_TYPE;
    }
}