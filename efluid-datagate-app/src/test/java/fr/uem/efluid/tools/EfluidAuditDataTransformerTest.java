package fr.uem.efluid.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.tools.EfluidAuditDataTransformer.Config;
import fr.uem.efluid.utils.DataGenerationUtils;
import fr.uem.efluid.utils.FormatUtils;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.Test;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class EfluidAuditDataTransformerTest {

    private static final String CURRENT_DATE = FormatUtils.format(LocalDateTime.of(2020, 06, 12, 22, 14));

    private ManagedValueConverter converter = new ManagedValueConverter();
    private TransformerValueProvider provider =   new TransformerValueProvider() {
        @Override
        public String getFormatedCurrentTime() {
            return CURRENT_DATE;
        }
    };
    private EfluidAuditDataTransformer transformer = new EfluidAuditDataTransformer(this.converter, this.provider);

    private static final String OLD = FormatUtils.format(LocalDateTime.of(2015, 12, 25, 15, 25, 46));

    @Test
    public void testVariousApplyOnAllDictionnary() {

        assertThat(this.transformer.isApplyOnDictionaryEntry(table("TANY"), config(
                "    {"
                        + "  \"tablePattern\":\".*\""
                        + "}"
        ))).isTrue();

        assertThat(this.transformer.isApplyOnDictionaryEntry(table("TOTHER"), config(
                "    {"
                        + "  \"tablePattern\":\".*\""
                        + "}"
        ))).isTrue();

        assertThat(this.transformer.isApplyOnDictionaryEntry(table("TOTHER"), config(
                "    {"
                        + "  \"tablePattern\":\"TOTH.*\""
                        + "}"
        ))).isTrue();

        assertThat(this.transformer.isApplyOnDictionaryEntry(table("TOTHER"), config(
                "    {"
                        + "  \"tablePattern\":\"TOTHER\""
                        + "}"
        ))).isTrue();

    }

    @Test
    public void testVariousNotApplyOnAllDictionnary() {

        assertThat(this.transformer.isApplyOnDictionaryEntry(table("TOTHER"), config(
                "    {"
                        + "  \"tablePattern\":\"TAN.*\""
                        + "}"
        ))).isFalse();

        assertThat(this.transformer.isApplyOnDictionaryEntry(table("TOTHER"), config(
                "    {"
                        + "  \"tablePattern\":\"TANY\""
                        + "}"
        ))).isFalse();
    }

    @Test
    public void testApplyOnLinesByIndex() {

        List<? extends PreparedIndexEntry> diff = diff(
                l("ID0", p("COL1", "test1"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test")),
                l("ID1", p("COL1", "test2"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test")),
                l("ID2", p("COL1", "test3"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test")),
                l("ID3", p("COL1", "test4"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test"))
        );

        this.transformer.transform(
                table("TOTHER"),
                config(
                        "    {"
                                + "  \"tablePattern\":\"TOTHER\","
                                + "  \"appliedKeyPatterns\":[\"ID1\"],"
                                + "  \"appliedValueFilterPatterns\":{\"COL1\":\".*\"},"
                                + "  \"dateUpdates\":{\"DATE1\":\"2022-12-25\", \"DATE2\":\"2025-12-25\"},"
                                + "  \"actorUpdates\":{\"ACT1\":\"bob\", \"ACT2\":\"toto\"}"
                                + "}"
                ), diff
        );

        // Only ID1 is changed
        check(diff, 0).isEqualTo("COL1:'test1', DATE1:'2015-12-25 15:25:46', DATE2:'2015-12-25 15:25:46', ACT1:'test', ACT2:'test', ACT3:'test'");
        check(diff, 1).isEqualTo("COL1:'test2', DATE1:'2022-12-25 00:00:00', DATE2:'2025-12-25 00:00:00', ACT1:'bob', ACT2:'toto', ACT3:'test'");
        check(diff, 2).isEqualTo("COL1:'test3', DATE1:'2015-12-25 15:25:46', DATE2:'2015-12-25 15:25:46', ACT1:'test', ACT2:'test', ACT3:'test'");
        check(diff, 3).isEqualTo("COL1:'test4', DATE1:'2015-12-25 15:25:46', DATE2:'2015-12-25 15:25:46', ACT1:'test', ACT2:'test', ACT3:'test'");
    }

    @Test
    public void testApplyOnLinesByIndexAndFilter() {

        List<? extends PreparedIndexEntry> diff = diff(
                l("ID0", p("COL1", "test1"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test")),
                l("ID1", p("COL1", "test2"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test")),
                l("ID2", p("COL1", "test3"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test")),
                l("ID3", p("COL1", "test4"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test"))
        );

        this.transformer.transform(
                table("TOTHER"),
                config(
                        "    {"
                                + "  \"tablePattern\":\"TOTHER\","
                                + "  \"appliedKeyPatterns\":[\"ID.*\"],"
                                + "  \"appliedValueFilterPatterns\":{\"COL1\":\".*st3\"},"
                                + "  \"dateUpdates\":{\"DATE1\":\"2022-12-25\", \"DATE2\":\"2025-12-25\"},"
                                + "  \"actorUpdates\":{\"ACT1\":\"bob\", \"ACT2\":\"toto\"}"
                                + "}"
                ), diff
        );

        // Only ID2 is changed
        check(diff, 0).isEqualTo("COL1:'test1', DATE1:'2015-12-25 15:25:46', DATE2:'2015-12-25 15:25:46', ACT1:'test', ACT2:'test', ACT3:'test'");
        check(diff, 1).isEqualTo("COL1:'test2', DATE1:'2015-12-25 15:25:46', DATE2:'2015-12-25 15:25:46', ACT1:'test', ACT2:'test', ACT3:'test'");
        check(diff, 2).isEqualTo("COL1:'test3', DATE1:'2022-12-25 00:00:00', DATE2:'2025-12-25 00:00:00', ACT1:'bob', ACT2:'toto', ACT3:'test'");
        check(diff, 3).isEqualTo("COL1:'test4', DATE1:'2015-12-25 15:25:46', DATE2:'2015-12-25 15:25:46', ACT1:'test', ACT2:'test', ACT3:'test'");
    }

    @Test
    public void testApplyOnLinesByIndexNoFilter() {

        List<? extends PreparedIndexEntry> diff = diff(
                l("ID0", p("COL1", "test1"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test")),
                l("ID1", p("COL1", "test2"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test")),
                l("ID2", p("COL1", "test3"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test")),
                l("ID3", p("COL1", "test4"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test"))
        );

        this.transformer.transform(
                table("TOTHER"),
                config(
                        "    {"
                                + "  \"tablePattern\":\"TOTHER\","
                                + "  \"appliedKeyPatterns\":[\"ID.*\"],"
                                + "  \"appliedValueFilterPatterns\":{},"
                                + "  \"dateUpdates\":{\"DATE1\":\"2022-12-25\", \"DATE2\":\"2025-12-25\"},"
                                + "  \"actorUpdates\":{\"ACT1\":\"bob\", \"ACT2\":\"toto\"}"
                                + "}"
                ), diff
        );

        // All are changed
        check(diff, 0).isEqualTo("COL1:'test1', DATE1:'2022-12-25 00:00:00', DATE2:'2025-12-25 00:00:00', ACT1:'bob', ACT2:'toto', ACT3:'test'");
        check(diff, 1).isEqualTo("COL1:'test2', DATE1:'2022-12-25 00:00:00', DATE2:'2025-12-25 00:00:00', ACT1:'bob', ACT2:'toto', ACT3:'test'");
        check(diff, 2).isEqualTo("COL1:'test3', DATE1:'2022-12-25 00:00:00', DATE2:'2025-12-25 00:00:00', ACT1:'bob', ACT2:'toto', ACT3:'test'");
        check(diff, 3).isEqualTo("COL1:'test4', DATE1:'2022-12-25 00:00:00', DATE2:'2025-12-25 00:00:00', ACT1:'bob', ACT2:'toto', ACT3:'test'");
    }

    @Test
    public void testApplyCombined() {

        List<? extends PreparedIndexEntry> diff = diff(
                l("1", p("VALUE", "INIT_1"), p("ETAT_OBJET", "TODO_DELETED"), p("DATE_SUPPRESSION", OLD), p("DATE_MODIFICATION", OLD), p("DATE_CREATION", OLD), p("ACTEUR_SUPPRESSION", "admin_del_src"), p("ACTEUR_MODIFICATION", "admin_src"), p("ACTEUR_CREATION", "admin_src")),
                l("2", p("VALUE", "INIT_2"), p("ETAT_OBJET", "TODO_UPDATE"), p("DATE_SUPPRESSION", OLD), p("DATE_MODIFICATION", OLD), p("DATE_CREATION", OLD), p("ACTEUR_SUPPRESSION", "admin_del_src2"), p("ACTEUR_MODIFICATION", "admin_src2"), p("ACTEUR_CREATION", "admin_src2"))
        );

        this.transformer.transform(
                table("TOTHER"),
                config(
                        "  {" +
                                "  \"tablePattern\":\"T_EFLUID_TEST_AUDIT\"," +
                                "  \"appliedKeyPatterns\":[\".*\"]," +
                                "  \"appliedValueFilterPatterns\":{\"ETAT_OBJET\":\"TODO.*\"}," +
                                "  \"dateUpdates\":{\"DATE_.*\":\"2020-05-11\"}," +
                                "  \"actorUpdates\":{\"ACTEUR_.*\":\"evt 154654\"}" +
                                "}"
                ), diff
        );

        // All are changed
        check(diff, 0).isEqualTo("VALUE:'INIT_1', ETAT_OBJET:'TODO_DELETED', DATE_SUPPRESSION:'2020-05-11 00:00:00', DATE_MODIFICATION:'2020-05-11 00:00:00', DATE_CREATION:'2020-05-11 00:00:00', ACTEUR_SUPPRESSION:'evt 154654', ACTEUR_MODIFICATION:'evt 154654', ACTEUR_CREATION:'evt 154654'");
        check(diff, 1).isEqualTo("VALUE:'INIT_2', ETAT_OBJET:'TODO_UPDATE', DATE_SUPPRESSION:'2020-05-11 00:00:00', DATE_MODIFICATION:'2020-05-11 00:00:00', DATE_CREATION:'2020-05-11 00:00:00', ACTEUR_SUPPRESSION:'evt 154654', ACTEUR_MODIFICATION:'evt 154654', ACTEUR_CREATION:'evt 154654'");
    }

    @Test
    public void testApplyCurrentDate() {

        List<? extends PreparedIndexEntry> diff = diff(
                l("1", p("VALUE", "INIT_1"), p("ETAT_OBJET", "TODO_DELETED"), p("DATE_SUPPRESSION", OLD), p("DATE_MODIFICATION", OLD), p("DATE_CREATION", OLD), p("ACTEUR_SUPPRESSION", "admin_del_src"), p("ACTEUR_MODIFICATION", "admin_src"), p("ACTEUR_CREATION", "admin_src")),
                l("2", p("VALUE", "INIT_2"), p("ETAT_OBJET", "TODO_UPDATE"), p("DATE_SUPPRESSION", OLD), p("DATE_MODIFICATION", OLD), p("DATE_CREATION", OLD), p("ACTEUR_SUPPRESSION", "admin_del_src2"), p("ACTEUR_MODIFICATION", "admin_src2"), p("ACTEUR_CREATION", "admin_src2"))
        );

        this.transformer.transform(
                table("TOTHER"),
                config(
                        "  {" +
                                "  \"tablePattern\":\"T_EFLUID_TEST_AUDIT\"," +
                                "  \"appliedKeyPatterns\":[\".*\"]," +
                                "  \"dateUpdates\":{\"DATE_.*\":\"current_date\"}," +
                                "  \"actorUpdates\":{\"ACTEUR_.*\":\"evt 154654\"}" +
                                "}"
                ), diff
        );

        // All are changed
        check(diff, 0).isEqualTo("VALUE:'INIT_1', ETAT_OBJET:'TODO_DELETED', DATE_SUPPRESSION:'" + CURRENT_DATE + "', DATE_MODIFICATION:'" + CURRENT_DATE + "', DATE_CREATION:'" + CURRENT_DATE + "', ACTEUR_SUPPRESSION:'evt 154654', ACTEUR_MODIFICATION:'evt 154654', ACTEUR_CREATION:'evt 154654'");
        check(diff, 1).isEqualTo("VALUE:'INIT_2', ETAT_OBJET:'TODO_UPDATE', DATE_SUPPRESSION:'" + CURRENT_DATE + "', DATE_MODIFICATION:'" + CURRENT_DATE + "', DATE_CREATION:'" + CURRENT_DATE + "', ACTEUR_SUPPRESSION:'evt 154654', ACTEUR_MODIFICATION:'evt 154654', ACTEUR_CREATION:'evt 154654'");
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


    private static DictionaryEntry table(String name) {
        return DataGenerationUtils.entry("Modèle de compteur", null, "\"CODE_SERIE\", \"CREATE_DATE\", \"DESCRIPTION\", \"FABRICANT\", \"TYPEID\"",
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


    private static Pair<String, Object> p(String name, Object obj) {
        return Pair.of(name, obj);
    }
}
