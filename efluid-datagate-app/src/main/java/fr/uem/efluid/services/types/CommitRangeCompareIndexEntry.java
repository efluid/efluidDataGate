package fr.uem.efluid.services.types;

import fr.uem.efluid.model.DiffLine;

import java.util.List;
import java.util.UUID;

/**
 * Model of diff display for
 *
 * @author elecomte
 * @version 1
 * @since v3.1.11
 */
public class CommitRangeCompareIndexEntry extends CommitCompareIndexEntry {

    private CommitRangeCompareIndexEntry(List<UUID> identifiedCommitUuids) {
        super(identifiedCommitUuids);
    }

    @Override
    public PreparedIndexEntry getContentByCommit(UUID commitUuid) {
        // The same for all commits as it is a combined line
        return this;
    }

    /**
     * <p>
     * For combining process : minimal rendering, with support of hr payload for rendering
     * </p>
     *
     * @param combined
     * @param hrPayload
     * @return
     */
    public static CommitRangeCompareIndexEntry fromCombined(DiffLine combined, String tableName, String hrPayload, List<UUID> identifiedCommitUuids) {

        CommitRangeCompareIndexEntry data = new CommitRangeCompareIndexEntry(identifiedCommitUuids);

        data.setAction(combined.getAction());
        data.setDictionaryEntryUuid(combined.getDictionaryEntryUuid());
        data.setTableName(tableName);
        data.setKeyValue(combined.getKeyValue());
        data.setHrPayload(hrPayload);
        data.setTimestamp(combined.getTimestamp());

        return data;
    }

}
