package fr.uem.efluid.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import fr.uem.efluid.services.types.ProjectExportPackage;

/**
 * @author elecomte
 * @since v0.2.0
 * @version 1
 */
public class GeneratedProjectPackage extends ProjectExportPackage<ParameterProjectDefinition> {

	private static final String RELOADABLE_TYPE = "fr.uem.efluid.services.types.ProjectPackage";

	/**
	 *
	 * @param allProjects
	 */
	public GeneratedProjectPackage(Collection<ParameterProjectDefinition> allProjects) {
		super(ProjectExportPackage.PROJECTS_EXPORT, LocalDateTime.now());
		initWithContent(allProjects.stream().sorted(Comparator.comparing(ParameterProjectDefinition::getName))
				.collect(Collectors.toList()));
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.SharedPackage#initContent()
	 */
	@Override
	protected ParameterProjectDefinition initContent() {
		return new ParameterProjectDefinition();
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.SharedPackage#getReloadableTypeName()
	 */
	@Override
	public String getReloadableTypeName() {
		// Allows reload at import from app
		return RELOADABLE_TYPE;
	}
}