package fr.uem.efluid.generation;

import fr.uem.efluid.*;
import fr.uem.efluid.model.*;
import fr.uem.efluid.utils.SelectClauseGenerator;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.uem.efluid.generation.GenerationUtils.*;

/**
 * <p>
 * Annotation processing / reflection based search for dictionary definition
 * </p>
 * <p>
 * Current version support inheritance and detect mappings
 * </p>
 *
 * @author elecomte
 * @version 5
 * @since v0.0.1
 */
public class DictionaryGenerator extends AbstractProcessor {

    public static final String VERSION = "5";

    private final SelectClauseGenerator selectClauseGen;

    final String DEBUG_BG_RED = "\u001B[41m";

    final String DEBUG_BG_BLUE = "\u001B[46m";

    final String DEBUG_RESET_COLOUR = "\u001B[0m";

    final String DEBUG_TEXT_BLACK = "\u001B[30m";

    /**
     *
     */
    public DictionaryGenerator(DictionaryGeneratorConfig config) {
        super(config);
        this.selectClauseGen = new SelectClauseGenerator(config.isProtectColumn());
    }

    /**
     * @param contextClassLoader processing classloader for api search
     */
    public DictionaryContent extractDictionary(ClassLoader contextClassLoader) {

        try {
            /* Search using filtering tools, based on a configured package root */
            Reflections reflections = initReflectionEntryPoint(contextClassLoader);

            /* Will prepare project, links and mappings when found */
            Map<String, ParameterProject> projects = new HashMap<>();
            Set<ParameterLinkDefinition> allLinks = new HashSet<>();
            Set<ParameterMappingDefinition> allMappings = new HashSet<>();

            /* Process all tables init + key for clean link building */
            Map<Class<?>, List<ParameterTableDefinition>> typeTables = initParameterTablesWithKeys(reflections,
                    searchDomainsByAnnotation(reflections, projects));

            /* Complete project definitions */
            Map<String, ParameterProjectDefinition> projectDefs = identifyParameterProjects(projects);

            /* Then process table values / links / mappings using refs */
            completeParameterValuesWithLinksAndMappings(typeTables, allLinks, allMappings, contextClassLoader);

            /* Then, extract domains */
            Collection<ParameterDomainDefinition> allDomains = completeParameterDomains(typeTables, projects, projectDefs);

            /* Then, prepare a version for each projects */
            Collection<ParameterVersionDefinition> allVersions = specifyVersionsByProjects(projectDefs.values());

            /* Finally, merge all duplicated changes and build final select clauses */
            combineTablesAndGenerateSelectClauses(typeTables, allLinks, allMappings);

            /* Now can export */
            return new DictionaryContent(projectDefs, allDomains, typeTables, allLinks, allMappings, allVersions);

        } catch (Exception e) {
            getLog().error("Process failed with", e);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Class<?>, String> searchDomainsByAnnotation(
            Reflections reflections,
            Map<String, ParameterProject> projects) {

        getLog().debug("Process domains spec search");

        // Prepare mapped domains
        Map<Class<?>, String> typeDomains = new HashMap<>();

        // Domains - direct search, with inherited
        reflections.getTypesAnnotatedWith(ParameterDomain.class, true)
                .stream()
                .filter(this::isValidMember)
                .forEach(typedDomain -> {
                    ParameterDomain domain = typedDomain.getAnnotation(ParameterDomain.class);

                    String domainName = domain.name();

                    // Specified project - keep it in ref
                    ParameterProject project = domain.project();
                    projects.put(domainName, project);

                    // Search for annotated with meta
                    if (Annotation.class.isAssignableFrom(typedDomain)) {
                        getLog().debug("Found domain meta-annotation as " + typedDomain.getName() + " with name \"" + domainName + "\"");
                        reflections.getTypesAnnotatedWith((Class<? extends Annotation>) typedDomain, true).stream()
                                .filter(this::isValidMember)
                                .peek(t -> getLog()
                                        .debug("For type " + t.getName() + " will use specified domain by meta-annotation \"" + domainName + "\""))
                                .forEach(t -> typeDomains.put(t, domainName));
                    }

                    // Directly specified (package)
                    else {
                        getLog().debug("Found domain directly on " + typedDomain + " with name \"" + domainName + "\"");
                        String packageName = typedDomain.getPackage().getName();

                        // Get all types for package, not directly annotated
                        Stream.concat(
                                reflections.getTypesAnnotatedWith(ParameterTable.class, true).stream(),
                                reflections.getTypesAnnotatedWith(ParameterTableSet.class, true).stream())
                                .filter(this::isValidMember)

                                .filter(c -> c.getPackage().getName().startsWith(packageName))
                                .filter(c -> !typeDomains.containsKey(c))
                                .peek(t -> getLog()
                                        .debug("For type " + t.getName() + " will use specified domain by package \"" + domainName + "\""))
                                .forEach(c -> typeDomains.put(c, domainName));
                    }
                });

        return typeDomains;
    }

    private Map<Class<?>, List<ParameterTableDefinition>> initParameterTablesWithKeys(
            Reflections reflections,
            Map<Class<?>, String> annotDomains) {

        getLog().debug("Process parameter table init with " + annotDomains.size() + " identified domain annotations");

        List<PossibleTableAnnotation> possibleTables = new ArrayList<>();
        Map<Class<?>, List<ParameterTableDefinition>> tables = new HashMap<>();

        // Search for possible tables with meta
        reflections.getTypesAnnotatedWith(ParameterTable.class, false).stream()
                .filter(Class::isAnnotation)
                .filter(this::isValidMember)
                .map(this::processParameterTableMeta)
                .filter(Objects::nonNull)
                .forEach(possibleTables::add);

        // Domains - direct search, with inherited
        reflections.getTypesAnnotatedWith(ParameterTable.class, false).stream()
                .filter(t -> !t.isAnnotation())
                .filter(t -> t.getAnnotation(ParameterTableSet.class) == null) // ParamTableSet "win"
                .filter(this::isValidMember)
                .map(t -> processParameterTableDirect(t, possibleTables))
                .forEach(possibleTables::add);

        // Then completed with ParameterTableSet
        reflections.getTypesAnnotatedWith(ParameterTableSet.class, false).stream()
                .filter(this::isValidMember)
                .flatMap(t -> processParameterTableSet(t, possibleTables))
                .forEach(possibleTables::add);

        // Prepare defs, organized by source types
        possibleTables.forEach(p -> {
            // Exclude meta types
            if (!p.isIntermediate()) {
                if (!p.getSourceClazz().isAnnotationPresent(ParameterIgnored.class)) {
                    tables.computeIfAbsent(p.getSourceClazz(), k -> new ArrayList<>())
                            .add(initOneParameterTableWithKeys(p, annotDomains));
                }
            }
        });

        return tables;
    }

    /**
     * <p>
     * For meta annotation with <tt>ParameterTable</tt>
     * </p>
     *
     * @param annotType meta annotation to search
     * @return possible table specs
     */
    private PossibleTableAnnotation processParameterTableMeta(Class<?> annotType) {

        // Table cfg - includes inherited
        ParameterTable paramTable = annotType.getAnnotation(ParameterTable.class);

        // Only process concrete use of annotation
        if (paramTable != null) {
            return new PossibleTableAnnotation(paramTable, annotType, true);
        }

        return null;
    }


    /**
     * <p>
     * For table directly annotated with <tt>ParameterTable</tt>
     * </p>
     *
     * @param tableType      target table type
     * @param possibleTables from metas
     * @return found possible
     */
    private PossibleTableAnnotation processParameterTableDirect(
            Class<?> tableType,
            List<PossibleTableAnnotation> possibleTables) {

        // Table cfg - includes inherited
        ParameterTable paramTable = tableType.getAnnotation(ParameterTable.class);

        // And if a possible parent is specified ...
        PossibleTableAnnotation sourceRef = searchPossible(tableType, possibleTables);

        // Only process concrete use of annotation
        if (paramTable != null) {
            return new PossibleTableAnnotation(paramTable, tableType, sourceRef, false);
        }

        return null;
    }

    /**
     * <p>
     * For table directly annotated with <tt>ParameterTable</tt>
     * </p>
     *
     * @param tableSetType   target table type
     * @param possibleTables from metas
     * @return stream on found possibles
     */
    private Stream<PossibleTableAnnotation> processParameterTableSet(
            Class<?> tableSetType,
            List<PossibleTableAnnotation> possibleTables) {

        // Table cfg - includes inherited
        ParameterTableSet paramTableSet = tableSetType.getAnnotation(ParameterTableSet.class);

        // And if a possible parent is specified ...
        PossibleTableAnnotation sourceRef = searchPossible(tableSetType, possibleTables);

        // Only process concrete use of annotation
        if (paramTableSet != null) {

            // Search table spec on on "tables" or alias
            ParameterTable[] paramTables = paramTableSet.value().length > 0 ? paramTableSet.value() : paramTableSet.tables();

            // If none specified, error
            if (paramTables.length == 0) {
                throw new IllegalArgumentException("No ParameterTable specified into ParameterTableSet from class " + tableSetType.getName()
                        + ". The @ParameterTable must be defined in \"value\" or \"tables\" attribute for a valid set");
            }

            return Stream.of(paramTables).map(a -> new PossibleTableAnnotation(a, paramTableSet, tableSetType, sourceRef, false));
        }

        return Stream.empty();
    }


    private ParameterTableDefinition initOneParameterTableWithKeys(
            PossibleTableAnnotation possible,
            Map<Class<?>, String> annotDomains) {

        ParameterTableDefinition def = new ParameterTableDefinition(possible.isHierarchyTop());
        def.setCreatedTime(LocalDateTime.now());
        def.setUpdatedTime(def.getCreatedTime());
        def.setDomain(new ParameterDomainDefinition()); // Will be merged later

        // Found domain name
        def.getDomain().setName(failback(
                possible.getDomainName(),
                searchDomainNameInParents(possible.getSourceClazz(), annotDomains)));

        // Domain is mandatory
        if (def.getDomain().getName() == null) {
            throw new IllegalArgumentException(
                    "No domain found for type " + possible.getSourceClazz()
                            + ". Need to specify the domain with meta-annotation, with package annotation or with domainName property in @ParameterTable");
        }

        // Init table def
        def.setParameterName(failback(possible.getValidName(), possible.getSourceClazz().getSimpleName()));
        def.setTableName(failback(possible.getTableName(), possible.getSourceClazz().getSimpleName().toUpperCase()));
        def.setWhereClause(possible.getFilterClause());
        def.setUuid(generateFixedUUID(def.getTableName(), ParameterTableDefinition.class));

        getLog().debug("Found new mapped parameter from set in type " + possible.getSourceClazz().getName() + " with table " + def.getTableName()
                + " and generated UUID " + def.getUuid().toString());

        // Search for key (field / method or parameterTable)
        completeTableParameterKey(possible, def);

        return def;
    }

    private void completeTableParameterKey(
            PossibleTableAnnotation paramTable,
            ParameterTableDefinition def) {

        Class<?> tableType = paramTable.getSourceClazz();

        // Search for key properties (field / method)
        Set<Field> foundFields = searchAnnotatedFields(tableType, ParameterKey.class);
        Set<Method> foundMethods = searchAnnotatedMethods(tableType, ParameterKey.class);

        Map<ParameterInheritance, Class<?>> excludeInheriteds = new HashMap<>();
        searchAllCombinedExcludeInheritedFrom(tableType, excludeInheriteds);

        // As a list of identified PossibleKeyAnnotation (ordered by name for consistency)
        List<PossibleKeyAnnotation> keys = Stream.concat(
                foundFields.stream().map(PossibleKeyAnnotation::new),
                foundMethods.stream().map(PossibleKeyAnnotation::new))
                .filter(k -> k.canKeepInType(tableType))
                .sorted(Comparator.comparing(PossibleKeyAnnotation::getValidName))
                .filter(v -> !v.isExcluded(tableType, excludeInheriteds))
                .collect(Collectors.toList());

        // No keys found on fields / methods, search other / auto-select
        if (keys.size() == 0) {

            if (!paramTable.getKeyField().equals("")) {

                PossibleKeyAnnotation foundKeySpec;

                // Not specified : search on field def
                if (ColumnType.UNKNOWN.equals(paramTable.getKeyType())) {
                    foundKeySpec = searchKey(tableType, paramTable);

                } else {
                    foundKeySpec = new PossibleKeyAnnotation(tableType, paramTable);
                    getLog().debug("Specified keytype " + foundKeySpec.getValidType() + " for ParameterTable " + tableType.getSimpleName());
                }

                // Init key def
                def.setKeyName(foundKeySpec.getValidName());
                def.setKeyType(foundKeySpec.getValidType());
            } else {
                throw new IllegalArgumentException(
                        "No key found for type " + tableType.getName()
                                + ". Need to specify a @ParameterKey on field or method, or to set keyField on @ParamaterTable");
            }
        }

        // Key(s) specified on fields / methods
        else {

            // Copy key spec with support for composite

            for (int i = 0; i < keys.size(); i++) {

                // In most case, only one key will be found
                PossibleKeyAnnotation foundKeySpec = keys.get(i);

                // If key compliant (in set -> with forTable else always select)
                if (foundKeySpec.isCompliantTable(paramTable.getTableName())) {

                    // Init key def
                    def.setKeyName(i, foundKeySpec.getValidName());
                    def.setKeyType(i, foundKeySpec.getValidType());

                    getLog().debug("Found key " + foundKeySpec.getValidName() + " of type " + foundKeySpec.getValidType() + " for type " + tableType.getName());
                }
            }
        }
    }

    private PossibleKeyAnnotation searchKey(Class<?> tableType, PossibleTableAnnotation paramTable) {

        String getterName = "get" + paramTable.getKeyField().substring(0, 1).toUpperCase()
                + paramTable.getKeyField().substring(1);

        // Still not found : failure
        if (tableType == Object.class) {
            throw new IllegalArgumentException("Specified key in ParameterTable " + tableType + " doesn't match with the"
                    + " class getters. Check if the field is specified correctly or that a method exists with the name \""
                    + getterName + "\"");
        }

        PossibleKeyAnnotation foundKeySpec = null;

        try {
            foundKeySpec = new PossibleKeyAnnotation(tableType.getDeclaredField(paramTable.getKeyField()));
            getLog().debug("Found key type from specified field " + foundKeySpec.getValidName() + " of type "
                    + foundKeySpec.getValidType() + " for ParameterTable " + tableType.getSimpleName());
        }

        // Field not found, search getter method
        catch (NoSuchFieldException n) {

            try {
                foundKeySpec = new PossibleKeyAnnotation(tableType.getMethod(getterName), paramTable.getKeyField());
                getLog().debug("Found key type from specified method " + foundKeySpec.getValidName() + " (getter \""
                        + getterName
                        + "\") of type " + foundKeySpec.getValidType() + " for ParameterTable " + tableType.getSimpleName());
            } catch (NoSuchMethodException e) {

                // Search on parent type (inherited ParameterTable)
                return searchKey(tableType.getSuperclass(), paramTable);
            }

        } catch (SecurityException e) {
            throw new IllegalArgumentException("Specified key in ParameterTable " + tableType
                    + " doesn't match with the class fields. Check if the field is specified correctly", e);
        }

        return foundKeySpec;
    }


    private void completeParameterValuesWithLinksAndMappings(
            Map<Class<?>, List<ParameterTableDefinition>> defs,
            Set<ParameterLinkDefinition> allLinks,
            Set<ParameterMappingDefinition> allMappings,
            ClassLoader ccl) {

        getLog().debug("Process completion of values, links and mappings for " + defs.size() + " identified tables");

        // For link transform
        Map<String, ParameterTableDefinition> allTables = defs.values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toMap(ParameterTableDefinition::getTableName, v -> v));

        // Domains - direct search, with inherited
        for (Class<?> tableType : defs.keySet()) {

            // Check if it's a set
            ParameterTableSet paramTableSet = tableType.getAnnotation(ParameterTableSet.class);

            // Table cfg - includes inherited
            if (paramTableSet != null) {
                processParameterValueSet(tableType, allTables, defs, allLinks, allMappings, ccl);
            } else {
                processParameterValueDirect(tableType, allTables, defs, allLinks, allMappings, ccl);
            }
        }
    }

    private void processParameterValueDirect(
            Class<?> tableType,
            Map<String, ParameterTableDefinition> allTables,
            Map<Class<?>, List<ParameterTableDefinition>> defs,
            Set<ParameterLinkDefinition> allLinks,
            Set<ParameterMappingDefinition> allMappings,
            ClassLoader ccl) {

        // Table cfg - includes inherited
        ParameterTable paramTable = tableType.getAnnotation(ParameterTable.class);

        List<ParameterTableDefinition> possibleDef = defs.get(tableType);

        // Cannot be != 1 item here
        if (possibleDef == null || possibleDef.size() != 1) {
            throw new IllegalArgumentException("Wrong def spec for type " + tableType);
        }

        // Edited def
        ParameterTableDefinition def = possibleDef.get(0);

        // Valid fields (regarding anot / table def)
        Set<Field> foundFields = searchFields(tableType, paramTable.useAllFields());

        // Valid methods (regarding anot)
        Set<Method> foundMethods = searchMethods(tableType);

        // Mixed all values + associated links, on both field and methods
        List<PossibleValueAnnotation> values =
                Stream.concat(
                        foundFields.stream().map(f -> new PossibleValueAnnotation(f, ccl)),
                        foundMethods.stream().map(m -> new PossibleValueAnnotation(m, ccl))
                ).collect(Collectors.toList());

        Map<ParameterInheritance, Class<?>> excludeInheriteds = new HashMap<>();
        searchAllCombinedExcludeInheritedFrom(tableType, excludeInheriteds);

        // Prepare value columns (with support for composite)
        def.setIdentifiedColumnNames(values.stream()
                .filter(v -> v.canKeepInType(tableType))
                .filter(v -> !v.isExcluded(tableType, excludeInheriteds))
                .flatMap(v -> v.isComposite() ? Stream.of(v.getCompositeNames()) : Stream.of(v.getValidName()))
                .filter(v -> !v.equalsIgnoreCase(def.getKeyName())) // Remove key if present
                .map(String::toUpperCase)
                .distinct()
                .peek(v -> getLog().debug("Found selected value " + v + " for type " + tableType.getName()))
                .collect(Collectors.toSet()));


        // Search for specified links
        Collection<ParameterLinkDefinition> foundLinks = extractLinksFromValues(defs, allTables, def, values);
        allLinks.addAll(foundLinks);

        // Search for specified mappings
        allMappings.addAll(extractMappingsFromValues(defs, allTables, def, values));
    }

    private void processParameterValueSet(
            Class<?> tableType,
            Map<String, ParameterTableDefinition> allTables,
            Map<Class<?>, List<ParameterTableDefinition>> allDefs,
            Set<ParameterLinkDefinition> allLinks,
            Set<ParameterMappingDefinition> allMappings,
            ClassLoader ccl) {

        ParameterTableSet paramTableSet = tableType.getAnnotation(ParameterTableSet.class);

        // Edited def
        List<ParameterTableDefinition> defs = allDefs.get(tableType);

        // Valid fields (regarding anot / table def)
        Set<Field> foundFields = searchFields(tableType, paramTableSet.useAllFields());

        // Valid methods (regarding anot)
        Set<Method> foundMethods = searchMethods(tableType);

        Map<ParameterInheritance, Class<?>> excludeInheriteds = new HashMap<>();
        searchAllCombinedExcludeInheritedFrom(tableType, excludeInheriteds);

        // Add set also
        Stream.of(paramTableSet.excludeInherited()).forEach(
                i -> excludeInheriteds.put(i, tableType));

        for (ParameterTableDefinition def : defs) {

            // Mixed all values + associated links, on both field and methods
            List<PossibleValueAnnotation> values = Stream.concat(
                    Stream.concat(
                            foundFields.stream().map(f -> new PossibleValueAnnotation(f, ccl)),
                            foundMethods.stream().map(m -> new PossibleValueAnnotation(m, ccl))),
                    streamPossibleValueInDirectTablesDef(tableType, paramTableSet))
                    .filter(v -> v.isCompliantTable(def.getTableName())) // Only compliant
                    .filter(v -> v.canKeepInType(tableType))
                    .filter(v -> !v.isExcluded(tableType, excludeInheriteds))
                    .collect(Collectors.toList());

            // Prepare value columns (with support for composite)
            def.setIdentifiedColumnNames(values.stream()
                    .flatMap(v -> v.isComposite() ? Stream.of(v.getCompositeNames()) : Stream.of(v.getValidName()))
                    .map(String::toUpperCase)
                    .distinct()
                    .peek(v -> getLog().debug("Found selected value " + v + " for type " + tableType.getName()))
                    .collect(Collectors.toSet()));

            // Search for specified links
            Collection<ParameterLinkDefinition> foundLinks = extractLinksFromValues(allDefs, allTables, def, values);
            allLinks.addAll(foundLinks);

            // Search for specified mappings
            allMappings.addAll(extractMappingsFromValues(allDefs, allTables, def, values));
        }
    }

    private static Stream<PossibleValueAnnotation> streamPossibleValueInDirectTablesDef(Class<?> declaringClazz, ParameterTableSet setAnnot) {
        return Stream.of(setAnnot.tables())
                .filter(t -> t.values().length > 0)
                .flatMap(t -> Stream.of(t.values())
                        .map(v -> new PossibleValueAnnotation(declaringClazz, v, failback(t.value(), t.tableName()))));
    }

    /**
     * <p>
     * Search for the referenced table for a value used in a link
     * </p>
     */
    private static ParameterTableDefinition prepareReferencedTable(
            Map<Class<?>, List<ParameterTableDefinition>> defs,
            Map<String, ParameterTableDefinition> allTables,
            ParameterTableDefinition currentTableDef,
            PossibleValueAnnotation possibleValue,
            String annotToTableName,
            Class<?> annotToParameter) {

        ParameterTableDefinition toDef = null;

        if (annotToTableName == null || annotToTableName.trim().equals("")) {
            List<ParameterTableDefinition> possibleToDefs = defs.get(annotToParameter);
            if (possibleToDefs != null) {
                toDef = possibleToDefs.get(0);
            }
        } else {
            toDef = allTables.get(annotToTableName);
        }

        // If still empty, search by attribute type
        if (toDef == null) {
            List<ParameterTableDefinition> possibleToDefs = defs.get(possibleValue.getValidType());
            if (possibleToDefs != null) {
                toDef = possibleToDefs.get(0);
            }
        }

        // Mandatory, fail if still empty
        if (toDef == null) {
            throw new IllegalArgumentException(
                    "No valid specified table found for link or mapping on column " + possibleValue.getValidName() + " for type "
                            + currentTableDef.getTableName()
                            + ". Specify @ParameterLink or @ParameterMapping toTableName or toParameter with a valid value");
        }

        return toDef;
    }


    private Collection<ParameterLinkDefinition> extractLinksFromValues(
            Map<Class<?>, List<ParameterTableDefinition>> defs,
            Map<String, ParameterTableDefinition> allTables,
            ParameterTableDefinition currentTableDef,
            List<PossibleValueAnnotation> values) {

        /*
         * Keeps the links mapped to their table "to" for clean association of multiple
         * link definition in case of composite key
         */
        Map<String, List<ParameterLinkDefinition>> linksByTablesTo = new HashMap<>();

        // Prepare links (where found)
        values.stream()
                .filter(a -> a.getLinkAnnot() != null)
                .filter(v -> currentTableDef.getIdentifiedColumnNames().contains(v.getValidName()))
                .forEach(a -> {
                    ParameterLink annot = a.getLinkAnnot();

                    // Search the referenced table
                    ParameterTableDefinition toDef = prepareReferencedTable(
                            defs,
                            allTables,
                            currentTableDef,
                            a,
                            annot.toTableName(),
                            annot.toParameter());

                    // Referenced column or auto-set from parameter key
                    String columnTo = annot.toColumn().length > 0 ? annot.toColumn()[0] : toDef.getKeyName().toUpperCase();

                    // Referenced name or auto-set from referenced parameter name
                    String linkName = failback(annot.name(), toDef.getParameterName());

                    // Get link already referenced or update "composite key" link
                    List<ParameterLinkDefinition> links = linksByTablesTo.computeIfAbsent(toDef.getTableName(), k -> new ArrayList<>());

                    // Most case will only go there
                    if (links.isEmpty() || a.isComposite()) {

                        ParameterLinkDefinition link = new ParameterLinkDefinition();

                        link.setCreatedTime(LocalDateTime.now());
                        link.setUpdatedTime(link.getCreatedTime());
                        link.setDictionaryEntry(currentTableDef);
                        link.setName(linkName);
                        link.setTableTo(toDef.getTableName());

                        // In most case, not composite
                        if (!a.isComposite()) {
                            // To column, if not specified, is based on dest entity key
                            link.setColumnTo(columnTo);
                            link.setColumnFrom(a.getValidName());
                        }

                        // Else rarest situation : composite - single def
                        else {

                            // Check compliance between referenced "from" values and "tos"
                            if (a.getCompositeNames().length != annot.toColumn().length) {
                                throw new IllegalArgumentException("Error in Link spec for name " + a.getValidName() + " into "
                                        + currentTableDef.getParameterName() + " : If a link is specified with composite key, for"
                                        + " each specified \"to\" column in @ParameterLink, a corresponding value column must be"
                                        + " specified in @ParameterCompositeValue");
                            }

                            for (int i = 0; i < a.getCompositeNames().length; i++) {
                                // Apply in same order to - from
                                link.setColumnTo(i, annot.toColumn()[i]);
                                link.setColumnFrom(i, a.getCompositeNames()[i]);
                            }
                        }

                        String refForUuid = currentTableDef.getTableName() + "." + link.getColumnFrom() + " -> " + link.getTableTo() + "."
                                + link.getColumnTo();

                        // Produces uuid
                        link.setUuid(generateFixedUUID(refForUuid, ParameterLinkDefinition.class));

                        getLog().debug("Found link " + refForUuid + " with generated UUID " + link.getUuid().toString() + " for type "
                                + currentTableDef.getTableName());

                        links.add(link);
                    }

                    // Rare case : composite key - multiple def (all keys referenced
                    // directly)
                    else {
                        if (links.isEmpty()) {
                            throw new IllegalArgumentException("Cannot define links for name " + a.getValidName() + " into "
                                    + currentTableDef.getParameterName() + " as both @ParameterValueComposite and dispatched" +
                                    "@ParameterValue : use only one kind of composite for the same table");
                        }
                        ParameterLinkDefinition link = links.get(0);
                        int colIndex = (int) links.get(0).columnTos().count();

                        link.setColumnFrom(colIndex, a.getValidName());
                        link.setColumnTo(colIndex, columnTo);
                    }

                });

        return linksByTablesTo.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    private List<ParameterMappingDefinition> extractMappingsFromValues(
            Map<Class<?>, List<ParameterTableDefinition>> defs,
            Map<String, ParameterTableDefinition> allTables,
            ParameterTableDefinition currentTableDef,
            List<PossibleValueAnnotation> values) {

        // Prepare mappings (where found)
        return values.stream()
                .filter(a -> a.getMappingAnnot() != null)
                .filter(v -> currentTableDef.getIdentifiedColumnNames().contains(v.getValidName()))
                .map(a -> {
                    ParameterMapping annot = a.getMappingAnnot();
                    ParameterMappingDefinition mapping = new ParameterMappingDefinition();
                    mapping.setCreatedTime(LocalDateTime.now());
                    mapping.setUpdatedTime(mapping.getCreatedTime());
                    mapping.setDictionaryEntry(currentTableDef);
                    mapping.setMapTable(annot.mapTableName());

                    // Search the referenced table
                    ParameterTableDefinition toDef = prepareReferencedTable(
                            defs,
                            allTables,
                            currentTableDef,
                            a,
                            annot.toTableName(),
                            annot.toParameter());

                    // Name is column alias used for payload, not a real column
                    mapping.setName(a.getValidName());

                    // Must be specified
                    mapping.setTableTo(toDef.getTableName());

                    // To column, if not specified, is based on dest entity key
                    mapping.setColumnTo(failback(annot.toColumn(), toDef.getKeyName().toUpperCase()));
                    mapping.setMapTableColumnTo(failback(annot.mapColumnTo(), mapping.getColumnTo()));

                    // From column, if not specified, is based on local key
                    mapping.setColumnFrom(failback(annot.fromColumn(), currentTableDef.getKeyName().toUpperCase()));
                    mapping.setMapTableColumnFrom(failback(annot.mapColumnFrom(), mapping.getColumnFrom()));

                    // Composite string used for UUID building
                    String refForUuid = mapping.getName() + " : " + currentTableDef.getTableName() + "." + mapping.getColumnFrom()
                            + " -> " + mapping.getMapTable() + "." + mapping.getMapTableColumnFrom() + " -> " + mapping.getMapTable() + "."
                            + mapping.getMapTableColumnTo() + " -> " + mapping.getTableTo() + "." + mapping.getColumnTo();

                    // Produces uuid
                    mapping.setUuid(generateFixedUUID(refForUuid, ParameterMappingDefinition.class));

                    getLog().debug("Found mapping " + refForUuid + " with generated UUID " + mapping.getUuid().toString() + " for type "
                            + currentTableDef.getTableName());

                    return mapping;
                })
                .collect(Collectors.toList());

    }

    private Map<String, ParameterProjectDefinition> identifyParameterProjects(Map<String, ParameterProject> projectsByDomains) {

        getLog().debug("Process completion of all projects extracted from " + projectsByDomains.size() + " identified ref + 1 default");

        Map<String, ParameterProjectDefinition> projectByNames = projectsByDomains.values().stream()
                .distinct()
                .map(p -> {
                    ParameterProjectDefinition def = new ParameterProjectDefinition();
                    def.setCreatedTime(LocalDateTime.now());
                    def.setName(p.name());
                    def.setColor(p.color());
                    def.setUuid(generateFixedUUID(p.name(), ParameterProjectDefinition.class));
                    getLog().debug("Identified distinct project \"" + def.getName() + "\" with generated UUID " + def.getUuid());
                    return def;
                })
                .collect(Collectors.toMap(ParameterProjectDefinition::getName, d -> d));

        // If not done yet, add also default
        ParameterProjectDefinition defaultDef = new ParameterProjectDefinition();
        defaultDef.setCreatedTime(LocalDateTime.now());
        defaultDef.setName(ParameterProjectDefinition.DEFAULT_PROJECT);
        defaultDef.setColor(ProjectColor.GREY);
        defaultDef.setUuid(generateFixedUUID(ParameterProjectDefinition.DEFAULT_PROJECT, ParameterProjectDefinition.class));
        projectByNames.put(ParameterProjectDefinition.DEFAULT_PROJECT, defaultDef);

        return projectByNames;
    }

    private void combineTablesAndGenerateSelectClauses(
            Map<Class<?>, List<ParameterTableDefinition>> allTables,
            Set<ParameterLinkDefinition> allLinks,
            Set<ParameterMappingDefinition> allMappings) {

        Map<String, ParameterTableDefinition> allTableByNames = new HashMap<>();
        Map<String, List<Class<?>>> allTableSourceTypes = new HashMap<>();

        allTables.forEach((t, l) -> {

            for (int i = 0; i < l.size(); i++) {

                ParameterTableDefinition found = l.get(i);

                allTableSourceTypes.computeIfAbsent(found.getTableName(), k -> new ArrayList<>()).add(t);
                ParameterTableDefinition existing = allTableByNames.get(found.getTableName());
                Collection<ParameterLinkDefinition> foundLinks = allLinks.stream().filter(k -> k.getDictionaryEntry().equals(found)).collect(Collectors.toSet());
                Collection<ParameterMappingDefinition> foundMappings = allMappings.stream().filter(k -> k.getDictionaryEntry().equals(found)).collect(Collectors.toSet());
                if (existing != null) {
                    l.set(i, existing);
                    existing.getIdentifiedColumnNames().addAll(found.getIdentifiedColumnNames());
                    foundLinks.forEach(k -> k.setDictionaryEntry(existing));
                    foundMappings.forEach(k -> k.setDictionaryEntry(existing));

                    // Priority on hierarchy top properties
                    if (found.isHierarchyTop()) {
                        existing.setParameterName(found.getParameterName());
                        existing.setDomain(found.getDomain());
                    }
                } else {
                    allTableByNames.put(found.getTableName(), found);
                }
            }
        });

        // Final select clause generation
        allTableByNames.values().forEach(p -> {
            Collection<ParameterLinkDefinition> foundLinks = allLinks.stream().filter(k -> k.getDictionaryEntry().equals(p)).collect(Collectors.toSet());
            Collection<ParameterMappingDefinition> foundMappings = allMappings.stream().filter(k -> k.getDictionaryEntry().equals(p)).collect(Collectors.toSet());

            // Produces select clause
            p.setSelectClause(this.selectClauseGen.mergeSelectClause(
                    p.getIdentifiedColumnNames(),
                    p.getIdentifiedColumnNames().size() + 2,
                    foundLinks,
                    foundMappings,
                    allTableByNames));

            getLog().info(DEBUG_BG_RED + DEBUG_TEXT_BLACK + "Found parameter Table \"" + p.getParameterName() + "\" for table " + p.getTableName() + " :" + DEBUG_RESET_COLOUR);
            getLog().info(DEBUG_BG_BLUE + DEBUG_TEXT_BLACK + "Keys are " + Arrays.toString(p.getAllKeyNames().toArray()) + DEBUG_RESET_COLOUR);
            getLog().info(DEBUG_BG_BLUE + DEBUG_TEXT_BLACK + "Generated select clause is " + p.getSelectClause() + DEBUG_RESET_COLOUR);
            getLog().info(DEBUG_BG_BLUE + DEBUG_TEXT_BLACK + "It was found on types : " + Arrays.toString(allTableSourceTypes.get(p.getTableName()).toArray()) + DEBUG_RESET_COLOUR);
            getLog().info(DEBUG_BG_BLUE + DEBUG_TEXT_BLACK + "Links are : "
                    + (foundLinks.isEmpty() ? "none found" : foundLinks.stream().map(Object::toString).collect(Collectors.joining(" , "))) + DEBUG_RESET_COLOUR);


        });
    }

    private Collection<ParameterVersionDefinition> specifyVersionsByProjects(
            Collection<ParameterProjectDefinition> projects) {

        getLog().debug("Init versions for each " + projects.size() + " identified projects");

        // Get all project versions
        return projects.stream().map(p -> {
            ParameterVersionDefinition version = new ParameterVersionDefinition();
            version.setName(config().getProjectVersion());
            version.setCreatedTime(LocalDateTime.now());
            version.setUpdatedTime(LocalDateTime.now());
            version.setProject(p);
            version.setUuid(generateFixedUUID(version.getName() + "-###-" + p.getName(), ParameterVersionDefinition.class));
            return version;
        }).collect(Collectors.toList());
    }

    private Collection<ParameterDomainDefinition> completeParameterDomains(
            Map<Class<?>, List<ParameterTableDefinition>> defs,
            Map<String, ParameterProject> projectsByDomains,
            Map<String, ParameterProjectDefinition> projectsByName) {

        getLog().debug("Process completion of distinct domains extracted from " + defs.size() + " identified tables");

        // Get all domain values
        Map<String, ParameterDomainDefinition> domains = defs.values().stream()
                .flatMap(Collection::stream)
                .map(d -> d.getDomain().getName()).distinct().map(n -> {
                    ParameterDomainDefinition def = new ParameterDomainDefinition();
                    def.setCreatedTime(LocalDateTime.now());
                    def.setUpdatedTime(def.getCreatedTime());
                    def.setName(n);

                    // linking to project
                    ParameterProject projectPar = projectsByDomains.get(n);

                    // If none found, set default project
                    def.setProject(projectPar != null ? projectsByName.get(projectPar.name())
                            : projectsByName.get(ParameterProjectDefinition.DEFAULT_PROJECT));

                    def.setUuid(generateFixedUUID(n + "-###-" + def.getProject().getName(), ParameterDomainDefinition.class));

                    getLog().debug("Identified distinct domain \"" + n + "\" with generated UUID " + def.getUuid() + " onto project "
                            + def.getProject().getName());
                    return def;
                }).collect(Collectors.toMap(ParameterDomainDefinition::getName, v -> v));

        // Then fix associated domains on param tables
        defs.values().stream()
                .flatMap(Collection::stream)
                .forEach(d -> {
                    ParameterDomainDefinition clean = domains.get(d.getDomain().getName());
                    d.setDomain(clean);
                });

        return domains.values();
    }

    private static PossibleTableAnnotation searchPossible(Class<?> tableType, List<PossibleTableAnnotation> possibleTables) {

        // Search possible from meta annotations
        PossibleTableAnnotation possible = possibleTables.stream()
                .filter(t -> Stream.of(tableType.getAnnotations()).anyMatch(a -> a.getClass().equals(t.getSourceClazz())))
                .findFirst()
                .orElse(null);

        // Search also on parent
        if (possible == null && tableType != Object.class) {
            possible = searchPossibleOnParent(tableType.getSuperclass(), possibleTables);
        }

        return possible;
    }

    private static PossibleTableAnnotation searchPossibleOnParent(Class<?> tableType, List<PossibleTableAnnotation> possibleTables) {

        // Init possible on specified parent type
        if (tableType.getAnnotation(ParameterTable.class) != null) {
            PossibleTableAnnotation possible = new PossibleTableAnnotation(tableType.getAnnotation(ParameterTable.class), tableType, true);
            possibleTables.add(possible);
            return possible;
        }

        return searchPossible(tableType, possibleTables);
    }

    /**
     * @param tableType current type
     * @param excludeds inheritance mapped to the type where they are defined (to detect "starting what inherited type the properties have to be excluded")
     */
    private static void searchAllCombinedExcludeInheritedFrom(Class<?> tableType, Map<ParameterInheritance, Class<?>> excludeds) {

        if (tableType == Object.class) {
            return;
        }

        // Init possible on specified parent type
        if (tableType.getAnnotation(ParameterTable.class) != null) {
            ParameterInheritance[] excludedsLocally = tableType.getAnnotation(ParameterTable.class).excludeInherited();

            if (excludedsLocally.length > 0) {
                Stream.of(excludedsLocally).forEach(l -> excludeds.put(l, tableType));
            }
        }

        searchAllCombinedExcludeInheritedFrom(tableType.getSuperclass(), excludeds);
    }

}