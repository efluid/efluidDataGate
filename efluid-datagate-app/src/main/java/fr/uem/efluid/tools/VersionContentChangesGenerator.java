package fr.uem.efluid.tools;

import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.metas.TableDescription;
import fr.uem.efluid.services.types.DictionaryEntryEditData;
import fr.uem.efluid.services.types.LinkUpdateFollow;
import fr.uem.efluid.services.types.VersionCompare;
import fr.uem.efluid.services.types.VersionCompare.ChangeType;
import fr.uem.efluid.services.types.VersionCompare.Changes;
import fr.uem.efluid.services.types.VersionCompare.DictionaryTableChanges;
import fr.uem.efluid.services.types.VersionCompare.DomainChanges;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VersionContentChangesGenerator {

    public static final String VERSION_CONTENT_ITEM_SEP = ";";

    @Autowired
    private ManagedQueriesGenerator queryGenerator;

    public List<VersionCompare.DomainChanges> generateChanges(Version one, Version two) {

        List<FunctionalDomain> oneDomains = readDomains(one);
        List<FunctionalDomain> twoDomains = readDomains(two);

        List<DictionaryEntry> oneDicts = readDict(one);
        List<DictionaryEntry> twoDicts = readDict(two);

        List<TableLink> oneLinks = readLinks(one);
        List<TableLink> twoLinks = readLinks(two);

        List<TableMapping> oneMappings = readMappings(one);
        List<TableMapping> twoMappings = readMappings(two);

        return detectChanges(
                oneDomains,
                twoDomains,
                FunctionalDomain::getUuid,
                diffDomainChange(oneDicts, twoDicts, oneLinks, twoLinks, oneMappings, twoMappings),
                diffDomainDelete(),
                diffDomainAdd(twoDicts, twoLinks, twoMappings)
        );
    }

    private static BiFunction<FunctionalDomain, FunctionalDomain, DomainChanges> diffDomainChange(
            List<DictionaryEntry> oneDicts,
            List<DictionaryEntry> twoDicts,
            List<TableLink> oneLinks,
            List<TableLink> twoLinks,
            List<TableMapping> oneMappings,
            List<TableMapping> twoMappings) {

        return (d1, d2) -> {

            List<DictionaryEntry> d1Dicts = filterForId(d1.getUuid(), oneDicts, t -> t.getDomain().getUuid());
            List<DictionaryEntry> d2Dicts = filterForId(d2.getUuid(), twoDicts, t -> t.getDomain().getUuid());

            DomainChanges c = new DomainChanges();
            c.setName(d1.getName());
            c.setChangeType(ChangeType.MODIFIED);

            c.setTableChanges(detectChanges(
                    d1Dicts,
                    d2Dicts,
                    DictionaryEntry::getUuid,
                    diffTableChange(oneLinks, twoLinks, oneMappings, twoMappings),
                    diffDomainDelete(),
                    diffDomainAdd(twoDicts, twoLinks, twoMappings)
            );
            return c;
        };
    }

    private static Function<FunctionalDomain, DomainChanges> diffDomainDelete() {

        return (d1) -> {

            DomainChanges c = new DomainChanges();
            c.setName(d1.getName());
            c.setChangeType(ChangeType.REMOVED);
            return c;
        };
    }

    private static Function<FunctionalDomain, DomainChanges> diffDomainAdd(
            List<DictionaryEntry> twoDicts,
            List<TableLink> twoLinks,
            List<TableMapping> twoMappings) {

        return (d2) -> {

            List<DictionaryEntry> d2Dicts = filterForId(d2.getUuid(), twoDicts, t -> t.getDomain().getUuid());

            DomainChanges c = new DomainChanges();
            c.setName(d2.getName());
            c.setChangeType(ChangeType.ADDED);
            return c;
        };
    }

    private static BiFunction<DictionaryEntry, DictionaryEntry, DictionaryTableChanges> diffTableChange(
            List<TableLink> oneLinks,
            List<TableLink> twoLinks,
            List<TableMapping> oneMappings,
            List<TableMapping> twoMappings) {

        return (t1, t2) -> {

            List<TableLink> d1Links = filterForId(t1.getUuid(), oneLinks, t -> t.getDictionaryEntry().getUuid());
            List<TableLink> d2Links = filterForId(t2.getUuid(), twoLinks, t -> t.getDictionaryEntry().getUuid());

            DictionaryTableChanges c = new DictionaryTableChanges();
            c.setName(t1.getParameterName());
            c.setChangeType(ChangeType.MODIFIED);
            return c;
        };
    }

    private static Function<DictionaryEntry, DictionaryTableChanges> diffTableDelete() {

        return (t1) -> {

            DictionaryTableChanges c = new DictionaryTableChanges();
            c.setName(t1.getParameterName());
            c.setChangeType(ChangeType.REMOVED);
            return c;
        };
    }

    private static Function<DictionaryEntry, DictionaryTableChanges> diffTableAdd(

            List<TableLink> twoLinks,
            List<TableMapping> twoMappings) {

        return (t2) -> {

            List<TableLink> d2Links = filterForId(t2.getUuid(), twoLinks, t -> t.getDictionaryEntry().getUuid());

            DictionaryTableChanges c = new DictionaryTableChanges();
            c.setName(t2.getParameterName());
            c.setChangeType(ChangeType.ADDED);
            return c;
        };
    }

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

        // Deleted ones
        ones.stream().filter(o -> twos.stream().noneMatch(t -> identityAccess.apply(t).equals(identityAccess.apply(o)))).map(delGen).forEach(changes::add);

        // Use a pair for new or update
        var associateds = twos.stream().map(t -> Pair.of(t, ones.stream().filter(o -> identityAccess.apply(o).equals(identityAccess.apply(t))).findFirst())).collect(Collectors.toList());
        associateds.stream().filter(p -> !p.getSecond().isPresent()).map(p -> createGen.apply(p.getFirst())).forEach(changes::add);
        associateds.stream().filter(p -> p.getSecond().isPresent()).map(p -> diffGen.apply(p.getFirst(), p.getSecond().get())).forEach(changes::add);

        return changes;
    }

    private static List<FunctionalDomain> readDomains(Version version) {
        return Stream.of(version.getDomainsContent().split(VERSION_CONTENT_ITEM_SEP)).map(s -> {
            FunctionalDomain item = new FunctionalDomain();
            item.deserialize(s);
            return item;
        }).collect(Collectors.toList());
    }

    private static List<DictionaryEntry> readDict(Version version) {
        return Stream.of(version.getDictionaryContent().split(VERSION_CONTENT_ITEM_SEP)).map(s -> {
            DictionaryEntry item = new DictionaryEntry();
            item.deserialize(s);
            return item;
        }).collect(Collectors.toList());
    }

    private static List<TableLink> readLinks(Version version) {
        return Stream.of(version.getLinksContent().split(VERSION_CONTENT_ITEM_SEP)).map(s -> {
            TableLink item = new TableLink();
            item.deserialize(s);
            return item;
        }).collect(Collectors.toList());
    }

    private static List<TableMapping> readMappings(Version version) {
        return Stream.of(version.getMappingsContent().split(VERSION_CONTENT_ITEM_SEP)).map(s -> {
            TableMapping item = new TableMapping();
            item.deserialize(s);
            return item;
        }).collect(Collectors.toList());
    }

    private Object getTableColumns(DictionaryEntry entry, List<TableLink> dicLinks, List<DictionaryEntry> allDicts){

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
                    .map(c -> DictionaryEntryEditData.ColumnEditData.fromSelecteds(c, keyNames, entry.keyTypes().collect(Collectors.toList()), mappedLinks.get(c)))
                    .sorted()
                    .collect(Collectors.toList());
    }
}
