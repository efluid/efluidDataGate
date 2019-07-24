package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.entities.Project;

/**
 * @author elecomte
 * @since v0.2.0
 * @version 1
 */
public class ProjectPackage extends ProjectExportPackage<Project> {

	/**
	 * @param name
	 * @param exportDate
	 */
	public ProjectPackage(String name, LocalDateTime exportDate) {
		super(name, exportDate);
	}

	/**
	 * @return
	 */
	@Override
	protected Project initContent() {
		return new Project();
	}

}
