package fr.uem.efluid.generation;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.ParameterCompositeValue;
import fr.uem.efluid.ParameterDomain;
import fr.uem.efluid.ParameterIgnored;
import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterLink;
import fr.uem.efluid.ParameterMapping;
import fr.uem.efluid.ParameterProject;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterTableSet;
import fr.uem.efluid.ParameterValue;
import fr.uem.efluid.ProjectColor;
import fr.uem.efluid.clients.DictionaryApiClient;
import fr.uem.efluid.generation.DictionaryGeneratorConfig.LogFacade;
import fr.uem.efluid.model.GeneratedDictionaryPackage;
import fr.uem.efluid.model.GeneratedFunctionalDomainPackage;
import fr.uem.efluid.model.GeneratedProjectPackage;
import fr.uem.efluid.model.GeneratedTableLinkPackage;
import fr.uem.efluid.model.GeneratedTableMappingPackage;
import fr.uem.efluid.model.GeneratedVersionPackage;
import fr.uem.efluid.model.ParameterDomainDefinition;
import fr.uem.efluid.model.ParameterLinkDefinition;
import fr.uem.efluid.model.ParameterMappingDefinition;
import fr.uem.efluid.model.ParameterProjectDefinition;
import fr.uem.efluid.model.ParameterTableDefinition;
import fr.uem.efluid.model.ParameterVersionDefinition;
import fr.uem.efluid.rest.v1.DictionaryApi;
import fr.uem.efluid.rest.v1.model.CreatedDictionaryView;
import fr.uem.efluid.services.ExportService;
import fr.uem.efluid.services.types.ExportFile;
import fr.uem.efluid.utils.RuntimeValuesUtils;
import fr.uem.efluid.utils.SelectClauseGenerator;

/**
 * <p>
 * Annotation processing / reflection based search for dictionary definition
 * </p>
 * <p>
 * Current version support inheritance and detect mappings
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 4
 */
public class DictionaryGenerator {

	private static final String VERSION = "4";

	private final DictionaryGeneratorConfig config;

	private final SelectClauseGenerator selectClauseGen;

	private final ExportService export;

	/**
	 * 
	 */
	public DictionaryGenerator(DictionaryGeneratorConfig config) {
		this.config = config;
		this.selectClauseGen = new SelectClauseGenerator(config.isProtectColumn());
		this.export = new ExportService();
	}

	/**
	 * @param contextClassLoader
	 */
	public void generateDictionaryExport(ClassLoader contextClassLoader) throws Exception {

		try {
			long start = System.currentTimeMillis();
			getLog().info("###### datagate-generator VERSION " + VERSION + " - START GENERATE ######");

			/* Search using filtering tools, based on a configured package root */
			Reflections reflections = initReflectionEntryPoint(contextClassLoader);

			/* Will prepare project, links and mappings when found */
			Map<String, ParameterProject> projects = new HashMap<>();
			List<ParameterLinkDefinition> allLinks = new ArrayList<>();
			List<ParameterMappingDefinition> allMappings = new ArrayList<>();

			/* Process all tables init + key for clean link building */
			Map<Class<?>, List<ParameterTableDefinition>> typeTables = initParameterTablesWithKeys(reflections,
					searchDomainsByAnnotation(reflections, projects));

			/* Complete project definitions */
			Map<String, ParameterProjectDefinition> projectDefs = identifyParameterProjects(projects);

			/* Then process table values / links / mappings using refs */
			completeParameterValuesWithLinksAndMappings(reflections, typeTables, allLinks, allMappings, contextClassLoader);

			/* Then, extract domains */
			Collection<ParameterDomainDefinition> allDomains = completeParameterDomains(typeTables, projects, projectDefs);

			/* Finally, prepare a version for each projects */
			Collection<ParameterVersionDefinition> allVersions = specifyVersionsByProjects(projectDefs.values());

			/* Check everything is OK */
			assertTableNotDuplicated(typeTables);

			/* Now can export */
			export(
					projectDefs.values(),
					allDomains,
					typeTables.values().stream().flatMap(Collection::stream).collect(Collectors.toList()),
					allLinks,
					allMappings,
					allVersions);

			getLog().info("###### datagate-generator VERSION " + VERSION + " - GENERATE COMPLETED IN "
					+ BigDecimal.valueOf(System.currentTimeMillis() - start, 3).toPlainString()
					+ " s ######");

		} catch (Exception e) {
			getLog().error("Process failed with", e);
			throw e;
		}
	}

	/**
	 * @param contextClassLoader
	 * @return
	 */
	private Reflections initReflectionEntryPoint(ClassLoader contextClassLoader) {

		return new Reflections(new ConfigurationBuilder()
				.addClassLoader(contextClassLoader)
				.setUrls(ClasspathHelper.forPackage(this.config.getSourcePackage()))
				.setScanners(new SubTypesScanner(), new TypeAnnotationsScanner(), new FieldAnnotationsScanner(),
						new MethodAnnotationsScanner()));
	}

