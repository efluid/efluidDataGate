package fr.uem.efluid.services.types;

import java.util.Map;
import java.util.UUID;

/**
 * Content of an export preparation of commits : selected export content, details on applied transformers, other details ...
 *
 * @author elecomte
 * @since v1.2.0
 */
public class CommitExportEditData {

    private CommitSelectType type;

    private UUID selectedCommitUuid;

    private String selectedCommitComment;

    private String selectedCommitVersion;

    private Map<UUID, String> specificTransformerConfigurations;

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

    public Map<UUID, String> getSpecificTransformerConfigurations() {
        return specificTransformerConfigurations;
    }

    public String getSelectedCommitVersion() {
        return selectedCommitVersion;
    }

    public void setSelectedCommitVersion(String selectedCommitVersion) {
        this.selectedCommitVersion = selectedCommitVersion;
    }

    public void setSpecificTransformerConfigurations(Map<UUID, String> specificTransformerConfigurations) {
        this.specificTransformerConfigurations = specificTransformerConfigurations;
    }

    public enum CommitSelectType {

        RANGE_FROM, SINGLE_ONE;
    }

}
