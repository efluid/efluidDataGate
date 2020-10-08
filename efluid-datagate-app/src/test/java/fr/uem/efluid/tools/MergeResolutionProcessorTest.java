package fr.uem.efluid.tools;

import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.PreparedMergeIndexEntry;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Predicate;

import static fr.uem.efluid.model.entities.IndexAction.*;
import static fr.uem.efluid.tools.MergeResolutionProcessor.ResolutionCase.PayloadType.DIFFERENT;
import static fr.uem.efluid.tools.MergeResolutionProcessor.ResolutionCase.PayloadType.SIMILAR;
import static fr.uem.efluid.tools.MergeResolutionProcessor.ResolutionCase.Result.PayloadResultType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class MergeResolutionProcessorTest {

    private static final ManagedValueConverter CONVERTER = new ManagedValueConverter();

    @Test
    public void testBasicResolutionRulesAccess() {

        MergeResolutionProcessor processor = processor(
                res("test1").onActions(ADD, ADD).onPayloads(SIMILAR, SIMILAR).thenNoResolution(),
                res("test2").onActions(UPDATE, UPDATE).onPayloads(SIMILAR, SIMILAR).thenNoResolution(),
                res("test3").onActions(null, UPDATE).onPayloads(DIFFERENT, DIFFERENT).thenResolution(UPDATE, THEIR_PAYLOAD, THEIR_PREVIOUS, true, null)
        );

        assertThat(processor.resolveMerge(entry(ADD, "BOB", null), entry(ADD, "BOB", null), true)).isNull();
        assertThat(processor.resolveMerge(entry(UPDATE, "BOB", "OLD"), entry(UPDATE, "BOB", "OLD"), true)).isNull();
        assertThat(processor.resolveMerge(null, entry(UPDATE, "BOB", "OLD"), true)).matches(res(UPDATE, "BOB", "OLD", true, null));
    }

    @Test
    public void testMissingResolutionRulesFail() {

        MergeResolutionProcessor processor = processor(
                res("test1").onActions(ADD, ADD).onPayloads(SIMILAR, SIMILAR).thenNoResolution(),
                res("test2").onActions(UPDATE, UPDATE).onPayloads(SIMILAR, SIMILAR).thenNoResolution(),
                res("test3").onActions(null, UPDATE).onPayloads(DIFFERENT, DIFFERENT).thenResolution(UPDATE, THEIR_PAYLOAD, THEIR_PREVIOUS, true, null)
        );

        try {
            processor.resolveMerge(entry(ADD, "BOB", null), entry(UPDATE, "BOB", "PREV"), true);
            fail("Must fail on exception");
        } catch (Throwable t) {
            assertThat(t).hasMessageContaining("Unsupported merge case resolution");
        }
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
        entry.setPrevious(previous != null ? CONVERTER.convertToExtractedValue(new LinkedHashMap<>(Collections.singletonMap("COL", previous))) : null);
        entry.setPayload(payload != null ? CONVERTER.convertToExtractedValue(new LinkedHashMap<>(Collections.singletonMap("COL", payload))) : null);
        entry.setAction(action);
        return entry;
    }

    private static MergeResolutionProcessor processor(MergeResolutionProcessor.ResolutionCase... cases) {
        return new MergeResolutionProcessor(Arrays.asList(cases), CONVERTER);
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


}