	/**
	 * @param reflections
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<Class<?>, String> searchDomainsByAnnotation(
			Reflections reflections,
			Map<String, ParameterProject> projects) {

		getLog().debug("Process domains spec search");

		// Prepare mapped domains
		Map<Class<?>, String> typeDomains = new HashMap<>();

		// Domains - direct search, with inherited
		Set<Class<?>> domains = reflections.getTypesAnnotatedWith(ParameterDomain.class, true);

		// Domain search is the entry point of all mapped types
		for (Class<?> typedDomain : domains) {
			ParameterDomain domain = typedDomain.getAnnotation(ParameterDomain.class);

			String domainName = domain.name();

			// Specified project - keep it in ref
			ParameterProject project = domain.project();
			projects.put(domainName, project);

			// Search for annotated with meta
			if (Annotation.class.isAssignableFrom(typedDomain)) {
				getLog().debug("Found domain meta-annotation as " + typedDomain.getName() + " with name \"" + domainName + "\"");
				reflections.getTypesAnnotatedWith((Class<? extends Annotation>) typedDomain, true).stream()
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

						.filter(c -> c.getPackage().getName().startsWith(packageName))
						.filter(c -> !typeDomains.containsKey(c))
						.peek(t -> getLog()
								.debug("For type " + t.getName() + " will use specified domain by package \"" + domainName + "\""))
						.forEach(c -> typeDomains.put(c, domainName));
			}
		}

		return typeDomains;
	}

	/**
	 * @param reflections
	 * @return
	 */
	private Map<Class<?>, List<ParameterTableDefinition>> initParameterTablesWithKeys(
			Reflections reflections,
			Map<Class<?>, String> annotDomains) {

		getLog().debug("Process parameter table init with " + annotDomains.size() + " identified domain annotations");

		Map<Class<?>, List<ParameterTableDefinition>> tables = new HashMap<>();

		// Domains - direct search, with inherited
		for (Class<?> tableType : reflections.getTypesAnnotatedWith(ParameterTable.class, false)) {
			processParameterTableDirect(tableType, annotDomains, tables);
		}

		// Then completed with ParameterTableSet
		for (Class<?> tableSetType : reflections.getTypesAnnotatedWith(ParameterTableSet.class, false)) {
			processParameterTableSet(tableSetType, annotDomains, tables);
		}

		return tables;
	}

	/**
	 * <p>
	 * For table directly annotated with <tt>ParameterTable</tt>
	 * </p>
	 * 
	 * @param tableType
	 * @param annotDomains
	 * @param tables
	 */
	private void processParameterTableDirect(
			Class<?> tableType,
			Map<Class<?>, String> annotDomains,
			Map<Class<?>, List<ParameterTableDefinition>> tables) {

		// Table cfg - includes inherited
		ParameterTable paramTable = tableType.getAnnotation(ParameterTable.class);

		// Only process concrete use of annotation
		if (paramTable != null) {

			ParameterTableDefinition def = new ParameterTableDefinition();
			def.setCreatedTime(LocalDateTime.now());
			def.setUpdatedTime(def.getCreatedTime());
			def.setDomain(new ParameterDomainDefinition()); // Will be merged later

			// Found domain name
			def.getDomain()
					.setName(paramTable.domainName() != null && paramTable.domainName().trim().length() > 0 ? paramTable.domainName()
							: annotDomains.get(tableType));

			// Domain is mandatory
			if (def.getDomain().getName() == null) {
				throw new IllegalArgumentException(
						"No domain found for type " + tableType.getName()
								+ ". Need to specify the domain with meta-annotation, with package annotation or with domainName property in @ParameterTable");
			}

			// Init table def
			def.setParameterName(
					paramTable.name() == null || "".equals(paramTable.name()) ? tableType.getSimpleName() : paramTable.name());
			def.setTableName("".equals(paramTable.tableName()) ? tableType.getSimpleName().toUpperCase() : paramTable.tableName());
			def.setWhereClause(paramTable.filterClause());
			def.setUuid(generateFixedUUID(def.getTableName(), ParameterTableDefinition.class));

			getLog().debug("Found mapped parameter type " + tableType.getName() + " with table " + def.getTableName()
					+ " and generated UUID " + def.getUuid().toString());

			// Search for key (field / method or parameterTable)
			completeTableParameterKey(tableType, paramTable, def);

			List<ParameterTableDefinition> defs = tables.get(tableType);

			if (defs == null) {
				defs = new ArrayList<>();
				tables.put(tableType, defs);
			}

			defs.add(def);
		}
	}

