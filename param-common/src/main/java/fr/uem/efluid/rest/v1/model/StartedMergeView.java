package fr.uem.efluid.rest.v1.model;

import fr.uem.efluid.services.types.PilotedCommitStatus;

import java.util.List;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class StartedMergeView {

    private final long packageCount;

    private final PilotedCommitStatus status;

    private final int attachementCount;

    private final List<AttachementView> attachements;

    /**
     * @param packageCount
     * @param status
     */
    public StartedMergeView(long packageCount, PilotedCommitStatus status, int attachementCount, List<AttachementView> attachements) {
        this.packageCount = packageCount;
        this.status = status;
        this.attachementCount = attachementCount;
        this.attachements = attachements;
    }

    /**
     * @return
     */
    public long getPackageCount() {
        return this.packageCount;
    }

    /**
     * @return
     */
    public PilotedCommitStatus getStatus() {
        return this.status;
    }

    /**
     * @return
     */
    public int getAttachementCount() {
        return this.attachementCount;
    }

    /**
     * @return
     */
    public List<AttachementView> getAttachements() {
        return this.attachements;
    }

    /**
     * @return
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "packageCount=" + this.packageCount;
    }

    /**
     *
     */
    public static class AttachementView {

        private final String name;
        private final String type;
        private final int size;

        public AttachementView(String name, String type, int size) {
            this.name = name;
            this.type = type;
            this.size = size;
        }

        public String getName() {
            return this.name;
        }

        public String getType() {
            return this.type;
        }

        public int getSize() {
            return this.size;
        }
    }

}
