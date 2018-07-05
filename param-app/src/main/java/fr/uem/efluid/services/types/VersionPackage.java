package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.entities.Version;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class VersionPackage extends VersionExportPackage<Version> {

	/**
	 * @param name
	 * @param exportDate
	 */
	public VersionPackage(String name, LocalDateTime exportDate) {
		super(name, exportDate);
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.SharedPackage#initContent()
	 */
	@Override
	protected Version initContent() {
		return new Version();
	}

}