	/**
	 * <p>
	 * For table directly annotated with <tt>ParameterTable</tt>
	 * </p>
	 * 
	 * @param tableType
	 * @param annotDomains
	 * @param tables
	 */
	private void processParameterTableSet(
			Class<?> tableSetType,
			Map<Class<?>, String> annotDomains,
			Map<Class<?>, List<ParameterTableDefinition>> tables) {

		// Table cfg - includes inherited
		ParameterTableSet paramTableSet = tableSetType.getAnnotation(ParameterTableSet.class);

		List<ParameterTableDefinition> defs = tables.get(tableSetType);

		if (defs == null) {
			defs = new ArrayList<>();
		}

		// Only process concrete use of annotation
		if (paramTableSet != null) {

			// Search table spec on on "tables" or alias
			ParameterTable[] paramTables = paramTableSet.value().length > 0 ? paramTableSet.value() : paramTableSet.tables();

			// If none specified, error
			if (paramTables.length == 0) {
				throw new IllegalArgumentException("No ParameterTable specified into ParameterTableSet from class " + tableSetType.getName()
						+ ". The @ParameterTable must be defined in \"value\" or \"tables\" attribute for a valid set");
			}

			for (ParameterTable paramTable : paramTables) {

				ParameterTableDefinition def = new ParameterTableDefinition();
				def.setCreatedTime(LocalDateTime.now());
				def.setUpdatedTime(def.getCreatedTime());
				def.setDomain(new ParameterDomainDefinition()); // Will be merged later

				// Found domain name
				def.getDomain()
						.setName(paramTable.domainName() != null && paramTable.domainName().trim().length() > 0 ? paramTable.domainName()
								: annotDomains.get(tableSetType));

				// If not found this way, use the parameterTableSet domain
				if (def.getDomain().getName() == null) {
					def.getDomain().setName(paramTableSet.domainName());

					// Domain is mandatory - if still missing = error
					if (def.getDomain().getName() == null) {
						throw new IllegalArgumentException("No domain found for type " + tableSetType.getName() + ". Need to specify the "
								+ "domain with meta-annotation, with package annotation or with domainName property in @ParameterTable or in "
								+ "@ParameterTableSet");
					}
				}

				// Init table def
				def.setParameterName(
						paramTable.name() == null || "".equals(paramTable.name()) ? tableSetType.getSimpleName() : paramTable.name());
				def.setTableName("".equals(paramTable.tableName()) ? tableSetType.getSimpleName().toUpperCase() : paramTable.tableName());
				def.setWhereClause(paramTable.filterClause());
				def.setUuid(generateFixedUUID(def.getTableName(), ParameterTableDefinition.class));

				getLog().debug("Found new mapped parameter from set in type " + tableSetType.getName() + " with table " + def.getTableName()
						+ " and generated UUID " + def.getUuid().toString());

				// Search for key (field / method or parameterTable)
				completeTableParameterKey(tableSetType, paramTable, def);

				defs.add(def);
			}
		}

		tables.put(tableSetType, defs);
	}

	/**
	 * @param tableType
	 * @param paramTable
	 * @param def
	 */
	@SuppressWarnings("unchecked")
	private void completeTableParameterKey(
			Class<?> tableType,
			ParameterTable paramTable,
			ParameterTableDefinition def) {

		// Search for key properties (field / method)
		Set<Field> foundFields = ReflectionUtils.getAllFields(tableType, f -> f.isAnnotationPresent(ParameterKey.class));
		Set<Method> foundMethods = ReflectionUtils.getAllMethods(tableType, f -> f.isAnnotationPresent(ParameterKey.class));

		// As a list of identified PossibleKeyAnnotation (ordered by name for consistency)
		List<PossibleKeyAnnotation> keys = Stream.concat(foundFields.stream().map(f -> new PossibleKeyAnnotation(f)),
				foundMethods.stream().map(m -> new PossibleKeyAnnotation(m)))
				.sorted(Comparator.comparing(PossibleKeyAnnotation::getValidName))
				.collect(Collectors.toList());

		int keyFounds = 0;

		// No keys found on fields / methods, search other / auto-select
		if (keys.size() == 0) {

			if (!paramTable.keyField().equals("")) {

				PossibleKeyAnnotation foundKeySpec = null;

				// Not specified : search on field def
				if (ColumnType.UNKNOWN.equals(paramTable.keyType())) {
					try {
						foundKeySpec = new PossibleKeyAnnotation(tableType.getField(paramTable.keyField()));
						getLog().debug("Found key type from specified field " + foundKeySpec.getValidName() + " of type "
								+ foundKeySpec.getValidType() + " for ParameterTable " + tableType.getSimpleName());
					}

					// Field not found, search getter method
					catch (NoSuchFieldException n) {

						String getterName = "get" + paramTable.keyField().substring(0, 1).toUpperCase()
								+ paramTable.keyField().substring(1);

						try {
							foundKeySpec = new PossibleKeyAnnotation(tableType.getMethod(getterName));
							getLog().debug("Found key type from specified method " + foundKeySpec.getValidName() + " (getter \""
									+ getterName
									+ "\") of type " + foundKeySpec.getValidType() + " for ParameterTable " + tableType.getSimpleName());
						}

						// Still not found : failure
						catch (NoSuchMethodException e) {
							throw new IllegalArgumentException("Specified key in ParameterTable " + tableType + " doesn't match with the"
									+ " class getters. Check if the field is specified correctly or that a method exists with the name \""
									+ getterName + "\"", e);
						}

					} catch (SecurityException e) {
						throw new IllegalArgumentException("Specified key in ParameterTable " + tableType
								+ " doesn't match with the class fields. Check if the field is specified correctly", e);
					}
				} else {
					foundKeySpec = new PossibleKeyAnnotation(paramTable);
					getLog().debug("Specified keytype " + foundKeySpec.getValidType() + " for ParameterTable " + tableType.getSimpleName());
				}

				// Init key def
				def.setKeyName(foundKeySpec.getValidName());
				def.setKeyType(foundKeySpec.getValidType());
			}

			else {
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
				if (foundKeySpec.isCompliantTable(paramTable.tableName())) {

					// Init key def
					def.setKeyName(i, foundKeySpec.getValidName());
					def.setKeyType(i, foundKeySpec.getValidType());
					keyFounds++;
				}
			}
		}

		getLog().debug("Found key " + def.getKeyName() + " of type " + def.getKeyType() + " for type " + tableType.getName()
				+ ((def.getExt1KeyName() != null) ? " and " + (keyFounds - 1) + " other ext keys" : ""));
	}

