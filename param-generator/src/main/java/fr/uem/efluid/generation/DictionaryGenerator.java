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
import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterLink;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;
import fr.uem.efluid.clients.DictionaryApiClient;
import fr.uem.efluid.generation.DictionaryGeneratorConfig.LogFacade;
import fr.uem.efluid.model.GeneratedDictionaryPackage;
import fr.uem.efluid.model.GeneratedFunctionalDomainPackage;
import fr.uem.efluid.model.GeneratedTableLinkPackage;
import fr.uem.efluid.model.ParameterDomainDefinition;
import fr.uem.efluid.model.ParameterLinkDefinition;
import fr.uem.efluid.model.ParameterTableDefinition;
import fr.uem.efluid.rest.v1.DictionaryApi;
import fr.uem.efluid.rest.v1.model.CreatedDictionaryView;
import fr.uem.efluid.services.ExportService;
import fr.uem.efluid.services.types.ExportFile;
import fr.uem.efluid.utils.Associate;
import fr.uem.efluid.utils.RuntimeValuesUtils;
import fr.uem.efluid.utils.SelectClauseGenerator;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class DictionaryGenerator {

	private static final String VERSION = "1";

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

			/* Process all tables init + key for clean link building */
			Map<Class<?>, ParameterTableDefinition> typeTables = initParameterTablesWithKeys(reflections,
					searchDomainsByAnnotation(reflections));

			/* Then process table values / links using refs */
			Collection<ParameterLinkDefinition> allLinks = completeParameterValuesAndLinks(reflections, typeTables);

			/* Finally, extract domains */
			Collection<ParameterDomainDefinition> allDomains = completeParameterDomains(typeTables);

			/* Now can export */
			export(allDomains, typeTables.values(), allLinks);

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
	private Map<Class<?>, String> searchDomainsByAnnotation(Reflections reflections) {

		getLog().debug("Process domains spec search");

		// Prepare mapped domains
		Map<Class<?>, String> typeDomains = new HashMap<>();

		// Domains - direct search, with inherited
		Set<Class<?>> domains = reflections.getTypesAnnotatedWith(ParameterDomain.class, true);

		// Domain search is the entry point of all mapped types
		for (Class<?> typedDomain : domains) {
			String domainName = typedDomain.getAnnotation(ParameterDomain.class).value();

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

			// Table cfg
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

				// Search for key (field / method)
				List<Associate<ParameterKey, Class<?>>> keys = findAnnotOnFieldOrMethod(tableType, ParameterKey.class, Field::getType,
						Method::getReturnType);

				// At least one is needed
				if (keys.size() == 0) {
					throw new IllegalArgumentException(
							"No key found for type " + tableType.getName() + ". Need to specify a @ParameterKey on field or method");
				}

				// Use first
				Associate<ParameterKey, Class<?>> key = keys.get(0);

				// Init key def
				def.setKeyName(key.getOne().value());
				def.setKeyType(key.getOne().type() != null && key.getOne().type() != ColumnType.UNKNOWN ? key.getOne().type()
						: ColumnType.forClass(key.getTwo()));

				getLog().debug("Found key " + def.getKeyName() + " of type " + def.getKeyType() + " for type " + tableType.getName());

				tables.put(tableType, def);
			}
		}

		return tables;
	}

	/**
	 * @param reflections
	 * @return
	 */
	private Collection<ParameterLinkDefinition> completeParameterValuesAndLinks(Reflections reflections,
			Map<Class<?>, ParameterTableDefinition> defs) {

		getLog().debug("Process completion of values and links for " + defs.size() + " identified tables");

		// For link transform
		Map<String, ParameterTableDefinition> allTables = defs.values().stream().collect(Collectors.toMap(e -> e.getTableName(), v -> v));
		List<ParameterLinkDefinition> allLinks = new ArrayList<>();

		// Domains - direct search, with inherited
		for (Class<?> tableType : defs.keySet()) {

			// Edited def
			ParameterTableDefinition def = defs.get(tableType);

			// All values + associated links, on both field and
			List<Associate<ParameterValue, Associate<ParameterLink, Class<?>>>> values = findAnnotOnFieldOrMethod(tableType,
					ParameterValue.class,
					f -> Associate.of(f.getAnnotation(ParameterLink.class), f.getType()),
					m -> Associate.of(m.getAnnotation(ParameterLink.class), m.getReturnType()));

			// Prepare links (where found)
			List<ParameterLinkDefinition> links = values.stream()
					.filter(a -> a.getTwo().hasBoth())
					.map(a -> {
						ParameterLinkDefinition link = new ParameterLinkDefinition();
						link.setCreatedTime(LocalDateTime.now());
						link.setDictionaryEntry(def);
						link.setColumnFrom(a.getOne().value());
						link.setColumnTo(a.getTwo().getOne().toColumn());

						// If not specified directly, search table to by param
						ParameterTableDefinition toDef = (a.getTwo().getOne().toTableName() == null
								|| a.getTwo().getOne().toTableName().trim().equals(""))
										? defs.get(a.getTwo().getOne().toParameter())
										: allTables.get(a.getTwo().getOne().toTableName());

						// If still empty, search by attribute type
						if (toDef == null) {
							toDef = defs.get(a.getTwo().getTwo());
						}

						// Mandatory, fail if still empty
						if (toDef == null) {
							throw new IllegalArgumentException(
									"No valid specified table found for link on column " + a.getOne().value() + " for type "
											+ tableType.getName()
											+ ". Specify @ParameterLink toTableName or toParameter with a valid value");
						}

						link.setTableTo(toDef.getTableName());

						String refForUuid = def.getTableName() + "." + link.getColumnFrom() + " -> " + link.getTableTo() + "."
								+ link.getColumnTo();

						// Produces uuid
						link.setUuid(generateFixedUUID(refForUuid, ParameterLinkDefinition.class));

						getLog().debug("Found link " + refForUuid + " with generated UUID " + link.getUuid().toString() + " for type "
								+ tableType.getName());

						return link;
					})
					.collect(Collectors.toList());

			// Prepare value columns
			List<String> columnNames = values.stream()
					.map(Associate::getOne)
					.map(ParameterValue::value)
					.peek(v -> getLog().debug("Found selected value " + v + " for type " + tableType.getName()))
					.collect(Collectors.toList());

			// Produces select clause
			String selectClause = this.selectClauseGen.mergeSelectClause(columnNames, columnNames.size() + 2, links, allTables);

			getLog().debug("Generated select clause for type " + tableType.getName() + " is " + selectClause);

			def.setSelectClause(selectClause);
			allLinks.addAll(links);
		}

		return allLinks;
	}

	/**
	 * @param reflections
	 * @return
	 */
	private Collection<ParameterDomainDefinition> completeParameterDomains(Map<Class<?>, ParameterTableDefinition> defs) {

		getLog().debug("Process completion of distinct domains extracted from " + defs.size() + " identified tables");

		// Get all domain values
		Map<String, ParameterDomainDefinition> domains = defs.values().stream().map(d -> d.getDomain().getName()).distinct().map(n -> {
			ParameterDomainDefinition def = new ParameterDomainDefinition();
			def.setCreatedTime(LocalDateTime.now());
			def.setName(n);
			def.setUuid(generateFixedUUID(n, ParameterDomainDefinition.class));
			getLog().debug("Identified distinct domain \"" + n + "\" with generated UUID " + def.getUuid());
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
	 * @param domains
	 * @param tables
	 * @param links
	 */
	private void export(
			Collection<ParameterDomainDefinition> allDomains,
			Collection<ParameterTableDefinition> allTables,
			Collection<ParameterLinkDefinition> allLinks) throws Exception {

		getLog().info("Identified " + allDomains.size() + " domains, " + allTables.size() + " tables and " + allLinks.size()
				+ " links : now export dictionary to " + this.config.getDestinationFolder());

		ExportFile file = this.export.exportPackages(Arrays.asList(
				new GeneratedDictionaryPackage(allTables),
				new GeneratedFunctionalDomainPackage(allDomains),
				new GeneratedTableLinkPackage(allLinks)));

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
