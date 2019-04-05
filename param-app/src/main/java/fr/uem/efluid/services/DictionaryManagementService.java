package fr.uem.efluid.services;

import static fr.uem.efluid.utils.ErrorType.DIC_ENTRY_NOT_FOUND;
import static fr.uem.efluid.utils.ErrorType.DIC_KEY_NOT_UNIQ;
import static fr.uem.efluid.utils.ErrorType.DIC_NOT_REMOVABLE;
import static fr.uem.efluid.utils.ErrorType.DIC_NO_KEY;
import static fr.uem.efluid.utils.ErrorType.DIC_TOO_MANY_KEYS;
import static fr.uem.efluid.utils.ErrorType.DOMAIN_NOT_EXIST;
import static fr.uem.efluid.utils.ErrorType.DOMAIN_NOT_REMOVABLE;
import static fr.uem.efluid.utils.ErrorType.VERSION_NOT_MODEL_ID;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.Shared;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.entities.TableLink;
import fr.uem.efluid.model.entities.TableMapping;
import fr.uem.efluid.model.entities.Version;
import fr.uem.efluid.model.metas.ColumnDescription;
import fr.uem.efluid.model.metas.TableDescription;
import fr.uem.efluid.model.repositories.DatabaseDescriptionRepository;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.FunctionalDomainRepository;
import fr.uem.efluid.model.repositories.ManagedModelDescriptionRepository;
import fr.uem.efluid.model.repositories.ManagedModelDescriptionRepository.IdentifierType;
import fr.uem.efluid.model.repositories.ProjectRepository;
import fr.uem.efluid.model.repositories.TableLinkRepository;
import fr.uem.efluid.model.repositories.TableMappingRepository;
import fr.uem.efluid.model.repositories.VersionRepository;
import fr.uem.efluid.services.types.DictionaryEntryEditData;
import fr.uem.efluid.services.types.DictionaryEntryEditData.ColumnEditData;
import fr.uem.efluid.services.types.DictionaryEntrySummary;
import fr.uem.efluid.services.types.DictionaryExportPackage;
import fr.uem.efluid.services.types.DictionaryPackage;
import fr.uem.efluid.services.types.ExportFile;
import fr.uem.efluid.services.types.ExportImportResult;
import fr.uem.efluid.services.types.FunctionalDomainData;
import fr.uem.efluid.services.types.FunctionalDomainExportPackage;
import fr.uem.efluid.services.types.FunctionalDomainPackage;
import fr.uem.efluid.services.types.LinkUpdateFollow;
import fr.uem.efluid.services.types.ProjectExportPackage;
import fr.uem.efluid.services.types.ProjectPackage;
import fr.uem.efluid.services.types.SelectableTable;
import fr.uem.efluid.services.types.SharedPackage;
import fr.uem.efluid.services.types.TableLinkExportPackage;
import fr.uem.efluid.services.types.TableLinkPackage;
import fr.uem.efluid.services.types.TableMappingExportPackage;
import fr.uem.efluid.services.types.TableMappingPackage;
import fr.uem.efluid.services.types.TestQueryData;
import fr.uem.efluid.services.types.VersionData;
import fr.uem.efluid.services.types.VersionExportPackage;
import fr.uem.efluid.services.types.VersionPackage;
import fr.uem.efluid.tools.ManagedQueriesGenerator;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.SelectClauseGenerator;

/**
 * <p>
 * For the management of everything included in a "dictionary" :
 * <ul>
 * <li>The dictionary entries (parameter table)</li>
 * <li>The functional Domains</li>
 * <li>The links</li>
 * <li>The versions</li>
 * </ul>
 * The dictionary is associated to a project.
 * </p>
 * <p>
 * Entity hierarchy is :
 * <ul>
 * <li><b>Project</b></li>
 * <ul>
 * <li>-&gt; Versions</li>
 * <li>-&gt; FunctionalDomain</li>
 * <ul>
 * <li>DictionaryEntry</li>
 * <ul>
 * <li>Links</li>
 * <li>Mappings</li>
 * </ul>
 * </ul>
 * </ul>
 * </ul>
 * </p>
 * <p>
 * <b>Features included</b> :
 * <ul>
 * <li>Basic CRUD</li>
 * <li>Import / export features (full / For one project / for one domaine)</li>
 * <li>Some specific features related to business rules shared with user ui</li>
 * <li>Support for composite key in dictionary entries</li>
 * <li>Use of composite keys for links</li>
 * </ul>
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 7
 */
