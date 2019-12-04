package fr.uem.efluid.model;

import fr.uem.efluid.services.types.VersionExportPackage;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class GeneratedVersionPackage extends VersionExportPackage<ParameterVersionDefinition> {

    private static final String RELOADABLE_TYPE = "fr.uem.efluid.services.types.VersionPackage";

    /**
     * @param allVersion all included versions in package
     */
    public GeneratedVersionPackage(Collection<ParameterVersionDefinition> allVersion) {
        super(VersionExportPackage.VERSIONS_EXPORT, LocalDateTime.now());
        initWithContent(allVersion.stream().sorted(Comparator.comparing(ParameterVersionDefinition::getCreatedTime))
                .collect(Collectors.toList()));
    }

    /**
     * @see fr.uem.efluid.services.types.SharedPackage#initContent()
     */
    @Override
    protected ParameterVersionDefinition initContent() {
        return new ParameterVersionDefinition();
    }

    /**
     * @see fr.uem.efluid.services.types.SharedPackage#getReloadableTypeName()
     */
    @Override
    public String getReloadableTypeName() {
        // Allows reload at import from app
        return RELOADABLE_TYPE;
    }
}