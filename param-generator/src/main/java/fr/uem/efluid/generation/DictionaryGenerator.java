package fr.uem.efluid.generation;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
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
import fr.uem.efluid.ParameterDomain;
import fr.uem.efluid.ParameterIgnored;
import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterLink;
import fr.uem.efluid.ParameterMapping;
import fr.uem.efluid.ParameterProject;
import fr.uem.efluid.ParameterTable;
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
import fr.uem.efluid.utils.Associate;
import fr.uem.efluid.utils.RuntimeValuesUtils;
import fr.uem.efluid.utils.SelectClauseGenerator;

/**
 * <p>
 * Annotation processing / reflexion based search for dictionary definition
 * </p>
 * <p>
 * Current version support inheritance and detect mappings
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 3
 */
public class DictionaryGenerator {

	private static final String VERSION = "3";

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
			getLog().info("###### PARAM-GENERATOR VERSION " + VERSION + " - START GENERATE ######");

			/* Search using filtering tools, based on a configured package root */
			Reflections reflections = initReflectionEntryPoint(contextClassLoader);

			/* Will prepare project, links and mappings when found */
			Map<String, ParameterProject> projects = new HashMap<>();
			List<ParameterLinkDefinition> allLinks = new ArrayList<>();
			List<ParameterMappingDefinition> allMappings = new ArrayList<>();

			/* Process all tables init + key for clean link building */
			Map<Class<?>, ParameterTableDefinition> typeTables = initParameterTablesWithKeys(reflections,
					searchDomainsByAnnotation(reflections, projects));

			/* Complete project definitions */
			Map<String, ParameterProjectDefinition> projectDefs = identifyParameterProjects(projects);

			/* Then process table values / links / mappings using refs */
			completeParameterValuesAndLinks(reflections, typeTables, allLinks, allMappings);

			/* Then, extract domains */
			Collection<ParameterDomainDefinition> allDomains = completeParameterDomains(typeTables, projects, projectDefs);

			/* Finally, prepare a version for each projects */
			Collection<ParameterVersionDefinition> allVersions = specifyVersionsByProjects(projectDefs.values());

			/* Now can export */
			export(projectDefs.values(), allDomains, typeTables.values(), allLinks, allMappings, allVersions);

