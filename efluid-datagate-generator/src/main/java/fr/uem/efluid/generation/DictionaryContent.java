package fr.uem.efluid.generation;

import fr.uem.efluid.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * Holder of content for a generated dictionary,
 * extracted from package def, annotation and all rules for API generation
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v2.0.0
 */
public class DictionaryContent {

    private final Collection<ParameterProjectDefinition> allProjects;
    private final Collection<ParameterDomainDefinition> allDomains;
    private final Collection<ParameterTableDefinition> allTables;
    private final Collection<ParameterLinkDefinition> allLinks;
    private final Collection<ParameterMappingDefinition> allMappings;
    private final Collection<ParameterVersionDefinition> allVersions;

    DictionaryContent(
            Map<String, ParameterProjectDefinition> projectDefs,
            Collection<ParameterDomainDefinition> allDomains,
            Map<Class<?>, List<ParameterTableDefinition>> typeTables,
            Collection<ParameterLinkDefinition> allLinks,
            Collection<ParameterMappingDefinition> allMappings,
            Collection<ParameterVersionDefinition> allVersions) {

        this.allProjects = projectDefs.values();
        this.allDomains = allDomains;
        this.allTables = typeTables.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        this.allLinks = allLinks;
        this.allMappings = allMappings;
        this.allVersions = allVersions;
    }

    public Collection<ParameterProjectDefinition> getAllProjects() {
        return allProjects;
    }

    public Collection<ParameterDomainDefinition> getAllDomains() {
        return allDomains;
    }

    public Collection<ParameterTableDefinition> getAllTables() {
        return allTables;
    }

    public Collection<ParameterLinkDefinition> getAllLinks() {
        return allLinks;
    }

    public Collection<ParameterMappingDefinition> getAllMappings() {
        return allMappings;
    }

    public Collection<ParameterVersionDefinition> getAllVersions() {
        return allVersions;
    }
}
