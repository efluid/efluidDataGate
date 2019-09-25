package fr.uem.efluid.tools;

import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.shared.ExportAwareDictionaryEntry;
import fr.uem.efluid.model.shared.ExportAwareFunctionalDomain;
import fr.uem.efluid.model.shared.ExportAwareTableLink;
import fr.uem.efluid.model.shared.ExportAwareTableMapping;
import fr.uem.efluid.services.types.DictionaryEntryEditData.ColumnEditData;
import fr.uem.efluid.services.types.LinkUpdateFollow;
import fr.uem.efluid.services.types.VersionCompare.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Dictionary version change builder and provider. Read 2 versions and provides the identified changes, built in a 3 level process.</p>
 * <p>The Mapping data are currently ignored</p>
 *
 * @author elecomte
 * @version 2
 * @since v2.0.0
 */
@Component
public class VersionContentChangesGenerator {

    private static final String VERSION_CONTENT_ITEM_SEP = ";";

    private final ManagedQueriesGenerator queryGenerator;

    @Autowired
    public VersionContentChangesGenerator(ManagedQueriesGenerator queryGenerator) {
        this.queryGenerator = queryGenerator;
    }

    /**
     * Process
     *
     * @param one dic version "left"
     * @param two dic version "right"
     * @return identified changes
     */
    public List<DomainChanges> generateChanges(Version one, Version two) {

        // Process with a builder because of large amount of processing properties to handle
        return new DomainChangesBuilder(this.queryGenerator, one, two).generateChanges();
    }

    /**
     * Complete specified version with full content, compliant for further version diff processes
     *
     * @param version    Version to complete
     * @param domains    all domains
     * @param dictionary all dict entries
     * @param links      all associated links
     * @param mappings   all associated mappings
     */
    public void completeVersionContentForChangeGeneration(
            Version version,
            List<FunctionalDomain> domains,
            List<DictionaryEntry> dictionary,
            List<TableLink> links,
            List<TableMapping> mappings) {

        // Apply all contents
        version.setDomainsContent(domains.stream().map(ExportAwareFunctionalDomain::serialize).collect(Collectors.joining(VERSION_CONTENT_ITEM_SEP)));
        version.setDictionaryContent(dictionary.stream().map(ExportAwareDictionaryEntry::serialize).collect(Collectors.joining(VERSION_CONTENT_ITEM_SEP)));
        version.setLinksContent(links.stream().map(ExportAwareTableLink::serialize).collect(Collectors.joining(VERSION_CONTENT_ITEM_SEP)));
        version.setMappingsContent(mappings.stream().map(ExportAwareTableMapping::serialize).collect(Collectors.joining(VERSION_CONTENT_ITEM_SEP)));
    }

    /**
     * Change builder for two versions of dictionaries
     */
    private static class DomainChangesBuilder {

        private final ManagedQueriesGenerator queryGenerator;

        private final List<FunctionalDomain> oneDomains;
        private final List<FunctionalDomain> twoDomains;

        private final List<DictionaryEntry> oneDicts;
        private final List<DictionaryEntry> twoDicts;

        private final List<TableLink> oneLinks;
        private final List<TableLink> twoLinks;

        /*
        private final List<TableMapping> oneMappings;
        private final List<TableMapping> twoMappings;
        */

        private final Map<String, DictionaryEntry> oneAllDicts;
        private final Map<String, DictionaryEntry> twoAllDicts;

        /**
         * Load content from version and prepare all items to process for changes, in 3 layers
         *
         * @param queryGenerator for column data building
         * @param one            dic version "left"
         * @param two            dic version "right"
         */
        DomainChangesBuilder(ManagedQueriesGenerator queryGenerator, Version one, Version two) {

            this.queryGenerator = queryGenerator;

            // 1st layer : the domains
            this.oneDomains = readDomains(one);
            this.twoDomains = readDomains(two);

            // 2nd layer : the tables
            this.oneDicts = readDict(one);
            this.twoDicts = readDict(two);

            // 3rd layer : column data (needs links and mappings)
            this.oneLinks = readLinks(one);
            this.twoLinks = readLinks(two);

            /*
            this.oneMappings = readMappings(one);
            this.twoMappings = readMappings(two);
            */

            // All referenced dicts for 2 version for column link data building
            this.oneAllDicts = this.oneDicts.stream().collect(Collectors.toMap(DictionaryEntry::getTableName, d -> d));
            this.twoAllDicts = this.twoDicts.stream().collect(Collectors.toMap(DictionaryEntry::getTableName, d -> d));
        }

