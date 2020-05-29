package fr.uem.efluid.services.types;

import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>For search / filter and Sort support on a diff content.</p>
 * <p>Used to specify the required DiffContent selection to display and navigate through</p>
 *
 * @author elecomte
 * @version 1
 * @since v1.2.0
 */
public class DiffContentSearch {

    private Set<UUID> selectedDictionaryEntries;
    private Set<UUID> selectedDomains;
    private String keySearch;
    private String valueSearch;
    private SortCriteria sortCriteria;
    private boolean sortAsc;
    private boolean hideItemsWithoutAction ;

    private transient boolean doSearchDicts = false;
    private transient boolean doSearchKey = false;
    private transient boolean doSearchValue = false;

    private transient Comparator<PreparedIndexEntry> comparator;

    private transient boolean prepared = false;

    public DiffContentSearch() {
        super();
    }

    public Set<UUID> getSelectedDictionaryEntries() {
        return selectedDictionaryEntries;
    }

    public void setSelectedDictionaryEntries(Set<UUID> selectedDictionaryEntries) {
        this.selectedDictionaryEntries = selectedDictionaryEntries;
    }

    public Set<UUID> getSelectedDomains() {
        return selectedDomains;
    }

    public void setSelectedDomains(Set<UUID> selectedDomains) {
        this.selectedDomains = selectedDomains;
    }

    public String getKeySearch() {
        return keySearch;
    }

    public void setKeySearch(String keySearch) {
        this.keySearch = keySearch;
    }

    public String getValueSearch() {
        return valueSearch;
    }

    public void setValueSearch(String valueSearch) {
        this.valueSearch = valueSearch;
    }

    public SortCriteria getSortCriteria() {
        return sortCriteria;
    }

    public void setSortCriteria(SortCriteria sortCriteria) {
        this.sortCriteria = sortCriteria;
    }

    public boolean isSortAsc() {
        return sortAsc;
    }

    public void setSortAsc(boolean sortAsc) {
        this.sortAsc = sortAsc;
    }

    /**
     * <p>Apply the search on specified content. If not yet done, a preparation step is processed
     * to init some filter / sort shortcut. Once prepared the search cannot be reset : create a new one
     * on every search criteria change.</p>
     * <p>After filtering, the index entries are completed for display of tableName and DomainName</p>
     *
     * <p>This process is not really optimized, and can be quite heavy on large diff ...</p>
     *
     * @param holder accessor on flat, unfiltered / unsorted diff content
     * @param <T>    diff item type
     * @return filtered and sorted content, as a list (as it is now sorted)
     */
    public <T extends PreparedIndexEntry> List<T> filterAndSortDiffContent(DiffContentHolder<T> holder) {

        if (!this.prepared) {

            // Prepare domain search (as table search)
            if (this.selectedDomains != null && !this.selectedDomains.isEmpty()) {
                if (this.selectedDictionaryEntries == null) {
                    this.selectedDictionaryEntries = new HashSet<>();
                }

                // Complete selected table criteria with the tables of selected domains
                this.selectedDomains.forEach(d ->
                        holder.getReferencedTables().values().stream()
                                .filter(t -> t.getDomainUuid().equals(d))
                                .findFirst().ifPresent(c -> this.selectedDictionaryEntries.add(c.getUuid())));
            }

            // Identify basic checks on
            this.doSearchDicts = this.selectedDictionaryEntries != null && this.selectedDictionaryEntries.size() > 0;
            this.doSearchKey = StringUtils.hasText(this.keySearch);
            this.doSearchValue = StringUtils.hasText(this.valueSearch);
            this.hideItemsWithoutAction = !holder.isDisplayAll();

            // Prepare comparator (here default fixed one)
            // TODO : apply real comparing feature
            this.comparator = Comparator.comparing(PreparedIndexEntry::getDictionaryEntryUuid)
                    .thenComparing(PreparedIndexEntry::getCombinedKey);

            this.prepared = true;
        }

        // Use simple filtering then sorting on content
        return holder.getDiffContent().stream()
                .filter(this::filter)
                // Complete tableName / DomainName to display (only on listed results)
                .peek(i -> {
                    DictionaryEntrySummary dic = holder.getReferencedTables().get(i.getDictionaryEntryUuid());
                    if (dic != null) {
                        i.setTableName(dic.getTableName());
                        i.setDomainName(dic.getDomainName());
                    }
                })
                .sorted(this.comparator)
                .collect(Collectors.toList());
    }

    /**
     * Internal filter on one entry, used prepared criterias
     *
     * @param preparedIndexEntry item to filter
     * @return basic filtering result. Default is true : includes everything when not filtered
     */
    private boolean filter(PreparedIndexEntry preparedIndexEntry) {

        if(this.hideItemsWithoutAction && !preparedIndexEntry.isNeedAction()){
            return false;
        }

        // Search by table - includes also the search by domains as the corresponding tables are added to criteria
        if (this.doSearchDicts && !this.selectedDictionaryEntries.contains(preparedIndexEntry.getDictionaryEntryUuid())) {
            return false;
        }

        if (this.doSearchKey && !preparedIndexEntry.getCombinedKey().contains(this.keySearch)) {
            return false;
        }

        return !this.doSearchValue || preparedIndexEntry.getHrPayload().contains(this.valueSearch);
    }

    public enum SortCriteria {

        BY_DOMAIN,
        BY_DICTIONARY_ENTRY,
        BY_KEY,
        BY_SELECT

    }
}
