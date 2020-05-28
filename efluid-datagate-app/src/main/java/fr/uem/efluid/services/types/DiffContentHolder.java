package fr.uem.efluid.services.types;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
}