        /**
         * Generation of changes from initialized layer data
         *
         * @return identified changes
         */
        List<DomainChanges> generateChanges() {
            return detectChanges(
                    this.oneDomains,
                    this.twoDomains,
                    FunctionalDomain::getUuid,
                    diffDomainChange(),
                    diffDomainDelete(),
                    diffDomainAdd()
            );
        }

        /* ################### 1st layer processes : the domains #################### */

        private BiFunction<FunctionalDomain, FunctionalDomain, DomainChanges> diffDomainChange() {

            return (d1, d2) -> {

                DomainChanges c = new DomainChanges();
                c.setName(d1.getName());

                c.setTableChanges(detectChanges(
                        filterForId(d1.getUuid(), this.oneDicts, t -> t.getDomain().getUuid()),
                        filterForId(d2.getUuid(), this.twoDicts, t -> t.getDomain().getUuid()),
                        DictionaryEntry::getUuid,
                        diffTableChange(),
                        diffTableDelete(),
                        diffTableAdd()
                ));

                // Basic search for diffs (only from tables)
                boolean unchanged = c.getTableChanges().stream().allMatch(t -> t.getChangeType() == ChangeType.UNCHANGED);

                c.setChangeType(unchanged ? ChangeType.UNCHANGED : ChangeType.MODIFIED);

                return c;
            };
        }

        private Function<FunctionalDomain, DomainChanges> diffDomainDelete() {

            return (d1) -> {

                DomainChanges c = new DomainChanges();
                c.setName(d1.getName());
                c.setChangeType(ChangeType.REMOVED);
                return c;
            };
        }

        private Function<FunctionalDomain, DomainChanges> diffDomainAdd() {

            return (d2) -> {

                DomainChanges c = new DomainChanges();
                c.setName(d2.getName());
                c.setChangeType(ChangeType.ADDED);

                c.setTableChanges(
                        filterForId(d2.getUuid(), this.twoDicts, t -> t.getDomain().getUuid())
                                .stream()
                                .map(diffTableAdd())
                                .collect(Collectors.toList()));

                return c;
            };
        }

        /* ################### 2nd layer processes : the tables #################### */

        private BiFunction<DictionaryEntry, DictionaryEntry, DictionaryTableChanges> diffTableChange() {

            return (t1, t2) -> {

                List<TableLink> d1Links = filterForId(t1.getUuid(), oneLinks, t -> t.getDictionaryEntry().getUuid());
                List<TableLink> d2Links = filterForId(t2.getUuid(), twoLinks, t -> t.getDictionaryEntry().getUuid());

                DictionaryTableChanges c = new DictionaryTableChanges();

                c.setName(t1.getParameterName());
                c.setFilter(t1.getWhereClause());
                c.setTableName(t1.getTableName());

                c.setNameChange(t2.getParameterName());
                c.setFilterChange(t2.getWhereClause());
                c.setTableNameChange(t2.getTableName());

                c.setColumnChanges(detectChanges(
                        getTableColumns(t1, d1Links, this.oneAllDicts),
                        getTableColumns(t2, d2Links, this.twoAllDicts),
                        ColumnEditData::getName,
                        diffColumnChange(),
                        diffColumnDelete(),
                        diffColumnAdd()
                ));

                // Basic search for diffs
                boolean unchanged = Objects.equals(c.getName(), c.getNameChange())
                        && Objects.equals(c.getFilter(), c.getFilterChange())
                        && Objects.equals(c.getTableName(), c.getTableNameChange())
                        && c.getColumnChanges().stream().allMatch(l -> l.getChangeType() == ChangeType.UNCHANGED);

                c.setChangeType(unchanged ? ChangeType.UNCHANGED : ChangeType.MODIFIED);

                return c;
            };
        }

