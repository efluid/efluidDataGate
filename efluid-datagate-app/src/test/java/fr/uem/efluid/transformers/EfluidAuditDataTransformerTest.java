package fr.uem.efluid.transformers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.tools.diff.ManagedQueriesGenerator;
import fr.uem.efluid.tools.diff.ManagedValueConverter;
import fr.uem.efluid.transformers.EfluidAuditDataTransformer.Config;
import fr.uem.efluid.utils.DataGenerationUtils;
import fr.uem.efluid.utils.DatasourceUtils;
import fr.uem.efluid.utils.FormatUtils;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class EfluidAuditDataTransformerTest {

    private static final String CURRENT_DATE = FormatUtils.format(LocalDateTime.of(2020, 06, 12, 22, 14));

    private final ManagedQueriesGenerator queryGenerator = new ManagedQueriesGenerator(rules());
    private final ManagedValueConverter converter = new ManagedValueConverter();
    private final TransformerValueProvider provider = new TransformerValueProvider(this.queryGenerator) {
        @Override
        public String getFormatedCurrentTime() {
            return CURRENT_DATE;
        }
    };
    private final EfluidAuditDataTransformer transformer = new EfluidAuditDataTransformer(this.converter, this.provider);

    private static final LocalDateTime OLD = LocalDateTime.of(2015, 12, 25, 15, 25, 46);

    @Test
    public void testVariousApplyOnAllDictionnary() {

        assertThat(this.transformer.isApplyOnDictionaryEntry(table("TANY", "COL1"), config(
                "    {"
                        + "  \"tablePattern\":\".*\""
                        + "}"
        ))).isTrue();

        assertThat(this.transformer.isApplyOnDictionaryEntry(table("TOTHER", "COL1", "COL2"), config(
                "    {"
                        + "  \"tablePattern\":\".*\""
                        + "}"
        ))).isTrue();

        assertThat(this.transformer.isApplyOnDictionaryEntry(table("TOTHER", "COL1", "COL2"), config(
                "    {"
                        + "  \"tablePattern\":\"TOTH.*\""
                        + "}"
        ))).isTrue();

        assertThat(this.transformer.isApplyOnDictionaryEntry(table("TOTHER", "COL1", "COL2"), config(
                "    {"
                        + "  \"tablePattern\":\"TOTHER\""
                        + "}"
        ))).isTrue();

    }

    @Test
    public void testVariousNotApplyOnAllDictionnary() {

        assertThat(this.transformer.isApplyOnDictionaryEntry(table("TOTHER", "COL1", "COL2"), config(
                "    {"
                        + "  \"tablePattern\":\"TAN.*\""
                        + "}"
        ))).isFalse();

        assertThat(this.transformer.isApplyOnDictionaryEntry(table("TOTHER", "COL1", "COL2"), config(
                "    {"
                        + "  \"tablePattern\":\"TANY\""
                        + "}"
        ))).isFalse();
    }

    @Test
    public void testApplyOnLinesByIndex() {

        List<? extends PreparedIndexEntry> diff = diff(
                l("ID0", s("COL1", "test1"), d("DATE1", OLD), d("DATE2", OLD), s("ACT1", "test"), s("ACT2", "test"), s("ACT3", "test")),
                l("ID1", s("COL1", "test2"), d("DATE1", OLD), d("DATE2", OLD), s("ACT1", "test"), s("ACT2", "test"), s("ACT3", "test")),
                l("ID2", s("COL1", "test3"), d("DATE1", OLD), d("DATE2", OLD), s("ACT1", "test"), s("ACT2", "test"), s("ACT3", "test")),
                l("ID3", s("COL1", "test4"), d("DATE1", OLD), d("DATE2", OLD), s("ACT1", "test"), s("ACT2", "test"), s("ACT3", "test"))
        );

        this.transformer.transform(
                table("TOTHER", "COL1", "DATE1", "DATE2", "ACT1", "ACT2", "ACT3"),
                config(
                        "    {"
                                + "  \"tablePattern\":\"TOTHER\","
                                + "  \"appliedKeyPatterns\":[\"ID1\"],"
                                + "  \"appliedValueFilterPatterns\":{\"COL1\":\".*\"},"
                                + "  \"dateUpdates\":{\"DATE1\":{\"value\":\"2022-12-25\",\"onActions\": [\"ADD\"]}, \"DATE2\":{\"value\":\"2025-12-25\",\"onActions\": [\"ADD\"]}},"
                                + "  \"actorUpdates\":{\"ACT1\":{\"value\":\"bob\",\"onActions\": [\"ADD\"]}, \"ACT2\":{\"value\":\"toto\",\"onActions\": [\"ADD\"]}}"
                                + "}"
                ), diff
        );

        // Only ID1 is changed
        check(diff, 0).isEqualTo("COL1:'test1', DATE1:2015-12-25 15:25:46, DATE2:2015-12-25 15:25:46, ACT1:'test', ACT2:'test', ACT3:'test'");
        check(diff, 1).isEqualTo("COL1:'test2', DATE1:2022-12-25 00:00:00, DATE2:2025-12-25 00:00:00, ACT1:'bob', ACT2:'toto', ACT3:'test'");
        check(diff, 2).isEqualTo("COL1:'test3', DATE1:2015-12-25 15:25:46, DATE2:2015-12-25 15:25:46, ACT1:'test', ACT2:'test', ACT3:'test'");
        check(diff, 3).isEqualTo("COL1:'test4', DATE1:2015-12-25 15:25:46, DATE2:2015-12-25 15:25:46, ACT1:'test', ACT2:'test', ACT3:'test'");
    }

    @Test
    public void testApplyOnLinesByIndexAndFilter() {

        List<? extends PreparedIndexEntry> diff = diff(
                l("ID0", s("COL1", "test1"), d("DATE1", OLD), d("DATE2", OLD), s("ACT1", "test"), s("ACT2", "test"), s("ACT3", "test")),
                l("ID1", s("COL1", "test2"), d("DATE1", OLD), d("DATE2", OLD), s("ACT1", "test"), s("ACT2", "test"), s("ACT3", "test")),
                l("ID2", s("COL1", "test3"), d("DATE1", OLD), d("DATE2", OLD), s("ACT1", "test"), s("ACT2", "test"), s("ACT3", "test")),
                l("ID3", s("COL1", "test4"), d("DATE1", OLD), d("DATE2", OLD), s("ACT1", "test"), s("ACT2", "test"), s("ACT3", "test"))
        );

        this.transformer.transform(
                table("TOTHER", "COL1", "DATE1", "DATE2", "ACT1", "ACT2", "ACT3"),
                config(
                        "    {"
                                + "  \"tablePattern\":\"TOTHER\","
                                + "  \"appliedKeyPatterns\":[\"ID.*\"],"
                                + "  \"appliedValueFilterPatterns\":{\"COL1\":\".*st3\"},"
                                + "  \"dateUpdates\":{\"DATE1\":{\"value\":\"2022-12-25\",\"onActions\": [\"ADD\"]}, \"DATE2\":{\"value\":\"2025-12-25\",\"onActions\": [\"ADD\"]}},"
                                + "  \"actorUpdates\":{\"ACT1\":{\"value\":\"bob\",\"onActions\": [\"ADD\"]}, \"ACT2\":{\"value\":\"toto\",\"onActions\": [\"ADD\"]}}"
                                + "}"
                ), diff
        );

        // Only ID2 is changed
        check(diff, 0).isEqualTo("COL1:'test1', DATE1:2015-12-25 15:25:46, DATE2:2015-12-25 15:25:46, ACT1:'test', ACT2:'test', ACT3:'test'");
        check(diff, 1).isEqualTo("COL1:'test2', DATE1:2015-12-25 15:25:46, DATE2:2015-12-25 15:25:46, ACT1:'test', ACT2:'test', ACT3:'test'");
        check(diff, 2).isEqualTo("COL1:'test3', DATE1:2022-12-25 00:00:00, DATE2:2025-12-25 00:00:00, ACT1:'bob', ACT2:'toto', ACT3:'test'");
        check(diff, 3).isEqualTo("COL1:'test4', DATE1:2015-12-25 15:25:46, DATE2:2015-12-25 15:25:46, ACT1:'test', ACT2:'test', ACT3:'test'");
    }

    @Test
    public void testApplyOnLinesWithMissingValues() {

        List<? extends PreparedIndexEntry> diff = diff(
                l("ID0", s("COL1", "test1"), s("ACT2", "test"), s("ACT3", "test")),
                l("ID1", s("COL1", "test2"), s("ACT2", "test"), s("ACT3", "test")),
                l("ID2", s("COL1", "test3"), s("ACT2", "test"), s("ACT3", "test")),
                l("ID3", s("COL1", "test4"), s("ACT2", "test"), s("ACT3", "test"))
        );

        this.transformer.transform(
                table("TOTHER", "COL1", "DATE1", "DATE2", "ACT1", "ACT2", "ACT3"),
                config(
                        "    {"
                                + "  \"tablePattern\":\"TOTHER\","
                                + "  \"appliedKeyPatterns\":[\"ID.*\"],"
                                + "  \"appliedValueFilterPatterns\":{\"COL1\":\".*st[2,3]\"},"
                                + "  \"dateUpdates\":{\"DATE1\":{\"value\":\"2022-12-25\",\"onActions\": [\"ADD\"]}, \"DATE2\":{\"value\":\"2025-12-25\",\"onActions\": [\"ADD\"]}},"
                                + "  \"actorUpdates\":{\"ACT1\":{\"value\":\"bob\",\"onActions\": [\"ADD\"]}, \"ACT2\":{\"value\":\"toto\",\"onActions\": [\"ADD\"]}}"
                                + "}"
                ), diff
        );

        // Only ID2 is changed
        check(diff, 0).isEqualTo("COL1:'test1', ACT2:'test', ACT3:'test'");
        check(diff, 1).isEqualTo("COL1:'test2', ACT2:'toto', ACT3:'test', DATE1:2022-12-25 00:00:00, DATE2:2025-12-25 00:00:00, ACT1:'bob'");
        check(diff, 2).isEqualTo("COL1:'test3', ACT2:'toto', ACT3:'test', DATE1:2022-12-25 00:00:00, DATE2:2025-12-25 00:00:00, ACT1:'bob'");
        check(diff, 3).isEqualTo("COL1:'test4', ACT2:'test', ACT3:'test'");
    }

    @Test
    public void testApplyOnLinesByIndexNoFilter() {

        List<? extends PreparedIndexEntry> diff = diff(
                l("ID0", s("COL1", "test1"), d("DATE1", OLD), d("DATE2", OLD), s("ACT1", "test"), s("ACT2", "test"), s("ACT3", "test")),
                l("ID1", s("COL1", "test2"), d("DATE1", OLD), d("DATE2", OLD), s("ACT1", "test"), s("ACT2", "test"), s("ACT3", "test")),
                l("ID2", s("COL1", "test3"), d("DATE1", OLD), d("DATE2", OLD), s("ACT1", "test"), s("ACT2", "test"), s("ACT3", "test")),
                l("ID3", s("COL1", "test4"), d("DATE1", OLD), d("DATE2", OLD), s("ACT1", "test"), s("ACT2", "test"), s("ACT3", "test"))
        );

        this.transformer.transform(
                table("TOTHER", "COL1", "DATE1", "DATE2", "ACT1", "ACT2", "ACT3"),
                config(
                        "    {"
                                + "  \"tablePattern\":\"TOTHER\","
                                + "  \"appliedKeyPatterns\":[\"ID.*\"],"
                                + "  \"appliedValueFilterPatterns\":{},"
                                + "  \"dateUpdates\":{\"DATE1\":{\"value\":\"2022-12-25\",\"onActions\": [\"ADD\",\"REMOVE\",\"UPDATE\"]}, \"DATE2\":{\"value\":\"2025-12-25\",\"onActions\": [\"ADD\",\"REMOVE\",\"UPDATE\"]}},"
                                + "  \"actorUpdates\":{\"ACT1\":{\"value\":\"bob\",\"onActions\": [\"ADD\",\"REMOVE\",\"UPDATE\"]}, \"ACT2\":{\"value\":\"toto\",\"onActions\": [\"ADD\",\"REMOVE\",\"UPDATE\"]}}"
                                + "}"
                ), diff
        );

        // All are changed
        check(diff, 0).isEqualTo("COL1:'test1', DATE1:2022-12-25 00:00:00, DATE2:2025-12-25 00:00:00, ACT1:'bob', ACT2:'toto', ACT3:'test'");
        check(diff, 1).isEqualTo("COL1:'test2', DATE1:2022-12-25 00:00:00, DATE2:2025-12-25 00:00:00, ACT1:'bob', ACT2:'toto', ACT3:'test'");
        check(diff, 2).isEqualTo("COL1:'test3', DATE1:2022-12-25 00:00:00, DATE2:2025-12-25 00:00:00, ACT1:'bob', ACT2:'toto', ACT3:'test'");
        check(diff, 3).isEqualTo("COL1:'test4', DATE1:2022-12-25 00:00:00, DATE2:2025-12-25 00:00:00, ACT1:'bob', ACT2:'toto', ACT3:'test'");
    }

    @Test
    public void testApplyCombined() {

        List<? extends PreparedIndexEntry> diff = diff(
                l("1", s("VALUE", "INIT_1"), s("ETAT_OBJET", "TODO_DELETED"), d("DATE_SUPPRESSION", OLD), d("DATE_MODIFICATION", OLD), d("DATE_CREATION", OLD), s("ACTEUR_SUPPRESSION", "admin_del_src"), s("ACTEUR_MODIFICATION", "admin_src"), s("ACTEUR_CREATION", "admin_src")),
                l("2", s("VALUE", "INIT_2"), s("ETAT_OBJET", "TODO_UPDATE"), d("DATE_SUPPRESSION", OLD), d("DATE_MODIFICATION", OLD), d("DATE_CREATION", OLD), s("ACTEUR_SUPPRESSION", "admin_del_src2"), s("ACTEUR_MODIFICATION", "admin_src2"), s("ACTEUR_CREATION", "admin_src2"))
        );

        this.transformer.transform(
                table("TOTHER", "VALUE", "ETAT_OBJET", "DATE_SUPPRESSION", "DATE_MODIFICATION", "DATE_CREATION", "ACTEUR_SUPPRESSION", "ACTEUR_MODIFICATION", "ACTEUR_CREATION"),
                config(
                        "  {" +
                                "  \"tablePattern\":\"T_EFLUID_TEST_AUDIT\"," +
                                "  \"appliedKeyPatterns\":[\".*\"]," +
                                "  \"appliedValueFilterPatterns\":{\"ETAT_OBJET\":\"TODO.*\"}," +
                                "  \"dateUpdates\":{\"DATE_.*\":{\"value\":\"2020-05-11\",\"onActions\": [\"ADD\"]}}," +
                                "  \"actorUpdates\":{\"ACTEUR_.*\":{\"value\":\"evt 154654\",\"onActions\": [\"ADD\"]}}" +
                                "}"
                ), diff
        );

        // All are changed
        check(diff, 0).isEqualTo("VALUE:'INIT_1', ETAT_OBJET:'TODO_DELETED', DATE_SUPPRESSION:2020-05-11 00:00:00, DATE_MODIFICATION:2020-05-11 00:00:00, DATE_CREATION:2020-05-11 00:00:00, ACTEUR_SUPPRESSION:'evt 154654', ACTEUR_MODIFICATION:'evt 154654', ACTEUR_CREATION:'evt 154654'");
        check(diff, 1).isEqualTo("VALUE:'INIT_2', ETAT_OBJET:'TODO_UPDATE', DATE_SUPPRESSION:2020-05-11 00:00:00, DATE_MODIFICATION:2020-05-11 00:00:00, DATE_CREATION:2020-05-11 00:00:00, ACTEUR_SUPPRESSION:'evt 154654', ACTEUR_MODIFICATION:'evt 154654', ACTEUR_CREATION:'evt 154654'");
    }

    @Test
    public void testApplyCurrentDate() {

        List<? extends PreparedIndexEntry> diff = diff(
                l("1", s("VALUE", "INIT_1"), s("ETAT_OBJET", "TODO_DELETED"), d("DATE_SUPPRESSION", OLD), d("DATE_MODIFICATION", OLD), d("DATE_CREATION", OLD), s("ACTEUR_SUPPRESSION", "admin_del_src"), s("ACTEUR_MODIFICATION", "admin_src"), s("ACTEUR_CREATION", "admin_src")),
                l("2", s("VALUE", "INIT_2"), s("ETAT_OBJET", "TODO_UPDATE"), d("DATE_SUPPRESSION", OLD), d("DATE_MODIFICATION", OLD), d("DATE_CREATION", OLD), s("ACTEUR_SUPPRESSION", "admin_del_src2"), s("ACTEUR_MODIFICATION", "admin_src2"), s("ACTEUR_CREATION", "admin_src2"))
        );

        this.transformer.transform(
                table("TOTHER", "VALUE", "ETAT_OBJET", "DATE_SUPPRESSION", "DATE_MODIFICATION", "DATE_CREATION", "ACTEUR_SUPPRESSION", "ACTEUR_MODIFICATION", "ACTEUR_CREATION"),
                config(
                        "  {" +
                                "  \"tablePattern\":\"T_EFLUID_TEST_AUDIT\"," +
                                "  \"appliedKeyPatterns\":[\".*\"]," +
                                "  \"dateUpdates\":{\"DATE_.*\":{\"value\":\"current_date\",\"onActions\": [\"ADD\"]}}," +
                                "  \"actorUpdates\":{\"ACTEUR_.*\":{\"value\":\"evt 154654\",\"onActions\": [\"ADD\"]}}" +
                                "}"
                ), diff
        );

        // All are changed
        check(diff, 0).isEqualTo("VALUE:'INIT_1', ETAT_OBJET:'TODO_DELETED', DATE_SUPPRESSION:" + CURRENT_DATE + ", DATE_MODIFICATION:" + CURRENT_DATE + ", DATE_CREATION:" + CURRENT_DATE + ", ACTEUR_SUPPRESSION:'evt 154654', ACTEUR_MODIFICATION:'evt 154654', ACTEUR_CREATION:'evt 154654'");
        check(diff, 1).isEqualTo("VALUE:'INIT_2', ETAT_OBJET:'TODO_UPDATE', DATE_SUPPRESSION:" + CURRENT_DATE + ", DATE_MODIFICATION:" + CURRENT_DATE + ", DATE_CREATION:" + CURRENT_DATE + ", ACTEUR_SUPPRESSION:'evt 154654', ACTEUR_MODIFICATION:'evt 154654', ACTEUR_CREATION:'evt 154654'");
    }

    private AbstractStringAssert<?> check(List<? extends PreparedIndexEntry> transformed, int index) {
        return assertThat(this.converter.convertToHrPayload(transformed.get(index).getPayload(), null));
    }

    private Config config(String json) {

        try {
            return new ObjectMapper().readValue(json, Config.class);
        } catch (IOException e) {
            throw new AssertionError("Failed json config : not valid", e);
        }

    }


    private static DictionaryEntry table(String name, String... columns) {
        return DataGenerationUtils.entry("Modèle de compteur", null, Stream.of(columns).map(c -> String.format("cur.\"%s\"", c)).collect(Collectors.joining(", ")),
                name, "\"ACTIF\"=true", "CODE_SERIE", ColumnType.STRING);
    }

    private PreparedIndexEntry l(String key, Pair<String, Object>... contentValues) {

        PreparedIndexEntry entry = new PreparedIndexEntry();

        entry.setAction(IndexAction.ADD);
        entry.setKeyValue(key);

        LinkedHashMap<String, Object> content = new LinkedHashMap<>();

        for (Pair<String, Object> contentValue : contentValues) {
            content.put(contentValue.getFirst(), contentValue.getSecond());
        }

        entry.setPayload(this.converter.convertToExtractedValue(content));

        return entry;
    }

    private static List<? extends PreparedIndexEntry> diff(PreparedIndexEntry... entries) {
        return Stream.of(entries).collect(Collectors.toList());
    }

    private static Pair<String, Object> s(String name, String obj) {
        return Pair.of(name, obj);
    }

    private static Pair<String, Object> d(String name, LocalDateTime obj) {
        return Pair.of(name, obj);
    }

    private static DatasourceUtils.CustomQueryGenerationRules rules() {
        DatasourceUtils.CustomQueryGenerationRules rules = new DatasourceUtils.CustomQueryGenerationRules();

        rules.setColumnNamesProtected(true);
        rules.setDatabaseDateFormat("dd-MM-yyyy HH:mm:ss");
        rules.setTableNamesProtected(true);

        return rules;
    }
}
