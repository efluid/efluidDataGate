package fr.uem.efluid.services.types;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import fr.uem.efluid.model.entities.FunctionalDomain;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
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

	public Stream<FunctionalDomain> content() {
		return super.content();
	}
}