        private Function<DictionaryEntry, DictionaryTableChanges> diffTableDelete() {

            return (t1) -> {

                DictionaryTableChanges c = new DictionaryTableChanges();
                c.setName(t1.getParameterName());
                c.setChangeType(ChangeType.REMOVED);
                return c;
            };
        }

        private Function<DictionaryEntry, DictionaryTableChanges> diffTableAdd() {

            return (t2) -> {

                List<TableLink> d2Links = filterForId(t2.getUuid(), this.twoLinks, t -> t.getDictionaryEntry().getUuid());

                DictionaryTableChanges c = new DictionaryTableChanges();
                c.setName(t2.getParameterName());
                c.setFilter(t2.getWhereClause());
                c.setTableName(t2.getTableName());

                c.setChangeType(ChangeType.ADDED);

                c.setColumnChanges(
                        getTableColumns(t2, d2Links, this.twoAllDicts)
                                .stream()
                                .map(diffColumnAdd())
                                .collect(Collectors.toList()));

                return c;
            };
        }

        /* ################### 3rd layer processes : the columns #################### */

        private BiFunction<ColumnEditData, ColumnEditData, ColumnChanges> diffColumnChange() {

            return (c1, c2) -> {

                ColumnChanges c = new ColumnChanges();
                c.setName(c1.getName());

                if (c1.getType() != null) {
                    c.setType(c1.getType().getDisplayName());
                }

                c.setKey(c1.isKey());

                if (c1.getForeignKeyTable() != null) {
                    c.setLink(c1.getForeignKeyTable() + ":" + c1.getForeignKeyColumn());
                }

                if (c2.getType() != null) {
                    c.setTypeChange(c2.getType().getDisplayName());
                }

                c.setKeyChange(c2.isKey());

                if (c2.getForeignKeyTable() != null) {
                    c.setLinkChange(c2.getForeignKeyTable() + ":" + c2.getForeignKeyColumn());
                }

                // Basic search for diffs
                boolean unchanged = Objects.equals(c.getLink(), c.getLinkChange())
                        && Objects.equals(c.getType(), c.getTypeChange())
                        && c.isKey() == c.isKeyChange();

                c.setChangeType(unchanged ? ChangeType.UNCHANGED : ChangeType.MODIFIED);

                return c;
            };
        }

        private Function<ColumnEditData, ColumnChanges> diffColumnDelete() {

            return (c1) -> {

                ColumnChanges c = new ColumnChanges();
                c.setName(c1.getName());

                if (c1.getType() != null) {
                    c.setType(c1.getType().getDisplayName());
                }

                if (c1.getForeignKeyColumn() != null) {
                    c.setLink(c1.getForeignKeyTable() + ":" + c1.getForeignKeyColumn());
                }

                c.setChangeType(ChangeType.REMOVED);
                return c;
            };
        }

        private Function<ColumnEditData, ColumnChanges> diffColumnAdd() {

            return (c2) -> {

                ColumnChanges c = new ColumnChanges();
                c.setName(c2.getName());

                if (c2.getType() != null) {
                    c.setType(c2.getType().getDisplayName());
                }

                if (c2.getForeignKeyColumn() != null) {
                    c.setLink(c2.getForeignKeyTable() + ":" + c2.getForeignKeyColumn());
                }

                c.setChangeType(ChangeType.ADDED);
                return c;
            };
        }

