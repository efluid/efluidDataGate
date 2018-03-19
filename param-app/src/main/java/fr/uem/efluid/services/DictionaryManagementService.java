package fr.uem.efluid.services;

import static fr.uem.efluid.utils.ErrorType.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.TableLink;
import fr.uem.efluid.model.metas.ColumnDescription;
import fr.uem.efluid.model.metas.TableDescription;
import fr.uem.efluid.model.repositories.DatabaseDescriptionRepository;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.FunctionalDomainRepository;
import fr.uem.efluid.model.repositories.TableLinkRepository;
import fr.uem.efluid.services.types.DictionaryEntryEditData;
import fr.uem.efluid.services.types.DictionaryEntryEditData.ColumnEditData;
import fr.uem.efluid.services.types.DictionaryEntrySummary;
import fr.uem.efluid.services.types.DictionaryPackage;
import fr.uem.efluid.services.types.ExportFile;
import fr.uem.efluid.services.types.ExportImportResult;
import fr.uem.efluid.services.types.FunctionalDomainData;
import fr.uem.efluid.services.types.FunctionalDomainPackage;
import fr.uem.efluid.services.types.SelectableTable;
import fr.uem.efluid.services.types.SharedPackage;
import fr.uem.efluid.services.types.TableLinkPackage;
import fr.uem.efluid.tools.ManagedQueriesGenerator;
import fr.uem.efluid.utils.ApplicationException;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Service
@Transactional
public class DictionaryManagementService extends AbstractApplicationService {

	private static final String DICT_EXPORT = "full-dictionary";
	private static final String DOMAINS_EXPORT = "full-domains";
	private static final String LINKS_EXPORT = "full-links";

	private static final String PARTIAL_DICT_EXPORT = "partial-dictionary";
	private static final String PARTIAL_DOMAINS_EXPORT = "partial-domains";
	private static final String PARTIAL_LINKS_EXPORT = "partial-links";

