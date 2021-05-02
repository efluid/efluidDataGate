package fr.uem.efluid.services;

import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.metas.ColumnDescription;
import fr.uem.efluid.model.metas.TableDescription;
import fr.uem.efluid.model.repositories.*;
import fr.uem.efluid.model.repositories.ManagedModelDescriptionRepository.IdentifierType;
import fr.uem.efluid.services.types.*;
import fr.uem.efluid.services.types.DictionaryEntryEditData.ColumnEditData;
import fr.uem.efluid.tools.ManagedQueriesGenerator;
import fr.uem.efluid.tools.VersionContentChangesGenerator;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.SelectClauseGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.uem.efluid.services.types.DictionaryExportPackage.DICT_EXPORT;
import static fr.uem.efluid.services.types.DictionaryExportPackage.PARTIAL_DICT_EXPORT;
import static fr.uem.efluid.services.types.FunctionalDomainExportPackage.DOMAINS_EXPORT;
import static fr.uem.efluid.services.types.FunctionalDomainExportPackage.PARTIAL_DOMAINS_EXPORT;
import static fr.uem.efluid.services.types.ProjectExportPackage.PARTIAL_PROJECTS_EXPORT;
import static fr.uem.efluid.services.types.ProjectExportPackage.PROJECTS_EXPORT;
import static fr.uem.efluid.services.types.TableLinkExportPackage.LINKS_EXPORT;
import static fr.uem.efluid.services.types.TableLinkExportPackage.PARTIAL_LINKS_EXPORT;
import static fr.uem.efluid.services.types.TableMappingExportPackage.MAPPINGS_EXPORT;
import static fr.uem.efluid.services.types.VersionExportPackage.PARTIAL_VERSIONS_EXPORT;
import static fr.uem.efluid.services.types.VersionExportPackage.VERSIONS_EXPORT;
import static fr.uem.efluid.utils.ErrorType.*;
import static java.time.LocalDateTime.now;

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
 * @version 8
 * @since v0.0.1
 */
@Service
@Transactional
public class DictionaryManagementService extends AbstractApplicationService {

    private static final VersionData NOT_SET_VERSION = new VersionData(null, NOT_SET_VERSION_NAME, "", null, null, true, false, false);

