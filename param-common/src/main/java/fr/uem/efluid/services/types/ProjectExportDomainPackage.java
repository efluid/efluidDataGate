package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.shared.ExportAwareProject;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public abstract class ProjectExportDomainPackage<P extends ExportAwareProject> extends SharedPackage<P> {

	public static final String PROJECTS_EXPORT = "full-projects";
	public static final String PARTIAL_PROJECTS_EXPORT = "partial-projects";

	/**
	 * @param name
	 * @param exportDate
	 */
	public ProjectExportDomainPackage(String name, LocalDateTime exportDate) {
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
