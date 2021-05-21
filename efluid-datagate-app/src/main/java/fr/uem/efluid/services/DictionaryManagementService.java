package fr.uem.efluid.services;

import fr.uem.efluid.model.SharedDictionary;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
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
    private IndexRepository index;

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

        // Search ref identifier if enabled
        String modelId = this.modelDescs.getCurrentModelIdentifier() != null ? this.modelDescs.getCurrentModelIdentifier() : name;

        // Search by name
        Version version = this.versions.findByNameAndProject(name, project);

        // Create
        if (version == null) {
            LOGGER.info("Create version {} in current project", name);
            version = new Version();
            version.setUuid(UUID.randomUUID());
            version.setName(name);
            version.setCreatedTime(now());
            version.setProject(project);
            created = true;
        } else {
            LOGGER.info("Update version {} in current project", name);
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

        // Counters for processed items
        AtomicInteger newDomainsCount = new AtomicInteger(0);
        AtomicInteger newDictCount = new AtomicInteger(0);
        AtomicInteger newLinksCount = new AtomicInteger(0);
        AtomicInteger newMappingsCount = new AtomicInteger(0);
        AtomicInteger newVersCount = new AtomicInteger(0);

        // For domain deduplicate
        Set<UUID> deduplicatedDomains = new HashSet<>();

        // All import processes as "importers"
        DictionaryImportAction<FunctionalDomain> domainImporter = domainImporter(copyMode, destinationProject, deduplicatedDomains);
        DictionaryImportAction<Version> versionImporter = versionImporter(destinationProject);
        DictionaryImportAction<DictionaryEntry> tableImporter = dictionaryEntryImporter(copyMode, destinationProject);
        DictionaryImportAction<TableLink> linkImporter = linkImporter(copyMode);
        DictionaryImportAction<TableMapping> mappingImporter = mappingImporter(copyMode);

        // #1st The functional domains (used by other)
        importing.domains().forEach(d -> domainImporter.importEntity(d, newDomainsCount));

        // #2rd The dictionary (referencing domains)
        importing.tables().forEach(d -> tableImporter.importEntity(d, newDictCount));

        // #4th The links (referencing dictionary entries)
        importing.links().forEach(d -> linkImporter.importEntity(d, newLinksCount));

        // #5th The mappings (referencing dictionary entries)
        importing.mappings().forEach(d -> mappingImporter.importEntity(d, newMappingsCount));

        LOGGER.info("Import completed of {} domains, {} dictionary entry, {} table links and {} table mappings on destination project {}",
                importing.domains().size(), importing.tables().size(), importing.links().size(), importing.mappings().size(), destinationProject.getName());
        ExportImportResult<Void> result = ExportImportResult.newVoid();

        // Now clean duplicated domains
        if (deduplicatedDomains.size() > 0) {
            LOGGER.info("Some domains need to be deduplicated from existing");
            this.domains.deleteAll(this.domains.findAllById(deduplicatedDomains));
        }

        // Process version only when not copying project data
        if (!copyMode) {
            // #6th The versions (referencing projects)
            List<Version> importedVersions = importing.versions()
                    .stream().peek(d -> versionImporter.importEntity(d, newVersCount))
                    .sorted(Comparator.comparing(Version::getUpdatedTime))
                    .collect(Collectors.toList());

            if (importedVersions.size() > 0) {
                result.addCount(VersionExportPackage.VERSIONS_EXPORT, newVersCount.get(),
                        importedVersions.size() - newVersCount.get(), 0);
            }
        }

        if (importing.domains().size() > 0) {
            result.addCount(DOMAINS_EXPORT, newDomainsCount.get(),
                    importing.domains().size() - newDomainsCount.get(), 0);
        }

        if (importing.tables().size() > 0) {
            result.addCount(DICT_EXPORT, newDictCount.get(),
                    importing.tables().size() - newDictCount.get(), 0);
        }

        if (importing.links().size() > 0) {
            result.addCount(LINKS_EXPORT, newLinksCount.get(),
                    importing.links().size() - newLinksCount.get(), 0);
        }

        if (importing.mappings().size() > 0) {
            result.addCount(MAPPINGS_EXPORT, newMappingsCount.get(),
                    importing.mappings().size() - newMappingsCount.get(), 0);

        }

        if (deduplicatedDomains.size() > 0) {
            result.addCount(DEDUPLICATED_DOMAINS, deduplicatedDomains.size(),
                    0, deduplicatedDomains.size());
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
     * Init import Process for a FunctionalDomain.
     *
     * @param copyMode           true if destination is processed for copy
     * @param destinationProject destination project to apply
     * @return import process
     */
    private DictionaryImportAction<FunctionalDomain> domainImporter(boolean copyMode, Project destinationProject, Set<UUID> deduplicatedDomains) {

        return new DictionaryImportAction<FunctionalDomain>(copyMode)
                .searchingByNameWith(d -> this.domains.findByProjectAndName(destinationProject, d.getName()))
                .savingWith(d -> {
                    d.setProject(destinationProject);
                    this.domains.saveAndFlush(d);
                })
                .replacingSubstituteWith((existing, imported) -> {
                    // Update dic entries with imported domain
                    this.dictionary.findByDomain(existing).forEach(d -> {
                        d.setDomain(imported);
                        this.dictionary.save(d);
                    });
                    deduplicatedDomains.add(existing.getUuid());
                });
    }

    /**
     * <p>
     * Process one version
     * </p>
     *
     * @param destinationProject target project for imported version
     * @return import process
     */
    private DictionaryImportAction<Version> versionImporter(Project destinationProject) {

        return new DictionaryImportAction<Version>(false)
                .searchingByNameWith(v -> Optional.ofNullable(this.versions.findByNameAndProject(v.getName(), destinationProject)))
                .savingWith(v -> {
                    v.setProject(destinationProject);
                    // Model identifier update when none set (import from api gen)
                    if (v.getModelIdentity() == null) {
                        // Search ref identifier if enabled
                        v.setModelIdentity(this.modelDescs.getCurrentModelIdentifier() != null ? this.modelDescs.getCurrentModelIdentifier() : v.getName());
                    }
                    // And complete dict model
                    completeVersionContents(v);
                    this.versions.saveAndFlush(v);
                })
                .replacingSubstituteWith((existing, imported) -> {
                    this.versions.delete(existing);
                });
    }

    /**
     * Process one DictionaryEntry
     *
     * @param copyMode           true if destination is processed for copy
     * @param destinationProject destination project to apply
     * @return import process
     */
    private DictionaryImportAction<DictionaryEntry> dictionaryEntryImporter(boolean copyMode, Project destinationProject) {

        return new DictionaryImportAction<DictionaryEntry>(copyMode)
                .searchingByNameWith(d -> this.dictionary.findByTableNameAndDomainProject(d.getTableName(), destinationProject))
                .savingWith(d -> {
                    // Clean referenced substitutes domains
                    d.setDomain(this.domains.getOne(d.getDomain().getUuid()));
                    this.dictionary.save(d);
                })
                .replacingSubstituteWith((existing, imported) -> {
                    this.index.updateDictionaryEntryReference(existing.getUuid(), imported.getUuid());
                    this.mappings.deleteAll(this.mappings.findByDictionaryEntry(existing));
                    this.links.deleteAll(this.links.findByDictionaryEntry(existing));
                    this.dictionary.delete(existing);
                });
    }

    /**
     * Process one TableLink
     *
     * @param copyMode true if destination is processed for copy
     * @return import process
     */
    private DictionaryImportAction<TableLink> linkImporter(boolean copyMode) {

        return new DictionaryImportAction<TableLink>(copyMode)
                .searchingByNameWith(imported ->
                        this.links.findByDictionaryEntryAndColumnFromAndTableToAndColumnTo(
                                imported.getDictionaryEntry(),
                                imported.getColumnFrom(),
                                imported.getTableTo(),
                                imported.getColumnTo()))
                .savingWith(this.links::save)
                .replacingSubstituteWith((existing, imported) -> {
                    // Nothing
                });
    }

    /**
     * Process one TableMapping
     *
     * @param copyMode true if destination is processed for copy
     * @return import process
     */
    private DictionaryImportAction<TableMapping> mappingImporter(boolean copyMode) {

        return new DictionaryImportAction<TableMapping>(copyMode)
                .searchingByNameWith(imported ->
                        this.mappings.findByDictionaryEntryAndColumnFromAndTableToAndColumnToAndMapTable(
                                imported.getDictionaryEntry(),
                                imported.getColumnFrom(),
                                imported.getTableTo(),
                                imported.getColumnTo(),
                                imported.getMapTable()))
                .savingWith(this.mappings::save)
                .replacingSubstituteWith((existing, imported) -> {
                    // Nothing
                });
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

        /**
         * Read the package content, using streamed extraction process, then restore full references
         * between the imported elements (domains linked to their tables, tables linked to their links and mappings)
         *
         * @param packages
         */
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

            // Prepare strong references between entities for easier substitute process
            restoreReferences();
        }

        /**
         * The imported items all have weak references : the associated objects are associated together using "link entities"
         * (entities initialized only with their id).
         * To make it easier to apply substitutes in each elements when resolving conflicts or copying new version,
         * this method restore strong references between each entities for the current importing dictionary.
         */
        private void restoreReferences() {

            // References to domains in tables
            this.tables.forEach(t ->
                    this.domains.stream().filter(d -> d.getUuid().equals(t.getDomain().getUuid())).findFirst()
                            .ifPresent(t::setDomain)
            );

            // References to tables in links
            this.links.forEach(l ->
                    this.tables.stream().filter(d -> d.getUuid().equals(l.getDictionaryEntry().getUuid())).findFirst()
                            .ifPresent(l::setDictionaryEntry)
            );

            // References to tables in mappings
            this.mappings.forEach(l ->
                    this.tables.stream().filter(d -> d.getUuid().equals(l.getDictionaryEntry().getUuid())).findFirst()
                            .ifPresent(l::setDictionaryEntry)
            );
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

            public List<FunctionalDomain> domains() {
                return domains;
            }

            public List<DictionaryEntry> tables() {
                return tables;
            }

            public List<TableLink> links() {
                return links;
            }

            public List<TableMapping> mappings() {
                return mappings;
            }

            public List<Version> versions() {
                return versions;
            }
        }
    }

    /**
     * A chained action definition for a dictionary item import : they are all specified with the same "general model"
     * but we must define some details on each steps. This action builder / runner allows to define these steps
     * while a good readability is kept
     *
     * @author elecomte
     * @version 1
     * @since v2.1.18
     */
    private static class DictionaryImportAction<T extends SharedDictionary> {

        private final boolean copyMode;

        private Function<T, Optional<T>> searchByName;
        private BiConsumer<T, T> replaceEntity;
        private Consumer<T> saver;

        private DictionaryImportAction(boolean copyMode) {
            this.copyMode = copyMode;
        }

        public DictionaryImportAction<T> searchingByNameWith(Function<T, Optional<T>> searchByName) {
            this.searchByName = searchByName;
            return this;
        }

        public DictionaryImportAction<T> replacingSubstituteWith(BiConsumer<T, T> replaceEntity) {
            this.replaceEntity = replaceEntity;
            return this;
        }

        public DictionaryImportAction<T> savingWith(Consumer<T> saver) {
            this.saver = saver;
            return this;
        }

        public void importEntity(T imported, AtomicInteger newCounts) {

            Optional<T> localEntity = this.searchByName.apply(imported);
            imported.setImportedTime(LocalDateTime.now());

            // Exists already on name
            if (localEntity.isPresent()) {
                T existing = localEntity.get();
                if (existing.getUuid().equals(imported.getUuid()) || this.copyMode) {
                    LOGGER.debug("Import existing entity \"{}\" : will update currently owned", imported);
                    imported.setUuid(existing.getUuid());
                    this.saver.accept(imported);
                } else {
                    LOGGER.info("Conflict identified on dictionary Import. Existing entity " +
                            "\"{}\" will be replaced by imported one \"{}\" ", existing, imported);
                    this.saver.accept(imported);
                    // Then Replace all required references for substitued entity
                    this.replaceEntity.accept(existing, imported);
                }
            } else {
                LOGGER.debug("Import new entity \"{}\" : will create currently owned", imported);

                // Import new one
                this.saver.accept(imported);
                newCounts.incrementAndGet();
            }
        }
    }

}