        /**
         * Generator of column data which can be processed for change search.
         *
         * @param entry    current table entry
         * @param dicLinks current table links
         * @param allDicts all tables for link reference building
         * @return all the identified columns as change-search compliant entries
         */
        private List<ColumnEditData> getTableColumns(DictionaryEntry entry, List<TableLink> dicLinks, Map<String, DictionaryEntry> allDicts) {

            // Need select clause as a list
            Collection<String> selecteds = StringUtils.hasText(entry.getSelectClause())
                    ? this.queryGenerator.splitSelectClause(entry.getSelectClause(), dicLinks, allDicts)
                    : Collections.emptyList();

            List<String> keyNames = entry.keyNames().collect(Collectors.toList());

            // Keep links
            Map<String, LinkUpdateFollow> mappedLinks = dicLinks.stream().flatMap(
                    l -> LinkUpdateFollow.flatMapFromColumn(l, l.columnFroms()))
                    .collect(Collectors.toMap(LinkUpdateFollow::getColumn, v -> v));

            // Avoid immutable lists
            Collection<String> editableSelecteds = new ArrayList<>(selecteds);

            // On missing, add key column(s)
            if (!editableSelecteds.contains(entry.getKeyName())) {
                editableSelecteds.addAll(keyNames);
            }

            return editableSelecteds.stream()
                    .map(c -> ColumnEditData.fromSelecteds(c, keyNames, entry.keyTypes().collect(Collectors.toList()), mappedLinks.get(c)))
                    .sorted()
                    .collect(Collectors.toList());
        }

        /* ######################## Generic filtering / diff search processes ####################### */

        private static <P, C> List<C> filterForId(P parentId, List<C> childs, Function<C, P> identityAccess) {
            return childs.stream().filter(c -> identityAccess.apply(c).equals(parentId)).collect(Collectors.toList());
        }

        private static <C extends Changes, T> List<C> detectChanges(
                List<T> ones,
                List<T> twos,
                Function<T, Serializable> identityAccess,
                BiFunction<T, T, C> diffGen,
                Function<T, C> delGen,
                Function<T, C> createGen) {

            List<C> changes = new ArrayList<>();

            // Added ones
            ones.stream().filter(o -> twos.stream().noneMatch(t -> identityAccess.apply(t).equals(identityAccess.apply(o)))).map(createGen).forEach(changes::add);

            // Use a pair for removed or updated
            var associateds = twos.stream().map(t -> Pair.of(t, ones.stream().filter(o -> identityAccess.apply(o).equals(identityAccess.apply(t))).findFirst())).collect(Collectors.toList());
            associateds.stream().filter(p -> !p.getSecond().isPresent()).map(p -> delGen.apply(p.getFirst())).forEach(changes::add);
            associateds.stream().filter(p -> p.getSecond().isPresent()).map(p -> diffGen.apply(p.getFirst(), p.getSecond().get())).forEach(changes::add);

            return changes;
        }

        /* ######################## Version content reading processes ####################### */

        private static List<FunctionalDomain> readDomains(Version version) {

            if (StringUtils.isEmpty(version.getDomainsContent())) {
                return Collections.emptyList();
            }

            return Stream.of(version.getDomainsContent().split(VERSION_CONTENT_ITEM_SEP)).map(s -> {
                FunctionalDomain item = new FunctionalDomain();
                item.deserialize(s);
                return item;
            }).collect(Collectors.toList());
        }

        private static List<DictionaryEntry> readDict(Version version) {

            if (StringUtils.isEmpty(version.getDictionaryContent())) {
                return Collections.emptyList();
            }

            return Stream.of(version.getDictionaryContent().split(VERSION_CONTENT_ITEM_SEP)).map(s -> {
                DictionaryEntry item = new DictionaryEntry();
                item.deserialize(s);
                return item;
            }).collect(Collectors.toList());
        }

        private static List<TableLink> readLinks(Version version) {

            if (StringUtils.isEmpty(version.getLinksContent())) {
                return Collections.emptyList();
            }

            return Stream.of(version.getLinksContent().split(VERSION_CONTENT_ITEM_SEP)).map(s -> {
                TableLink item = new TableLink();
                item.deserialize(s);
                return item;
            }).collect(Collectors.toList());
        }

        private static List<TableMapping> readMappings(Version version) {

            if (StringUtils.isEmpty(version.getMappingsContent())) {
                return Collections.emptyList();
            }
            return Stream.of(version.getMappingsContent().split(VERSION_CONTENT_ITEM_SEP)).map(s -> {
                TableMapping item = new TableMapping();
                item.deserialize(s);
                return item;
            }).collect(Collectors.toList());
        }
    }


}