	/**
	 * @param defs
	 */
	private static void assertTableNotDuplicated(Map<Class<?>, List<ParameterTableDefinition>> defs) {

		// Get duplicated table name (and count for each)
		Map<String, Integer> duplicates = defs.values().stream()
				.flatMap(Collection::stream)
				.map(ParameterTableDefinition::getTableName)
				.collect(Collectors.groupingBy(v -> v))
				.entrySet().stream()
				.filter(e -> e.getValue().size() > 1)
				.collect(Collectors.toMap(e -> e.getKey(), e -> Integer.valueOf(e.getValue().size())));

		// Shouldn't have duplicate on table name
		if (duplicates.size() > 0) {
			StringBuilder details = new StringBuilder();
			details.append("Error on duplicated table names : ");
			duplicates.entrySet().forEach(e -> {
				details.append("table \"").append(e.getKey()).append("\" (duplicated ").append(e.getValue()).append(") ");
			});
			details.append("Check @ParameterTable and @ParameterTableSet definition");

			throw new IllegalArgumentException(details.toString());
		}
	}

	/**
	 * @param reflections
	 * @return
	 */
	private void completeParameterValuesWithLinksAndMappings(
			Reflections reflections,
			Map<Class<?>, List<ParameterTableDefinition>> defs,
			List<ParameterLinkDefinition> allLinks,
			List<ParameterMappingDefinition> allMappings,
			ClassLoader ccl) {

		getLog().debug("Process completion of values, links and mappings for " + defs.size() + " identified tables");

		// For link transform
		Map<String, ParameterTableDefinition> allTables = defs.values().stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toMap(e -> e.getTableName(), v -> v));

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

	/**
	 * @param tableType
	 * @param allTables
	 * @param defs
	 * @param allLinks
	 * @param allMappings
	 * @param ccl
	 */
	@SuppressWarnings("unchecked")
	private void processParameterValueDirect(
			Class<?> tableType,
			Map<String, ParameterTableDefinition> allTables,
			Map<Class<?>, List<ParameterTableDefinition>> defs,
			List<ParameterLinkDefinition> allLinks,
			List<ParameterMappingDefinition> allMappings,
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
		Set<Field> foundFields = ReflectionUtils.getAllFields(tableType, f -> isFieldValue(f, paramTable.useAllFields()));

		// Valid methods (regarding anot)
		Set<Method> foundMethods = ReflectionUtils.getAllMethods(tableType, m -> isMethodValue(m));

		// Mixed all values + associated links, on both field and methods
		List<PossibleValueAnnotation> values = Stream.concat(foundFields.stream().map(f -> new PossibleValueAnnotation(f, ccl)),
				foundMethods.stream().map(m -> new PossibleValueAnnotation(m, ccl))).collect(Collectors.toList());

		// Prepare value columns (with support for composite)
		List<String> columnNames = values.stream()
				.flatMap(v -> v.isComposite() ? Stream.of(v.getCompositeNames()) : Stream.of(v.getValidName()))
				.peek(v -> getLog().debug("Found selected value " + v + " for type " + tableType.getName()))
				.collect(Collectors.toList());

		// Search for specified links
		Collection<ParameterLinkDefinition> links = extractLinksFromValues(defs, allTables, def, values);

		// Search for specified mappings
		Collection<ParameterMappingDefinition> mappings = extractMappingsFromValues(defs, allTables, def, values);

		// Produces select clause
		String selectClause = this.selectClauseGen.mergeSelectClause(
				columnNames,
				columnNames.size() + 2,
				links,
				mappings,
				allTables);

		getLog().debug("Generated select clause for type " + tableType.getName() + " is " + selectClause);

		def.setSelectClause(selectClause);
		allLinks.addAll(links);
		allMappings.addAll(mappings);
	}

	private static Stream<PossibleValueAnnotation> streamPossibleValueInDirectTablesDef(ParameterTableSet setAnnot) {
		return Stream.of(setAnnot.tables())
				.filter(t -> t.values().length > 0)
				.flatMap(t -> Stream.of(t.values())
						.map(v -> new PossibleValueAnnotation(v, t.tableName())));
	}

