package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.IndexEntry;
import fr.uem.efluid.utils.FormatUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>For search / filter and Sort support on a diff content : can process an extracted content OR can create a criteria
 * (as a <tt>Specification</tt>) to extract similar content in an <tt>IndexEntry</tt> repository</p>
 * <p>Used to specify the required DiffContent selection to display and navigate through</p>
 * <p>Initialized from json content on fields <i>filters</i> and <i>sorts</i></p>
 *
 * @author elecomte
 * @version 2
 * @since v1.2.0
 */
public class DiffContentSearch {

    public static final String PROP_DOMAIN = "domain";
    public static final String PROP_TABLE = "table";
    public static final String PROP_KEY = "key";
    public static final String PROP_TYPE = "type";

    /*
    var activeSearch = {
            filters: {
        domain:"",
                table:"",
                key:"",
                type:""
    },
    sorts: {
        domain:"",
                table:"",
                key:"",
                type:""
    }
    */

    /* Properties for search json content hold */

    private Map<String, String> filters;
    private Map<String, String> sorts;

    /* Properties for prepared search processing */

    private final transient Set<UUID> selectedDictionaryEntries = new HashSet<>();

    private boolean hideItemsWithoutAction;

    private transient Pattern keyPattern;
    private transient IndexAction typeFilter;

    private transient boolean doSearchDicts = false;
    private transient boolean doSearchKey = false;
    private transient boolean doSearchType = false;

    private transient Comparator<PreparedIndexEntry> comparator;

    private transient boolean prepared = false;

    public DiffContentSearch() {
        super();
    }

