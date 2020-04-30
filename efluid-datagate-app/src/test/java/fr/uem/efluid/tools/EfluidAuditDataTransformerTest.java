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

    private ManagedValueConverter converter = new ManagedValueConverter();
    private EfluidAuditDataTransformer transformer = new EfluidAuditDataTransformer(this.converter);

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

        List<? extends PreparedIndexEntry> res = this.transformer.transform(
                table("TOTHER"),
                config(
                        "    {"
                                + "  \"tablePattern\":\"TOTHER\","
                                + "  \"appliedKeyPatterns\":[\"ID1\"],"
                                + "  \"appliedValueFilterPatterns\":{\"COL1\":\".*\"},"
                                + "  \"dateUpdates\":{\"DATE1\":\"2022-12-25\", \"DATE2\":\"2025-12-25\"},"
                                + "  \"actorUpdates\":{\"ACT1\":\"bob\", \"ACT2\":\"toto\"}"
                                + "}"
                ), diff(
                        l("ID0", p("COL1", "test1"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test")),
                        l("ID1", p("COL1", "test2"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test")),
                        l("ID2", p("COL1", "test3"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test")),
                        l("ID3", p("COL1", "test4"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test"))
                )
        );

        // Only ID1 is changed
        check(res, 0).isEqualTo("COL1:'test1', DATE1:'2015-12-25 15:25:46', DATE2:'2015-12-25 15:25:46', ACT1:'test', ACT2:'test', ACT3:'test'");
        check(res, 1).isEqualTo("COL1:'test2', DATE1:'2022-12-25 00:00:00', DATE2:'2025-12-25 00:00:00', ACT1:'bob', ACT2:'toto', ACT3:'test'");
        check(res, 2).isEqualTo("COL1:'test3', DATE1:'2015-12-25 15:25:46', DATE2:'2015-12-25 15:25:46', ACT1:'test', ACT2:'test', ACT3:'test'");
        check(res, 3).isEqualTo("COL1:'test4', DATE1:'2015-12-25 15:25:46', DATE2:'2015-12-25 15:25:46', ACT1:'test', ACT2:'test', ACT3:'test'");
    }

    @Test
    public void testApplyOnLinesByIndexAndFilter() {

        List<? extends PreparedIndexEntry> res = this.transformer.transform(
                table("TOTHER"),
                config(
                        "    {"
                                + "  \"tablePattern\":\"TOTHER\","
                                + "  \"appliedKeyPatterns\":[\"ID.*\"],"
                                + "  \"appliedValueFilterPatterns\":{\"COL1\":\".*st3\"},"
                                + "  \"dateUpdates\":{\"DATE1\":\"2022-12-25\", \"DATE2\":\"2025-12-25\"},"
                                + "  \"actorUpdates\":{\"ACT1\":\"bob\", \"ACT2\":\"toto\"}"
                                + "}"
                ), diff(
                        l("ID0", p("COL1", "test1"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test")),
                        l("ID1", p("COL1", "test2"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test")),
                        l("ID2", p("COL1", "test3"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test")),
                        l("ID3", p("COL1", "test4"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test"))
                )
        );

        // Only ID2 is changed
        check(res, 0).isEqualTo("COL1:'test1', DATE1:'2015-12-25 15:25:46', DATE2:'2015-12-25 15:25:46', ACT1:'test', ACT2:'test', ACT3:'test'");
        check(res, 1).isEqualTo("COL1:'test2', DATE1:'2015-12-25 15:25:46', DATE2:'2015-12-25 15:25:46', ACT1:'test', ACT2:'test', ACT3:'test'");
        check(res, 2).isEqualTo("COL1:'test3', DATE1:'2022-12-25 00:00:00', DATE2:'2025-12-25 00:00:00', ACT1:'bob', ACT2:'toto', ACT3:'test'");
        check(res, 3).isEqualTo("COL1:'test4', DATE1:'2015-12-25 15:25:46', DATE2:'2015-12-25 15:25:46', ACT1:'test', ACT2:'test', ACT3:'test'");
    }

    @Test
    public void testApplyOnLinesByIndexNoFilter() {

        List<? extends PreparedIndexEntry> res = this.transformer.transform(
                table("TOTHER"),
                config(
                        "    {"
                                + "  \"tablePattern\":\"TOTHER\","
                                + "  \"appliedKeyPatterns\":[\"ID.*\"],"
                                + "  \"appliedValueFilterPatterns\":{},"
                                + "  \"dateUpdates\":{\"DATE1\":\"2022-12-25\", \"DATE2\":\"2025-12-25\"},"
                                + "  \"actorUpdates\":{\"ACT1\":\"bob\", \"ACT2\":\"toto\"}"
                                + "}"
                ), diff(
                        l("ID0", p("COL1", "test1"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test")),
                        l("ID1", p("COL1", "test2"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test")),
                        l("ID2", p("COL1", "test3"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test")),
                        l("ID3", p("COL1", "test4"), p("DATE1", OLD), p("DATE2", OLD), p("ACT1", "test"), p("ACT2", "test"), p("ACT3", "test"))
                )
        );

        // All are changed
        check(res, 0).isEqualTo("COL1:'test1', DATE1:'2022-12-25 00:00:00', DATE2:'2025-12-25 00:00:00', ACT1:'bob', ACT2:'toto', ACT3:'test'");
        check(res, 1).isEqualTo("COL1:'test2', DATE1:'2022-12-25 00:00:00', DATE2:'2025-12-25 00:00:00', ACT1:'bob', ACT2:'toto', ACT3:'test'");
        check(res, 2).isEqualTo("COL1:'test3', DATE1:'2022-12-25 00:00:00', DATE2:'2025-12-25 00:00:00', ACT1:'bob', ACT2:'toto', ACT3:'test'");
        check(res, 3).isEqualTo("COL1:'test4', DATE1:'2022-12-25 00:00:00', DATE2:'2025-12-25 00:00:00', ACT1:'bob', ACT2:'toto', ACT3:'test'");
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
