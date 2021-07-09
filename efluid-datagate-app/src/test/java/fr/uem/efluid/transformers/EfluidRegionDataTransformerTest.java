package fr.uem.efluid.transformers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.tools.diff.ManagedQueriesGenerator;
import fr.uem.efluid.tools.diff.ManagedValueConverter;
import fr.uem.efluid.transformers.EfluidRegionDataTransformer.Config;
import fr.uem.efluid.utils.DataGenerationUtils;
import fr.uem.efluid.utils.DatasourceUtils;
import fr.uem.efluid.utils.FormatUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class EfluidRegionDataTransformerTest {

    private static final String CURRENT_DATE = FormatUtils.format(LocalDateTime.of(2020, 6, 12, 22, 14));

    private final ManagedQueriesGenerator queryGenerator = new ManagedQueriesGenerator(rules());
    private final ManagedValueConverter converter = new ManagedValueConverter();
    private final TransformerValueProvider provider = new TransformerValueProvider(this.queryGenerator) {
        @Override
        public String getFormatedCurrentTime() {
            return CURRENT_DATE;
        }
    };
    private final EfluidRegionDataTransformer transformer = new EfluidRegionDataTransformer(this.converter, this.provider);

    @Test
    public void testVariousDictionnaryApply() {

        List<String[]> sources = List.of(
                new String[]{"DIR", "TABNAME", "OP", "COLS_PK", "SRC_ID1", "SRC_ID2", "SRC_ID3", "SRC_ID4", "SRC_ID5"},
                new String[]{"regA", "T_MATCH", "INS", "+ KEY", "KEYAA  ", "       ", "       ", "       ", "       "},
                new String[]{"regA", "T_MATCH", "INS", "+ KEY", "KEYBB  ", "       ", "       ", "       ", "       "},
                new String[]{"regA", "T_MATCH", "INS", "+ KEY", "KEYCC  ", "       ", "       ", "       ", "       "},
                new String[]{"regA", "T_MATCH", "INS", "+ KEY", "KEYDD  ", "       ", "       ", "       ", "       "}
        );

        // Match in json cfg + specified regions
        assertThat(this.transformer.isApplyOnDictionaryEntry(table("T_MATCH", "COL1"),
                config(sources, "{\"tablePattern\":\".*\",\"project\":\"test\"}", "regA"))).isTrue();

        // Match in json cfg but not in specified regions
        assertThat(this.transformer.isApplyOnDictionaryEntry(table("TANY", "COL1"),
                config(sources, "{\"tablePattern\":\".*\",\"project\":\"test\"}", "regA"))).isFalse();

        // Doesn't match in json cfg neither in specified regions
        assertThat(this.transformer.isApplyOnDictionaryEntry(table("T_MATCH", "COL1"),
                config(sources, "{\"tablePattern\":\"TANY\",\"project\":\"test\"}", "regA"))).isFalse();
    }

    @Test
    public void testVariousApplyOnLinesRegardingRegion() {

        List<String[]> sources = List.of(
                new String[]{"DIR", "TABNAME", "OP", "COLS_PK", "SRC_ID1", "SRC_ID2", "SRC_ID3", "SRC_ID4", "SRC_ID5"},
                new String[]{"regA", "T_MATCH", "INS", "+ KEY", "KEYAA  ", "       ", "       ", "       ", "       "},
                new String[]{"regA", "T_MATCH", "INS", "+ KEY", "KEYBB  ", "       ", "       ", "       ", "       "},
                new String[]{"regA", "T_MATCH", "INS", "+ KEY", "KEYCC  ", "       ", "       ", "       ", "       "},
                new String[]{"regA", "T_MATCH", "INS", "+ KEY", "KEYDD  ", "       ", "       ", "       ", "       "},
                new String[]{"regB", "T_MATCH", "INS", "+ KEY", "KEYEE  ", "       ", "       ", "       ", "       "},
                new String[]{"regB", "T_MATCH", "INS", "+ KEY", "KEYFF  ", "       ", "       ", "       ", "       "},
                new String[]{"regB", "T_MATCH", "INS", "+ KEY", "KEYGG  ", "       ", "       ", "       ", "       "},
                new String[]{"regB", "T_MATCH", "INS", "+ KEY", "KEYHH  ", "       ", "       ", "       ", "       "}
        );

        String cfg = "{\"tablePattern\":\".*\",\"project\":\"test\"}";

        DictionaryEntry table = table("T_MATCH", "KEY", "COL1", "COL2");

        // Check with regA match
        var updatedDiffRegA = diffForKeys("KEYAA", "KEYBB", "KEYCC", "KEYDD", "KEYEE", "KEYFF", "KEYGG", "KEYHH");
        this.transformer.transform(table, config(sources, cfg, "regA"), updatedDiffRegA);

        assertThat(updatedDiffRegA).hasSize(4);
        assertThat(updatedDiffRegA.stream().map(PreparedIndexEntry::getKeyValue)).containsOnly("KEYAA", "KEYBB", "KEYCC", "KEYDD");

        // Check with regB match
        var updatedDiffRegB = diffForKeys("KEYAA", "KEYBB", "KEYCC", "KEYDD", "KEYEE", "KEYFF", "KEYGG", "KEYHH");
        this.transformer.transform(table, config(sources, cfg, "regB"), updatedDiffRegB);

        assertThat(updatedDiffRegB).hasSize(4);
        assertThat(updatedDiffRegB.stream().map(PreparedIndexEntry::getKeyValue)).containsOnly("KEYEE", "KEYFF", "KEYGG", "KEYHH");

        // Check with less matching items
        var updatedDiffRegBis = diffForKeys("KEYAA", "KEYBB", "KEYCC", "KEYDD", "KEYEE", "KEYGG", "KEYHH", "KEYZZ", "KEYXX", "KEY12");
        this.transformer.transform(table, config(sources, cfg, "regB"), updatedDiffRegBis);

        assertThat(updatedDiffRegBis).hasSize(3);
        assertThat(updatedDiffRegBis.stream().map(PreparedIndexEntry::getKeyValue)).containsOnly("KEYEE", "KEYGG", "KEYHH");
    }

    @Test
    public void testVariousNotApply() {

        List<String[]> sources = List.of(
                new String[]{"DIR", "TABNAME", "OP", "COLS_PK", "SRC_ID1", "SRC_ID2", "SRC_ID3", "SRC_ID4", "SRC_ID5"},
                new String[]{"regA", "T_MATCH", "INS", "+ KEY", "KEYAA  ", "       ", "       ", "       ", "       "},
                new String[]{"regA", "T_MATCH", "INS", "+ KEY", "KEYBB  ", "       ", "       ", "       ", "       "},
                new String[]{"regA", "T_MATCH", "INS", "+ KEY", "KEYCC  ", "       ", "       ", "       ", "       "},
                new String[]{"regA", "T_MATCH", "INS", "+ KEY", "KEYDD  ", "       ", "       ", "       ", "       "},
                new String[]{"regB", "T_MATCH", "INS", "+ KEY", "KEYEE  ", "       ", "       ", "       ", "       "},
                new String[]{"regB", "T_MATCH", "INS", "+ KEY", "KEYFF  ", "       ", "       ", "       ", "       "},
                new String[]{"regB", "T_MATCH", "INS", "+ KEY", "KEYGG  ", "       ", "       ", "       ", "       "},
                new String[]{"regB", "T_MATCH", "INS", "+ KEY", "KEYHH  ", "       ", "       ", "       ", "       "}
        );

        String cfg = "{\"tablePattern\":\".*\",\"project\":\"test\"}";

        // Check with not matching table
        assertThat(this.transformer.isApplyOnDictionaryEntry(table("NOT_MATCHING", "KEY", "COL1", "COL2"), config(sources, cfg, "regA"))).isFalse();

        // Check with a region not specified at all in source
        var updatedDiffRegC = diffForKeys("KEYAA", "KEYBB", "KEYCC", "KEYEE");
        this.transformer.transform(table("T_MATCH", "KEY", "COL1", "COL2"), config(sources, cfg, "regC"), updatedDiffRegC);

        // Not matched region = drop all
        assertThat(updatedDiffRegC).hasSize(0);
    }

    private Config config(List<String[]> source, String json, String regionCode) {

        /*
         * Prepare a complete configuration from some source objects, to simulate DB load of region code, and apply
         * of CSV content of source definition (from a List<String[]>)
         */
        try {
            JdbcTemplate template = Mockito.mock(JdbcTemplate.class);
            when(template.query(any(String.class), any(ResultSetExtractor.class), any(String.class))).thenReturn(regionCode);
            Config cfg = new ObjectMapper().readValue(json, Config.class);
            StringBuilder data = new StringBuilder();
            source.forEach(l -> data.append(Stream.of(l).map(String::trim).collect(Collectors.joining(";"))).append("\n"));
            cfg.importAttachmentPackageData(data.toString().getBytes(StandardCharsets.UTF_8), template, this.converter);
            return cfg;
        } catch (IOException e) {
            throw new AssertionError("Failed json config : not valid", e);
        }
    }

    private static DictionaryEntry table(String name, String keyName, String... columns) {
        return DataGenerationUtils.entry("Modèle de compteur", null, Stream.of(columns).map(c -> String.format("cur.\"%s\"", c)).collect(Collectors.joining(", ")),
                name, "\"ACTIF\"=true", keyName, ColumnType.STRING);
    }

    private List<? extends PreparedIndexEntry> diffForKeys(String... keys) {

        AtomicInteger pos = new AtomicInteger(1);

        return diff(
                Stream.of(keys).map(
                        k -> l(k, s("COL1", "test" + pos.incrementAndGet()), s("COL2", "test"))
                ).collect(Collectors.toList()).toArray(new PreparedIndexEntry[]{})
        );
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

    private static DatasourceUtils.CustomQueryGenerationRules rules() {
        DatasourceUtils.CustomQueryGenerationRules rules = new DatasourceUtils.CustomQueryGenerationRules();

        rules.setColumnNamesProtected(true);
        rules.setDatabaseDateFormat("dd-MM-yyyy HH:mm:ss");
        rules.setTableNamesProtected(true);

        return rules;
    }

}
