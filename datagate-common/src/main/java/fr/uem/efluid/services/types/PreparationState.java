package fr.uem.efluid.services.types;

/**
 * Detail on Async preparation state : current status and percent Done
 */
public class PreparationState {

    private final PilotedCommitStatus status;
    private final int percentDone;

    public PreparationState(PilotedCommitStatus status, int percentDone) {
        this.status = status;
        this.percentDone = percentDone;
    }

    public PilotedCommitStatus getStatus() {
        return status;
    }

    public int getPercentDone() {
        return percentDone;
    }
}
