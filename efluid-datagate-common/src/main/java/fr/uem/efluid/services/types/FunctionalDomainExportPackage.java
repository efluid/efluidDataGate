package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.shared.ExportAwareFunctionalDomain;
import fr.uem.efluid.model.shared.ExportAwareProject;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public abstract class FunctionalDomainExportPackage<D extends ExportAwareFunctionalDomain<? extends ExportAwareProject>>
		extends SharedPackage<D> {

	public static final String DOMAINS_EXPORT = "full-domains";
	public static final String PARTIAL_DOMAINS_EXPORT = "partial-domains";

	/**
	 * @param name
	 * @param exportDate
	 */
	public FunctionalDomainExportPackage(String name, LocalDateTime exportDate) {
		super(name, exportDate);
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.SharedPackage#getVersion()
	 */
	@Override
	public final String getVersion() {
		return "3";
	}

}
