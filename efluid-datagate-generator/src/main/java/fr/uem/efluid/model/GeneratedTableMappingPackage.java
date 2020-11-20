package fr.uem.efluid.model;

import fr.uem.efluid.services.types.TableMappingExportPackage;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class GeneratedTableMappingPackage extends TableMappingExportPackage<ParameterMappingDefinition> {

    private static final String RELOADABLE_TYPE = "fr.uem.efluid.services.types.TableMappingPackage";

    /**
     * @param allMappings all included entities in package
     */
    public GeneratedTableMappingPackage(Collection<ParameterMappingDefinition> allMappings) {
        super(TableMappingExportPackage.MAPPINGS_EXPORT, LocalDateTime.now());
        from(allMappings.stream().sorted(Comparator.comparing(ParameterMappingDefinition::getTableTo))
                .collect(Collectors.toList()));
    }

    /**
     * @see fr.uem.efluid.services.types.SharedPackage#initContent()
     */
    @Override
    protected ParameterMappingDefinition initContent() {
        return new ParameterMappingDefinition();
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