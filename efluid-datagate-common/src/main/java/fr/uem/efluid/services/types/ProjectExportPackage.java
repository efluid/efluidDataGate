package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.shared.ExportAwareProject;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public abstract class ProjectExportPackage<P extends ExportAwareProject> extends SharedPackage<P> {

	public static final String PROJECTS_EXPORT = "full-projects";
	public static final String PARTIAL_PROJECTS_EXPORT = "partial-projects";

	/**
	 * @param name
	 * @param exportDate
	 */
	public ProjectExportPackage(String name, LocalDateTime exportDate) {
		super(name, exportDate);
	}

	/**
	 * @return
	 */
	@Override
	public final String getVersion() {
		return "1";
	}

}
