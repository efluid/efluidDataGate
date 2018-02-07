package fr.uem.efluid.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.entities.TableLink;
import fr.uem.efluid.model.metas.TableDescription;
import fr.uem.efluid.model.repositories.DatabaseDescriptionRepository;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.FunctionalDomainRepository;
import fr.uem.efluid.model.repositories.TableLinkRepository;
import fr.uem.efluid.services.types.DictionaryEntryEditData;
import fr.uem.efluid.services.types.DictionaryEntryEditData.ColumnEditData;
import fr.uem.efluid.services.types.DictionaryEntrySummary;
import fr.uem.efluid.services.types.FunctionalDomainData;
import fr.uem.efluid.services.types.SelectableTable;
import fr.uem.efluid.tools.ManagedQueriesGenerator;
import fr.uem.efluid.utils.TechnicalException;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Service
@Transactional
public class DictionaryManagementService {

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
	public void deleteFunctionalDomain(UUID uuid) {

		LOGGER.info("Process delete on functional domain {}", uuid);

		assertDomainCanBeRemoved(uuid);

		this.domains.delete(uuid);
	}

	/**
	 * @return
	 */
	public List<SelectableTable> getSelectableTables() {

		LOGGER.debug("Listing selectable tables for a new dictionary entry");

		// Existing data
		List<DictionaryEntry> entries = this.dictionary.findAll();
		List<String> allTables = this.metadatas.getTables().stream().map(t -> t.getName()).sorted().collect(Collectors.toList());

		// Convert dictionnary as selectable
		List<SelectableTable> selectables = entries.stream()
				.map(e -> new SelectableTable(e.getTableName(), e.getParameterName(), e.getDomain().getName()))
				.peek(s -> allTables.remove(s.getTableName())).collect(Collectors.toList());

		// And add table not yet mapped
		selectables.addAll(allTables.stream().map(t -> new SelectableTable(t, null, null)).collect(Collectors.toSet()));

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

		return this.dictionary.findAll().stream()
				.map(e -> DictionaryEntrySummary.fromEntity(e, this.queryGenerator))
				.peek(d -> d.setCanDelete(!usedIds.contains(d.getUuid())))
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

		// Open existing one
		DictionaryEntry entry = this.dictionary.findOne(entryUuid);

		// Prepare basic fields to edit
		DictionaryEntryEditData edit = DictionaryEntryEditData.fromEntity(entry);

		// Need select clause as a list
		Collection<String> selecteds = entry.getSelectClause() != null ? this.queryGenerator.splitSelectClause(entry.getSelectClause())
				: Collections.emptyList();

		TableDescription desc = getTableDescription(edit.getTable());

		// Keep links
		Map<String, String> mappedLinks = this.links.findByDictionaryEntry(entry).stream()
				.collect(Collectors.toMap(TableLink::getColumnFrom, TableLink::getTableTo));

		// Dedicated case : missing table, pure simulated content
		if (desc == TableDescription.MISSING) {
			edit.setMissingTable(true);
			edit.setColumns(selecteds.stream()
					.map(c -> ColumnEditData.fromSelecteds(c, entry.getTableName(), mappedLinks.get(c)))
					.sorted()
					.collect(Collectors.toList()));
		} else {
			// Add metadata to use for edit
			edit.setColumns(desc.getColumns().stream()
					.map(c -> ColumnEditData.fromColumnDescription(c, selecteds, mappedLinks.get(c.getName())))
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
				.map(c -> ColumnEditData.fromColumnDescription(c, null, null))
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
			entry = this.dictionary.findOne(editData.getUuid());
		}

		// Create new one
		else {
			entry = new DictionaryEntry();
			entry.setUuid(UUID.randomUUID());
			entry.setTableName(editData.getTable());
			entry.setCreatedTime(LocalDateTime.now());
		}

		// Specify key from columns
		ColumnEditData key = editData.getColumns().stream().filter(ColumnEditData::isPrimaryKey).findFirst()
				.orElseThrow(() -> new TechnicalException("The key is mandatory"));
		entry.setKeyName(key.getName());
		entry.setKeyType(key.getType());

		// Other common edited properties
		entry.setDomain(new FunctionalDomain(editData.getDomainUuid()));
		entry.setParameterName(editData.getName());
		entry.setSelectClause(columnsAsSelectClause(editData.getColumns()));
		entry.setWhereClause(editData.getWhere());

		this.dictionary.save(entry);

		updateLinks(entry, editData.getColumns());
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
		List<TableLink> editedLinks = cols.stream().filter(c -> c.getForeignKeyTable() != null).map(c -> {
			TableLink link = new TableLink();
			link.setColumnFrom(c.getName());
			link.setTableTo(c.getForeignKeyTable());
			link.setEntry(entry);
			return link;
		}).collect(Collectors.toList());

		// Get existing to update / removoe
		Map<String, TableLink> existingLinks = this.links.findByDictionaryEntry(entry).stream()
				.collect(Collectors.toMap(TableLink::getColumnFrom, l -> l));

		// And prepare 3 sets of links
		for (TableLink link : editedLinks) {

			TableLink existing = existingLinks.remove(link.getTableTo());

			// Update
			if (existing != null) {
				existing.setTableTo(link.getTableTo());
				updatedLinks.add(existing);
			}

			// New one
			else {
				link.setCreatedTime(LocalDateTime.now());
				link.setEntry(entry);
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
			this.links.save(createdLinks);
		if (updatedLinks.size() > 0)
			this.links.save(updatedLinks);
		if (deletedLinks.size() > 0)
			this.links.delete(deletedLinks);
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
	private void assertDomainCanBeRemoved(UUID uuid) {

		if (this.domains.findUsedIds().contains(uuid)) {
			throw new IllegalArgumentException("FunctionalDomain with UUID " + uuid + " is used in index and therefore cannot be deleted");
		}
	}

	/**
	 * @param columns
	 * @return
	 */
	private String columnsAsSelectClause(List<ColumnEditData> columns) {
		return this.queryGenerator.mergeSelectClause(
				columns.stream().filter(ColumnEditData::isSelected).map(ColumnEditData::getName),
				columns.size());
	}
}
