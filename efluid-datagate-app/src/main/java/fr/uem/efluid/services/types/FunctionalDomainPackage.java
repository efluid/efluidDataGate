package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.FunctionalDomain;

import java.time.LocalDateTime;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class FunctionalDomainPackage extends FunctionalDomainExportPackage<FunctionalDomain> {

    /**
     * @param name
     * @param exportDate
     */
    public FunctionalDomainPackage(String name, LocalDateTime exportDate) {
        super(name, exportDate);
    }

    /**
     * @return
     */
    @Override
    protected FunctionalDomain initContent() {
        return new FunctionalDomain();
    }
}
