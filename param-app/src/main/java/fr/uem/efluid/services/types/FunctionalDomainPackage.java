package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.entities.FunctionalDomain;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class FunctionalDomainPackage extends FunctionalExportDomainPackage<FunctionalDomain> {

	/**
	 * @param name
	 * @param exportDate
	 */
	public FunctionalDomainPackage(String name, LocalDateTime exportDate) {
		super(name, exportDate);
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.SharedPackage#initContent()
	 */
	@Override
	protected FunctionalDomain initContent() {
		return new FunctionalDomain();
	}

}
