package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.shared.ExportAwareFunctionalDomain;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public abstract class FunctionalExportDomainPackage<D extends ExportAwareFunctionalDomain> extends SharedPackage<D> {

	/**
	 * @param name
	 * @param exportDate
	 */
	public FunctionalExportDomainPackage(String name, LocalDateTime exportDate) {
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