@Service
@Transactional
public class DictionaryManagementService extends AbstractApplicationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryManagementService.class);

	@Autowired
	private FunctionalDomainRepository domains;

	@Autowired
	private DictionaryRepository dictionary;

	@Autowired
	private VersionRepository versions;

	@Autowired
	private DatabaseDescriptionRepository metadatas;

	@Autowired
	private TableMappingRepository mappings;

	@Autowired
	private TableLinkRepository links;

	@Autowired
	private ProjectRepository projects;

	@Autowired
	private ManagedQueriesGenerator queryGenerator;

	@Autowired
	private ExportImportService ioService;

	@Autowired
	private ProjectManagementService projectService;

	@Autowired
	private ManagedModelDescriptionRepository modelDescs;

	@Autowired
	private PrepareIndexService indexService;

	/**
	 * @param name
	 */
	public void setCurrentVersion(String name) {

		this.projectService.assertCurrentUserHasSelectedProject();
		Project project = this.projectService.getCurrentSelectedProjectEntity();

		// Search by name
		Version version = this.versions.findByNameAndProject(name, project);

		// Search ref identifier if enabled
		String modelId = this.modelDescs.getCurrentModelIdentifier();

		// Create
		if (version == null) {
			LOGGER.info("Create version {} in current project", name);
			version = new Version();
			version.setUuid(UUID.randomUUID());
			version.setName(name);
			version.setCreatedTime(LocalDateTime.now());
			version.setProject(project);
		} else {
			LOGGER.info("Update version {} in current project", name);
		}

		// Always init
		version.setUpdatedTime(LocalDateTime.now());

		// Never erase existing. Can create or update only
		if (modelId != null) {
			version.setModelIdentity(modelId);
		}

		this.versions.save(version);
	}

	/**
	 * @return
	 */
	public VersionData getLastVersion() {

		this.projectService.assertCurrentUserHasSelectedProject();
		Project project = this.projectService.getCurrentSelectedProjectEntity();
		Version last = this.versions.getLastVersionForProject(project);

		return VersionData.fromEntity(last, this.versions.isVersionUpdatable(last.getUuid()));
	}

	/**
	 * @return
	 */
	public boolean isDictionaryUpdatedAfterLastVersion() {

		this.projectService.assertCurrentUserHasSelectedProject();
		Project project = this.projectService.getCurrentSelectedProjectEntity();

		return this.versions.hasDictionaryUpdatesAfterLastVersionForProject(project);
	}

	/**
	 * @return
	 */
	public List<VersionData> getAvailableVersions() {

		this.projectService.assertCurrentUserHasSelectedProject();
		Project project = this.projectService.getCurrentSelectedProjectEntity();
		Version last = this.versions.getLastVersionForProject(project);

		return this.versions.findByProject(project).stream()
				.map(v -> getCompletedVersion(v, last))
				.collect(Collectors.toList());
	}

	/**
	 * @return
	 */
	public List<FunctionalDomainData> getAvailableFunctionalDomains() {

		this.projectService.assertCurrentUserHasSelectedProject();
		Project project = this.projectService.getCurrentSelectedProjectEntity();

		LOGGER.debug("Listing functional Domains");

		// TODO : keep this in cache, or precalculated (once used, cannot be "unused")
		List<UUID> usedIds = this.domains.findUsedIds();

		return this.domains.findByProject(project).stream()
				.map(FunctionalDomainData::fromEntity)
				.peek(d -> d.setCanDelete(!usedIds.contains(d.getUuid())))
				.collect(Collectors.toList());
	}

	/**
	 * @param uuid
	 */
	public void deleteDictionaryEntry(UUID uuid) {

		LOGGER.info("Process delete on dictionary entry {}", uuid);

		assertDictionaryEntryCanBeRemoved(uuid);

		List<TableLink> dicLinks = this.links.findByDictionaryEntry(new DictionaryEntry(uuid));

		// Remove also associated links
		if (dicLinks != null && !dicLinks.isEmpty()) {
			LOGGER.debug("Remove {} associated links from dictionary entry {}", Integer.valueOf(dicLinks.size()), uuid);
			this.links.deleteAll(dicLinks);
		}

		this.dictionary.deleteById(uuid);
	}

	/**
	 * @param uuid
	 */
	public void deleteFunctionalDomain(UUID uuid) {

		LOGGER.info("Process delete on functional domain {}", uuid);

		assertDomainCanBeRemoved(uuid);

		this.domains.deleteById(uuid);
	}

	/**
	 * @return
	 */
	public List<SelectableTable> getSelectableTables() {

		LOGGER.debug("Listing selectable tables for a new dictionary entry");

		this.projectService.assertCurrentUserHasSelectedProject();
		Project project = this.projectService.getCurrentSelectedProjectEntity();

		// Existing data
		List<DictionaryEntry> entries = this.dictionary.findByDomainProject(project);

		// Table with columns
		Map<String, List<String>> allTables = this.metadatas.getTables().stream().collect(Collectors.toMap(t -> t.getName(),
				t -> t.getColumns().stream().map(ColumnDescription::getName).sorted().collect(Collectors.toList())));

		// Convert dictionnary as selectable
		List<SelectableTable> selectables = entries.stream()
				.map(e -> new SelectableTable(e.getTableName(), e.getParameterName(), e.getDomain().getName(),
						allTables.get(e.getTableName())))
				.peek(s -> allTables.remove(s.getTableName())).collect(Collectors.toList());

		// And add table not yet mapped
		selectables.addAll(allTables.keySet().stream().sorted().map(t -> new SelectableTable(t, null, null, allTables.get(t)))
				.collect(Collectors.toSet()));

		// Sorted by table name
		Collections.sort(selectables);

		return selectables;
	}

	/**
	 * @return
	 */
	public boolean isDictionnaryExists() {
		return this.dictionary.count() > 0;
	}

	/**
	 * As summaries, for display or first level edit
	 * 
	 * @return
	 */
	public List<DictionaryEntrySummary> getDictionnaryEntrySummaries() {

		this.projectService.assertCurrentUserHasSelectedProject();
		Project project = this.projectService.getCurrentSelectedProjectEntity();

		LOGGER.debug("Listing dictionary content");

		// TODO : keep this in cache, or precalculated (once used, cannot be "unused")
		List<UUID> usedIds = this.dictionary.findUsedIds();

		// For link building, need other dicts
		Map<String, DictionaryEntry> allDicts = this.dictionary.findAllMappedByTableName(project);

		return this.dictionary.findByDomainProject(project).stream()
				.map(e -> DictionaryEntrySummary.fromEntity(e,
						this.queryGenerator.producesSelectParameterQuery(e, this.links.findByDictionaryEntry(e), allDicts)))
				.peek(d -> d.setCanDelete(!usedIds.contains(d.getUuid())))
				.sorted()
				.collect(Collectors.toList());
	}

	/**
	 * When editing an existing entry
	 * 
	 * @param entryUuid
	 * @return
	 */
	public DictionaryEntryEditData editEditableDictionaryEntry(UUID entryUuid) {

		this.projectService.assertCurrentUserHasSelectedProject();
		Project project = this.projectService.getCurrentSelectedProjectEntity();

		LOGGER.info("Open editable content for dictionary entry {}", entryUuid);

		// Check valid uuid
		assertDictionaryEntryExists(entryUuid);

		// Open existing one
		DictionaryEntry entry = this.dictionary.getOne(entryUuid);

		// Prepare basic fields to edit
		DictionaryEntryEditData edit = DictionaryEntryEditData.fromEntity(entry);

		// For link building, need other dicts
		Map<String, DictionaryEntry> allDicts = this.dictionary.findAllMappedByTableName(project);

		// Links used for mapped tables
		List<TableLink> dicLinks = this.links.findByDictionaryEntry(entry);

		// Need select clause as a list
		Collection<String> selecteds = isNotEmpty(entry.getSelectClause())
				? this.queryGenerator.splitSelectClause(entry.getSelectClause(), dicLinks, allDicts)
				: Collections.emptyList();

		TableDescription desc = getTableDescription(edit.getTable());

		List<String> keyNames = entry.keyNames().collect(Collectors.toList());

		// Keep links
		Map<String, LinkUpdateFollow> mappedLinks = dicLinks.stream().flatMap(
				l -> LinkUpdateFollow.flatMapFromColumn(l, l.columnFroms()))
				.collect(Collectors.toMap(LinkUpdateFollow::getColumn, v -> v));

		// Dedicated case : missing table, pure simulated content
		if (desc == TableDescription.MISSING) {

			// Avoid immutable lists
			Collection<String> editableSelecteds = new ArrayList<>(selecteds);

			// On missing, add key column(s)
			if (!editableSelecteds.contains(entry.getKeyName())) {
				editableSelecteds.addAll(keyNames);
			}

			edit.setMissingTable(true);
			edit.setColumns(editableSelecteds.stream()
					.map(c -> ColumnEditData.fromSelecteds(c, keyNames, entry.keyTypes().collect(Collectors.toList()), mappedLinks.get(c)))
					.sorted()
					.collect(Collectors.toList()));
		} else {
			// Add metadata to use for edit
			edit.setColumns(desc.getColumns().stream()
					.sorted()
					.map(c -> ColumnEditData.fromColumnDescription(c, selecteds, keyNames, mappedLinks.get(c.getName())))
					.collect(Collectors.toList()));
		}

		return edit;
	}

	/**
	 * For a new entry
	 * 
	 * @param tableName
	 * @return
	 */
	public DictionaryEntryEditData prepareNewEditableDictionaryEntry(String tableName) {

		LOGGER.info("Init new content of dictionary entry for table {}", tableName);

		List<FunctionalDomainData> domainDatas = this.getAvailableFunctionalDomains();

		// Must have domain
		if (domainDatas == null || domainDatas.size() == 0) {
			throw new ApplicationException(DOMAIN_NOT_EXIST,
					"No domain specified yet for current project. Cannot init entry for table " + tableName);
		}

		DictionaryEntryEditData edit = new DictionaryEntryEditData();

		// Prepare minimal values
		edit.setTable(tableName);
		edit.setName(tableName);
		edit.setWhere(SelectClauseGenerator.DEFAULT_WHERE_CLAUSE);

		// Pre-select first domain
		edit.setDomainUuid(domainDatas.get(0).getUuid());

		// Add metadata to use for edit
		edit.setColumns(getTableDescription(edit.getTable()).getColumns().stream()
				.map(c -> ColumnEditData.fromColumnDescription(c, null, null, null))
				.peek(c -> c.setSelected(true)) // Default : select all
				.sorted()
				.collect(Collectors.toList()));

		return edit;
	}

	/**
	 * Create / update a dictionary entry from editData
	 * 
	 * @param editData
	 */
	public void saveDictionaryEntry(DictionaryEntryEditData editData) {

		this.projectService.assertCurrentUserHasSelectedProject();
		Project project = this.projectService.getCurrentSelectedProjectEntity();

		LOGGER.info("Process saving on dictionary Entry on table {} for project {} (current id : {})",
				editData.getTable(), project.getName(), editData.getUuid());

		DictionaryEntry entry;

		// Update existing
		if (editData.getUuid() != null) {
			entry = this.dictionary.getOne(editData.getUuid());
		}

		// Create new one
		else {
			entry = new DictionaryEntry();
			entry.setUuid(UUID.randomUUID());
			entry.setTableName(editData.getTable());
			entry.setCreatedTime(LocalDateTime.now());
		}

		// Specified keys from columns
		List<ColumnEditData> keys = editData.getColumns().stream().filter(ColumnEditData::isKey).sorted().collect(Collectors.toList());

		// Apply keys, with support for composite keys
		applyEditedKeys(entry, keys, true);

		// Other common edited properties
		entry.setDomain(this.domains.getOne(editData.getDomainUuid()));
		entry.setParameterName(editData.getName());
		entry.setSelectClause("- to update -");
		entry.setWhereClause(editData.getWhere());
		entry.setUpdatedTime(LocalDateTime.now());

		this.dictionary.save(entry);

		// Prepare validated links
		updateLinks(entry, editData.getColumns());

		// Now update select clause using validated tableLinks
		entry.setSelectClause(columnsAsSelectClause(editData.getColumns(),
				this.links.findByDictionaryEntry(entry),
				this.mappings.findByDictionaryEntry(entry),
				this.dictionary.findAllMappedByTableName(project)));

		// And refresh dict Entry
		this.dictionary.save(entry);
	}

	/**
	 * <p>
	 * Use details from a dictionary entry edit data to get corresponding result table.
	 * Content is provided as a list of lines, 1st line (list of string) contains the
	 * identified query headers, other lines are the corresponding content
	 * </p>
	 * <p>
	 * Ignore all links
	 * </p>
	 * 
	 * @param editData
	 */
	public TestQueryData testDictionaryEntryExtract(DictionaryEntryEditData editData) {

		this.projectService.assertCurrentUserHasSelectedProject();
		Project project = this.projectService.getCurrentSelectedProjectEntity();

		LOGGER.debug("Process testing on dictionary Entry on table {} for project {} (current id : {})",
				editData.getTable(), project.getName(), editData.getUuid());

		// Use a temporary entry
		DictionaryEntry tmpEntry = new DictionaryEntry();

		tmpEntry.setTableName(editData.getTable());

		// Specified keys from columns
		List<ColumnEditData> keys = editData.getColumns().stream().filter(ColumnEditData::isKey).sorted().collect(Collectors.toList());

		// Apply keys, with support for composite keys
		applyEditedKeys(tmpEntry, keys, false);

		// Other common edited properties
		tmpEntry.setDomain(this.domains.getOne(editData.getDomainUuid()));
		tmpEntry.setParameterName(editData.getName());
		tmpEntry.setWhereClause(editData.getWhere());

		// Now update select clause using validated tableLinks
		tmpEntry.setSelectClause(columnsAsSelectClause(editData.getColumns(),
				Collections.emptyList(),
				Collections.emptyList(),
				new HashMap<>()));

		return this.indexService.testActualContent(tmpEntry);
	}

	/**
	 * @param name
	 * @return
	 */
	public FunctionalDomainData createNewFunctionalDomain(String name) {

		// Requires project
		this.projectService.assertCurrentUserHasSelectedProject();

		Project project = this.projectService.getCurrentSelectedProjectEntity();

		LOGGER.info("Process add of a new functional domain with name {} into project {}", name, project.getName());

		FunctionalDomain domain = new FunctionalDomain();

		domain.setUuid(UUID.randomUUID());
		domain.setCreatedTime(LocalDateTime.now());
		domain.setUpdatedTime(LocalDateTime.now());
		domain.setName(name);
		domain.setProject(project);

		this.domains.save(domain);

		return FunctionalDomainData.fromEntity(domain);
	}

	/**
	 * 
	 */
	public void refreshCachedMetadata() {

		LOGGER.debug("Forced refresh on cache for metadata");

		this.metadatas.refreshAll();
	}

	/**
	 * @return
	 */
	public ExportImportResult<ExportFile> exportFonctionalDomains(UUID domainUUID) {

		LOGGER.info("Process export of specified fonctional domain {} items", domainUUID);

		FunctionalDomain domain = this.domains.getOne(domainUUID);

		// Packages on limited data sets
		ProjectPackage proj = new ProjectPackage(ProjectExportPackage.PARTIAL_PROJECTS_EXPORT, LocalDateTime.now())
				.initWithContent(Arrays.asList(domain.getProject()));
		DictionaryPackage dict = new DictionaryPackage(DictionaryExportPackage.PARTIAL_DICT_EXPORT, LocalDateTime.now())
				.initWithContent(this.dictionary.findByDomain(domain));
		FunctionalDomainPackage doms = new FunctionalDomainPackage(FunctionalDomainExportPackage.PARTIAL_DOMAINS_EXPORT,
				LocalDateTime.now()).initWithContent(Collections.singletonList(domain));
		TableLinkPackage tl = new TableLinkPackage(TableLinkExportPackage.PARTIAL_LINKS_EXPORT, LocalDateTime.now())
				.initWithContent(this.links.findByDictionaryEntryDomain(domain));
		TableMappingPackage tm = new TableMappingPackage(TableMappingExportPackage.MAPPINGS_EXPORT, LocalDateTime.now())
				.initWithContent(this.mappings.findByDictionaryEntryDomain(domain));

		// Easy : just take all
		ExportFile file = this.ioService.exportPackages(Arrays.asList(proj, dict, doms, tl, tm));

		ExportImportResult<ExportFile> result = new ExportImportResult<>(file);

		result.addCount(ProjectExportPackage.PARTIAL_PROJECTS_EXPORT, proj.getContentSize(), 0, 0);
		result.addCount(DictionaryExportPackage.PARTIAL_DICT_EXPORT, dict.getContentSize(), 0, 0);
		result.addCount(FunctionalDomainExportPackage.PARTIAL_DOMAINS_EXPORT, doms.getContentSize(), 0, 0);
		result.addCount(TableLinkExportPackage.PARTIAL_LINKS_EXPORT, tl.getContentSize(), 0, 0);
		result.addCount(TableMappingExportPackage.MAPPINGS_EXPORT, tm.getContentSize(), 0, 0);

		return result;
	}

	/**
	 * @return
	 */
	public ExportImportResult<ExportFile> exportCurrentProject() {

		this.projectService.assertCurrentUserHasSelectedProject();

		Project project = this.projectService.getCurrentSelectedProjectEntity();

		LOGGER.info("Process export of complete dictionary related entities for current project {}", project.getName());

		ProjectPackage proj = new ProjectPackage(ProjectExportPackage.PARTIAL_PROJECTS_EXPORT, LocalDateTime.now())
				.initWithContent(Arrays.asList(project));

		// Versions for project
		VersionPackage vers = new VersionPackage(VersionExportPackage.PARTIAL_VERSIONS_EXPORT, LocalDateTime.now())
				.initWithContent(this.versions.findByProject(project));

		// Will filter by domains from package
		List<FunctionalDomain> fdoms = this.domains.findByProject(project);

		FunctionalDomainPackage doms = new FunctionalDomainPackage(FunctionalDomainExportPackage.PARTIAL_DOMAINS_EXPORT,
				LocalDateTime.now())
						.initWithContent(fdoms);
		DictionaryPackage dict = new DictionaryPackage(DictionaryExportPackage.PARTIAL_DICT_EXPORT, LocalDateTime.now())
				.initWithContent(this.dictionary.findByDomainIn(fdoms));
		TableLinkPackage tl = new TableLinkPackage(TableLinkExportPackage.PARTIAL_LINKS_EXPORT, LocalDateTime.now())
				.initWithContent(this.links.findByDictionaryEntryDomainIn(fdoms));
		TableMappingPackage tm = new TableMappingPackage(TableMappingExportPackage.MAPPINGS_EXPORT, LocalDateTime.now())
				.initWithContent(this.mappings.findByDictionaryEntryDomainIn(fdoms));

		// Easy : just take all
		ExportFile file = this.ioService.exportPackages(Arrays.asList(proj, vers, dict, doms, tl, tm));

		ExportImportResult<ExportFile> result = new ExportImportResult<>(file);

		result.addCount(ProjectExportPackage.PARTIAL_PROJECTS_EXPORT, proj.getContentSize(), 0, 0);
		result.addCount(DictionaryExportPackage.PARTIAL_DICT_EXPORT, dict.getContentSize(), 0, 0);
		result.addCount(FunctionalDomainExportPackage.PARTIAL_DOMAINS_EXPORT, doms.getContentSize(), 0, 0);
		result.addCount(TableLinkExportPackage.PARTIAL_LINKS_EXPORT, tl.getContentSize(), 0, 0);
		result.addCount(TableMappingExportPackage.MAPPINGS_EXPORT, tm.getContentSize(), 0, 0);
		result.addCount(VersionExportPackage.VERSIONS_EXPORT, vers.getContentSize(), 0, 0);

		return result;
	}

	/**
	 * @return
	 */
	public ExportImportResult<ExportFile> exportAll() {

		LOGGER.info("Process export of complete dictionary related entities");

		ProjectPackage proj = new ProjectPackage(ProjectExportPackage.PROJECTS_EXPORT, LocalDateTime.now())
				.initWithContent(this.projects.findAll());
		VersionPackage vers = new VersionPackage(VersionExportPackage.VERSIONS_EXPORT, LocalDateTime.now())
				.initWithContent(this.versions.findAll());
		DictionaryPackage dict = new DictionaryPackage(DictionaryExportPackage.DICT_EXPORT, LocalDateTime.now())
				.initWithContent(this.dictionary.findAll());
		FunctionalDomainPackage doms = new FunctionalDomainPackage(FunctionalDomainExportPackage.DOMAINS_EXPORT, LocalDateTime.now())
				.initWithContent(this.domains.findAll());
		TableLinkPackage tl = new TableLinkPackage(TableLinkExportPackage.LINKS_EXPORT, LocalDateTime.now())
				.initWithContent(this.links.findAll());
		TableMappingPackage tm = new TableMappingPackage(TableMappingExportPackage.MAPPINGS_EXPORT, LocalDateTime.now())
				.initWithContent(this.mappings.findAll());

		// Easy : just take all
		ExportFile file = this.ioService.exportPackages(Arrays.asList(proj, vers, dict, doms, tl, tm));

		ExportImportResult<ExportFile> result = new ExportImportResult<>(file);

		result.addCount(ProjectExportPackage.PROJECTS_EXPORT, proj.getContentSize(), 0, 0);
		result.addCount(DictionaryExportPackage.DICT_EXPORT, dict.getContentSize(), 0, 0);
		result.addCount(FunctionalDomainExportPackage.DOMAINS_EXPORT, doms.getContentSize(), 0, 0);
		result.addCount(TableLinkExportPackage.LINKS_EXPORT, tl.getContentSize(), 0, 0);
		result.addCount(TableMappingExportPackage.MAPPINGS_EXPORT, tm.getContentSize(), 0, 0);
		result.addCount(VersionExportPackage.VERSIONS_EXPORT, vers.getContentSize(), 0, 0);

		return result;
	}

	/**
	 * @param file
	 */
	public ExportImportResult<Void> importAll(ExportFile file) {

		LOGGER.info("Process import of complete dictionary related entities");

		// Less easy : need to complete and identify if value is new or not
		List<SharedPackage<?>> packages = this.ioService.importPackages(file);
		AtomicInteger newProjsCount = new AtomicInteger(0);
		AtomicInteger newVersCount = new AtomicInteger(0);
		AtomicInteger newDomainsCount = new AtomicInteger(0);
		AtomicInteger newDictCount = new AtomicInteger(0);
		AtomicInteger newLinksCount = new AtomicInteger(0);
		AtomicInteger newMappingsCount = new AtomicInteger(0);

		// Can substitute project by name : need refer update
		Map<UUID, Project> substituteProjects = new HashMap<>();

		// Process on each, with right order :

		// #1st The projects (used by other)
		List<Project> importedProjects = packages.stream()
				.filter(p -> p.getClass() == ProjectPackage.class)
				.flatMap(p -> ((ProjectPackage) p).streamContent())
				.map(d -> this.projectService.importProject(d, newProjsCount, substituteProjects))
				.collect(Collectors.toList());

		// #2nd The functional domains (used by other)
		List<FunctionalDomain> importedDomains = packages.stream()
				.filter(p -> p.getClass() == FunctionalDomainPackage.class)
				.flatMap(p -> ((FunctionalDomainPackage) p).streamContent())
				.map(d -> importDomain(d, newDomainsCount, substituteProjects))
				.collect(Collectors.toList());

		// #3rd The dictionary (referencing domains)
		List<DictionaryEntry> importedDicts = packages.stream()
				.filter(p -> p.getClass() == DictionaryPackage.class)
				.flatMap(p -> ((DictionaryPackage) p).streamContent())
				.map(d -> importDictionaryEntry(d, newDictCount))
				.collect(Collectors.toList());

		// #4th The links (referencing dictionary entries)
		List<TableLink> importedLinks = packages.stream()
				.filter(p -> p.getClass() == TableLinkPackage.class)
				.flatMap(p -> ((TableLinkPackage) p).streamContent())
				.map(d -> importTableLink(d, newLinksCount))
				.collect(Collectors.toList());

		// #5th The mappings (referencing dictionary entries)
		List<TableMapping> importedMappings = packages.stream()
				.filter(p -> p.getClass() == TableMappingPackage.class)
				.flatMap(p -> ((TableMappingPackage) p).streamContent())
				.map(d -> importTableMapping(d, newMappingsCount))
				.collect(Collectors.toList());

		// #6th The projects (referencing projects)
		List<Version> importedVersions = packages.stream()
				.filter(p -> p.getClass() == VersionPackage.class)
				.flatMap(p -> ((VersionPackage) p).streamContent())
				.map(d -> importVersion(d, newVersCount, substituteProjects))
				.sorted(Comparator.comparing(Version::getUpdatedTime))
				.collect(Collectors.toList());

		// Check all versions
		assertVersionModelsAreValid(importedVersions);

		// Batched save on all imported
		this.projects.saveAll(importedProjects);
		this.domains.saveAll(importedDomains);
		this.dictionary.saveAll(importedDicts);
		this.links.saveAll(importedLinks);
		this.mappings.saveAll(importedMappings);
		this.versions.saveAll(importedVersions);

		// Add also all imported projects to current User prefered list
		this.projectService.setPreferedProjectsForCurrentUser(importedProjects.stream()
				.map(Shared::getUuid)
				.collect(Collectors.toList()));

		LOGGER.info("Import completed of {} projects, {} domains, {} dictionary entry, {} table links and {} table mappings",
				Integer.valueOf(importedProjects.size()), Integer.valueOf(importedDomains.size()), Integer.valueOf(importedDicts.size()),
				Integer.valueOf(importedLinks.size()), Integer.valueOf(importedMappings.size()));

		ExportImportResult<Void> result = ExportImportResult.newVoid();

		// Details on imported counts (add vs updated items)
		if (importedProjects.size() > 0) {
			result.addCount(ProjectExportPackage.PROJECTS_EXPORT, newProjsCount.get(),
					importedProjects.size() - newProjsCount.get(), 0);
		}

		if (importedDomains.size() > 0) {
			result.addCount(FunctionalDomainExportPackage.DOMAINS_EXPORT, newDomainsCount.get(),
					importedDomains.size() - newDomainsCount.get(), 0);
		}

		if (importedDicts.size() > 0) {
			result.addCount(DictionaryExportPackage.DICT_EXPORT, newDictCount.get(),
					importedDicts.size() - newDictCount.get(), 0);
		}

		if (importedLinks.size() > 0) {
			result.addCount(TableLinkExportPackage.LINKS_EXPORT, newLinksCount.get(),
					importedLinks.size() - newLinksCount.get(), 0);
		}

		if (importedMappings.size() > 0) {
			result.addCount(TableMappingExportPackage.MAPPINGS_EXPORT, newMappingsCount.get(),
					importedMappings.size() - newMappingsCount.get(), 0);
		}

		if (importedVersions.size() > 0) {
			result.addCount(VersionExportPackage.VERSIONS_EXPORT, newVersCount.get(),
					importedVersions.size() - newVersCount.get(), 0);
		}

		return result;
	}

	/**
	 * <p>
	 * For a given version entity, get completed data, regarding "last version" of current
	 * project for some rules
	 * </p>
	 * 
	 * @param version
	 * @param lastProjectVersion
	 * @return <code>version</code> populated as a <tt>VersionData</tt>
	 */
	private VersionData getCompletedVersion(Version version, Version lastProjectVersion) {

		boolean isLastVersion = version.getUuid().equals(lastProjectVersion.getUuid());
		return VersionData.fromEntity(version, isLastVersion ? this.versions.isVersionUpdatable(lastProjectVersion.getUuid()) : false);
	}

	/**
	 * <p>
	 * Using a flat switch with fall-through, process key validation and specification in
	 * provided <tt>DictionaryEntry</tt> from all the selected keys. Support composite
	 * keys (max 5 key columns)
	 * </p>
	 * <p>
	 * Do not run validations if <code>validate</code> is false (validation is mandatory
	 * for dictionary entry edit, but can be ignored when running a simple data extract
	 * test
	 * </p>
	 * 
	 * @param entry
	 * @param keys
	 * @param validate
	 */
	private void applyEditedKeys(DictionaryEntry entry, List<ColumnEditData> keys, boolean validate) {

		// Assert key count (1-5)
		assertKeysSelection(keys);

		// Always use first as "normal" key, then ext for others.
		ColumnEditData first = keys.get(0);

		if (validate) {
			warnKeyIsPk(entry, first);
		}

		entry.setKeyName(first.getName());
		entry.setKeyType(first.getType());
		switch (keys.size()) {
		case 5:
			ColumnEditData key4 = keys.get(4);
			if (validate) {
				warnKeyIsPk(entry, key4);
			}
			entry.setExt4KeyName(key4.getName());
			entry.setExt4KeyType(key4.getType());
			//$FALL-THROUGH$
		case 4:
			ColumnEditData key3 = keys.get(3);
			if (validate) {
				warnKeyIsPk(entry, key3);
			}
			entry.setExt3KeyName(key3.getName());
			entry.setExt3KeyType(key3.getType());
			//$FALL-THROUGH$
		case 3:
			ColumnEditData key2 = keys.get(2);
			if (validate) {
				warnKeyIsPk(entry, key2);
			}
			entry.setExt2KeyName(key2.getName());
			entry.setExt2KeyType(key2.getType());
			//$FALL-THROUGH$
		case 2:
			ColumnEditData key1 = keys.get(1);
			if (validate) {
				warnKeyIsPk(entry, key1);
			}
			entry.setExt1KeyName(key1.getName());
			entry.setExt1KeyType(key1.getType());
			//$FALL-THROUGH$
		default:
			break;
		}

		// Controle also that the key is unique (heavy load)
		if (validate) {
			assertKeyIsUniqueValue(entry);
		}
	}

	/**
	 * <p>
	 * Process one FunctionalDomain. If the associated project was substituted by another
	 * by name, the substitute will be gathered from given map and applied to domain
	 * </p>
	 * 
	 * @param imported
	 * @param newCounts
	 * @param substituteProjects
	 * @return
	 */
	private FunctionalDomain importDomain(FunctionalDomain imported, AtomicInteger newCounts, Map<UUID, Project> substituteProjects) {

		Optional<FunctionalDomain> localOpt = this.domains.findById(imported.getUuid());

		// Exists already
		localOpt.ifPresent(d -> LOGGER.debug("Import existing domain {} : will update currently owned", imported.getUuid()));

		// Or is a new one
		FunctionalDomain local = localOpt.orElseGet(() -> {
			LOGGER.debug("Import new domain {} : will create currently owned", imported.getUuid());
			FunctionalDomain loc = new FunctionalDomain(imported.getUuid());
			loc.setCreatedTime(imported.getCreatedTime());
			newCounts.incrementAndGet();
			return loc;
		});

		// Common attrs
		local.setUpdatedTime(imported.getUpdatedTime());
		local.setName(imported.getName());

		// Use substitute
		if (substituteProjects.containsKey(imported.getProject().getUuid())) {
			Project substitute = substituteProjects.get(imported.getProject().getUuid());
			LOGGER.info("Imported project {} is used as substitute for domain {} instead of initial project {}", substitute.getUuid(),
					imported.getUuid(), imported.getProject().getUuid());
			local.setProject(substitute);
		}

		// Keep referenced
		else {
			local.setProject(imported.getProject());
		}

		local.setImportedTime(LocalDateTime.now());

		return local;
	}

	/**
	 * <p>
	 * Process one Project
	 * </p>
	 * <p>
	 * Project can be identified by uuid or by name during import
	 * <p>
	 * 
	 * @param imported
	 * @param newCounts
	 * @param substituteProjects
	 * @return
	 */
	private Version importVersion(Version imported, AtomicInteger newCounts, Map<UUID, Project> substituteProjects) {

		Optional<Version> localOpt = this.versions.findById(imported.getUuid());

		// Exists already
		localOpt.ifPresent(d -> LOGGER.debug("Import existing project by uuid {} : will update currently owned", imported.getUuid()));

		// Will try also by name
		Version byName = this.versions.findByNameAndProject(imported.getName(), imported.getProject());

		// Search on existing Or is a new one
		Version local = localOpt.orElseGet(() -> {
			Version loc;
			if (byName == null) {
				LOGGER.debug("Import new version {} : will create currently owned", imported.getUuid());
				loc = new Version(imported.getUuid());
				loc.setCreatedTime(imported.getCreatedTime());
				newCounts.incrementAndGet();
			} else {
				LOGGER.debug("Import exsting version by name \"{}\"", imported.getName());
				loc = byName;
			}
			return loc;
		});

		// Use substitute
		if (substituteProjects.containsKey(imported.getProject().getUuid())) {
			Project substitute = substituteProjects.get(imported.getProject().getUuid());
			LOGGER.info("Imported project {} is used as substitute for version {} instead of initial project {}", substitute.getUuid(),
					imported.getUuid(), imported.getProject().getUuid());
			local.setProject(substitute);
		}

		// Keep referenced
		else {
			local.setProject(imported.getProject());
		}

		// Common attrs
		local.setUpdatedTime(imported.getUpdatedTime());
		local.setName(imported.getName());
		local.setModelIdentity(imported.getModelIdentity());

		local.setImportedTime(LocalDateTime.now());

		return local;
	}

	/**
	 * Process one DictionaryEntry
	 * 
	 * @param imported
	 * @return
	 */
	private DictionaryEntry importDictionaryEntry(DictionaryEntry imported, AtomicInteger newCounts) {

		Optional<DictionaryEntry> localOpt = this.dictionary.findById(imported.getUuid());

		// Exists already
		localOpt.ifPresent(d -> LOGGER.debug("Import existing dictionary entry {} : will update currently owned", imported.getUuid()));

		// Or is a new one
		DictionaryEntry local = localOpt.orElseGet(() -> {
			LOGGER.debug("Import new dictionary entry {} : will create currently owned", imported.getUuid());
			DictionaryEntry loc = new DictionaryEntry(imported.getUuid());
			loc.setCreatedTime(imported.getCreatedTime());
			newCounts.incrementAndGet();
			return loc;
		});

		// Common attrs
		local.setUpdatedTime(imported.getUpdatedTime());
		local.setDomain(new FunctionalDomain(imported.getDomain().getUuid()));
		local.setKeyName(imported.getKeyName());
		local.setKeyType(imported.getKeyType());
		local.setParameterName(imported.getParameterName());
		local.setTableName(imported.getTableName());
		local.setSelectClause(imported.getSelectClause());
		local.setWhereClause(imported.getWhereClause());

		local.setImportedTime(LocalDateTime.now());

		return local;
	}

	/**
	 * Process one TableLink
	 * 
	 * @param imported
	 * @return
	 */
	private TableLink importTableLink(TableLink imported, AtomicInteger newCounts) {

		Optional<TableLink> localOpt = this.links.findById(imported.getUuid());

		// Exists already
		localOpt.ifPresent(d -> LOGGER.debug("Import existing TableLink {} : will update currently owned", imported.getUuid()));

		// Or is a new one
		TableLink local = localOpt.orElseGet(() -> {
			LOGGER.debug("Import new TableLink {} : will create currently owned", imported.getUuid());
			TableLink loc = new TableLink(imported.getUuid());
			loc.setCreatedTime(imported.getCreatedTime());
			newCounts.incrementAndGet();
			return loc;
		});

		// Common attrs
		local.setUpdatedTime(imported.getUpdatedTime());
		local.setDictionaryEntry(new DictionaryEntry(imported.getDictionaryEntry().getUuid()));
		local.setColumnFrom(imported.getColumnFrom());
		local.setColumnTo(imported.getColumnTo());
		local.setTableTo(imported.getTableTo());

		local.setImportedTime(LocalDateTime.now());

		return local;
	}

	/**
	 * Process one TableMapping
	 * 
	 * @param imported
	 * @return
	 */
	private TableMapping importTableMapping(TableMapping imported, AtomicInteger newCounts) {

		Optional<TableMapping> localOpt = this.mappings.findById(imported.getUuid());

		// Exists already
		localOpt.ifPresent(d -> LOGGER.debug("Import existing TableMapping {} : will update currently owned", imported.getUuid()));

		// Or is a new one
		TableMapping local = localOpt.orElseGet(() -> {
			LOGGER.debug("Import new TableLink {} : will create currently owned", imported.getUuid());
			TableMapping loc = new TableMapping(imported.getUuid());
			loc.setCreatedTime(imported.getCreatedTime());
			newCounts.incrementAndGet();
			return loc;
		});

		// Common attrs
		local.setUpdatedTime(imported.getUpdatedTime());
		local.setDictionaryEntry(new DictionaryEntry(imported.getDictionaryEntry().getUuid()));
		local.setName(imported.getName());
		local.setColumnFrom(imported.getColumnFrom());
		local.setColumnTo(imported.getColumnTo());
		local.setTableTo(imported.getTableTo());
		local.setMapTable(imported.getMapTable());
		local.setMapTableColumnFrom(imported.getMapTableColumnFrom());
		local.setMapTableColumnTo(imported.getMapTableColumnTo());

		local.setImportedTime(LocalDateTime.now());

		return local;
	}

	/**
	 * Update / Create / delete tablelink regarding specified FK in columnEditDatas
	 * 
	 * @param entry
	 * @param cols
	 */
	private void updateLinks(DictionaryEntry entry, List<ColumnEditData> cols) {

		// For final save
		Collection<TableLink> updatedLinks = new ArrayList<>();
		Collection<TableLink> createdLinks = new ArrayList<>();

		Map<String, TableLink> editedLinksByTableTo = new HashMap<>();

		// Produces links from col foreign key
		for (ColumnEditData col : cols) {

			// When FK is set
			if (isNotEmpty(col.getForeignKeyTable())) {

				// Mix on same table to for composites
				TableLink link = editedLinksByTableTo.get(col.getForeignKeyTable());

				// Not set yet (common case - single key)
				if (link == null) {
					link = new TableLink();
					link.setColumnFrom(col.getName());
					link.setTableTo(col.getForeignKeyTable());
					link.setColumnTo(col.getForeignKeyColumn());
					link.setDictionaryEntry(entry);
					editedLinksByTableTo.put(col.getForeignKeyTable(), link);
				}

				// Already set - composite key - rare
				else {
					int next = (int) link.columnFroms().count();
					link.setColumnFrom(next, col.getName());
					link.setColumnTo(next, col.getForeignKeyColumn());
				}
			}
		}

		// Get existing to update / remove
		Map<String, TableLink> existingLinksByTableTo = this.links.findByDictionaryEntry(entry).stream().distinct()
				.collect(Collectors.toMap(TableLink::getTableTo, l -> l));

		int compositeCount = 0;

		// And prepare 3 sets of links
		for (TableLink link : editedLinksByTableTo.values()) {

			TableLink existing = existingLinksByTableTo.remove(link.getTableTo());

			// Update
			if (existing != null) {
				existing.setUpdatedTime(LocalDateTime.now());
				existing.setTableTo(link.getTableTo());
				existing.setColumnTo(link.getColumnTo());
				existing.setColumnFrom(link.getColumnFrom());

				// Copy other if composite
				if (link.isCompositeKey() || existing.isCompositeKey()) {
					existing.setExt1ColumnFrom(link.getExt1ColumnFrom());
					existing.setExt2ColumnFrom(link.getExt2ColumnFrom());
					existing.setExt3ColumnFrom(link.getExt3ColumnFrom());
					existing.setExt4ColumnFrom(link.getExt4ColumnFrom());
					existing.setExt1ColumnTo(link.getExt1ColumnTo());
					existing.setExt2ColumnTo(link.getExt2ColumnTo());
					existing.setExt3ColumnTo(link.getExt3ColumnTo());
					existing.setExt4ColumnTo(link.getExt4ColumnTo());
					compositeCount++;
				}

				updatedLinks.add(existing);
			}

			// New one
			else {
				link.setCreatedTime(LocalDateTime.now());
				link.setUpdatedTime(LocalDateTime.now());
				link.setDictionaryEntry(entry);
				link.setUuid(UUID.randomUUID());
				createdLinks.add(link);
			}
		}

		// Remaining are deleted
		Collection<TableLink> deletedLinks = existingLinksByTableTo.values();

		LOGGER.info("Links updated for dictionary Entry {}. {} links added, {} links updated and {} deleted, including "
				+ "{} links with composite keys", entry.getUuid(), Integer.valueOf(createdLinks.size()),
				Integer.valueOf(updatedLinks.size()), Integer.valueOf(deletedLinks.size()), Integer.valueOf(compositeCount));

		// Process DB updates
		if (createdLinks.size() > 0)
			this.links.saveAll(createdLinks);
		if (updatedLinks.size() > 0)
			this.links.saveAll(updatedLinks);
		if (deletedLinks.size() > 0)
			this.links.deleteAll(deletedLinks);
	}

	/**
	 * @param tableName
	 * @return
	 */
	private TableDescription getTableDescription(String tableName) {

		return this.metadatas.getTables().stream()
				.filter(t -> t.getName().equals(tableName))
				.findFirst()
				.orElseGet(() -> TableDescription.MISSING);
	}

	/**
	 * @param uuid
	 */
	private void assertDictionaryEntryCanBeRemoved(UUID uuid) {

		if (this.dictionary.findUsedIds().contains(uuid)) {
			throw new ApplicationException(DIC_NOT_REMOVABLE,
					"Dictionary with UUID " + uuid + " is used in index and therefore cannot be deleted");
		}
	}

	/**
	 * @param uuid
	 */
	private void assertDomainCanBeRemoved(UUID uuid) {

		if (this.domains.findUsedIds().contains(uuid)) {
			throw new ApplicationException(DOMAIN_NOT_REMOVABLE,
					"FunctionalDomain with UUID " + uuid + " is used in index and therefore cannot be deleted");
		}
	}

	/**
	 * Using values verif, control that key is unique
	 * 
	 * @param dict
	 */
	private void assertKeyIsUniqueValue(DictionaryEntry dict) {

		// Unique key check
		if (dict.getExt1KeyName() == null) {
			if (!this.metadatas.isColumnSetHasUniqueValue(dict.getTableName(), Arrays.asList(dict.getKeyName()))) {
				throw new ApplicationException(DIC_KEY_NOT_UNIQ, "Cannot edit dictionary entry for table " + dict.getTableName() +
						" with unique key " + dict.getKeyName() + " has not unique values",
						dict.getTableName() + "." + dict.getKeyName());
			}
		}

		// Check on composite key
		else {
			Collection<String> keys = dict.keyNames().collect(Collectors.toSet());
			if (!this.metadatas.isColumnSetHasUniqueValue(dict.getTableName(), keys)) {
				throw new ApplicationException(DIC_KEY_NOT_UNIQ, "Cannot edit dictionary entry for table " + dict.getTableName() +
						" with composite key on columns " + keys + " has not unique values",
						keys.stream().map(s -> (dict.getTableName() + "." + s)).collect(Collectors.joining(" / ")));
			}
		}
	}

	/**
	 * @param columns
	 * @return
	 */
	private String columnsAsSelectClause(List<ColumnEditData> columns, List<TableLink> tabLinks, List<TableMapping> tabMappings,
			Map<String, DictionaryEntry> allEntries) {
		return this.queryGenerator.mergeSelectClause(
				columns.stream().filter(ColumnEditData::isSelected).map(ColumnEditData::getName).collect(Collectors.toList()),
				columns.size(),
				tabLinks,
				tabMappings,
				allEntries);
	}

	/**
	 * Basic existance check
	 * 
	 * @param uuid
	 */
	private void assertDictionaryEntryExists(UUID uuid) {
		if (!this.dictionary.existsById(uuid)) {
			throw new ApplicationException(DIC_ENTRY_NOT_FOUND, "Dictionary entry doesn't exist for uuid " + uuid);
		}
	}

	/**
	 * <p>
	 * Control from model Id
	 * </p>
	 * 
	 * @param importedVersions
	 */
	private void assertVersionModelsAreValid(List<Version> importedVersions) {

		if (this.modelDescs.hasToCheckDescriptions()) {

			for (int i = 0; i < importedVersions.size(); i++) {

				Version version = importedVersions.get(i);
				boolean last = (i == (importedVersions.size() - 1));

				if (version.getModelIdentity() == null) {
					LOGGER.warn("[VERSION-ID] Uncheckable model id for version \"{}\"", version.getName());
				} else {

					IdentifierType type = this.modelDescs.getModelIdentifierType(version.getModelIdentity());

					// Last must not be on current model
					if (last) {
						if (type != IdentifierType.CURRENT) {
							throw new ApplicationException(VERSION_NOT_MODEL_ID, "Model id " + version.getModelIdentity()
									+ " is not of the required type in version \"" + version.getName() + "\"", version.getModelIdentity());
						}
					}

					// // If not yet last
					// else {
					//
					// String nextModelIdentity = importedVersions.get(i +
					// 1).getModelIdentity();
					//
					// // If it's not a reuse of current model identity on new version, a
					// // version is then missing
					// if (type != IdentifierType.OLD_ONE && version.getModelIdentity() !=
					// null
					// && !version.getModelIdentity().equals(nextModelIdentity)) {
					// throw new ApplicationException(VERSION_NOT_MODEL_ID, "Model id " +
					// version.getModelIdentity()
					// + " is not of the required intermediate type in version \"" +
					// version.getName() + "\"",
					// version.getModelIdentity());
					// }
					// }

				}
			}
		}
	}

	/**
	 * <p>
	 * Check rules for column key selection : minimum 1, maximum 5 (for composite keys)
	 * </p>
	 * 
	 * @param keys
	 */
	private static void assertKeysSelection(List<ColumnEditData> keys) {

		if (keys == null || keys.size() == 0) {
			throw new ApplicationException(DIC_NO_KEY, "The key is mandatory");
		}

		if (keys.size() > 5) {
			throw new ApplicationException(DIC_TOO_MANY_KEYS, "Too much selected columns for composite key definition");
		}
	}

	/**
	 * @param entry
	 * @param key
	 */
	private static void warnKeyIsPk(DictionaryEntry entry, ColumnEditData key) {

		// Shouldn't use PK as parameter key
		if (key.getType().isPk()) {
			LOGGER.warn("Using the PK \"{}\" as parameter key on table \"{}\" : it may cause wrong conflict if"
					+ " the id is not a real valid business identifier for the parameter table !!!", key.getName(), entry.getTableName());
		}
	}
}
