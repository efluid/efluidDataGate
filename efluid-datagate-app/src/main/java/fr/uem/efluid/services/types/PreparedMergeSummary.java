package fr.uem.efluid.services.types;

/**
 * Summary of the merge process, with details on processed changes and changes which was already present in current instance
 *
 * @author elecomte
 * @version 1
 * @since v3.1.11
 */
public class PreparedMergeSummary extends PreparedDiffSummary {

    private int alreadyTheirUpdates;
    private int alreadyTheirAdds;
    private int alreadyTheirDeletes;

    public int getAlreadyTheirUpdates() {
        return alreadyTheirUpdates;
    }

    public void setAlreadyTheirUpdates(int alreadyTheirUpdates) {
        this.alreadyTheirUpdates = alreadyTheirUpdates;
    }

    public int getAlreadyTheirAdds() {
        return alreadyTheirAdds;
    }

    public void setAlreadyTheirAdds(int alreadyTheirAdds) {
        this.alreadyTheirAdds = alreadyTheirAdds;
    }

    public int getAlreadyTheirDeletes() {
        return alreadyTheirDeletes;
    }

    public void setAlreadyTheirDeletes(int alreadyTheirDeletes) {
        this.alreadyTheirDeletes = alreadyTheirDeletes;
    }
}
