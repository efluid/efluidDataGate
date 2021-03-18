package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.CommitState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * To identify the commit currently processing in a merge process
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class CommitEditData {

    private UUID uuid;

    private String hash;

    private String originalUserEmail;

    private String comment;

    private CommitState state;

    private LocalDateTime createdTime;

    private LocalDateTime importedTime;

    private List<UUID> mergeSources;

    private String domainNames;

    private LocalDateTime rangeStartTime;

    private String versionName;

    private String versionModelId;

    private List<AttachmentLine> attachments;

    private UUID revertSourceCommitUuid;

    /**
     *
     */
    public CommitEditData() {
        super();
    }

    /**
     * @return the uuid
     */
    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the domainNames
     */
    public String getDomainNames() {
        return this.domainNames;
    }

    public UUID getRevertSourceCommitUuid() {
        return revertSourceCommitUuid;
    }

    public void setRevertSourceCommitUuid(UUID revertSourceCommitUuid) {
        this.revertSourceCommitUuid = revertSourceCommitUuid;
    }

    /**
     * @param domainNames the domainNames to set
     */
    public void setDomainNames(String domainNames) {
        this.domainNames = domainNames;
    }

    /**
     * @return the hash
     */
    public String getHash() {
        return this.hash;
    }

    /**
     * @param hash the hash to set
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * @return the originalUserEmail
     */
    public String getOriginalUserEmail() {
        return this.originalUserEmail;
    }

    /**
     * @param originalUserEmail the originalUserEmail to set
     */
    public void setOriginalUserEmail(String originalUserEmail) {
        this.originalUserEmail = originalUserEmail;
    }

    /**
     * @return the state
     */
    public CommitState getState() {
        return this.state;
    }

    /**
     * @param state the state to set
     */
    public void setState(CommitState state) {
        this.state = state;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return the createdTime
     */
    public LocalDateTime getCreatedTime() {
        return this.createdTime;
    }

    /**
     * @param createdTime the createdTime to set
     */
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * @return the importedTime
     */
    public LocalDateTime getImportedTime() {
        return this.importedTime;
    }

    /**
     * @param importedTime the importedTime to set
     */
    public void setImportedTime(LocalDateTime importedTime) {
        this.importedTime = importedTime;
    }

    /**
     * @return
     */
    public String getVersionName() {
        return this.versionName;
    }

    /**
     * @param versionName
     */
    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    /**
     * @return the mergeSources
     */
    public List<UUID> getMergeSources() {
        return this.mergeSources;
    }

    /**
     * @param mergeSources the mergeSources to set
     */
    public void setMergeSources(List<UUID> mergeSources) {
        this.mergeSources = mergeSources;
    }

    /**
     * @return the rangeStartTime
     */
    public LocalDateTime getRangeStartTime() {
        return this.rangeStartTime;
    }

    /**
     * @param rangeStartTime the rangeStartTime to set
     */
    public void setRangeStartTime(LocalDateTime rangeStartTime) {
        this.rangeStartTime = rangeStartTime;
    }

    /**
     * @return the versionModelId
     */
    public String getVersionModelId() {
        return this.versionModelId;
    }

    /**
     * @param versionModelId the versionModelId to set
     */
    public void setVersionModelId(String versionModelId) {
        this.versionModelId = versionModelId;
    }

    /**
     * @return the attachments
     */
    public List<AttachmentLine> getAttachments() {
        return this.attachments;
    }

    /**
     * @param attachments the attachments to set
     */
    public void setAttachments(List<AttachmentLine> attachments) {
        this.attachments = attachments;
    }

    /**
     * @param commit
     * @return
     */
    public static CommitEditData fromEntity(Commit commit) {

        CommitEditData edit = new CommitEditData();

        edit.setComment(commit.getComment());
        edit.setCreatedTime(commit.getCreatedTime());
        edit.setImportedTime(commit.getImportedTime());
        edit.setState(commit.getState());
        edit.setMergeSources(commit.getMergeSources());
        edit.setOriginalUserEmail(commit.getOriginalUserEmail());
        edit.setUuid(commit.getUuid());
        edit.setVersionName(commit.getVersion().getName());
        edit.setVersionModelId(commit.getVersion().getModelIdentity());

        if (commit.getRevertSource() != null) {
            edit.setRevertSourceCommitUuid(commit.getRevertSource().getUuid());
        }

        return edit;
    }

    /**
     * @param edit
     * @return
     */
    public static Commit toEntity(CommitEditData edit) {

        Commit commit = new Commit();

        commit.setComment(edit.getComment());
        commit.setCreatedTime(edit.getCreatedTime());
        commit.setImportedTime(edit.getImportedTime());
        if (edit.getMergeSources() != null && edit.getMergeSources().size() > 0) {
            commit.setMergeSources(edit.getMergeSources());
        }
        commit.setOriginalUserEmail(edit.getOriginalUserEmail());
        commit.setUuid(edit.getUuid());
        if (edit.getRevertSourceCommitUuid() != null) {
            commit.setRevertSource(new Commit(edit.getRevertSourceCommitUuid()));
        }
        return commit;
    }
}
