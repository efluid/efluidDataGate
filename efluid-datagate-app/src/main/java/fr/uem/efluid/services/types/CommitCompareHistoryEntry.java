package fr.uem.efluid.services.types;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.CommitState;

import java.util.List;
import java.util.UUID;

/**
 * For value history in compare
 */
public class CommitCompareHistoryEntry extends PreparedIndexEntry {

    private boolean inComparedCommit;

    private CommitEditData commitDetails;

    public boolean isInComparedCommit() {
        return inComparedCommit;
    }

    public void setInComparedCommit(boolean inComparedCommit) {
        this.inComparedCommit = inComparedCommit;
    }

    public CommitEditData getCommitDetails() {
        return commitDetails;
    }

    public void setCommitDetails(CommitEditData commitDetails) {
        this.commitDetails = commitDetails;
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
    public static CommitCompareHistoryEntry fromCombined(DiffLine combined, String tableName, String hrPayload) {

        CommitCompareHistoryEntry data = new CommitCompareHistoryEntry();

        completeFromDiffLine(data, combined);

        data.setTableName(tableName);
        data.setHrPayload(hrPayload);

        return data;
    }
}
