package fr.uem.efluid.services.types;

/**
 * Summary of the diff process, with details on processed changes
 *
 * @author elecomte
 * @version 1
 * @since v3.1.11
 */
public class PreparedDiffSummary {

    private int identifiedUpdates;
    private int identifiedAdds;
    private int identifiedDeletes;
    private long durationSeconds;

    public int getIdentifiedUpdates() {
        return identifiedUpdates;
    }

    public void setIdentifiedUpdates(int identifiedUpdates) {
        this.identifiedUpdates = identifiedUpdates;
    }

    public int getIdentifiedAdds() {
        return identifiedAdds;
    }

    public void setIdentifiedAdds(int identifiedAdds) {
        this.identifiedAdds = identifiedAdds;
    }

    public int getIdentifiedDeletes() {
        return identifiedDeletes;
    }

    public void setIdentifiedDeletes(int identifiedDeletes) {
        this.identifiedDeletes = identifiedDeletes;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
}
