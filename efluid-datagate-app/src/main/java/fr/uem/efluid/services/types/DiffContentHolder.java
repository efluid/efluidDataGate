package fr.uem.efluid.services.types;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Holder of a diff, like a commit or a preparing diff result.
 * </p>
 * <p>
 * Provides :
 * <ul>
 *     <li>access to diff content and associated tables.</li>
 *     <li>Pagination and sorting of results</li>
 * </ul>
 * </p>
 *
 * @param <T>
 * @author elecomte
 * @version 1
 * @since v1.2.0
 */
public abstract class DiffContentHolder<T extends PreparedIndexEntry> {

    private final Collection<T> diffContent;

    private final Map<UUID, DictionaryEntrySummary> referencedTables;

    // For simplification of rendering
    private boolean displayAll = true;

    protected DiffContentHolder(Collection<T> diffContent, Map<UUID, DictionaryEntrySummary> referencedTables) {
        this.diffContent = diffContent;
        this.referencedTables = referencedTables;
    }

    /**
     * Entry point to diff content holder. Referenced collection is thread safe and can be updated / accessed without locking
     *
     * @return holder accessor
     */
    public Collection<T> getDiffContent() {
        return this.diffContent;
    }

    /**
     * Accessor for common use of diffContent related to the dictionary Entries
     *
     * @return
     */
    public Stream<Map.Entry<UUID, List<T>>> streamDiffContentMappedToDictionaryEntryUuid() {
        return getDiffContent().stream()
                .collect(Collectors.groupingBy(PreparedIndexEntry::getDictionaryEntryUuid))
                .entrySet().stream();
    }

    /**
     * Entry point to all diff related tables (DictionaryEntry), mapped to their uuids. Referenced collection is thread safe and can be updated / accessed without locking
     *
     * @return holder accessor
     */
    public Map<UUID, DictionaryEntrySummary> getReferencedTables() {
        return this.referencedTables;
    }

    /**
     * Access to the referenced table names only, in a consistent list
     *
     * @return list of names, sorted
     */
    public List<String> getReferencedTableNames() {
        return getReferencedTables().values().stream().map(DictionaryEntrySummary::getTableName).sorted().collect(Collectors.toList());
    }

    /**
     * @return
     */
    public boolean isEmptyDiff() {
        return this.diffContent.isEmpty();
    }

    /**
     * @return
     */
    public long getTotalCount() {
        return this.diffContent.stream().mapToLong(Rendered::getRealSize).sum();
    }

    /**
     * @return
     */
    public int getTotalTableCount() {
        return this.referencedTables.size();
    }

    /**
     * @return
     */
    public int getTotalDomainCount() {
        return getReferencedDomainNames().size();
    }


    public List<String> getReferencedDomainNames() {
        return this.referencedTables.values().stream().map(DictionaryEntrySummary::getDomainName).distinct().sorted().collect(Collectors.toList());
    }

    public boolean isDisplayAll() {
        return this.displayAll;
    }

    public void setDisplayAll(boolean displayAll) {
        this.displayAll = displayAll;
    }

    /**
     * Quick access to covered Functional domains in preparation (used for commit detail
     * page)
     *
     * @return
     */
    public Collection<String> getSelectedFunctionalDomainNames() {
        return this.diffContent.stream()
                .filter(PreparedIndexEntry::isSelected)
                .map(PreparedIndexEntry::getDictionaryEntryUuid)
                .distinct()
                .map(u -> getReferencedTables().get(u).getDomainName())
                .collect(Collectors.toSet());
    }

    /**
     * For testing / quick validation of content, gives access to the diff for a specified table only
     *
     * @param name
     * @return
     */
    public Collection<T> getDiffContentForTableName(String name) {

        Optional<DictionaryEntrySummary> table = getReferencedTables().values().stream()
                .filter(t -> t.getTableName().equals(name))
                .findFirst();

        return table.map(d -> getDiffContent().stream()
                .filter(i -> i.getDictionaryEntryUuid().equals(d.getUuid()))
                .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }
}
