package fr.uem.efluid.model.entities;

/**
 * <p>
 * Various states to identify the kind of commit
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public enum CommitState {


    /**
     * Commit was imported, without conflict / diff : simply applied, and kept locally
     */
    IMPORTED(7),

    /**
     * Commit was created as a merge from an imported commit. Ref to the imported is kept
     * in "mergeSource". The source is not saved locally, but seen as "applied" during a
     * merge
     */
    MERGED(8),

    /**
     * Commit was fully created locally
     */
    LOCAL(5);

    private final int processingSteps;

    CommitState(int st) {
        this.processingSteps = st;
    }

    public int getProcessingSteps() {
        return this.processingSteps;
    }
}
