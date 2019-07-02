package fr.uem.efluid.tools;

import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.PreparedMergeIndexEntry;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import java.util.Collection;

/**
 * Dedicated processing unit for merge : process "mine" and "their" entries built using basic rules,
 * and apply the resolution specified in ResolutionRules configuration
 *
 * @version 1
 * @since v0.2
 */
public class MergeResolutionProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergeResolutionProcessor.class);

    private final Collection<ResolutionCase> cases;

    @Autowired
    private ManagedValueConverter valueConverter;

    /**
     * Init from loaded configured Resolution cases
     *
     * @param cases
     */
    public MergeResolutionProcessor(Collection<ResolutionCase> cases) {
        this.cases = cases;
    }

    /**
     * Process one entry against resolution case rules
     *
     * @param mineEntry
     * @param theirEntry
     * @return built merge entry
     */
    public PreparedMergeIndexEntry resolveMerge(PreparedIndexEntry mineEntry, PreparedIndexEntry theirEntry, String actualContent) {

        ResolutionCase resolutionCase = searchResolutionCase(mineEntry, theirEntry);

        LOGGER.debug("For mineEntry {}, theirEntry {}, found resolution case {}", mineEntry, theirEntry, resolutionCase.getCaseName());

        // If resolution or resolution action is null, drop it
        if (resolutionCase.getResolution() == null || resolutionCase.getResolution().getAction() == null) {
            return null;
        }

        PreparedMergeIndexEntry entry = applyResolutionResult(resolutionCase, mineEntry, theirEntry);

        if (actualContent == null && entry.getAction() != IndexAction.ADD) {
            LOGGER.debug("Drop unknown update on entry {} for dict entry {} with update {} as this key doesn't exist at all locally",
                    entry.getKeyValue(), entry.getDictionaryEntryUuid(), entry.getAction());
            return null;
        }

        return entry;
    }

    /**
     * From identified resolutionCase, apply specified Result
     *
     * @param resolutionCase
     * @param mineEntry
     * @param theirEntry
     * @return
     */
    private PreparedMergeIndexEntry applyResolutionResult(
            ResolutionCase resolutionCase,
            PreparedIndexEntry mineEntry,
            PreparedIndexEntry theirEntry) {

        // Impossible case
        if (mineEntry == null && theirEntry == null) {
            return null;
        }

        PreparedMergeIndexEntry entry = new PreparedMergeIndexEntry();

        // All are selected as default in merge
        entry.setSelected(true);

        // Apply resolution "as this" on action check
        entry.setAction(resolutionCase.getResolution().getAction());
        entry.setNeedAction(resolutionCase.getResolution().isNeedAction());

        // Apply identifier from available source
        copyIdentifier(entry, theirEntry != null ? theirEntry : mineEntry);

        // Copy refered sources
        copySourceEntries(entry, mineEntry, theirEntry);

        // Copy payload regarding source values
        if (resolutionCase.getResolution().getPayload() != null) {
            switch (resolutionCase.getResolution().getPayload()) {
                case THEIR_PAYLOAD:
                    copyPayloads(entry, theirEntry);
                    break;
                case THEIR_PAYLOAD_REGENERATE_HR_FROM_MINE:
                    // 1st copy payload from their
                    copyPayloads(entry, theirEntry);

                    // Then regen HR
                    entry.setHrPayload(
                            this.valueConverter.convertToHrPayload(
                                    theirEntry != null ? theirEntry.getPayload() : null,
                                    mineEntry != null ? mineEntry.getPayload() : null));
                    break;
                case MINE_PAYLOAD:
                default:
                    copyPayloads(entry, mineEntry);
                    break;
            }
        }

        // Refer applied resolution rule (for validation)
        entry.setResolutionRule(resolutionCase.getCaseName());

        return entry;
    }

    /**
     * Copy properties which are used to identify the diff entry : commit uuid if any, dic uuid, key ...
     *
     * @param resultEntry
     * @param source
     */
    private void copyIdentifier(PreparedMergeIndexEntry resultEntry, PreparedIndexEntry source) {
        if (source != null) {
            resultEntry.setKeyValue(source.getKeyValue());
            resultEntry.setDictionaryEntryUuid(source.getDictionaryEntryUuid());
            resultEntry.setCommitUuid(source.getCommitUuid());
            resultEntry.setTimestamp(source.getTimestamp());
        }
    }

    /**
     * Copy payload + hrPayload from specified source
     *
     * @param resultEntry
     * @param source
     */
    private void copyPayloads(PreparedMergeIndexEntry resultEntry, PreparedIndexEntry source) {
        if (source != null) {
            resultEntry.setHrPayload(source.getHrPayload());
            resultEntry.setPayload(source.getPayload());
        }
    }

    /**
     * Copy mine / their to reference regarding nullability
     *
     * @param resultEntry
     * @param mineEntry
     * @param theirEntry
     */
    private void copySourceEntries(
            PreparedMergeIndexEntry resultEntry,
            PreparedIndexEntry mineEntry,
            PreparedIndexEntry theirEntry) {

        resultEntry.setMine(mineEntry);
        resultEntry.setTheir(theirEntry);

        if (theirEntry == null) {
            resultEntry.setTheir(new PreparedIndexEntry());
        }

        if (mineEntry == null) {
            resultEntry.setMine(new PreparedIndexEntry());
        }
    }

    private ResolutionCase searchResolutionCase(PreparedIndexEntry mineEntry, PreparedIndexEntry theirEntry) {

        IndexAction their = theirEntry != null ? theirEntry.getAction() : null;
        IndexAction mine = mineEntry != null ? mineEntry.getAction() : null;

        ResolutionCase.PayloadType payload = ObjectUtils.nullSafeEquals(
                theirEntry != null ? theirEntry.getPayload() : null,
                mineEntry != null ? mineEntry.getPayload() : null
        ) ? ResolutionCase.PayloadType.SIMILAR : ResolutionCase.PayloadType.DIFFERENT;

        return this.cases.stream()
                .filter(r -> r.getMine() == mine && r.getTheir() == their && r.getPayload() == payload).findFirst()
                .orElseThrow(() -> new ApplicationException(ErrorType.MERGE_RESOLUTION_UNKNOWN,
                        "Unsupported merge case resolution. Get their=\"" + their + "\", mine=\"" + mine + "\", payload=\"" + payload + "\""));
    }

    /**
     * Definition of one case of resolution, with requested result, for a merge
     */
    public static class ResolutionCase {

        private String caseName;
        private IndexAction their;
        private IndexAction mine;
        private PayloadType payload;
        private Result resolution;

        public ResolutionCase() {
        }

        public String getCaseName() {
            return caseName;
        }

        public void setCaseName(String caseName) {
            this.caseName = caseName;
        }

        public IndexAction getTheir() {
            return their;
        }

        public void setTheir(IndexAction their) {
            this.their = their;
        }

        public IndexAction getMine() {
            return mine;
        }

        public void setMine(IndexAction mine) {
            this.mine = mine;
        }

        public PayloadType getPayload() {
            return payload;
        }

        public void setPayload(PayloadType payload) {
            this.payload = payload;
        }

        public Result getResolution() {
            return resolution;
        }

        public void setResolution(Result resolution) {
            this.resolution = resolution;
        }

        public static class Result {

            private PayloadResultType payload;
            private IndexAction action;
            private boolean needAction;

            public Result() {
            }

            public PayloadResultType getPayload() {
                return payload;
            }

            public void setPayload(PayloadResultType payload) {
                this.payload = payload;
            }

            public IndexAction getAction() {
                return action;
            }

            public void setAction(IndexAction action) {
                this.action = action;
            }

            public boolean isNeedAction() {
                return needAction;
            }

            public void setNeedAction(boolean needAction) {
                this.needAction = needAction;
            }

            public enum PayloadResultType {
                THEIR_PAYLOAD, MINE_PAYLOAD, THEIR_PAYLOAD_REGENERATE_HR_FROM_MINE
            }


        }

        public enum PayloadType {
            SIMILAR, DIFFERENT
        }
    }
}
