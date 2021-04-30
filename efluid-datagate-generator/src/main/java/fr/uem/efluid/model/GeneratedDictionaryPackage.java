package fr.uem.efluid.model;

import fr.uem.efluid.services.types.DictionaryExportPackage;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class GeneratedDictionaryPackage extends DictionaryExportPackage<ParameterTableDefinition> {

    private static final String RELOADABLE_TYPE = "fr.uem.efluid.services.types.DictionaryPackage";

    /**
     * @param allTables for generate
     */
    public GeneratedDictionaryPackage(Collection<ParameterTableDefinition> allTables) {
        super(DictionaryExportPackage.DICT_EXPORT, LocalDateTime.now());
        from(allTables.stream().sorted(Comparator.comparing(ParameterTableDefinition::getParameterName)));
    }

    /**
     * @return initialized generation definition
     */
    @Override
    protected ParameterTableDefinition initContent() {
        return new ParameterTableDefinition(true);
    }

    /**
     * @return name
     */
    @Override
    public String getReloadableTypeName() {
        // Allows reload at import from app
        return RELOADABLE_TYPE;
    }

}