	private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryManagementService.class);

	@Autowired
	private FunctionalDomainRepository domains;

	@Autowired
	private DictionaryRepository dictionary;

	@Autowired
	private DatabaseDescriptionRepository metadatas;

	@Autowired
	private TableLinkRepository links;

	@Autowired
	private ManagedQueriesGenerator queryGenerator;

	@Autowired
	private ExportImportService ioService;

	/**
	 * @return
	 */
	public List<FunctionalDomainData> getAvailableFunctionalDomains() {

		LOGGER.debug("Listing functional Domains");

		// TODO : keep this in cache, or precalculated (once used, cannot be "unused")
		List<UUID> usedIds = this.domains.findUsedIds();

		return this.domains.findAll().stream()
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

		// Existing data
		List<DictionaryEntry> entries = this.dictionary.findAll();

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
	 * As summaries, for display or first level edit
	 * 
	 * @return
	 */
	public List<DictionaryEntrySummary> getDictionnaryEntrySummaries() {

		LOGGER.debug("Listing dictionary content");

		// TODO : keep this in cache, or precalculated (once used, cannot be "unused")
		List<UUID> usedIds = this.dictionary.findUsedIds();

		// For link building, need other dicts
		Map<String, DictionaryEntry> allDicts = this.dictionary.findAllMappedByTableName();

		return this.dictionary.findAll().stream()
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

		LOGGER.info("Open editable content for dictionary entry {}", entryUuid);

		// Check valid uuid
		assertDictionaryEntryExists(entryUuid);

		// Open existing one
		DictionaryEntry entry = this.dictionary.getOne(entryUuid);

		// Prepare basic fields to edit
		DictionaryEntryEditData edit = DictionaryEntryEditData.fromEntity(entry);

		// For link building, need other dicts
		Map<String, DictionaryEntry> allDicts = this.dictionary.findAllMappedByTableName();

		// Links used for mapped tables
		List<TableLink> dicLinks = this.links.findByDictionaryEntry(entry);

		// Need select clause as a list
		Collection<String> selecteds = isNotEmpty(entry.getSelectClause())
				? this.queryGenerator.splitSelectClause(entry.getSelectClause(), dicLinks, allDicts)
				: Collections.emptyList();

		TableDescription desc = getTableDescription(edit.getTable());
		// dicLinks.get(0).equals(dicLinks.get(1))
		// Keep links
		Map<String, TableLink> mappedLinks = dicLinks.stream().distinct()
				.collect(Collectors.toMap(TableLink::getColumnFrom, v -> v));

		// Dedicated case : missing table, pure simulated content
		if (desc == TableDescription.MISSING) {

			// On missing, add key column
			if (!selecteds.contains(entry.getKeyName())) {
				selecteds.add(entry.getKeyName());
			}

			edit.setMissingTable(true);
			edit.setColumns(selecteds.stream()
					.map(c -> ColumnEditData.fromSelecteds(c, entry.getKeyName(), entry.getKeyType(), mappedLinks.get(c)))
					.sorted()
					.collect(Collectors.toList()));
		} else {
			// Add metadata to use for edit
			edit.setColumns(desc.getColumns().stream()
					.map(c -> ColumnEditData.fromColumnDescription(c, selecteds, entry.getKeyName(), mappedLinks.get(c.getName())))
					.sorted()
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

		DictionaryEntryEditData edit = new DictionaryEntryEditData();

		// Prepare minimal values
		edit.setTable(tableName);
		edit.setName(tableName);
		edit.setWhere(ManagedQueriesGenerator.DEFAULT_WHERE_CLAUSE);

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

		LOGGER.info("Process saving on dictionary Entry on table {} (current id : {})", editData.getTable(), editData.getUuid());

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

		// Specify key from columns
		ColumnEditData key = editData.getColumns().stream().filter(ColumnEditData::isKey).findFirst()
				.orElseThrow(() -> new ApplicationException(DIC_NO_KEY, "The key is mandatory"));
		entry.setKeyName(key.getName());
		entry.setKeyType(key.getType());

		// Shouldn't use PK as parameter key
		if (key.getType() == ColumnType.PK) {
			LOGGER.warn("Using the PK \"{}\" as parameter key on table \"{}\" : it may cause wrong conflict if"
					+ " the id is not a real valid business identifier for the parameter table !!!", key.getName(), editData.getTable());
		}

		// Controle also that the key is unique (heavy load)
		assertKeyIsUniqueValue(entry);

		// Other common edited properties
		entry.setDomain(this.domains.getOne(editData.getDomainUuid()));
		entry.setParameterName(editData.getName());
		entry.setSelectClause("- to update -");
		entry.setWhereClause(editData.getWhere());

		this.dictionary.save(entry);

		// Prepare validated links
		updateLinks(entry, editData.getColumns());

		// Now update select clause using validated tableLinks
		entry.setSelectClause(columnsAsSelectClause(editData.getColumns(), this.links.findByDictionaryEntry(entry),
				this.dictionary.findAllMappedByTableName()));

		// And refresh dict Entry
		this.dictionary.save(entry);
	}

	/**
	 * @param name
	 * @return
	 */
	public FunctionalDomainData createNewFunctionalDomain(String name) {

		LOGGER.info("Process add of a new functional domain with name {}", name);

		FunctionalDomain domain = new FunctionalDomain();

		domain.setUuid(UUID.randomUUID());
		domain.setCreatedTime(LocalDateTime.now());
		domain.setName(name);

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
		DictionaryPackage dict = new DictionaryPackage(PARTIAL_DICT_EXPORT, LocalDateTime.now())
				.initWithContent(this.dictionary.findByDomain(domain));
		FunctionalDomainPackage doms = new FunctionalDomainPackage(PARTIAL_DOMAINS_EXPORT, LocalDateTime.now())
				.initWithContent(Collections.singletonList(domain));
		TableLinkPackage tl = new TableLinkPackage(PARTIAL_LINKS_EXPORT, LocalDateTime.now())
				.initWithContent(this.links.findByDictionaryEntryDomain(domain));

		// Easy : just take all
		ExportFile file = this.ioService.exportPackages(Arrays.asList(dict, doms, tl));

		ExportImportResult<ExportFile> result = new ExportImportResult<>(file);

		result.addCount(PARTIAL_DICT_EXPORT, dict.getContentSize(), 0, 0);
		result.addCount(PARTIAL_DOMAINS_EXPORT, doms.getContentSize(), 0, 0);
		result.addCount(PARTIAL_LINKS_EXPORT, tl.getContentSize(), 0, 0);

		return result;
	}

	/**
	 * @return
	 */
	public ExportImportResult<ExportFile> exportAll() {

		LOGGER.info("Process export of complete dictionary related entities");

		DictionaryPackage dict = new DictionaryPackage(DICT_EXPORT, LocalDateTime.now()).initWithContent(this.dictionary.findAll());
		FunctionalDomainPackage doms = new FunctionalDomainPackage(DOMAINS_EXPORT, LocalDateTime.now())
				.initWithContent(this.domains.findAll());
		TableLinkPackage tl = new TableLinkPackage(LINKS_EXPORT, LocalDateTime.now()).initWithContent(this.links.findAll());

		// Easy : just take all
		ExportFile file = this.ioService.exportPackages(Arrays.asList(dict, doms, tl));

		ExportImportResult<ExportFile> result = new ExportImportResult<>(file);

		result.addCount(DICT_EXPORT, dict.getContentSize(), 0, 0);
		result.addCount(DOMAINS_EXPORT, doms.getContentSize(), 0, 0);
		result.addCount(LINKS_EXPORT, tl.getContentSize(), 0, 0);

		return result;
	}

	/**
	 * @param file
	 */
	public ExportImportResult<Void> importAll(ExportFile file) {

		LOGGER.info("Process import of complete dictionary related entities");

		// Less easy : need to complete and identify if value is new or not
		List<SharedPackage<?>> packages = this.ioService.importPackages(file);
		AtomicInteger newDomainsCount = new AtomicInteger(0);
		AtomicInteger newDictCount = new AtomicInteger(0);
		AtomicInteger newLinksCount = new AtomicInteger(0);

		// Process on each, with right order :

		// #1st The functional domains (used by other)
		List<FunctionalDomain> importedDomains = packages.stream().filter(p -> p.getClass() == FunctionalDomainPackage.class)
				.flatMap(p -> ((FunctionalDomainPackage) p).streamContent()).map(d -> importDomain(d, newDomainsCount))
				.collect(Collectors.toList());

		// #2nd The dictionary (referencing domains)
		List<DictionaryEntry> importedDicts = packages.stream().filter(p -> p.getClass() == DictionaryPackage.class)
				.flatMap(p -> ((DictionaryPackage) p).streamContent()).map(d -> importDictionaryEntry(d, newDictCount))
				.collect(Collectors.toList());

		// #3rd The links (referencing dictionary entries)
		List<TableLink> importedLinks = packages.stream().filter(p -> p.getClass() == TableLinkPackage.class)
				.flatMap(p -> ((TableLinkPackage) p).streamContent()).map(d -> importTableLink(d, newLinksCount))
				.collect(Collectors.toList());

		// Batched save on all imported
		this.domains.saveAll(importedDomains);
		this.dictionary.saveAll(importedDicts);
		this.links.saveAll(importedLinks);

		LOGGER.info("Import completed of {} domains, {} dictionary entry and {} table links",
				Integer.valueOf(importedDomains.size()), Integer.valueOf(importedDicts.size()), Integer.valueOf(importedLinks.size()));

		ExportImportResult<Void> result = ExportImportResult.newVoid();

		// Details on imported counts (add vs updated items)
		if (importedDicts.size() > 0) {
			result.addCount(DICT_EXPORT, newDictCount.get(), importedDicts.size() - newDictCount.get(), 0);
		}

		if (importedDomains.size() > 0) {
			result.addCount(DOMAINS_EXPORT, newDomainsCount.get(), importedDomains.size() - newDomainsCount.get(), 0);
		}

		if (importedLinks.size() > 0) {
			result.addCount(LINKS_EXPORT, newLinksCount.get(), importedLinks.size() - newLinksCount.get(), 0);
		}

		return result;
	}

	/**
	 * Process one FunctionalDomain
	 * 
	 * @param imported
	 * @return
	 */
	private FunctionalDomain importDomain(FunctionalDomain imported, AtomicInteger newCounts) {

		Optional<FunctionalDomain> localOpt = this.domains.findById(imported.getUuid());

		// Exists already
		localOpt.ifPresent(d -> LOGGER.debug("Import existing domain {} : will update currently owned", imported.getUuid()));

		// Or is a new one
		FunctionalDomain local = localOpt.orElseGet(() -> {
			LOGGER.debug("Import new domain {} : will create currently owned", imported.getUuid());
			FunctionalDomain loc = new FunctionalDomain(imported.getUuid());
			newCounts.incrementAndGet();
			return loc;
		});

		// Common attrs
		local.setCreatedTime(imported.getCreatedTime());
		local.setName(imported.getName());

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
			newCounts.incrementAndGet();
			return loc;
		});

		// Common attrs
		local.setCreatedTime(imported.getCreatedTime());
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
			newCounts.incrementAndGet();
			return loc;
		});

		// Common attrs
		local.setCreatedTime(imported.getCreatedTime());
		local.setDictionaryEntry(new DictionaryEntry(imported.getDictionaryEntry().getUuid()));
		local.setColumnFrom(imported.getColumnFrom());
		local.setColumnTo(imported.getColumnTo());
		local.setTableTo(imported.getTableTo());

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

		// Produces links from col foreign key
		List<TableLink> editedLinks = cols.stream()
				.filter(c -> isNotEmpty(c.getForeignKeyTable()))
				.map(c -> {
					TableLink link = new TableLink();
					link.setColumnFrom(c.getName());
					link.setTableTo(c.getForeignKeyTable());
					link.setColumnTo(c.getForeignKeyColumn());
					link.setDictionaryEntry(entry);
					return link;
				}).collect(Collectors.toList());

		// Get existing to update / removoe
		Map<String, TableLink> existingLinks = this.links.findByDictionaryEntry(entry).stream().distinct()
				.collect(Collectors.toMap(TableLink::getColumnFrom, l -> l));

		// And prepare 3 sets of links
		for (TableLink link : editedLinks) {

			TableLink existing = existingLinks.remove(link.getColumnFrom());

			// Update
			if (existing != null) {
				existing.setTableTo(link.getTableTo());
				existing.setColumnTo(link.getColumnTo());
				updatedLinks.add(existing);
			}

			// New one
			else {
				link.setCreatedTime(LocalDateTime.now());
				link.setDictionaryEntry(entry);
				link.setUuid(UUID.randomUUID());
				createdLinks.add(link);
			}
		}

		// Remaining are deleted
		Collection<TableLink> deletedLinks = existingLinks.values();

		LOGGER.info("Links updated for dictionary Entry {}. {} links added, {} links updated and {} deleted", entry.getUuid(),
				Integer.valueOf(createdLinks.size()), Integer.valueOf(updatedLinks.size()), Integer.valueOf(deletedLinks.size()));

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
		if (!this.metadatas.isColumnHasUniqueValue(dict.getTableName(), dict.getKeyName())) {
			throw new ApplicationException(DIC_KEY_NOT_UNIQ, "Cannot edit dictionary entry for table " + dict.getTableName() + " with key "
					+ dict.getKeyName() + " has its values are not unique", dict.getTableName() + "." + dict.getKeyName());

		}
	}

	/**
	 * @param columns
	 * @return
	 */
	private String columnsAsSelectClause(List<ColumnEditData> columns, List<TableLink> tabLinks, Map<String, DictionaryEntry> allEntries) {
		return this.queryGenerator.mergeSelectClause(
				columns.stream().filter(ColumnEditData::isSelected).map(ColumnEditData::getName).collect(Collectors.toList()),
				columns.size(),
				tabLinks,
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
}
