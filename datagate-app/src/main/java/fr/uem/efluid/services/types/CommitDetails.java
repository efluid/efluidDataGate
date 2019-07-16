package fr.uem.efluid.services.types;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.CommitState;
import fr.uem.efluid.model.entities.IndexEntry;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class CommitDetails {

	private UUID uuid;

	private String hash;

	private String originalUserEmail;

	private String comment;

	private CommitState state;
	
	private LocalDateTime createdTime;

	private LocalDateTime importedTime;

	private List<UUID> mergeSources;

	private List<DiffDisplay<PreparedIndexEntry>> content;

	private boolean tooMuchData;

	private long size;

	private String versionName;

	private String versionModelId;

	private List<AttachmentLine> attachments;

	// For attachments
	private boolean attachmentDisplaySupport;

	/**
	 * 
	 */
	private CommitDetails() {
		super();
	}

	/**
	 * @return the uuid
	 */
	public UUID getUuid() {
		return this.uuid;
	}

	/**
	 * @param uuid
	 *            the uuid to set
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
	 * @param hash
	 *            the hash to set
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
	 * @param originalUserEmail
	 *            the originalUserEmail to set
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
	 * @param comment
	 *            the comment to set
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
	 * @param createdTime
	 *            the createdTime to set
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
	 * @param importedTime
	 *            the importedTime to set
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
	 * @param mergeSources
	 *            the mergeSources to set
	 */
	private void setMergeSources(List<UUID> mergeSources) {
		this.mergeSources = mergeSources;
	}

	/**
	 * @return the content
	 */
	public List<DiffDisplay<PreparedIndexEntry>> getContent() {
		return this.content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	private void setContent(List<DiffDisplay<PreparedIndexEntry>> content) {
		this.content = content;
	}

	/**
	 * @return the state
	 */
	public CommitState getState() {
		return this.state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	private void setState(CommitState state) {
		this.state = state;
	}

	/**
	 * @return
	 */
	public boolean isEmptyDiff() {
		return this.content == null || this.content.stream().allMatch(d -> d.getDiff().isEmpty());
	}

	/**
	 * @return the tooMuchData
	 */
	public boolean isTooMuchData() {
		return this.tooMuchData;
	}

	/**
	 * @param tooMuchData
	 *            the tooMuchData to set
	 */
	public void setTooMuchData(boolean tooMuchData) {
		this.tooMuchData = tooMuchData;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return this.size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(long size) {
		this.size = size;
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
	 * @param versionModelId
	 *            the versionModelId to set
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
	 * @param attachments
	 *            the attachments to set
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
	 * @param commit
	 * @return
	 */
	public static CommitDetails fromEntity(Commit commit) {

		CommitDetails details = new CommitDetails();

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

	/**
	 * @param details
	 * @param index
	 */
	public static void completeIndex(CommitDetails details, List<IndexEntry> index) {

		// Using DiffDisplay for grouping index values
		details.setContent(index.stream()
				.map(PreparedIndexEntry::fromExistingEntity)
				.collect(Collectors.groupingBy(PreparedIndexEntry::getDictionaryEntryUuid))
				.entrySet().stream()
				.map(e -> {
					DiffDisplay<PreparedIndexEntry> diff = new DiffDisplay<>();
					diff.setDictionaryEntryUuid(e.getKey());
					diff.setDiff(e.getValue());
					return diff;
				}).collect(Collectors.toList()));
	}
}
