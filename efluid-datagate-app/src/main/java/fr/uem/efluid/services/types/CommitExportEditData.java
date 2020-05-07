package fr.uem.efluid.services.types;

import java.util.Map;
import java.util.UUID;

/**
 * Content of an export preparation of commits : selected export content, details on applied transformers, other details ...
 *
 * @author elecomte
 * @version 2
 * @since v1.2.0
 */
public class CommitExportEditData {

    private CommitSelectType type;

    private UUID selectedCommitUuid;

    private String selectedCommitComment;

    private String selectedCommitVersion;

    private Map<UUID, CustomTransformerConfiguration> specificTransformers;

    public CommitExportEditData() {
        super();
    }

    public CommitSelectType getCommitSelectType() {
        return type;
    }

    public void setCommitSelectType(CommitSelectType type) {
        this.type = type;
    }

    public UUID getSelectedCommitUuid() {
        return selectedCommitUuid;
    }

    public void setSelectedCommitUuid(UUID selectedCommitUuid) {
        this.selectedCommitUuid = selectedCommitUuid;
    }

    public String getSelectedCommitComment() {
        return selectedCommitComment;
    }

    public void setSelectedCommitComment(String selectedCommitComment) {
        this.selectedCommitComment = selectedCommitComment;
    }

    public String getSelectedCommitVersion() {
        return selectedCommitVersion;
    }

    public void setSelectedCommitVersion(String selectedCommitVersion) {
        this.selectedCommitVersion = selectedCommitVersion;
    }

    public Map<UUID, CustomTransformerConfiguration> getSpecificTransformers() {
        return specificTransformers;
    }

    public void setSpecificTransformers(Map<UUID, CustomTransformerConfiguration> specificTransformers) {
        this.specificTransformers = specificTransformers;
    }

    public enum CommitSelectType {

        RANGE_FROM, SINGLE_ONE;
    }

    public static class CustomTransformerConfiguration {
        private String configuration;
        private boolean disabled;

        public CustomTransformerConfiguration() {
            super();
        }

        public CustomTransformerConfiguration(String configuration) {
            this.configuration = configuration;
            this.disabled = false;
        }

        public String getConfiguration() {
            return configuration;
        }

        public void setConfiguration(String configuration) {
            this.configuration = configuration;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }
    }

}