			getLog().info("###### PARAM-GENERATOR VERSION " + VERSION + " - GENERATE COMPLETED IN "
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
	private Map<Class<?>, String> searchDomainsByAnnotation(Reflections reflections, Map<String, ParameterProject> projects) {

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
				reflections.getTypesAnnotatedWith(ParameterTable.class, true).stream()
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
	private Map<Class<?>, ParameterTableDefinition> initParameterTablesWithKeys(Reflections reflections,
			Map<Class<?>, String> annotDomains) {

		getLog().debug("Process parameter table init with " + annotDomains.size() + " identified domain annotations");

		Map<Class<?>, ParameterTableDefinition> tables = new HashMap<>();

		// Domains - direct search, with inherited
		for (Class<?> tableType : reflections.getTypesAnnotatedWith(ParameterTable.class, false)) {

			// Table cfg - includes inherited
			ParameterTable paramTable = tableType.getAnnotation(ParameterTable.class);

			// Only process concrete use of annotation
			if (paramTable != null) {

				ParameterTableDefinition def = new ParameterTableDefinition();
				def.setCreatedTime(LocalDateTime.now());
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
				def.setParameterName(paramTable.name());
				def.setTableName(paramTable.tableName());
				def.setWhereClause(paramTable.filterClause());
				def.setUuid(generateFixedUUID(paramTable.tableName(), ParameterTableDefinition.class));

				getLog().debug("Found mapped parameter type " + tableType.getName() + " with table " + def.getTableName()
						+ " and generated UUID " + def.getUuid().toString());

				// Search for key (field / method or parameterTable)
				completeTableParameterKey(tableType, paramTable, def);

				tables.put(tableType, def);
			}
		}

		return tables;
	}

	/**
	 * @param tableType
	 * @param paramTable
	 * @param def
	 */
	private void completeTableParameterKey(Class<?> tableType, ParameterTable paramTable, ParameterTableDefinition def) {

		// Search for key properties (field / method)
		List<Associate<ParameterKey, Class<?>>> keys = findAnnotOnFieldOrMethod(tableType, ParameterKey.class, Field::getType,
				Method::getReturnType);

		String keyName;
		ColumnType keyType;

		// Found specified key annotation
		if (keys.size() != 0) {

			// Use first
			Associate<ParameterKey, Class<?>> key = keys.get(0);

			keyName = key.getOne().value();
			keyType = key.getOne().type() != null && key.getOne().type() != ColumnType.UNKNOWN ? key.getOne().type()
					: ColumnType.forClass(key.getTwo());
		}

		// Found key specified in table def
		else if (!paramTable.keyField().equals("")) {
			keyName = paramTable.keyField();
			keyType = paramTable.keyType();
		}

		else {
			throw new IllegalArgumentException(
					"No key found for type " + tableType.getName()
							+ ". Need to specify a @ParameterKey on field or method, or to set keyField on @ParamaterTable");
		}

		// Init key def
		def.setKeyName(keyName);
		def.setKeyType(keyType);

		getLog().debug("Found key " + def.getKeyName() + " of type " + def.getKeyType() + " for type " + tableType.getName());
	}

	/**
	 * @param reflections
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void completeParameterValuesAndLinks(
			Reflections reflections,
			Map<Class<?>, ParameterTableDefinition> defs,
			List<ParameterLinkDefinition> allLinks,
			List<ParameterMappingDefinition> allMappings) {

		getLog().debug("Process completion of values and links for " + defs.size() + " identified tables");

		// For link transform
		Map<String, ParameterTableDefinition> allTables = defs.values().stream().collect(Collectors.toMap(e -> e.getTableName(), v -> v));

		// Domains - direct search, with inherited
		for (Class<?> tableType : defs.keySet()) {

			// Table cfg - includes inherited
			ParameterTable paramTable = tableType.getAnnotation(ParameterTable.class);

			// Edited def
			ParameterTableDefinition def = defs.get(tableType);

			// Valid fields (regarding anot / table def)
			Stream<PossibleValueAnnotation> byFields = ReflectionUtils
					.getAllFields(tableType, f -> (paramTable.useAllFields()
							&& !f.isAnnotationPresent(ParameterIgnored.class))
							|| f.isAnnotationPresent(ParameterValue.class))
					.stream()
					.map(f -> new PossibleValueAnnotation(f));

			// Valid methods (regarding anot)
			Stream<PossibleValueAnnotation> byMethods = ReflectionUtils
					.getAllMethods(tableType, m -> (m.isAnnotationPresent(ParameterValue.class)
							|| m.isAnnotationPresent(ParameterLink.class)
							|| m.isAnnotationPresent(ParameterTable.class)) && !m.isAnnotationPresent(ParameterIgnored.class))
					.stream()
					.map(m -> new PossibleValueAnnotation(m));

			// Mixed all values + associated links, on both field and methods
			List<PossibleValueAnnotation> values = Stream.concat(byFields, byMethods).collect(Collectors.toList());

			// Prepare value columns
			List<String> columnNames = values.stream()
					.map(PossibleValueAnnotation::getValidName)
					.peek(v -> getLog().debug("Found selected value " + v + " for type " + tableType.getName()))
					.collect(Collectors.toList());

			// Search for specified links
			List<ParameterLinkDefinition> links = extractLinksFromValues(defs, allTables, def, values);

			// Search for specified mappings
			List<ParameterMappingDefinition> mappings = extractMappingsFromValues(defs, allTables, def, values);

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
	 * @author elecomte
	 * @since v0.0.8
	 * @version 1
	 */
	private static class PossibleValueAnnotation {

		private final ParameterValue valueAnnot;
		private final String validName;
		private final Class<?> validType;
		private final ParameterLink linkAnnot;
		private final ParameterMapping mappingAnnot;

		PossibleValueAnnotation(Method method) {

			this.valueAnnot = method.getAnnotation(ParameterValue.class);
			// If not set on ParameterValue annotation, uses method name
			this.validName = this.valueAnnot != null && !"".equals(this.valueAnnot.value()) ? this.valueAnnot.value()
					: method.getName().toUpperCase();
			this.validType = method.getReturnType();
			this.linkAnnot = method.getAnnotation(ParameterLink.class);
			this.mappingAnnot = method.getAnnotation(ParameterMapping.class);
		}

		PossibleValueAnnotation(Field field) {
			this.valueAnnot = field.getAnnotation(ParameterValue.class);
			// If not set on ParameterValue annotation, uses field name
			this.validName = this.valueAnnot != null && !"".equals(this.valueAnnot.value()) ? this.valueAnnot.value()
					: field.getName().toUpperCase();
			this.validType = field.getType();
			this.linkAnnot = field.getAnnotation(ParameterLink.class);
			this.mappingAnnot = field.getAnnotation(ParameterMapping.class);
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
	}

	/**
	 * @param defs
	 * @param allTables
	 * @param currentTableDef
	 * @param values
	 * @return
	 */
	private List<ParameterLinkDefinition> extractLinksFromValues(
			Map<Class<?>, ParameterTableDefinition> defs,
			Map<String, ParameterTableDefinition> allTables,
			ParameterTableDefinition currentTableDef,
			List<PossibleValueAnnotation> values) {

		// Prepare links (where found)
		return values.stream()
				.filter(a -> a.getLinkAnnot() != null)
				.map(a -> {
					ParameterLink annot = a.getLinkAnnot();
					ParameterLinkDefinition link = new ParameterLinkDefinition();
					link.setCreatedTime(LocalDateTime.now());
					link.setDictionaryEntry(currentTableDef);
					link.setColumnFrom(a.getValidName());

					// If not specified directly, search table to by param
					ParameterTableDefinition toDef = (annot.toTableName() == null || annot.toTableName().trim().equals(""))
							? defs.get(annot.toParameter())
							: allTables.get(annot.toTableName());

					// If still empty, search by attribute type
					if (toDef == null) {
						toDef = defs.get(a.getValidType());
					}

					// Mandatory, fail if still empty
					if (toDef == null) {
						throw new IllegalArgumentException(
								"No valid specified table found for link on column " + a.getValidName() + " for type "
										+ currentTableDef.getTableName()
										+ ". Specify @ParameterLink toTableName or toParameter with a valid value");
					}

					link.setTableTo(toDef.getTableName());
					link.setColumnTo(annot.toColumn() != null ? annot.toColumn() : toDef.getKeyName());

					String refForUuid = currentTableDef.getTableName() + "." + link.getColumnFrom() + " -> " + link.getTableTo() + "."
							+ link.getColumnTo();

					// Produces uuid
					link.setUuid(generateFixedUUID(refForUuid, ParameterLinkDefinition.class));

					getLog().debug("Found link " + refForUuid + " with generated UUID " + link.getUuid().toString() + " for type "
							+ currentTableDef.getTableName());

					return link;
				})
				.collect(Collectors.toList());

	}

	/**
	 * @param defs
	 * @param allTables
	 * @param currentTableDef
	 * @param values
	 * @return
	 */
	private List<ParameterMappingDefinition> extractMappingsFromValues(
			Map<Class<?>, ParameterTableDefinition> defs,
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
					mapping.setDictionaryEntry(currentTableDef);
					mapping.setColumnFrom(a.getValidName());
					mapping.setMapTable(annot.mapTableName());

					// If not specified directly, search table to by param
					ParameterTableDefinition toDef = (annot.toTableName() == null || annot.toTableName().trim().equals(""))
							? defs.get(annot.toParameter())
							: allTables.get(annot.toTableName());

					// If still empty, search by attribute type
					if (toDef == null) {
						toDef = defs.get(a.getValidType());
					}

					// Mandatory, fail if still empty
					if (toDef == null) {
						throw new IllegalArgumentException(
								"No valid specified table found for mapping on column " + a.getValidName() + " for type "
										+ currentTableDef.getTableName()
										+ ". Specify @ParameterMapping toTableName or toParameter with a valid value");
					}

					mapping.setTableTo(toDef.getTableName());
					mapping.setColumnTo(annot.toColumn() != null ? annot.toColumn() : toDef.getKeyName());
					mapping.setMapTableColumnFrom(annot.mapColumnFrom() != null ? annot.mapColumnFrom() : a.getValidName());
					mapping.setMapTableColumnTo(annot.mapColumnTo() != null ? annot.mapColumnTo() : toDef.getKeyName());

					String refForUuid = currentTableDef.getTableName() + "." + mapping.getColumnFrom() + " -> " + mapping.getMapTable()
							+ "." + mapping.getMapTableColumnFrom() + " -> " + mapping.getMapTable() + "." + mapping.getMapTableColumnTo()
							+ " -> " + mapping.getTableTo() + "." + mapping.getColumnTo();

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
			return version;
		}).collect(Collectors.toList());
	}

	/**
	 * @param reflections
	 * @return
	 */
	private Collection<ParameterDomainDefinition> completeParameterDomains(
			Map<Class<?>, ParameterTableDefinition> defs,
			Map<String, ParameterProject> projectsByDomains,
			Map<String, ParameterProjectDefinition> projectsByName) {

		getLog().debug("Process completion of distinct domains extracted from " + defs.size() + " identified tables");

		// Get all domain values
		Map<String, ParameterDomainDefinition> domains = defs.values().stream().map(d -> d.getDomain().getName()).distinct().map(n -> {
			ParameterDomainDefinition def = new ParameterDomainDefinition();
			def.setCreatedTime(LocalDateTime.now());
			def.setName(n);
			def.setUuid(generateFixedUUID(n, ParameterDomainDefinition.class));

			// linking to project
			ParameterProject projectPar = projectsByDomains.get(n);

			// If none found, set default project
			def.setProject(projectPar != null ? projectsByName.get(projectPar.name())
					: projectsByName.get(ParameterProjectDefinition.DEFAULT_PROJECT));

			getLog().debug("Identified distinct domain \"" + n + "\" with generated UUID " + def.getUuid() + " onto project "
					+ def.getProject().getName());
			return def;
		}).collect(Collectors.toMap(ParameterDomainDefinition::getName, v -> v));

		// Then fix associated domains on param tables
		defs.values().forEach(d -> {
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
				+ " domains, " + allTables.size() + " tables and " + allLinks.size() + " links : now export dictionary to "
				+ this.config.getDestinationFolder());

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

		Path path = new File(this.config.getDestinationFolder() + "/" + name).toPath();
		Files.write(path, file.getData());

		getLog().info("Dictionary export done in file " + path.toString());
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
	 * Combined search of annotation on field and methods, giving the associated return
	 * type / field type
	 * </p>
	 * 
	 * @param tableType
	 * @param annotType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static <A, T extends Annotation> List<Associate<T, A>> findAnnotOnFieldOrMethod(Class<?> tableType, Class<T> annotType,
			Function<Field, A> fieldExtract, Function<Method, A> methodExtract) {

		Stream<Associate<T, A>> byFields = ReflectionUtils.getAllFields(tableType, f -> f.isAnnotationPresent(annotType)).stream()
				.map(f -> Associate.of(f.getAnnotation(annotType), fieldExtract.apply(f)));

		Stream<Associate<T, A>> byMethods = ReflectionUtils.getAllMethods(tableType, f -> f.isAnnotationPresent(annotType)).stream()
				.map(f -> Associate.of(f.getAnnotation(annotType), methodExtract.apply(f)));

		return Stream.concat(byFields, byMethods).collect(Collectors.toList());
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
}
