package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.CommitState;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class CommitDetails extends DiffContentHolder<PreparedIndexEntry> {

    private UUID uuid;

    private long indexSize;

    private String hash;

    private String originalUserEmail;

    private String comment;

    private CommitState state;

    private LocalDateTime createdTime;

    private LocalDateTime importedTime;

    private List<UUID> mergeSources;

    private String versionName;

    private String versionModelId;

    private List<AttachmentLine> attachments;

    // For attachments
    private boolean attachmentDisplaySupport;

    /**
     *
     */
    private CommitDetails(Map<UUID, DictionaryEntrySummary> referencedTables) {
        // Default : no content embedded, it is loaded from paginated search
        super(Collections.emptyList(), referencedTables);
    }

    public long getIndexSize() {
        return indexSize;
    }

    public void setIndexSize(long indexSize) {
        this.indexSize = indexSize;
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
    private void setUuid(UUID uuid) {
        this.uuid = uuid;
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
    private void setHash(String hash) {
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
    private void setOriginalUserEmail(String originalUserEmail) {
        this.originalUserEmail = originalUserEmail;
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
    private void setComment(String comment) {
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
    private void setCreatedTime(LocalDateTime createdTime) {
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
    private void setImportedTime(LocalDateTime importedTime) {
        this.importedTime = importedTime;
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
    private void setMergeSources(List<UUID> mergeSources) {
        this.mergeSources = mergeSources;
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
    private void setState(CommitState state) {
        this.state = state;
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
     * @return the attachmentDisplaySupport
     */
    public boolean isAttachmentDisplaySupport() {
        return this.attachmentDisplaySupport;
    }

    /**
     * @param attachmentDisplaySupport the attachmentDisplaySupport to set
     */
    public void setAttachmentDisplaySupport(boolean attachmentDisplaySupport) {
        this.attachmentDisplaySupport = attachmentDisplaySupport;
    }

    /**
     * @return
     */
    @Override
    public boolean isEmptyDiff() {
        return this.indexSize == 0;
    }

    /**
     * @param commit           entity of specified commit
     * @param indexSize real size of commit index
     * @param referencedTables all identified tables for this commit
     * @return Ready to display commit content
     */
    public static CommitDetails fromEntityAndContent(
            Commit commit,
            long indexSize,
            Map<UUID, DictionaryEntrySummary> referencedTables) {

        CommitDetails details = new CommitDetails(referencedTables);

        details.setIndexSize(indexSize);
        details.setComment(commit.getComment());
        details.setCreatedTime(commit.getCreatedTime());
        details.setHash(commit.getHash());
        details.setImportedTime(commit.getImportedTime());
        details.setState(commit.getState());
        details.setOriginalUserEmail(commit.getOriginalUserEmail());
        details.setUuid(commit.getUuid());
        details.setMergeSources(commit.getMergeSources());
        details.setVersionName(commit.getVersion().getName());
        details.setVersionModelId(commit.getVersion().getModelIdentity());

        return details;
    }
}
