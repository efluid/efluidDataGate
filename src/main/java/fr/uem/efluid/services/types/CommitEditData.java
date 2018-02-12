package fr.uem.efluid.services.types;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import fr.uem.efluid.model.entities.Commit;

/**
 * <p>
 * To identify the commit currently processing in a merge process
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class CommitEditData {

	private UUID uuid;

	private String hash;

	private String originalUserEmail;

	private String comment;

	private LocalDateTime createdTime;

	private LocalDateTime importedTime;

	private List<UUID> mergeSources;

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
	 * @param uuid
	 *            the uuid to set
	 */
	public void setUuid(UUID uuid) {
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
	 * @param originalUserEmail
	 *            the originalUserEmail to set
	 */
	public void setOriginalUserEmail(String originalUserEmail) {
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
	 * @param createdTime
	 *            the createdTime to set
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
	 * @param importedTime
	 *            the importedTime to set
	 */
	public void setImportedTime(LocalDateTime importedTime) {
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
	public void setMergeSources(List<UUID> mergeSources) {
		this.mergeSources = mergeSources;
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
		edit.setMergeSources(commit.getMergeSources());
		edit.setOriginalUserEmail(commit.getOriginalUserEmail());
		edit.setUuid(commit.getUuid());

		return edit;
	}
}
