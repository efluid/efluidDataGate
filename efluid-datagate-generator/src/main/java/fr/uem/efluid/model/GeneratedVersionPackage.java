package fr.uem.efluid.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import fr.uem.efluid.services.types.VersionExportPackage;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class GeneratedVersionPackage extends VersionExportPackage<ParameterVersionDefinition> {

	private static final String RELOADABLE_TYPE = "fr.uem.efluid.services.types.VersionPackage";

	/**
	 * @param name
	 * @param exportDate
	 */
	public GeneratedVersionPackage(Collection<ParameterVersionDefinition> allVersion) {
		super(VersionExportPackage.VERSIONS_EXPORT, LocalDateTime.now());
		initWithContent(allVersion.stream().sorted(Comparator.comparing(ParameterVersionDefinition::getCreatedTime))
				.collect(Collectors.toList()));
	}

	/**
	 * @return
	 * @see fr.uem.efluid.services.types.SharedPackage#initContent()
	 */
	@Override
	protected ParameterVersionDefinition initContent() {
		return new ParameterVersionDefinition();
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