    private static final String DEDUPLICATED_DOMAINS = "deduplicated-domains";

    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryManagementService.class);

    @Autowired
    private FunctionalDomainRepository domains;

    @Autowired
    private DictionaryRepository dictionary;

    @Autowired
    private VersionRepository versions;

    @Autowired
    private CommitRepository commits;

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

    @Autowired
    private FeatureManager features;

    @Autowired
    private VersionContentChangesGenerator changesGenerator;

    /**
     * @param name
     * @return true if new version created, or false for update
     */
    public boolean setCurrentVersion(final String name) {

        boolean created = false;
        this.projectService.assertCurrentUserHasSelectedProject();
        Project project = this.projectService.getCurrentSelectedProjectEntity();

        // Behavior based on version feature
        final boolean useModelId = this.features.isEnabled(Feature.USE_MODEL_ID_AS_VERSION_NAME);

        // Search ref identifier if enabled
        String modelId = this.modelDescs.getCurrentModelIdentifier() != null ? this.modelDescs.getCurrentModelIdentifier() : name;

        // Must check if model id is compliant
        assertCanCreateVersionFromModelId(useModelId, modelId);

        final String fixedName = useModelId ? modelId : name;

        // Search by name
        Version version = this.versions.findByNameAndProject(fixedName, project);

        // Create
        if (version == null) {
            LOGGER.info("Create version {} in current project", fixedName);
            version = new Version();
            version.setUuid(UUID.randomUUID());
            version.setName(fixedName);
            version.setCreatedTime(now());
            version.setProject(project);
            created = true;
        } else {
            LOGGER.info("Update version {} in current project", fixedName);
        }

        // Always init
        version.setUpdatedTime(now());

        // Never erase existing. Can create or update only
        if (modelId != null) {
            version.setModelIdentity(modelId);
        }

        // And finally, extract the content for validity
        completeVersionContents(version);

        this.versions.save(version);

        return created;
    }

    /**
     * @return
     */
    public VersionData getLastVersion() {

        Version last = getLastUpdatedVersion();

        if (last != null) {

            // Must have no commit for version
            return VersionData.fromEntity(last, true, this.commits.countCommitsForVersion(last.getUuid()) == 0);
        }

        return NOT_SET_VERSION;
    }

    /**
     * @return
     */
    public void deleteVersionById(UUID uuid) {
        this.versions.deleteById(uuid);
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
    public boolean isVersionCanCreate() {

        this.projectService.assertCurrentUserHasSelectedProject();
        Project project = this.projectService.getCurrentSelectedProjectEntity();

        Version last = this.versions.getLastVersionForProject(project);

        // Can always init
        if (last == null) {
            return true;
        }

        // If not based on model Id, can always create
        if (!this.features.isEnabled(Feature.USE_MODEL_ID_AS_VERSION_NAME)) {
            return true;
        }

        // Check not only for last but for all existing versions (to avoid duplicates)
        Version existing = this.versions.findByNameAndProject(this.modelDescs.getCurrentModelIdentifier(), project);

        // Cannot create new if model id is not updated from any existing version
        return existing == null;
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
                .sorted(Comparator.comparing(VersionData::getUpdatedTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * <p>
     * Function should check if a commit has <strong>VERSION_UUID = The version to delete</strong>
     * if it is true the user cannot delete the version otherwise he can delete it.
     * </p>
     *
     * @param versionId
     * @return
     * @Author Prescise
     */

    public Boolean isVersionLinkedToLot(UUID versionId) {

        Boolean versionIsLinkedToLot;

        //get current project
        this.projectService.assertCurrentUserHasSelectedProject();
        Project project = this.projectService.getCurrentSelectedProjectEntity();

        //get list commits by project
        List<Commit> commits = this.commits.findByProject(project);

        //check if commit has version_uuid equal to current version
        versionIsLinkedToLot = commits.stream().anyMatch(c -> c.getVersion().getUuid().equals(versionId));

        return versionIsLinkedToLot;
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
            LOGGER.debug("Remove {} associated links from dictionary entry {}", dicLinks.size(), uuid);
            this.links.deleteAll(dicLinks);
        }

        this.dictionary.delete(new DictionaryEntry(uuid));
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
        Map<String, List<String>> allTables = this.metadatas.getTables().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(TableDescription::getName,
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
     * Process a full compare between a selected version and the current last version for a dictionnary update following
     *
     * @param toCompareName identified version to compare, "left"
     * @return compare result
     */
    public VersionCompare compareVersionWithLast(String toCompareName) {

        Version last = getLastUpdatedVersion();

        if (last == null) {
            throw new ApplicationException(VERSION_NOT_EXIST, "No version specified yet for application");
        }

        return compareVersions(toCompareName, last.getName());
    }

    /**
     * Process a full compare between 2 selected versions for a dictionnary update following
     *
     * @param oneName identified version to compare, "left"
     * @param twoName identified version to compare, "right"
     * @return compare result
     */
    public VersionCompare compareVersions(String oneName, String twoName) {

        // Requires project
        this.projectService.assertCurrentUserHasSelectedProject();
        Project project = this.projectService.getCurrentSelectedProjectEntity();

        Version one = this.versions.findByNameAndProject(oneName, project);
        Version two = this.versions.findByNameAndProject(twoName, project);

        if (one != null && two != null) {
            return new VersionCompare(
                    VersionData.fromEntity(one, false, false),
                    VersionData.fromEntity(two, true, false),
                    this.changesGenerator.generateChanges(one, two));
        } else {
            throw new ApplicationException(VERSION_NOT_EXIST, "Selected version(s) doesn't exist (" + oneName + " - " + twoName + ")");
        }
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
        Map<String, DictionaryEntry> allDicts = this.dictionary.findAllByProjectMappedToTableName(project);

        return this.dictionary.findByDomainProject(project).stream()
                .map(e -> DictionaryEntrySummary.fromEntity(e,
                        this.queryGenerator.producesSelectParameterQuery(
                                e,
                                this.links.findByDictionaryEntry(e),
                                allDicts)))
                .peek(d -> d.setCanDelete(!usedIds.contains(d.getUuid())))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Apply a single select clause to all dictionary tables for the current project
     * Overwrite the existing clause
     *
     * @param clause a new select clause to apply
     * @return tables for which the apply failed
     */
    public List<DictionaryEntrySummary> checkAndApplySelectClauseForAllDictionary(String clause) {

        this.projectService.assertCurrentUserHasSelectedProject();
        Project project = this.projectService.getCurrentSelectedProjectEntity();

        Collection<DictionaryEntry> tables = this.dictionary.findAllByProjectMappedToUuid(project).values();

        List<DictionaryEntrySummary> failed = new ArrayList<>();

        tables.forEach(t -> {
            if (this.metadatas.isFilterCanApply(t.getTableName(), clause)) {
                t.setWhereClause(clause);
                t.setUpdatedTime(now());
            } else {
                failed.add(DictionaryEntrySummary.fromEntity(t, ""));
            }
        });

        this.dictionary.saveAll(tables);

        return failed;
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
        Map<String, DictionaryEntry> allDicts = this.dictionary.findAllByProjectMappedToTableName(project);

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

        final boolean isSelectPkAsKeys = this.features.isEnabled(Feature.SELECT_PK_AS_DEFAULT_DICT_ENTRY_KEY);

        // Add metadata to use for edit
        edit.setColumns(getTableDescription(edit.getTable()).getColumns().stream()
                .map(c -> ColumnEditData.fromColumnDescription(c, null, null, null))
                .peek(c -> {
                    // If identifier, select it as key
                    if (isSelectPkAsKeys && c.getType().isPk()) {
                        c.setKey(true);
                    }

                    // Default : select all
                    else {
                        c.setSelected(true);
                    }
                })
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
            entry.setCreatedTime(now());
        }

        // Specified keys from columns
        List<ColumnEditData> keys = editData.getColumns().stream().filter(ColumnEditData::isKey).sorted().collect(Collectors.toList());

        // Other common edited properties
        entry.setDomain(this.domains.getOne(editData.getDomainUuid()));
        entry.setParameterName(editData.getName());
        entry.setSelectClause("- to update -");
        entry.setWhereClause(editData.getWhere());
        entry.setUpdatedTime(now());

        // Apply keys, with support for composite keys
        applyEditedKeys(entry, keys, true);

        this.dictionary.save(entry);

        // Prepare validated links
        updateLinks(entry, editData.getColumns());

        // Now update select clause using validated tableLinks
        entry.setSelectClause(columnsAsSelectClause(editData.getColumns(),
                this.links.findByDictionaryEntry(entry),
                this.mappings.findByDictionaryEntry(entry),
                this.dictionary.findAllByProjectMappedToTableName(project)));

        // And refresh dict Entry
        this.dictionary.save(entry);
    }

    /**
     * Force generate the full query : used for editing
     *
     * @param editData
     */
    public String generateQuery(DictionaryEntryEditData editData) {

        this.projectService.assertCurrentUserHasSelectedProject();
        Project project = this.projectService.getCurrentSelectedProjectEntity();

        // For link building, need other dicts
        Map<String, DictionaryEntry> allDicts = this.dictionary.findAllByProjectMappedToTableName(project);

        // Will use a "temp" simulated dict entry
        DictionaryEntry entry = new DictionaryEntry();

        entry.setTableName(editData.getTable());

        // Specified keys from columns
        List<ColumnEditData> keys = editData.getColumns().stream().filter(ColumnEditData::isKey).sorted().collect(Collectors.toList());

        // Other common edited properties
        entry.setParameterName(editData.getName());
        entry.setWhereClause(editData.getWhere());

        // Apply keys, with support for composite keys
        applyEditedKeys(entry, keys, false);

        // Simulated links
        Collection<TableLink> links = prepareLinksFromEditData(entry, editData.getColumns()).values();

        // Now update select clause using validated tableLinks
        entry.setSelectClause(columnsAsSelectClause(editData.getColumns(),
                links,
                Collections.emptyList(),
                this.dictionary.findAllByProjectMappedToTableName(project)));

        return this.queryGenerator.producesSelectParameterQuery(entry, links, allDicts);
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
        domain.setCreatedTime(now());
        domain.setUpdatedTime(now());
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
     *
     */
    public void refreshCachedMetadataForOneTable(String tableName) {

        LOGGER.debug("Forced refresh on cache for metadata on table {}", tableName);

        this.metadatas.refreshTable(tableName != null ? tableName.toUpperCase() : null);
    }

    /**
     * @return
     */
    public ExportImportResult<ExportFile> exportFonctionalDomains(UUID domainUUID) {

        LOGGER.info("Process export of specified fonctional domain {} items", domainUUID);

        FunctionalDomain domain = this.domains.getOne(domainUUID);

        // Packages on limited data sets
        ProjectPackage proj = new ProjectPackage(PARTIAL_PROJECTS_EXPORT, now())
                .from(Stream.of(domain.getProject()));

        DictionaryPackage dict = new DictionaryPackage(PARTIAL_DICT_EXPORT, now())
                .from(this.dictionary.findByDomain(domain).stream());

        FunctionalDomainPackage doms = new FunctionalDomainPackage(PARTIAL_DOMAINS_EXPORT, now())
                .from(Stream.of(domain));

        TableLinkPackage tl = new TableLinkPackage(PARTIAL_LINKS_EXPORT, now())
                .from(this.links.findByDictionaryEntryDomain(domain).stream());

        TableMappingPackage tm = new TableMappingPackage(MAPPINGS_EXPORT, now())
                .from(this.mappings.findByDictionaryEntryDomain(domain).stream());

        // Easy : just take all
        ExportFile file = this.ioService.exportPackages(Arrays.asList(proj, dict, doms, tl, tm));

        return exportResult(file, proj, dict, doms, tl, tm);
    }


    /**
     * @return
     */
    public ExportImportResult<ExportFile> exportCurrentProject() {

        this.projectService.assertCurrentUserHasSelectedProject();

        Project project = this.projectService.getCurrentSelectedProjectEntity();

        LOGGER.info("Process export of complete dictionary related entities for current project {}", project.getName());

        ProjectPackage proj = new ProjectPackage(PARTIAL_PROJECTS_EXPORT, now())
                .from(Stream.of(project));

        // Versions for project
        VersionPackage vers = new VersionPackage(PARTIAL_VERSIONS_EXPORT, now())
                .from(this.versions.findByProject(project).stream());

        // Will filter by domains from package
        List<FunctionalDomain> fdoms = this.domains.findByProject(project);

        FunctionalDomainPackage doms = new FunctionalDomainPackage(PARTIAL_DOMAINS_EXPORT, now())
                .from(fdoms.stream());

        DictionaryPackage dict = new DictionaryPackage(PARTIAL_DICT_EXPORT, now())
                .from(this.dictionary.findByDomainIn(fdoms).stream());

        TableLinkPackage tl = new TableLinkPackage(PARTIAL_LINKS_EXPORT, now())
                .from(this.links.findByDictionaryEntryDomainIn(fdoms).stream());

        TableMappingPackage tm = new TableMappingPackage(MAPPINGS_EXPORT, now())
                .from(this.mappings.findByDictionaryEntryDomainIn(fdoms).stream());

        // Easy : just take all
        ExportFile file = this.ioService.exportPackages(Arrays.asList(proj, vers, dict, doms, tl, tm));

        return exportResult(file, proj, dict, doms, tl, tm);
    }

    /**
     * @return
     */
    public ExportImportResult<ExportFile> exportAll() {

        LOGGER.info("Process export of complete dictionary related entities");

        ProjectPackage proj = new ProjectPackage(PROJECTS_EXPORT, now())
                .from(this.projects.findAll().stream());

        VersionPackage vers = new VersionPackage(VERSIONS_EXPORT, now())
                .from(this.versions.findAll().stream());

        DictionaryPackage dict = new DictionaryPackage(DICT_EXPORT, now())
                .from(this.dictionary.findAll().stream());

        FunctionalDomainPackage doms = new FunctionalDomainPackage(DOMAINS_EXPORT, now())
                .from(this.domains.findAll().stream());

        TableLinkPackage tl = new TableLinkPackage(LINKS_EXPORT, now())
                .from(this.links.findAll().stream());

        TableMappingPackage tm = new TableMappingPackage(MAPPINGS_EXPORT, now())
                .from(this.mappings.findAll().stream());

        // Easy : just take all
        ExportFile file = this.ioService.exportPackages(Arrays.asList(proj, vers, dict, doms, tl, tm));

        return exportResult(file, proj, dict, doms, tl, tm);
    }

    /**
     * Process the package as a single project export, and apply it "as this" in current project :
     * <ul>
     * <li>Deduplicate everything</li>
     * <li>Rebuild tables / links / mappings by name</li>
     * </ul>
     *
     * @param file
     * @return
     */
    public ExportImportResult<Void> importAllInCurrentProject(ExportFile file) {

        LOGGER.info("Process import of complete dictionary related entities");

        // Less easy : need to complete and identify if value is new or not
        List<SharedPackage<?>> packages = this.ioService.importPackages(file);

        List<Project> importedProjects = packages.stream()
                .filter(p -> p.getClass() == ProjectPackage.class)
                .flatMap(p -> ((ProjectPackage) p).content())
                .collect(Collectors.toList());

        // Must have just 1
        assertImportSingleProject(importedProjects);

        // #1st Get reference to the single project
        Project importedProject = importedProjects.iterator().next();

        // Will work in current project
        Project destinationProject = this.projectService.getCurrentSelectedProjectEntity();

        // Current package is associated to a source package
        ImportingDictionary.ImportingDictionaryProject forSingleProject = new ImportingDictionary(packages).onProject(importedProject.getUuid());

        // Always import to current project
        return importIntoDestinationProject(forSingleProject, destinationProject, true);
    }

    /**
     * @param file
     */
    public ExportImportResult<Void> importAll(ExportFile file) {

        LOGGER.info("Process import of complete dictionary related entities");

        // Less easy : need to complete and identify if value is new or not
        List<SharedPackage<?>> packages = this.ioService.importPackages(file);
        AtomicInteger newProjsCount = new AtomicInteger(0);

        // Can substitute project by name : need refer update
        Map<UUID, Project> substituteProjects = new HashMap<>();

        // Will build result by projects
        final ExportImportResult<Void> result = ExportImportResult.newVoid();

        // Process on each, with right order :

        // #1st The projects (used by other)
        List<Project> importedProjects = packages.stream()
                .filter(p -> p.getClass() == ProjectPackage.class)
                .flatMap(p -> ((ProjectPackage) p).content())
                .map(d -> this.projectService.importProject(d, newProjsCount, substituteProjects))
                .collect(Collectors.toList());

        this.projects.saveAll(importedProjects);

        // Need first to stream-process all imported data
        ImportingDictionary fullImport = new ImportingDictionary(packages);

        List<ImportingDictionary.ImportingDictionaryProject> byProject = substituteProjects.keySet().stream()
                .map(fullImport::onProject)
                .collect(Collectors.toList());

        // Import each package source into identified substitute
        byProject.forEach(i -> {
            result.copyCounts(importIntoDestinationProject(i, substituteProjects.get(i.getProjectUuid()), false));
        });

        // Project + versions
        if (importedProjects.size() > 0) {
            result.addCount(PROJECTS_EXPORT, newProjsCount.get(),
                    importedProjects.size() - newProjsCount.get(), 0);
        }

        return result;
    }

    /**
     * Process import for one project
     *
     * @param importing          the data to import
     * @param destinationProject specified target project
     * @param copyMode           true if the process is a "copy" : will ignore versions and create new items for each imported data
     * @return result for the package part
     */
    private ExportImportResult<Void> importIntoDestinationProject(
            ImportingDictionary.ImportingDictionaryProject importing,
            Project destinationProject,
            boolean copyMode) {

        LOGGER.info("Process import of one specified dictionary project \"{}\" to destination project \"{}\" using mode {}",
                importing.getProjectUuid(), destinationProject.getUuid(), copyMode ? "copy" : "standard");

        AtomicInteger newDomainsCount = new AtomicInteger(0);
        AtomicInteger newDictCount = new AtomicInteger(0);
        AtomicInteger newLinksCount = new AtomicInteger(0);
        AtomicInteger newMappingsCount = new AtomicInteger(0);
        AtomicInteger deduplicatedDomainsCount = new AtomicInteger(0);
        AtomicInteger newVersCount = new AtomicInteger(0);

        // Can substitute domains by name : need refer update
        Map<UUID, FunctionalDomain> substituteDomains = new HashMap<>();
        Map<UUID, DictionaryEntry> substituteTables = new HashMap<>();

        // #1st The functional domains (used by other)
        List<FunctionalDomain> importedDomains = importing.domains()
                .map(d -> importDomainInProject(d, destinationProject, newDomainsCount, substituteDomains, copyMode))
                .collect(Collectors.toList());

        // #2rd The dictionary (referencing domains)
        List<DictionaryEntry> importedDicts = importing.tables()
                .map(d -> importDictionaryEntryInDomains(d, newDictCount, substituteDomains, substituteTables, copyMode))
                .collect(Collectors.toList());

        // #4th The links (referencing dictionary entries)
        List<TableLink> importedLinks = importing.links()
                .map(d -> importTableLinkInTable(d, newLinksCount, substituteTables, copyMode))
                .collect(Collectors.toList());

        // #5th The mappings (referencing dictionary entries)
        List<TableMapping> importedMappings = importing.mappings()
                .map(d -> importTableMappingInTable(d, newMappingsCount, substituteTables, copyMode))
                .collect(Collectors.toList());


        // Batched save on all imported
        this.domains.saveAll(importedDomains);
        this.dictionary.saveAll(importedDicts);
        this.links.saveAll(importedLinks);
        this.mappings.saveAll(importedMappings);

        LOGGER.info("Import completed of {} domains, {} dictionary entry, {} table links and {} table mappings on destination project {}",
                importedDomains.size(), importedDicts.size(), importedLinks.size(), importedMappings.size(), destinationProject.getName());
        ExportImportResult<Void> result = ExportImportResult.newVoid();

        // Now check for duplicate domain names and fix
        if (this.domains.findAll().stream().anyMatch(d -> importedDomains.stream().anyMatch(similareDomain(d)))) {

            LOGGER.info("Some domains need to be deduplicated from existing");

            deduplicateDomains(importedDomains, deduplicatedDomainsCount);
        }

        // Process version only when not copying project data
        if (!copyMode) {
            // #6th The versions (referencing projects)
            List<Version> importedVersions = importing.versions()
                    .map(d -> importVersionInProject(d, newVersCount, destinationProject))
                    .sorted(Comparator.comparing(Version::getUpdatedTime))
                    .collect(Collectors.toList());

            // Check all versions
            assertVersionModelsAreValid(importedVersions);

            this.versions.saveAll(importedVersions);

            // Complete version details
            importedVersions.forEach(this::completeVersionContents);

            if (importedVersions.size() > 0) {
                result.addCount(VersionExportPackage.VERSIONS_EXPORT, newVersCount.get(),
                        importedVersions.size() - newVersCount.get(), 0);
            }
        }

        if (importedDomains.size() > 0) {
            result.addCount(DOMAINS_EXPORT, newDomainsCount.get(),
                    importedDomains.size() - newDomainsCount.get(), 0);
        }

        if (importedDicts.size() > 0) {
            result.addCount(DICT_EXPORT, newDictCount.get(),
                    importedDicts.size() - newDictCount.get(), 0);
        }

        if (importedLinks.size() > 0) {
            result.addCount(LINKS_EXPORT, newLinksCount.get(),
                    importedLinks.size() - newLinksCount.get(), 0);
        }

        if (importedMappings.size() > 0) {
            result.addCount(MAPPINGS_EXPORT, newMappingsCount.get(),
                    importedMappings.size() - newMappingsCount.get(), 0);
        }

        if (deduplicatedDomainsCount.get() > 0) {
            result.addCount(DEDUPLICATED_DOMAINS, deduplicatedDomainsCount.get(),
                    0, deduplicatedDomainsCount.get());
        }

        return result;
    }

    private static ExportImportResult<ExportFile> exportResult(
            ExportFile file,
            ProjectPackage proj,
            DictionaryPackage dict,
            FunctionalDomainPackage doms,
            TableLinkPackage tl,
            TableMappingPackage tm) {

        ExportImportResult<ExportFile> result = new ExportImportResult<>(file);

        result.addCount(PARTIAL_PROJECTS_EXPORT, proj.getProcessedSize(), 0, 0);
        result.addCount(PARTIAL_DICT_EXPORT, dict.getProcessedSize(), 0, 0);
        result.addCount(PARTIAL_DOMAINS_EXPORT, doms.getProcessedSize(), 0, 0);
        result.addCount(PARTIAL_LINKS_EXPORT, tl.getProcessedSize(), 0, 0);
        result.addCount(MAPPINGS_EXPORT, tm.getProcessedSize(), 0, 0);

        return result;
    }

    private static Predicate<FunctionalDomain> similareDomain(FunctionalDomain existing) {
        // Not an imported one
        return i -> !i.getUuid().equals(existing.getUuid())
                // But similar in project
                && i.getProject().equals(existing.getProject())
                && i.getName().equals(existing.getName());
    }

    /**
     * Search for existing domain on similar name,
     *
     * @param importedDomains
     */
    private void deduplicateDomains(List<FunctionalDomain> importedDomains, AtomicInteger deduplicatedDomainsCount) {
        this.domains.findAll().forEach(existing ->
                // If a duplicate exists in imported domains
                importedDomains.stream().filter(similareDomain(existing)).findFirst().ifPresent(

                        // Then process it to switch existing to new imported one
                        imported -> {

                            // Update dic entries with imported domain
                            this.dictionary.findByDomain(existing).forEach(d -> {
                                d.setDomain(imported);
                                this.dictionary.save(d);
                            });

                            // And drop existing one
                            this.domains.delete(existing);

                            deduplicatedDomainsCount.incrementAndGet();
                        })
        );
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

        // ID is the one from last version
        boolean isLastVersion = version.getUuid().equals(lastProjectVersion.getUuid());

        // No commits for it
        boolean isUpdatable = this.commits.countCommitsForVersion(lastProjectVersion.getUuid()) == 0;

        return VersionData.fromEntity(version, isLastVersion, isLastVersion && isUpdatable);
    }

    /**
     * Keys are flat properties. Need to erase all of them before update from a row
     *
     * @param entry existing dic entry to clean
     */
    private static void resetExtKeys(DictionaryEntry entry) {

        entry.setExt1KeyName(null);
        entry.setExt1KeyType(null);
        entry.setExt2KeyName(null);
        entry.setExt2KeyType(null);
        entry.setExt3KeyName(null);
        entry.setExt3KeyType(null);
        entry.setExt4KeyName(null);
        entry.setExt4KeyType(null);
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

        List<String> existingKeys = entry.keyNames().collect(Collectors.toList());

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Update keys for entry {} for table \"{}\". Had {} existing keys, Now will have {} keys. Was \"{}\", now will be \"{}\"",
                    entry.getUuid(), entry.getTableName(), existingKeys.size(), keys.size(),
                    String.join("\", \"", existingKeys),
                    keys.stream().map(ColumnEditData::getName).collect(Collectors.joining("\", \"")));
        }

        // Reset all existings ext keys
        resetExtKeys(entry);

        // Always use first as "normal" key, then ext for others.
        ColumnEditData first = keys.get(0);

        if (validate) {
            warnKeyIsPk(entry, first);
        }

        entry.setKeyName(first.getName());
        entry.setKeyType(first.getType());

        // Uses FALL-THROUGH for combined update on multiple keys
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

        // Control also that the key is unique (heavy load)
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
     * @param substituteDomains
     * @return
     */
    private FunctionalDomain importDomainInProject(
            FunctionalDomain imported,
            Project destination,
            AtomicInteger newCounts,
            Map<UUID, FunctionalDomain> substituteDomains,
            boolean resetUuids) {

        // Deduplicate AFTER import
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

        local.setProject(destination);

        local.setImportedTime(LocalDateTime.now());
        substituteDomains.put(imported.getUuid(), local);

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
     * @param destinationProject target project for imported version
     * @return
     */
    private Version importVersionInProject(Version imported, AtomicInteger newCounts, Project destinationProject) {

        Optional<Version> localOpt = this.versions.findById(imported.getUuid());

        // Exists already
        localOpt.ifPresent(d -> LOGGER.debug("Import existing project by uuid {} : will update currently owned", imported.getUuid()));

        // Will try also by name
        Version byName = this.versions.findByNameAndProject(imported.getName(), destinationProject);

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

        // Use specified destination project
        local.setProject(destinationProject);

        // Common attrs
        local.setUpdatedTime(imported.getUpdatedTime());
        local.setName(imported.getName());
        local.setModelIdentity(imported.getModelIdentity());

        local.setImportedTime(now());

        return local;
    }

    /**
     * Process one DictionaryEntry
     *
     * @param imported          from package
     * @param newCounts         result count
     * @param substituteDomains
     * @param substituteTables
     * @param resetUuids        true for init of new dictEntry at import
     * @return imported dict Entry
     */
    private DictionaryEntry importDictionaryEntryInDomains(
            DictionaryEntry imported,
            AtomicInteger newCounts,
            Map<UUID, FunctionalDomain> substituteDomains,
            Map<UUID, DictionaryEntry> substituteTables,
            boolean resetUuids) {

        FunctionalDomain associatedDomain = substituteDomains.get(imported.getDomain().getUuid());

        Optional<DictionaryEntry> localOpt = this.dictionary.findByTableNameAndDomain(imported.getTableName(), associatedDomain);

        // Exists already on name
        localOpt.ifPresent(d -> LOGGER.debug("Import existing dictionary entry for table \"{}\" : will update currently owned", imported.getTableName()));

        // Or is a new one
        DictionaryEntry local = localOpt.orElseGet(() -> {
            LOGGER.debug("Import new dictionary entry for table \"{}\" : will create currently owned", imported.getTableName());
            DictionaryEntry loc = new DictionaryEntry(resetUuids ? UUID.randomUUID() : imported.getUuid());
            loc.setCreatedTime(imported.getCreatedTime());
            newCounts.incrementAndGet();
            return loc;
        });

        // Common attrs
        local.setDomain(associatedDomain);

        // Common attrs
        copyImportedDictionaryEntry(imported, local);

        substituteTables.put(imported.getUuid(), local);

        return local;
    }

    private void copyImportedDictionaryEntry(DictionaryEntry imported, DictionaryEntry local) {

        local.setUpdatedTime(imported.getUpdatedTime());
        local.setKeyName(imported.getKeyName());
        local.setKeyType(imported.getKeyType());
        local.setExt1KeyName(imported.getExt1KeyName());
        local.setExt1KeyType(imported.getExt1KeyType());
        local.setExt2KeyName(imported.getExt2KeyName());
        local.setExt2KeyType(imported.getExt2KeyType());
        local.setExt3KeyName(imported.getExt3KeyName());
        local.setExt3KeyType(imported.getExt3KeyType());
        local.setExt4KeyName(imported.getExt4KeyName());
        local.setExt4KeyType(imported.getExt4KeyType());
        local.setParameterName(imported.getParameterName());
        local.setTableName(imported.getTableName());
        local.setSelectClause(imported.getSelectClause());
        local.setWhereClause(imported.getWhereClause());

        local.setImportedTime(now());

    }

    /**
     * Process one TableLink
     *
     * @param imported
     * @return
     */
    private TableLink importTableLinkInTable(
            TableLink imported,
            AtomicInteger newCounts,
            Map<UUID, DictionaryEntry> substituteTables,
            boolean resetUuids) {

        DictionaryEntry associatedTable = substituteTables.get(imported.getDictionaryEntry().getUuid());

        Optional<TableLink> localOpt = this.links.findByDictionaryEntryAndColumnFromAndTableToAndColumnTo(associatedTable, imported.getColumnFrom(), imported.getTableTo(), imported.getColumnTo());

        // Exists already
        localOpt.ifPresent(d -> LOGGER.debug("Import existing TableLink {} : will update currently owned", imported.getUuid()));

        // Or is a new one
        TableLink local = localOpt.orElseGet(() -> {
            LOGGER.debug("Import new TableLink {} : will create currently owned", imported.getUuid());
            TableLink loc = new TableLink(resetUuids ? UUID.randomUUID() : imported.getUuid());
            loc.setCreatedTime(imported.getCreatedTime());
            newCounts.incrementAndGet();
            return loc;
        });

        local.setDictionaryEntry(associatedTable);

        // Common attrs
        copyImportedTableLink(imported, local);

        return local;
    }

    public void copyImportedTableLink(TableLink imported, TableLink local) {

        // Common attrs
        local.setUpdatedTime(imported.getUpdatedTime());
        local.setColumnFrom(imported.getColumnFrom());
        local.setColumnTo(imported.getColumnTo());
        local.setTableTo(imported.getTableTo());
        local.setExt1ColumnTo(imported.getExt1ColumnTo());
        local.setExt1ColumnFrom(imported.getExt1ColumnFrom());
        local.setExt2ColumnTo(imported.getExt2ColumnTo());
        local.setExt2ColumnFrom(imported.getExt2ColumnFrom());
        local.setExt3ColumnTo(imported.getExt3ColumnTo());
        local.setExt3ColumnFrom(imported.getExt3ColumnFrom());
        local.setExt4ColumnTo(imported.getExt4ColumnTo());
        local.setExt4ColumnFrom(imported.getExt4ColumnFrom());
        local.setImportedTime(now());
    }

    /**
     * Process one TableMapping
     *
     * @param imported
     * @return
     */
    private TableMapping importTableMappingInTable(
            TableMapping imported,
            AtomicInteger newCounts,
            Map<UUID, DictionaryEntry> substituteTables,
            boolean resetUuids) {

        DictionaryEntry associatedTable = substituteTables.get(imported.getDictionaryEntry().getUuid());

        Optional<TableMapping> localOpt = this.mappings.findByDictionaryEntryAndColumnFromAndTableToAndColumnToAndMapTable(associatedTable, imported.getColumnFrom(), imported.getTableTo(), imported.getColumnTo(), imported.getMapTable());

        // Exists already
        localOpt.ifPresent(d -> LOGGER.debug("Import existing TableMapping {} : will update currently owned", imported.getUuid()));

        // Or is a new one
        TableMapping local = localOpt.orElseGet(() -> {
            LOGGER.debug("Import new TableLink {} : will create currently owned", imported.getUuid());
            TableMapping loc = new TableMapping(resetUuids ? UUID.randomUUID() : imported.getUuid());
            loc.setCreatedTime(imported.getCreatedTime());
            newCounts.incrementAndGet();
            return loc;
        });

        local.setDictionaryEntry(associatedTable);

        // Common attrs
        copyImportedTableMapping(imported, local);

        return local;
    }

    public void copyImportedTableMapping(TableMapping imported, TableMapping local) {

        // Common attrs
        local.setUpdatedTime(imported.getUpdatedTime());
        local.setName(imported.getName());
        local.setColumnFrom(imported.getColumnFrom());
        local.setColumnTo(imported.getColumnTo());
        local.setTableTo(imported.getTableTo());
        local.setMapTable(imported.getMapTable());
        local.setMapTableColumnFrom(imported.getMapTableColumnFrom());
        local.setMapTableColumnTo(imported.getMapTableColumnTo());
        local.setImportedTime(now());
    }

    /**
     * Update / Create / delete tablelink regarding specified FK in columnEditDatas
     *
     * @param entry
     * @param cols
     */
    private Map<String, TableLink> prepareLinksFromEditData(DictionaryEntry entry, List<ColumnEditData> cols) {

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

        return editedLinksByTableTo;
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

        Map<String, TableLink> editedLinksByTableTo = prepareLinksFromEditData(entry, cols);

        // Get existing to update / remove
        Map<String, TableLink> existingLinksByTableTo = this.links.findByDictionaryEntry(entry).stream().distinct()
                .collect(Collectors.toMap(TableLink::getTableTo, l -> l));

        int compositeCount = 0;

        // And prepare 3 sets of links
        for (TableLink link : editedLinksByTableTo.values()) {

            TableLink existing = existingLinksByTableTo.remove(link.getTableTo());

            // Update
            if (existing != null) {
                existing.setUpdatedTime(now());
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
                link.setCreatedTime(now());
                link.setUpdatedTime(now());
                link.setDictionaryEntry(entry);
                link.setUuid(UUID.randomUUID());
                createdLinks.add(link);
            }
        }

        // Remaining are deleted
        Collection<TableLink> deletedLinks = existingLinksByTableTo.values();

        LOGGER.info("Links updated for dictionary Entry {}. {} links added, {} links updated and {} deleted, including "
                        + "{} links with composite keys",
                entry.getUuid(), createdLinks.size(), updatedLinks.size(), deletedLinks.size(), compositeCount);

        // Process DB updates
        if (createdLinks.size() > 0)
            this.links.saveAll(createdLinks);
        if (updatedLinks.size() > 0)
            this.links.saveAll(updatedLinks);
        if (deletedLinks.size() > 0)
            this.links.deleteAll(deletedLinks);
    }

    /**
     * @return
     */
    public Version getLastUpdatedVersion() {

        this.projectService.assertCurrentUserHasSelectedProject();
        Project project = this.projectService.getCurrentSelectedProjectEntity();

        if (project != null) {
            return this.versions.getLastVersionForProject(project);
        }

        return null;
    }

    /**
     * @param tableName
     * @return
     */
    private TableDescription getTableDescription(String tableName) {

        return this.metadatas.getTables().stream()
                .filter(t -> t.getName().equalsIgnoreCase(tableName))
                .findFirst()
                .orElse(TableDescription.MISSING);
    }

    /**
     * Extract and apply the content for an updated version (everything present when the version is updated)
     *
     * @param version
     */
    private void completeVersionContents(Version version) {

        Project project = version.getProject();

        // All contents
        Map<String, List<UUID>> contentUuids = this.versions.findLastVersionContents(project, version.getUpdatedTime());

        // Separated extracts
        List<UUID> domsUuids = contentUuids.get(VersionRepository.MAPPED_TYPE_DOMAIN);
        List<UUID> dictUuids = contentUuids.get(VersionRepository.MAPPED_TYPE_DICT);
        List<UUID> linksUuids = contentUuids.get(VersionRepository.MAPPED_TYPE_LINK);
        List<UUID> mappingsUuids = contentUuids.get(VersionRepository.MAPPED_TYPE_MAPPING);

        // Apply all contents (externalized for testability)
        this.changesGenerator.completeVersionContentForChangeGeneration(
                version,
                domsUuids != null ? this.domains.findAllById(domsUuids) : Collections.emptyList(),
                dictUuids != null ? this.dictionary.findAllById(dictUuids) : Collections.emptyList(),
                linksUuids != null ? this.links.findAllById(linksUuids) : Collections.emptyList(),
                mappingsUuids != null ? this.mappings.findAllById(mappingsUuids) : Collections.emptyList()
        );
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
            if (!this.metadatas.isColumnSetHasUniqueValue(dict.getTableName(), Collections.singletonList(dict.getKeyName()), dict.getWhereClause())) {
                throw new ApplicationException(DIC_KEY_NOT_UNIQ, "Cannot edit dictionary entry for table " + dict.getTableName() +
                        " with unique key " + dict.getKeyName() + " has not unique values",
                        dict.getTableName() + "." + dict.getKeyName());
            }
        }

        // Check on composite key
        else {
            Collection<String> keys = dict.keyNames().collect(Collectors.toSet());
            if (!this.metadatas.isColumnSetHasUniqueValue(dict.getTableName(), keys, dict.getWhereClause())) {
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
    private String columnsAsSelectClause(List<ColumnEditData> columns, Collection<TableLink> tabLinks, Collection<TableMapping> tabMappings,
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
     * <p>If we have to use the model ID as version name, but their is no version ID, then We cant !</p>
     *
     * @param useModelIdAsVersionName current feature status on "use model id as version name"
     * @param currentModelId          current model id extracted from available identifier
     */
    private static void assertCanCreateVersionFromModelId(boolean useModelIdAsVersionName, String currentModelId) {

        if (useModelIdAsVersionName && currentModelId == null) {
            throw new ApplicationException(VERSION_NOT_MODEL_ID, "Cannot use the model ID as version name if no model ID can be found");
        }
    }

    private static void assertImportSingleProject(List<Project> imported) {

        if (imported.size() > 1) {
            throw new ApplicationException(IMPORT_MULTIPLE_PROJECT, "Cannot import multiple projects when it is required to update current project");
        }

        if (imported.size() == 0) {
            throw new ApplicationException(IMPORT_MULTIPLE_PROJECT, "No imported project identified in package");
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

    private static class ImportingDictionary {

        private final List<FunctionalDomain> domains;

        private final List<DictionaryEntry> tables;

        private final List<TableLink> links;

        private final List<TableMapping> mappings;

        private final List<Version> versions;

        ImportingDictionary(List<SharedPackage<?>> packages) {

            this.domains = packages.stream()
                    .filter(p -> p.getClass() == FunctionalDomainPackage.class)
                    .flatMap(p -> ((FunctionalDomainPackage) p).content())
                    .collect(Collectors.toList());

            this.tables = packages.stream()
                    .filter(p -> p.getClass() == DictionaryPackage.class)
                    .flatMap(p -> ((DictionaryPackage) p).content())
                    .collect(Collectors.toList());

            this.links = packages.stream()
                    .filter(p -> p.getClass() == TableLinkPackage.class)
                    .flatMap(p -> ((TableLinkPackage) p).content())
                    .collect(Collectors.toList());

            this.mappings = packages.stream()
                    .filter(p -> p.getClass() == TableMappingPackage.class)
                    .flatMap(p -> ((TableMappingPackage) p).content())
                    .collect(Collectors.toList());

            this.versions = packages.stream()
                    .filter(p -> p.getClass() == VersionPackage.class)
                    .flatMap(p -> ((VersionPackage) p).content())
                    .sorted(Comparator.comparing(Version::getUpdatedTime))
                    .collect(Collectors.toList());
        }

        ImportingDictionaryProject onProject(UUID projectUuid) {
            return new ImportingDictionaryProject(this, projectUuid);
        }

        private static class ImportingDictionaryProject {

            private final List<FunctionalDomain> domains;

            private final List<DictionaryEntry> tables;

            private final List<TableLink> links;

            private final List<TableMapping> mappings;

            private final List<Version> versions;

            private final UUID projectUuid;

            private ImportingDictionaryProject(ImportingDictionary dictionary, UUID projectUuid) {

                this.projectUuid = projectUuid;

                this.domains = dictionary.domains.stream()
                        .filter(i -> i.getProject().getUuid().equals(projectUuid))
                        .collect(Collectors.toList());

                this.tables = dictionary.tables.stream()
                        .filter(i -> this.domains.stream().anyMatch(d -> d.getUuid().equals(i.getDomain().getUuid())))
                        .collect(Collectors.toList());

                this.links = dictionary.links.stream()
                        .filter(i -> this.tables.stream().anyMatch(t -> t.getUuid().equals(i.getDictionaryEntry().getUuid())))
                        .collect(Collectors.toList());

                this.mappings = dictionary.mappings.stream()
                        .filter(i -> this.tables.stream().anyMatch(t -> t.getUuid().equals(i.getDictionaryEntry().getUuid())))
                        .collect(Collectors.toList());

                this.versions = dictionary.versions.stream()
                        .sorted(Comparator.comparing(Version::getUpdatedTime))
                        .collect(Collectors.toList());
            }

            public UUID getProjectUuid() {
                return projectUuid;
            }

            public Stream<FunctionalDomain> domains() {
                return domains.stream();
            }

            public Stream<DictionaryEntry> tables() {
                return tables.stream();
            }

            public Stream<TableLink> links() {
                return links.stream();
            }

            public Stream<TableMapping> mappings() {
                return mappings.stream();
            }

            public Stream<Version> versions() {
                return versions.stream();
            }
        }
    }


}
