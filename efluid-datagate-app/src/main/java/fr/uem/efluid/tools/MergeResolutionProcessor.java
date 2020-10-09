package fr.uem.efluid.tools;

import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.PreparedMergeIndexEntry;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dedicated processing unit for merge : process "mine" and "their" entries built using basic rules,
 * and apply the resolution specified in ResolutionRules configuration
 *
 * @version 2
 * @since v0.2
 */
public class MergeResolutionProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergeResolutionProcessor.class);

    private static final Logger MERGE_WARNING_LOGGER = LoggerFactory.getLogger("merge.warnings");

    private final Collection<ResolutionCase> cases;

    private final ManagedValueConverter valueConverter;

    /**
     * Init from loaded configured Resolution cases
     *
     * @param cases loaded resolution config
     */
    public MergeResolutionProcessor(Collection<ResolutionCase> cases, ManagedValueConverter valueConverter) {
        this.cases = cases;
        this.valueConverter = valueConverter;
    }

    /**
     * Process one entry against resolution case rules
     *
     * @param mineEntry      current "mine" : entry on local db
     * @param theirEntry     current "their" : imported combined entry
     * @param actualPayload  actual active content in the managed database, as a payload
     * @param actualPrevious the last "previous" managed for the entry in the backlog. Used to build new "previous" for some results
     * @return built merge entry
     */
    public PreparedMergeIndexEntry resolveMerge(
            PreparedIndexEntry mineEntry,
            PreparedIndexEntry theirEntry,
            String actualPayload,
            String actualPrevious) {

        ResolutionCase resolutionCase =
                searchResolutionCase(mineEntry, theirEntry, actualPayload, actualPrevious);

        PreparedMergeIndexEntry entry =
                applyResolutionResult(
                        resolutionCase,
                        mineEntry,
                        theirEntry,
                        actualPayload,
                        actualPrevious);

        // Trace all resolution warnings
        if (entry.getResolutionWarning() != null && MERGE_WARNING_LOGGER.isInfoEnabled()) {
            MERGE_WARNING_LOGGER.info("{} => {}", renderDiffLine(entry), entry.getResolutionWarning());
        }

        return entry;
    }

    /**
     * From identified resolutionCase, apply specified Result
     */
    private PreparedMergeIndexEntry applyResolutionResult(
            ResolutionCase resolutionCase,
            PreparedIndexEntry mineEntry,
            PreparedIndexEntry theirEntry,
            String actualPayload,
            String actualPrevious) {

        // Impossible case
        if (mineEntry == null && theirEntry == null) {
            return null;
        }

        PreparedMergeIndexEntry entry = new PreparedMergeIndexEntry();

        // Always requires timestamp
        entry.setTimestamp(System.currentTimeMillis());

        // Keep reference to table
        entry.setTableName(mineEntry != null ? mineEntry.getTableName() : theirEntry.getTableName());

        // Apply identifier from available source
        copyIdentifier(entry, theirEntry != null ? theirEntry : mineEntry);

        // Copy identified sources
        copySourceEntries(entry, mineEntry, theirEntry);

        // Refer applied resolution rule (for validation)
        entry.setResolutionRule(resolutionCase.getCaseName());

        // Keep warning if any
        entry.setResolutionWarning(resolutionCase.getWarning());

        if (resolutionCase.getResolution() != null && resolutionCase.getResolution().getAction() != null) {

            // All are kept and selected as default in merge
            entry.setKeep(true);
            entry.setSelected(true);

            // Apply resolution "as this" on action check
            entry.setAction(resolutionCase.getResolution().getAction());
            entry.setNeedAction(resolutionCase.getResolution().isNeedAction());

            // Copy payload regarding source values
            if (resolutionCase.getResolution().getPayload() != null) {
                switch (resolutionCase.getResolution().getPayload()) {
                    case THEIR_PAYLOAD:
                        copyPayloads(entry, theirEntry);
                        break;
                    case THEIR_PREVIOUS: /* Unsupported */
                    case MINE_PREVIOUS: /* Unsupported */
                        throw new ApplicationException(ErrorType.MERGE_RESOLUTION_UNKNOWN,
                                "Unsupported merge case resolution. Cannot use THEIR_PREVIOUS or MINE_PREVIOUS " +
                                        "in payload resolution in case \"" + resolutionCase.getCaseName() + "\"");
                    case MINE_PAYLOAD:
                    default:
                        copyPayloads(entry, mineEntry);
                        break;
                }
            }

            // Copy previous regarding source values
            if (resolutionCase.getResolution().getPrevious() != null) {
                switch (resolutionCase.getResolution().getPrevious()) {
                    case THEIR_PAYLOAD:
                        if (theirEntry != null) {
                            entry.setPrevious(theirEntry.getPayload());
                        }
                        break;
                    case MINE_PAYLOAD:
                        if (mineEntry != null) {
                            entry.setPrevious(mineEntry.getPayload());
                        }
                        break;
                    case THEIR_PREVIOUS:
                        if (theirEntry != null) {
                            entry.setPrevious(theirEntry.getPrevious());
                        }
                        break;
                    case ACTUAL_CONTENT:
                        entry.setPrevious(actualPayload);
                        break;
                    case ACTUAL_PREVIOUS:
                        entry.setPrevious(actualPrevious);
                        break;
                    case MINE_PREVIOUS:
                    default:
                        if (mineEntry != null) {
                            entry.setPrevious(mineEntry.getPrevious());
                        }
                        break;
                }
            }

            // Alway regenerate HR Payload
            entry.setHrPayload(this.valueConverter.convertToHrPayload(entry.getPayload(), entry.getPrevious()));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[KEEP] Mine:{}, Their:{}, resolution {}, produces {}",
                        renderDiffLine(mineEntry), renderDiffLine(theirEntry),
                        resolutionCase.getCaseName(), renderDiffLine(entry));
            }

        } else {
            // Without resolution, we mark as not kept (will be dropped)
            entry.setKeep(false);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[DROP] Mine:{}, Their:{}, resolution {}, drop the line",
                        renderDiffLine(mineEntry), renderDiffLine(theirEntry),
                        resolutionCase.getCaseName());
            }
        }


        return entry;
    }

    /**
     * Copy properties which are used to identify the diff entry : commit uuid if any, dic uuid, key ...
     */
    private void copyIdentifier(PreparedMergeIndexEntry resultEntry, PreparedIndexEntry source) {
        if (source != null) {
            resultEntry.setKeyValue(source.getKeyValue());
            resultEntry.setDictionaryEntryUuid(source.getDictionaryEntryUuid());
            resultEntry.setCommitUuid(source.getCommitUuid());
            resultEntry.setIndexForDiff(source.getDictionaryEntryUuid() + "_" + source.getKeyValue());
        }
    }

    /**
     * Copy payload + hrPayload from specified source
     */
    private void copyPayloads(PreparedMergeIndexEntry resultEntry, PreparedIndexEntry source) {
        if (source != null) {
            resultEntry.setHrPayload(source.getHrPayload());
            resultEntry.setPayload(source.getPayload());
        }
    }

    /**
     * Copy mine / their to reference regarding nullability
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

    private ResolutionCase searchResolutionCase(PreparedIndexEntry mineEntry, PreparedIndexEntry theirEntry, String actualPayload, String lastKnewPrevious) {

        IndexAction their = theirEntry != null ? theirEntry.getAction() : null;
        IndexAction mine = mineEntry != null ? mineEntry.getAction() : null;

        ResolutionCase.PayloadType payload;
        ResolutionCase.PayloadType previous;

        // No local diff but existing content => use actual for payload / previous compare
        if (theirEntry != null && mineEntry == null && actualPayload != null) {
            payload = ObjectUtils.nullSafeEquals(theirEntry.getPayload(), actualPayload) ? ResolutionCase.PayloadType.SIMILAR : ResolutionCase.PayloadType.DIFFERENT;
            previous = ObjectUtils.nullSafeEquals(theirEntry.getPrevious(), lastKnewPrevious) ? ResolutionCase.PayloadType.SIMILAR : ResolutionCase.PayloadType.DIFFERENT;
        } else {
            payload = ObjectUtils.nullSafeEquals(
                    theirEntry != null ? theirEntry.getPayload() : null,
                    mineEntry != null ? mineEntry.getPayload() : null
            ) ? ResolutionCase.PayloadType.SIMILAR : ResolutionCase.PayloadType.DIFFERENT;

            previous = ObjectUtils.nullSafeEquals(
                    theirEntry != null ? theirEntry.getPrevious() : null,
                    mineEntry != null ? mineEntry.getPrevious() : null
            ) ? ResolutionCase.PayloadType.SIMILAR : ResolutionCase.PayloadType.DIFFERENT;
        }

        List<ResolutionCase> found = this.cases.stream()
                .filter(r -> r.getMine() == mine
                        && r.getTheir() == their
                        && (r.getPayload() == ResolutionCase.PayloadType.ANY || r.getPayload() == payload)
                        && (r.getPrevious() == ResolutionCase.PayloadType.ANY || r.getPrevious() == previous)
                        && r.isLineExists() == (actualPayload != null))
                .collect(Collectors.toList());

        if (found.size() == 0) {
            throw new ApplicationException(ErrorType.MERGE_RESOLUTION_UNKNOWN,
                    "Unsupported merge case resolution. Get their=\"" + their + "\", mine=\""
                            + mine + "\", payload=\"" + payload + "\", previous=\"" + previous
                            + "\", lineExists=\"" + (actualPayload != null) + "\"");
        }

        if (found.size() > 1) {
            String names = found.stream().map(ResolutionCase::getCaseName).collect(Collectors.joining(", ", "\"", "\""));
            throw new ApplicationException(ErrorType.MERGE_RESOLUTION_UNKNOWN,
                    "Found multiple merge case resolutions for situation. Get their=\"" + their + "\", mine=\""
                            + mine + "\", payload=\"" + payload + "\", previous=\"" + previous
                            + "\", lineExists=\"" + (actualPayload != null) + "\". Found : " + names);
        }

        return found.get(0);
    }

    private static String renderDiffLine(PreparedIndexEntry entry) {
        return entry != null ? entry.toLogRendering() : " - null - ";
    }

    /**
     * Definition of one case of resolution, with requested result, for a merge
     */
    public static class ResolutionCase {

        private boolean lineExists;
        private String caseName;
        private IndexAction their;
        private IndexAction mine;
        private PayloadType payload;
        private PayloadType previous;
        private Result resolution;

        private String warning;

        public ResolutionCase() {
        }

        public boolean isLineExists() {
            return lineExists;
        }

        public void setLineExists(boolean lineExists) {
            this.lineExists = lineExists;
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

        public PayloadType getPrevious() {
            return previous;
        }

        public void setPrevious(PayloadType previous) {
            this.previous = previous;
        }

        public Result getResolution() {
            return resolution;
        }

        public void setResolution(Result resolution) {
            this.resolution = resolution;
        }

        public String getWarning() {
            return this.warning;
        }

        public void setWarning(String warning) {
            this.warning = warning;
        }

        public static class Result {

            private PayloadResultType payload;
            private PayloadResultType previous;
            private IndexAction action;
            private boolean needAction;

            public Result() {
            }

            public PayloadResultType getPayload() {
                return this.payload;
            }

            public void setPayload(PayloadResultType payload) {
                this.payload = payload;
            }

            public PayloadResultType getPrevious() {
                return this.previous;
            }

            public void setPrevious(PayloadResultType previous) {
                this.previous = previous;
            }

            public IndexAction getAction() {
                return this.action;
            }

            public void setAction(IndexAction action) {
                this.action = action;
            }

            public boolean isNeedAction() {
                return this.needAction;
            }

            public void setNeedAction(boolean needAction) {
                this.needAction = needAction;
            }

            public enum PayloadResultType {
                THEIR_PAYLOAD, MINE_PAYLOAD, THEIR_PREVIOUS, MINE_PREVIOUS, ACTUAL_CONTENT, ACTUAL_PREVIOUS
            }


        }

        public enum PayloadType {
            SIMILAR, DIFFERENT, ANY
        }
    }
}
