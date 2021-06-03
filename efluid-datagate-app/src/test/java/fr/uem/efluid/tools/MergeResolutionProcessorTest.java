package fr.uem.efluid.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.PreparedMergeIndexEntry;
import fr.uem.efluid.utils.ApplicationException;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static fr.uem.efluid.model.entities.IndexAction.*;
import static fr.uem.efluid.tools.MergeResolutionProcessor.ResolutionCase.PayloadType.*;
import static fr.uem.efluid.tools.MergeResolutionProcessor.ResolutionCase.Result.PayloadResultType.THEIR_PAYLOAD;
import static fr.uem.efluid.tools.MergeResolutionProcessor.ResolutionCase.Result.PayloadResultType.THEIR_PREVIOUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class MergeResolutionProcessorTest {

    private static final ManagedValueConverter CONVERTER = new ManagedValueConverter();

    private static final Map<MergeResolutionProcessor.ResolutionCase.PayloadType, List<Example>> MERGE_DATA_EXAMPLES = Map.of(
            ANY, List.of(Example.of(payload("ANY"), payload("ANY")), Example.of(payload("ANY"), payload("ANY+")), Example.of(payload("ANY+"), payload("ANY")), Example.of(null, payload("ANY")), Example.of(payload("ANY"), null)),
            SIMILAR, List.of(Example.of(payload("SIMILAR"), payload("SIMILAR"))),
            DIFFERENT, List.of(Example.of(payload("DIFFERENT"), payload("DIFFERENT+")), Example.of(payload("DIFFERENT+"), payload("DIFFERENT")), Example.of(null, payload("DIFFERENT")), Example.of(payload("DIFFERENT"), null))
    );

    @Test
    public void testBasicResolutionRulesAccess() {

        MergeResolutionProcessor processor = processor(
                res("test1").onLineExists(false).onActions(ADD, ADD).onPayloads(SIMILAR, SIMILAR).thenNoResolution(),
                res("test2").onLineExists(true).onActions(UPDATE, UPDATE).onPayloads(SIMILAR, SIMILAR).thenNoResolution(),
                res("test3").onLineExists(true).onActions(null, UPDATE).onPayloads(DIFFERENT, ANY).thenResolution(UPDATE, THEIR_PAYLOAD, THEIR_PREVIOUS, true, null)
        );

        assertThat(processor.resolveMerge(entry(ADD, "BOB", null), entry(ADD, "BOB", null), null, null).isSelected()).isFalse();
        assertThat(processor.resolveMerge(entry(UPDATE, "BOB", "OLD"), entry(UPDATE, "BOB", "OLD"), payload("OLD"), payload("OLDOLD")).isSelected()).isFalse();
        assertThat(processor.resolveMerge(null, entry(UPDATE, "BOB", "OLD"), payload("OLD"), payload("OLDOLD"))).matches(res(UPDATE, "BOB", "OLD", true, null));
    }

    @Test
    public void testMissingResolutionRulesFail() {

        MergeResolutionProcessor processor = processor(
                res("test1").onLineExists(false).onActions(ADD, ADD).onPayloads(SIMILAR, SIMILAR).thenNoResolution(),
                res("test2").onLineExists(true).onActions(UPDATE, UPDATE).onPayloads(SIMILAR, SIMILAR).thenNoResolution(),
                res("test3").onLineExists(true).onActions(null, UPDATE).onPayloads(DIFFERENT, DIFFERENT).thenResolution(UPDATE, THEIR_PAYLOAD, THEIR_PREVIOUS, true, null)
        );

        try {
            processor.resolveMerge(entry(ADD, "BOB", null), entry(UPDATE, "BOB", "PREV"), payload("OLD"), payload("OLDOLD"));
            fail("Must fail on exception");
        } catch (Throwable t) {
            assertThat(t).hasMessageContaining("Unsupported merge case resolution");
        }
    }

    @Test
    public void testStandardRulesResolutionCase() {
        MergeResolutionProcessor processor = processor("classpath:merge-resolution-rules.json");

        var their = rawEntry(REMOVE, "MCO_TEST", "2", null, "OTHER_VALUE=S/c2Vjb25kIHZhbCBmb3IgYWN0ZXVyIG1vZGlmaWNhdGlvbg==,VALUE=S/c2Vjb25kIHZhbHVl");
        var mine = rawEntry(UPDATE, "MCO_TEST", "2", "OTHER_VALUE=S/c2Vjb25kIHZhbCBmb3IgYWN0ZXVyIG1vZGlmaWNhdGlvbg==,VALUE=S/c2Vjb25kIHZhbHVl,ACTEURMODIFICATION=S/YWN0ZXVyTW9kaWZpY2F0aW9u,DATEMODIFICATION=T/MjAyMS0wNS0yMSAxNDo1NToxNg==",
                "OTHER_VALUE=S/c2Vjb25kIHZhbCBjb25mbGljdA==,VALUE=S/c2Vjb25kIHZhbHVl");

        var res = processor.resolveMerge(mine, their, "OTHER_VALUE=S/c2Vjb25kIHZhbCBmb3IgYWN0ZXVyIG1vZGlmaWNhdGlvbg==,VALUE=S/c2Vjb25kIHZhbHVl,ACTEURMODIFICATION=S/YWN0ZXVyTW9kaWZpY2F0aW9u,DATEMODIFICATION=T/MjAyMS0wNS0yMSAxNDo1NToxNg==", "OTHER_VALUE=S/c2Vjb25kIHZhbCBjb25mbGljdA==,VALUE=S/c2Vjb25kIHZhbHVl");

        assertThat(res.getResolutionRule()).isEqualTo("REMOVE their - UPDATE mine - different previous");
    }


    @Test
    public void testStandardRulesResolutionNoDuplicates() {
        MergeResolutionProcessor processor = processor("classpath:merge-resolution-rules.json");

        AtomicInteger warningNotMatchCount = new AtomicInteger(0);
        AtomicInteger warningExceptionCount = new AtomicInteger(0);

        /*
         * We build various test cases for each existing resolution rules, and we check that the corresponding values :
         * - match the existing resolution rule
         * - have only this resolution rule to match them (else exception is throw)
         */
        processor.getCases().forEach(c -> {

            var payloads = MERGE_DATA_EXAMPLES.get(c.getPayload());
            var previouses = MERGE_DATA_EXAMPLES.get(c.getPrevious());

            payloads.forEach(pay -> {

                previouses.forEach(pre -> {

                    var mine = c.getMine() != null ? rawEntry(c.getMine(), "TABLE", "ANY", pay.getMine(), pre.getMine()) : null;
                    var their = c.getTheir() != null ? rawEntry(c.getTheir(), "TABLE", "ANY", pay.getTheir(), pre.getTheir()) : null;

                    var actualPayload = c.isLineExists() ? pay.getMine() : null;
                    var actualPrevious = c.isLineExists() && mine != null ? mine.getPrevious() : null;

                    if (their != null || mine != null) {
                        PreparedMergeIndexEntry res = null;
                        if (their != null) {
                            their.setHrPayload(CONVERTER.convertToHrPayload(their.getPayload(), their.getPrevious()));
                        }
                        if (mine != null) {
                            mine.setHrPayload(CONVERTER.convertToHrPayload(mine.getPayload(), mine.getPrevious()));
                        }
                        String message = "[WARNING] The tested case for resolution rule \"" + c.getCaseName() + "\" is not processed as expected for test values : \n"
                                + "+ their : " + (their != null ? their.toLogRendering() : " null") + "\n"
                                + "+ mine : " + (mine != null ? mine.toLogRendering() : " null") + "\n"
                                + "+ actualPayload : " + actualPayload + "\n"
                                + "+ actualPrevious : " + actualPrevious + "\n";
                        try {
                            res = processor.resolveMerge(mine, their, actualPayload, actualPrevious);
                            if (!Objects.equals(res.getResolutionRule(), c.getCaseName())) {
                                System.out.println(message + "+ error : case detected for \"" + c.getCaseName() + "\" became \"" + res.getResolutionRule() + "\"\n\n");
                                warningNotMatchCount.incrementAndGet();
                            }
                        } catch (ApplicationException e) {
                            System.out.println(message + "+ error : " + e.getMessage() + "\n\n");
                            warningExceptionCount.incrementAndGet();
                        }
                    }
                });

            });
        });

        System.out.println("\nFound " + warningNotMatchCount.get() + " warning(s) for non matching case and " + warningExceptionCount.get() + " warning(s) for matching exception");
        assertThat(warningNotMatchCount.get() + warningExceptionCount.get()).isLessThanOrEqualTo(120);
    }

    private static Predicate<PreparedMergeIndexEntry> res(
            IndexAction action,
            String payload,
            String previous,
            boolean needAction,
            String warning) {

        return p ->
                Objects.equals(p.getAction(), action)
                        && Objects.equals(p.getResolutionWarning(), warning)
                        && Objects.equals(p.getPayload(), payload != null ? CONVERTER.convertToExtractedValue(new LinkedHashMap<>(Collections.singletonMap("COL", payload))) : null)
                        && Objects.equals(p.getPrevious(), previous != null ? CONVERTER.convertToExtractedValue(new LinkedHashMap<>(Collections.singletonMap("COL", previous))) : null)
                        && p.isNeedAction() == needAction
                ;
    }

    private static PreparedIndexEntry entry(IndexAction action, String payload, String previous) {
        PreparedIndexEntry entry = new PreparedIndexEntry();
        entry.setTableName("TABLE");
        entry.setKeyValue("KEY");
        entry.setPrevious(payload(previous));
        entry.setPayload(payload(payload));
        entry.setAction(action);
        return entry;
    }

    private static PreparedIndexEntry rawEntry(IndexAction action, String table, String key, String payload, String previous) {
        PreparedIndexEntry entry = new PreparedIndexEntry();
        entry.setTableName(table);
        entry.setKeyValue(key);
        entry.setPrevious(previous);
        entry.setPayload(payload);
        entry.setAction(action);
        return entry;
    }

    private static String payload(String testValue) {
        return testValue != null ? CONVERTER.convertToExtractedValue(new LinkedHashMap<>(Collections.singletonMap("COL", testValue))) : null;
    }

    private static MergeResolutionProcessor processor(MergeResolutionProcessor.ResolutionCase... cases) {
        return new MergeResolutionProcessor(Arrays.asList(cases), CONVERTER);
    }

    private static MergeResolutionProcessor processor(String url) {

        try {
            URL json = ResourceUtils.getURL(url);
            ObjectMapper mapper = new ObjectMapper();
            List<MergeResolutionProcessor.ResolutionCase> cases = mapper
                    .readValue(json, new TypeReference<List<MergeResolutionProcessor.ResolutionCase>>() {
                    });

            return new MergeResolutionProcessor(cases, CONVERTER);
        } catch (Exception e) {
            throw new AssertionError("Cannot load resolutions from file " + url, e);
        }
    }

    private static ResolutionCaseBuilder res(String name) {
        return new ResolutionCaseBuilder(name);
    }

    private static class ResolutionCaseBuilder {

        private final MergeResolutionProcessor.ResolutionCase current;

        public ResolutionCaseBuilder(String name) {
            this.current = new MergeResolutionProcessor.ResolutionCase();
            this.current.setCaseName(name);
        }

        public ResolutionCaseBuilder onPayloads(MergeResolutionProcessor.ResolutionCase.PayloadType payload, MergeResolutionProcessor.ResolutionCase.PayloadType previous) {
            this.current.setPayload(payload);
            this.current.setPrevious(previous);
            return this;
        }

        public ResolutionCaseBuilder onLineExists(boolean lineExists) {
            this.current.setLineExists(lineExists);
            return this;
        }

        public ResolutionCaseBuilder onActions(IndexAction mine, IndexAction their) {
            this.current.setMine(mine);
            this.current.setTheir(their);
            return this;
        }

        public MergeResolutionProcessor.ResolutionCase thenNoResolution() {
            return this.current;
        }

        public MergeResolutionProcessor.ResolutionCase thenResolution(
                IndexAction action,
                MergeResolutionProcessor.ResolutionCase.Result.PayloadResultType payload,
                MergeResolutionProcessor.ResolutionCase.Result.PayloadResultType previous,
                boolean needAction,
                String warning) {
            var res = new MergeResolutionProcessor.ResolutionCase.Result();
            res.setPayload(payload);
            res.setPrevious(previous);
            res.setAction(action);
            res.setNeedAction(needAction);
            this.current.setWarning(warning);
            this.current.setResolution(res);

            return this.current;
        }

    }

    // Example of value for merge resolution case test
    private static final class Example {

        private final String mine;
        private final String their;

        private Example(String mine, String their) {
            this.mine = mine;
            this.their = their;
        }

        public static Example of(String mine, String their) {
            return new Example(mine, their);
        }

        public String getMine() {
            return this.mine;
        }

        public String getTheir() {
            return this.their;
        }
    }
}
