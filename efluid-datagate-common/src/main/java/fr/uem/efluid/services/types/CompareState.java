package fr.uem.efluid.services.types;

/**
 * Detail on Async commit compare state : current status and percent Done
 */
public class CompareState {

    private final CommitCompareStatus status;
    private final int percentDone;

    public CompareState(CommitCompareStatus status, int percentDone) {
        this.status = status;
        this.percentDone = percentDone;
    }

    public CommitCompareStatus getStatus() {
        return status;
    }

    public int getPercentDone() {
        return percentDone;
    }
}
