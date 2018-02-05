package fr.uem.efluid.services;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.FunctionalDomain;
import fr.uem.efluid.model.metas.TableDescription;
import fr.uem.efluid.model.repositories.DatabaseDescriptionRepository;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.FunctionalDomainRepository;
import fr.uem.efluid.services.types.DictionaryEntryEditData;
import fr.uem.efluid.services.types.DictionaryEntryEditData.ColumnEditData;
import fr.uem.efluid.services.types.DictionaryEntrySummary;
import fr.uem.efluid.services.types.FunctionalDomainData;
import fr.uem.efluid.services.types.SelectableTable;
import fr.uem.efluid.utils.ManagedQueriesUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Service
@Transactional
public class DictionaryManagementService {

	@Autowired
	private FunctionalDomainRepository domains;

	@Autowired
	private DictionaryRepository dictionary;

	@Autowired
	private DatabaseDescriptionRepository metadatas;

	/**
	 * @return
	 */
	public List<FunctionalDomainData> getAvailableFunctionalDomains() {

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

		assertDomainCanBeRemoved(uuid);

		this.domains.delete(uuid);
	}

	/**
	 * @return
	 */
	public List<SelectableTable> getSelectableTables() {

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

		// TODO : keep this in cache, or precalculated (once used, cannot be "unused")
		List<UUID> usedIds = this.dictionary.findUsedIds();

		return this.dictionary.findAll().stream()
				.map(DictionaryEntrySummary::fromEntity)
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

		// Open existing one
		DictionaryEntry entry = this.dictionary.findOne(entryUuid);

		// Prepare basic fields to edit
		DictionaryEntryEditData edit = DictionaryEntryEditData.fromEntity(entry);

		// Need select clause as a list
		Collection<String> selecteds = entry.getSelectClause() != null ? ManagedQueriesUtils.splitSelectClause(entry.getSelectClause())
				: Collections.emptyList();

		TableDescription desc = getTableDescription(edit.getTable());

		// Dedicated case : missing table, pure simulated content
		if (desc == TableDescription.MISSING) {
			edit.setMissingTable(true);
			edit.setColumns(selecteds.stream()
					.map(c -> ColumnEditData.fromSelecteds(c, entry.getTableName()))
					.sorted()
					.collect(Collectors.toList()));
		} else {
			// Add metadata to use for edit
			edit.setColumns(desc.getColumns().stream()
					.map(c -> ColumnEditData.fromColumnDescription(c, selecteds))
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

		DictionaryEntryEditData edit = new DictionaryEntryEditData();

		// Prepare minimal values
		edit.setTable(tableName);
		edit.setName(tableName);
		edit.setWhere(ManagedQueriesUtils.DEFAULT_WHERE_CLAUSE);

		// Add metadata to use for edit
		edit.setColumns(getTableDescription(edit.getTable()).getColumns().stream()
				.map(c -> ColumnEditData.fromColumnDescription(c, null))
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

		// Common edited properties
		entry.setDomain(new FunctionalDomain(editData.getDomainUuid()));
		entry.setKeyName(
				editData.getColumns().stream().filter(ColumnEditData::isPrimaryKey).map(ColumnEditData::getName).findFirst().orElse(null));
		entry.setParameterName(editData.getName());
		entry.setSelectClause(columnsAsSelectClause(editData.getColumns()));
		entry.setWhereClause(editData.getWhere());

		this.dictionary.save(entry);
	}

	/**
	 * @param name
	 * @return
	 */
	public FunctionalDomainData createNewFunctionalDomain(String name) {

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
		this.metadatas.refreshAll();
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
	private static String columnsAsSelectClause(List<ColumnEditData> columns) {
		return ManagedQueriesUtils.mergeSelectClause(
				columns.stream().filter(ColumnEditData::isSelected).map(ColumnEditData::getName),
				columns.size());
	}
}