    public Map<String, String> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }

    public Map<String, String> getSorts() {
        return sorts;
    }

    public void setSorts(Map<String, String> sorts) {
        this.sorts = sorts;
    }


    /**
     * <p>Apply the search on specified content. If not yet done, a preparation step is processed
     * to init some filter / sort shortcut. Once prepared the search cannot be reset : create a new one
     * on every search criteria change.</p>
     *
     * <p>This process is not really optimized, and can be quite heavy on large diff ...</p>
     *
     * @param holder accessor on flat, unfiltered / unsorted diff content
     * @param <T>    diff item type
     * @return filtered and sorted content, as a list (as it is now sorted)
     */
    public <T extends PreparedIndexEntry> List<T> filterAndSortDiffContentInMemory(DiffContentHolder<T> holder) {

        prepareCriterias(holder.getReferencedTables(), holder.isDisplayAll());

        // Use simple filtering then sorting on content
        return holder.getDiffContent().stream()
                .filter(this::filter)
                .sorted(this.comparator)
                .collect(Collectors.toList());
    }

    /**
     * From the current search definition, prepare a specification for criteria based content extract
     * to get the filtered / sorted IndexEntry from a repository
     *
     * @param referencedTables current process referenced table (required for table / domain filtering)
     * @return the Specification, providing an adapted Predicate to search for IndexEntry
     */
    public Specification<IndexEntry> toSpecification(Map<UUID, DictionaryEntrySummary> referencedTables, UUID commitUUID) {

        prepareCriterias(referencedTables, true);

        return (Specification<IndexEntry>) (root, query, builder) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(builder.equal(root.get("commit").get("uuid"), commitUUID));

            if (DiffContentSearch.this.doSearchKey) {
                String likePattern = FormatUtils.regexpToTokenSearch(DiffContentSearch.this.keyPattern.pattern()).replaceAll("\\*", "%");
                predicates.add(builder.like(root.get("keyValue"), likePattern));
            }

            if (DiffContentSearch.this.doSearchDicts) {
                CriteriaBuilder.In<UUID> in = builder.in(root.get("dictionaryEntry").get("uuid"));
                DiffContentSearch.this.selectedDictionaryEntries.forEach(in::value);
                predicates.add(in);
            }

            if (DiffContentSearch.this.doSearchType) {
                predicates.add(builder.equal(root.get("action"), DiffContentSearch.this.typeFilter));
            }

            query.orderBy(prepareSpecificationOrder(root, builder));

            return builder.and(predicates.toArray(new Predicate[]{}));
        };
    }

    /**
     * Internal filter on one entry, using prepared criterias
     *
     * @param preparedIndexEntry item to filter
     * @return basic filtering result. Default is true : includes everything when not filtered
     */
    private boolean filter(PreparedIndexEntry preparedIndexEntry) {

        if (this.hideItemsWithoutAction && !preparedIndexEntry.isNeedAction()) {
            return false;
        }

        // Search by table - includes also the search by domains as the corresponding tables are added to criteria
        if (this.doSearchDicts && !this.selectedDictionaryEntries.contains(preparedIndexEntry.getDictionaryEntryUuid())) {
            return false;
        }

        if (this.doSearchKey && !this.keyPattern.matcher(preparedIndexEntry.getCombinedKey()).matches()) {
            return false;
        }

        return !this.doSearchType || this.typeFilter == preparedIndexEntry.getAction();
    }

    /**
     * Init the criterias for mixed filtering support
     *
     * @param referencedTables current holder tables referenced in the diff
     * @param displayAll       true to show also items without actions to run
     */
    private void prepareCriterias(Map<UUID, DictionaryEntrySummary> referencedTables, boolean displayAll) {
        if (!this.prepared) {

            // Apply search on domains (using the table criteria)
            Pattern domainPattern = getFilterPattern(PROP_DOMAIN);
            if (domainPattern != null) {
                // Complete selected table uuids with the tables of matching domains
                referencedTables.values().stream()
                        .filter(t -> domainPattern.matcher(t.getDomainName()).matches())
                        .forEach(c -> this.selectedDictionaryEntries.add(c.getUuid()));
            }

            // Complete direct table criteria
            Pattern tablePattern = getFilterPattern(PROP_TABLE);
            if (tablePattern != null) {
                // Complete selected table uuids applying the table pattern
                referencedTables.values().stream()
                        .filter(t -> tablePattern.matcher(t.getTableName()).matches())
                        .forEach(c -> this.selectedDictionaryEntries.add(c.getUuid()));
            }

            // Prepare key pattern
            this.keyPattern = getFilterPattern(PROP_KEY);

            // Prepare type search
            this.typeFilter = getFilterForType();

            // Identify basic checks on
            this.doSearchDicts = this.selectedDictionaryEntries.size() > 0;
            this.doSearchKey = this.keyPattern != null;
            this.doSearchType = this.typeFilter != null;
            this.hideItemsWithoutAction = !displayAll;

            prepareComparator();

            this.prepared = true;
        }
    }

    /**
     * Init the comparator for java filtering sort
     */
    private void prepareComparator() {
        for (Map.Entry<String, String> sort : this.sorts.entrySet()) {

            if (StringUtils.hasText(sort.getValue())) {
                Sort order = Sort.valueOf(sort.getValue());

                Function<PreparedIndexEntry, String> extractor;

                // Apply property sorter
                switch (sort.getKey()) {
                    case PROP_DOMAIN:
                        extractor = PreparedIndexEntry::getDomainName;
                        break;
                    case PROP_TABLE:
                        extractor = PreparedIndexEntry::getTableName;
                        break;
                    case PROP_KEY:
                        extractor = PreparedIndexEntry::getCombinedKey;
                        break;
                    default:
                        extractor = e -> e.getAction().name();
                }

                // Support chained comparator
                if (this.comparator == null) {
                    this.comparator = Comparator.comparing(extractor);
                } else {
                    this.comparator = this.comparator.thenComparing(extractor);
                }

                // Apply order
                if (order == Sort.DESC) {
                    this.comparator = this.comparator.reversed();
                }
            }
        }

        // Init a default comparator if no sort specified
        if (this.comparator == null) {
            this.comparator = Comparator.comparing(PreparedIndexEntry::getTimestamp);
        }
    }

    private Order[] prepareSpecificationOrder(Root<IndexEntry> root, CriteriaBuilder builder) {

        Order[] orders = this.sorts.entrySet().stream()
                .filter(s -> StringUtils.hasText(s.getValue()))
                .map(sort -> {
                    Expression<?> extractor;

                    // Apply property sorter
                    switch (sort.getKey()) {
                        case PROP_DOMAIN:
                            extractor = root.get("dictionaryEntry").get("domain").get("name");
                            break;
                        case PROP_TABLE:
                            extractor = root.get("dictionaryEntry").get("tableName");
                            break;
                        case PROP_KEY:
                            extractor = root.get("keyValue");
                            break;
                        default:
                            extractor = root.get("action");
                    }

                    // Apply order
                    return sort.getValue().equals(Sort.ASC.name()) ? builder.asc(extractor) : builder.desc(extractor);
                })
                .collect(Collectors.toList()).toArray(new Order[]{});

        // Default orders if no sort specified
        return (orders.length == 0) ? new Order[]{builder.asc(root.get("timestamp"))} : orders;
    }

    private Pattern getFilterPattern(String name) {
        if (this.filters == null) {
            return null;
        }
        String value = this.filters.get(name);
        return StringUtils.hasText(value) ? Pattern.compile(value) : null;
    }

    private IndexAction getFilterForType() {
        if (this.filters == null) {
            return null;
        }
        String value = this.filters.get(PROP_TYPE);
        return StringUtils.hasText(value) ? IndexAction.valueOf(value) : null;
    }

    public enum Sort {
        ASC, DESC
    }


}