	/**
	 * @param tableType
	 * @param allTables
	 * @param defs
	 * @param allLinks
	 * @param allMappings
	 * @param ccl
	 */
	@SuppressWarnings("unchecked")
	private void processParameterValueSet(
			Class<?> tableType,
			Map<String, ParameterTableDefinition> allTables,
			Map<Class<?>, List<ParameterTableDefinition>> allDefs,
			List<ParameterLinkDefinition> allLinks,
			List<ParameterMappingDefinition> allMappings,
			ClassLoader ccl) {

		ParameterTableSet paramTableSet = tableType.getAnnotation(ParameterTableSet.class);

		// Edited def
		List<ParameterTableDefinition> defs = allDefs.get(tableType);

		// Valid fields (regarding anot / table def)
		Set<Field> foundFields = ReflectionUtils.getAllFields(tableType, f -> isFieldValue(f, paramTableSet.useAllFields()));

		// Valid methods (regarding anot)
		Set<Method> foundMethods = ReflectionUtils.getAllMethods(tableType, m -> isMethodValue(m));

		for (ParameterTableDefinition def : defs) {

			// Mixed all values + associated links, on both field and methods
			List<PossibleValueAnnotation> values = Stream.concat(
					Stream.concat(
							foundFields.stream().map(f -> new PossibleValueAnnotation(f, ccl)),
							foundMethods.stream().map(m -> new PossibleValueAnnotation(m, ccl))),
					streamPossibleValueInDirectTablesDef(paramTableSet))
					.filter(v -> v.isCompliantTable(def.getTableName())) // Only compliant
					.collect(Collectors.toList());

			// Prepare value columns (with support for composite)
			List<String> columnNames = values.stream()
					.flatMap(v -> v.isComposite() ? Stream.of(v.getCompositeNames()) : Stream.of(v.getValidName()))
					.peek(v -> getLog().debug("Found selected value " + v + " for type " + tableType.getName()))
					.collect(Collectors.toList());

			// Search for specified links
			Collection<ParameterLinkDefinition> links = extractLinksFromValues(allDefs, allTables, def, values);

			// Search for specified mappings
			Collection<ParameterMappingDefinition> mappings = extractMappingsFromValues(allDefs, allTables, def, values);

			// Produces select clause
			String selectClause = this.selectClauseGen.mergeSelectClause(
					columnNames,
					columnNames.size() + 2,
					links,
					mappings,
					allTables);

			getLog().debug("Generated select clause for type " + tableType.getName() + " is " + selectClause);

			def.setSelectClause(selectClause);
			allLinks.addAll(links);
			allMappings.addAll(mappings);
		}

	}

	/**
	 * <p>
	 * Search for the referenced table for a value used in a link
	 * </p>
	 * 
	 * @param defs
	 * @param allTables
	 * @param currentTableDef
	 * @param possibleValue
	 * @param annot
	 * @return
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

	/**
	 * @param defs
	 * @param allTables
	 * @param currentTableDef
	 * @param values
	 * @return
	 */
	private Collection<ParameterLinkDefinition> extractLinksFromValues(
			Map<Class<?>, List<ParameterTableDefinition>> defs,
			Map<String, ParameterTableDefinition> allTables,
			ParameterTableDefinition currentTableDef,
			List<PossibleValueAnnotation> values) {

		/*
		 * Keeps the links mapped to their table "to" for clean association of multiple
		 * link definition in case of composite key
		 */
		Map<String, ParameterLinkDefinition> linksByTablesTo = new HashMap<>();

		// Prepare links (where found)
		values.stream()
				.filter(a -> a.getLinkAnnot() != null)
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
					String linkName = !"".equals(annot.name()) ? annot.name() : toDef.getParameterName();

					// Get link already referenced or update "composite key" link
					ParameterLinkDefinition link = linksByTablesTo.get(toDef.getTableName());

					// Most case will only go there
					if (link == null) {

						link = new ParameterLinkDefinition();

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

						linksByTablesTo.put(toDef.getTableName(), link);
					}

					// Rare case : composite key - multiple def (all keys referenced
					// directly)
					else {
						int colIndex = (int) link.columnTos().count();

						link.setColumnFrom(colIndex, a.getValidName());
						link.setColumnTo(colIndex, columnTo);
					}

				});

		return linksByTablesTo.values();
	}

	/**
	 * @param defs
	 * @param allTables
	 * @param currentTableDef
	 * @param values
	 * @return
	 */
	private List<ParameterMappingDefinition> extractMappingsFromValues(
			Map<Class<?>, List<ParameterTableDefinition>> defs,
			Map<String, ParameterTableDefinition> allTables,
			ParameterTableDefinition currentTableDef,
			List<PossibleValueAnnotation> values) {

		// Prepare mappings (where found)
		return values.stream()
				.filter(a -> a.getMappingAnnot() != null)
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
					mapping.setColumnTo(!"".equals(annot.toColumn()) ? annot.toColumn() : toDef.getKeyName().toUpperCase());
					mapping.setMapTableColumnTo(!"".equals(annot.mapColumnTo()) ? annot.mapColumnTo() : mapping.getColumnTo());

					// From column, if not specified, is based on local key
					mapping.setColumnFrom(!"".equals(annot.fromColumn()) ? annot.fromColumn() : currentTableDef.getKeyName().toUpperCase());
					mapping.setMapTableColumnFrom(!"".equals(annot.mapColumnFrom()) ? annot.mapColumnFrom() : mapping.getColumnFrom());

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

	/**
	 * @param projectsByDomains
	 * @return
	 */
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
				.collect(Collectors.toMap(d -> d.getName(), d -> d));

		// If not done yet, add also default
		ParameterProjectDefinition defaultDef = new ParameterProjectDefinition();
		defaultDef.setCreatedTime(LocalDateTime.now());
		defaultDef.setName(ParameterProjectDefinition.DEFAULT_PROJECT);
		defaultDef.setColor(ProjectColor.GREY);
		defaultDef.setUuid(generateFixedUUID(ParameterProjectDefinition.DEFAULT_PROJECT, ParameterProjectDefinition.class));
		projectByNames.put(ParameterProjectDefinition.DEFAULT_PROJECT, defaultDef);

		return projectByNames;
	}

	/**
	 * @param reflections
	 * @return
	 */
	private Collection<ParameterVersionDefinition> specifyVersionsByProjects(
			Collection<ParameterProjectDefinition> projects) {

		getLog().debug("Init versions for each " + projects.size() + " identified projects");

		// Get all domain values
		return projects.stream().map(p -> {
			ParameterVersionDefinition version = new ParameterVersionDefinition();
			version.setName(this.config.getProjectVersion());
			version.setCreatedTime(LocalDateTime.now());
			version.setUpdatedTime(LocalDateTime.now());
			version.setProject(p);
			version.setUuid(generateFixedUUID(version.getName() + "-###-" + p.getName(), ParameterVersionDefinition.class));
			return version;
		}).collect(Collectors.toList());
	}

	/**
	 * @param reflections
	 * @return
	 */
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

	/**
	 * @param allProjects
	 * @param allDomains
	 * @param allTables
	 * @param allLinks
	 * @throws Exception
	 */
	private void export(
			Collection<ParameterProjectDefinition> allProjects,
			Collection<ParameterDomainDefinition> allDomains,
			Collection<ParameterTableDefinition> allTables,
			Collection<ParameterLinkDefinition> allLinks,
			Collection<ParameterMappingDefinition> allMappings,
			Collection<ParameterVersionDefinition> allVersions) throws Exception {

		getLog().info("Identified " + allProjects.size() + " projects, " + allVersions.size() + " versions, " + allDomains.size()
				+ " domains, " + allTables.size() + " tables, " + allLinks.size() + " links and " + allMappings.size()
				+ " mappings : now export dictionary to " + this.config.getDestinationFolder());

		ExportFile file = this.export.exportPackages(Arrays.asList(
				new GeneratedProjectPackage(allProjects),
				new GeneratedDictionaryPackage(allTables),
				new GeneratedFunctionalDomainPackage(allDomains),
				new GeneratedTableLinkPackage(allLinks),
				new GeneratedTableMappingPackage(allMappings),
				new GeneratedVersionPackage(allVersions)));

		String filename = DictionaryGeneratorConfig.AUTO_GEN_DEST_FILE_DESG.equals(this.config.getDestinationFileDesignation())
				? file.getFilename()
				: this.config.getDestinationFileDesignation() + ".par";

		// Write dictionary .par locally
		writeExportFile(file, filename);

		// Upload to remote server if asked for
		if (this.config.isUploadToServer()) {
			uploadExportFile(file);
		}
	}

	/**
	 * @param file
	 * @param name
	 * @throws IOException
	 */
	private void writeExportFile(ExportFile file, String name) throws IOException {

		Path destFolder = Paths.get(this.config.getDestinationFolder());

		if (!Files.exists(destFolder)) {
			getLog().debug("Creating missing destination folder " + this.config.getDestinationFolder());
			Files.createDirectories(destFolder);
		}

		Path destFile = destFolder.resolve(name);

		getLog().debug("Will write dictionary export in destination file " + destFile.toString());

		Files.write(destFile, file.getData(), StandardOpenOption.CREATE);

		getLog().info("Dictionary export done in file " + destFile.toString());
	}

	/**
	 * @param file
	 * @throws Exception
	 */
	private void uploadExportFile(ExportFile file) throws Exception {

		getLog().debug("Will upload the generated dictionary .par to " + this.config.getUploadEntryPointUri());

		DictionaryApi client = new DictionaryApiClient(this.config.getUploadEntryPointUri(), this.config.getUploadSecurityToken());

		CreatedDictionaryView view = client.uploadDictionaryPackage(file.toMultipartFile());

		getLog().info("Generated dictionary uploaded to " + this.config.getUploadEntryPointUri() + ". Get result : " + view);
	}

	/**
	 * <p>
	 * Proxy to logger
	 * </p>
	 * 
	 * @return
	 */
	private LogFacade getLog() {
		return this.config.getLogger();
	}

	/**
	 * <p>
	 * Select clause for fields used as value properties in a <tt>ParameterTable</tt>
	 * </p>
	 * 
	 * @param paramTable
	 * @return
	 */
	private static boolean isFieldValue(Field field, boolean useAll) {

		// All fields When the table is enabled "useAllFields"
		return (useAll

				// If not "useAllFields", keep only @ParameterValue fields or link or
				// mappings
				|| (field.isAnnotationPresent(ParameterValue.class) || field.isAnnotationPresent(ParameterCompositeValue.class)
						|| field.isAnnotationPresent(ParameterLink.class) || field.isAnnotationPresent(ParameterMapping.class)))

				// Exclude ignored with @ParameterIgnored or specified as key with
				// @ParameterKey
				&& !(field.isAnnotationPresent(ParameterIgnored.class) || field.isAnnotationPresent(ParameterKey.class));
	}

	/**
	 * <p>
	 * Select clause for methods used as value properties in a <tt>ParameterTable</tt>
	 * </p>
	 * 
	 * @param paramTable
	 * @return
	 */
	private static boolean isMethodValue(Method method) {
		return
		/*
		 * All methods specified as values, link or mappings, and not ignored or key
		 */
		(method.isAnnotationPresent(ParameterValue.class)
				|| method.isAnnotationPresent(ParameterCompositeValue.class)
				|| method.isAnnotationPresent(ParameterLink.class)
				|| method.isAnnotationPresent(ParameterMapping.class))
				&& !(method.isAnnotationPresent(ParameterIgnored.class)
						|| method.isAnnotationPresent(ParameterKey.class));
	}

	// 565c1448-6c74-4580-9595-6ee58817d985
	// 8 4 4 4 12
	// 32
	private static UUID generateFixedUUID(String refValue, Class<?> refType) {
		String refHas = Integer.toHexString(refValue.hashCode());
		String refTyp = Integer.toHexString(refType.getName().hashCode());
		int miss = 32 - (refHas.length() + refTyp.length());
		if (miss < 0) {
			throw new IllegalArgumentException(
					"Cannot process as UUID values " + refValue + " / " + refType.getName() + ". Got " + refHas + "-" + refTyp);
		}

		char[] complete = new char[miss];
		Arrays.fill(complete, '0');

		String raw = new StringBuilder(32).append(refTyp).append(complete).append(refHas).toString();

		return RuntimeValuesUtils.loadUUIDFromRaw(raw);
	}

	/**
	 * <p>
	 * Candidate for key identification. PRocess name / type detection for available field
	 * / method or direct annotation values
	 * </p>
	 * 
	 * @author elecomte
	 * @since v0.0.8
	 * @version 1
	 */
	private static class PossibleKeyAnnotation {

		private final ParameterKey keyAnnot;
		private final String validName;
		private final ColumnType validType;
		private final Collection<String> forTable;

		PossibleKeyAnnotation(Method method) {

			this.keyAnnot = method.getAnnotation(ParameterKey.class);
			// If not set on ParameterValue annotation, uses method name
			this.validName = this.keyAnnot != null && !"".equals(this.keyAnnot.value()) ? this.keyAnnot.value()
					: method.getName().toUpperCase();
			this.validType = this.keyAnnot != null && ColumnType.UNKNOWN != this.keyAnnot.type() ? this.keyAnnot.type()
					: ColumnType.forClass(method.getReturnType());
			this.forTable = this.keyAnnot != null && this.keyAnnot.forTable().length > 0 ? Arrays.asList(this.keyAnnot.forTable())
					: null;
		}

		PossibleKeyAnnotation(Field field) {
			this.keyAnnot = field.getAnnotation(ParameterKey.class);
			// If not set on ParameterValue annotation, uses field name
			this.validName = this.keyAnnot != null && !"".equals(this.keyAnnot.value()) ? this.keyAnnot.value()
					: field.getName().toUpperCase();
			// Type must be found from generic collection
			this.validType = this.keyAnnot != null && ColumnType.UNKNOWN != this.keyAnnot.type() ? this.keyAnnot.type()
					: ColumnType.forClass(field.getType());
			this.forTable = this.keyAnnot != null && this.keyAnnot.forTable().length > 0 ? Arrays.asList(this.keyAnnot.forTable())
					: null;
		}

		PossibleKeyAnnotation(ParameterTable paramTable) {
			this.keyAnnot = null;
			this.validName = paramTable.keyField().toUpperCase();
			this.validType = paramTable.keyType();
			this.forTable = Arrays.asList(paramTable.name());
		}

		/**
		 * @return the validName
		 */
		public String getValidName() {
			return this.validName;
		}

		/**
		 * @return the validType
		 */
		public ColumnType getValidType() {
			return this.validType;
		}

		/**
		 * @param name
		 * @return
		 */
		public boolean isCompliantTable(String name) {
			return this.forTable == null || this.forTable.contains(name);
		}

	}

	/**
	 * <p>
	 * Values are identified in various way :
	 * <ul>
	 * <li>Fields annotated with ParameterValue if table is not set to automatically
	 * include all fields as values</li>
	 * <li>All fields excepting the ones annotated with <tt>ParameterIgnored</tt> when the
	 * table is set to include all</li>
	 * <li>Methods annotated with <tt>ParameterValue</tt></li>
	 * <li>Inclusions of other value-related annotation : <tt>ParameterLink</tt> and
	 * <tt>ParameterMapping</tt></li>
	 * </ul>
	 * As the source for values are various and the available information depend on
	 * different conditions, <b>this inner component represent a candidate for value +
	 * link / mapping process</b>. It includes the required data for value, link and
	 * mapping specification, and process differently <tt>Method</tt> and <tt>Field</tt>
	 * to produce the same exact properties
	 * </p>
	 * 
	 * @author elecomte
	 * @since v0.0.8
	 * @version 1
	 */
	private static class PossibleValueAnnotation {

		private final ParameterValue valueAnnot;
		private final ParameterCompositeValue compositeValueAnnot;
		private final String validName;
		private final Class<?> validType;
		private final ParameterLink linkAnnot;
		private final ParameterMapping mappingAnnot;
		private final String[] compositeNames;
		private final Collection<String> forTable;

		// Detected on method
		PossibleValueAnnotation(Method method, ClassLoader contextClassLoader) {

			this.valueAnnot = method.getAnnotation(ParameterValue.class);
			this.compositeValueAnnot = method.getAnnotation(ParameterCompositeValue.class);
			// If not set on ParameterValue annotation, uses method name
			this.validName = prepareValidName(this.valueAnnot, method.getName());
			this.linkAnnot = method.getAnnotation(ParameterLink.class);
			this.mappingAnnot = method.getAnnotation(ParameterMapping.class);
			this.validType = this.mappingAnnot != null ? getMappingType(method, contextClassLoader) : method.getReturnType();
			this.compositeNames = prepareCompositeNames(this.compositeValueAnnot);
			this.forTable = prepareValidForTable(this.valueAnnot, this.compositeValueAnnot);
		}

		// Detected on field
		PossibleValueAnnotation(Field field, ClassLoader contextClassLoader) {

			this.valueAnnot = field.getAnnotation(ParameterValue.class);
			this.compositeValueAnnot = field.getAnnotation(ParameterCompositeValue.class);
			// If not set on ParameterValue annotation, uses field name
			this.validName = prepareValidName(this.valueAnnot, field.getName());
			this.linkAnnot = field.getAnnotation(ParameterLink.class);
			this.mappingAnnot = field.getAnnotation(ParameterMapping.class);
			// Type must be found from generic collection
			this.validType = this.mappingAnnot != null ? getMappingType(field, contextClassLoader) : field.getType();
			this.compositeNames = prepareCompositeNames(this.compositeValueAnnot);
			this.forTable = prepareValidForTable(this.valueAnnot, this.compositeValueAnnot);
		}

		// Declared directly into ParameterTable annot
		PossibleValueAnnotation(ParameterValue directValueSpec, String tableName) {

			this.valueAnnot = directValueSpec;
			this.compositeValueAnnot = null;
			// If not set on ParameterValue annotation, uses field name
			this.validName = prepareValidName(this.valueAnnot, null);
			this.linkAnnot = null;
			this.mappingAnnot = null;
			this.validType = null;
			this.compositeNames = null;
			this.forTable = Arrays.asList(tableName);
		}

		/**
		 * @return
		 */
		public String getValidName() {
			return this.validName;
		}

		/**
		 * @return
		 */
		public Class<?> getValidType() {
			return this.validType;
		}

		/**
		 * @return the linkAnnot
		 */
		public ParameterLink getLinkAnnot() {
			return this.linkAnnot;
		}

		/**
		 * @return the mappingAnnot
		 */
		public ParameterMapping getMappingAnnot() {
			return this.mappingAnnot;
		}

		/**
		 * @return
		 */
		public boolean isComposite() {
			// Rare
			return this.compositeValueAnnot != null;
		}

		/**
		 * @return
		 */
		public String[] getCompositeNames() {
			return this.compositeNames;
		}

		/**
		 * @param name
		 * @return
		 */
		public boolean isCompliantTable(String name) {
			return this.forTable == null || this.forTable.contains(name);
		}

		/**
		 * @param method
		 * @param contextClassLoader
		 * @return
		 * @throws ClassNotFoundException
		 */
		private static Class<?> getMappingType(Method method, ClassLoader contextClassLoader) {

			if (method.getReturnType().equals(Void.TYPE)) {

				throw new IllegalArgumentException("The void method " + method.getName()
						+ " is annotated with @ParameterMapping. Associated type cannot be found, you must set mapping definition on Set Of fields / methods");
			}

			try {
				return getMappingCompliantType(method.getGenericReturnType(), method.getReturnType(), contextClassLoader);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("The inner generic type for method " + method.getDeclaringClass() + "."
						+ method.getName() + " annotated with @ParameterMapping cannot be initialized", e);
			}
		}

		/**
		 * @param field
		 * @param contextClassLoader
		 * @return
		 * @throws ClassNotFoundException
		 */
		private static Class<?> getMappingType(Field field, ClassLoader contextClassLoader) {

			try {
				return getMappingCompliantType(field.getGenericType(), field.getType(), contextClassLoader);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("The inner generic type for field " + field.getDeclaringClass() + "." + field.getName()
						+ " annotated with @ParameterMapping cannot be initialized", e);
			}
		}

		/**
		 * @param resultGenericType
		 * @param contextClassLoader
		 * @return
		 * @throws ClassNotFoundException
		 */
		private static Class<?> getMappingCompliantType(Type resultGenericType, Class<?> rawType, ClassLoader contextClassLoader)
				throws ClassNotFoundException {

			if (resultGenericType instanceof ParameterizedType) {
				Type[] args = ((ParameterizedType) resultGenericType).getActualTypeArguments();
				return Class.forName(args[0].getTypeName(), false, contextClassLoader);
			}

			// Basic type
			return rawType;
		}

		private static String prepareValidName(ParameterValue annot, String holderName) {

			if (annot != null) {
				// Specified name
				if (!"".equals(annot.name())) {
					return annot.name();
				}

				// Value is an alias for name()
				if (!"".equals(annot.value())) {
					return annot.value();
				}
			}

			// If not found from annotation, use holderName
			return holderName.toUpperCase();
		}

		/**
		 * @param valueAnnot
		 * @param compAnnot
		 * @return
		 */
		private static Collection<String> prepareValidForTable(ParameterValue valueAnnot, ParameterCompositeValue compAnnot) {

			// Can be from a value annot
			if (valueAnnot != null) {
				if (valueAnnot.forTable().length > 0) {
					return Arrays.asList(valueAnnot.forTable());
				}
			}

			// Or a composite value annot
			if (compAnnot != null) {
				if (compAnnot.forTable().length > 0) {
					return Arrays.asList(compAnnot.forTable());
				}
			}

			return null;
		}

		/**
		 * @param compositeValueAnnot
		 * @return
		 */
		private static String[] prepareCompositeNames(ParameterCompositeValue compositeValueAnnot) {

			if (compositeValueAnnot != null) {

				if (compositeValueAnnot.names().length != 0) {
					return compositeValueAnnot.names();
				}

				// Support for alias
				if (compositeValueAnnot.value().length != 0) {
					return compositeValueAnnot.value();
				}
			}

			return new String[] {};
		}
	}
}