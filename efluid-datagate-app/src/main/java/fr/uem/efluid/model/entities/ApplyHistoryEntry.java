package fr.uem.efluid.model.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * <p>
 * Basic saving of every "apply" operations
 * </p>
 *
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Entity
@Table(name = "apply_history")
public class ApplyHistoryEntry {

	@Id
	@GeneratedValue
	private Long id;

	@NotNull
	@Enumerated(EnumType.STRING)
	private ApplyType type;

	@Lob
	private String query;

	@ManyToOne(optional = false)
	private User user;

	@ManyToOne
	private Commit commit;

	private Long timestamp;

	// Optional - weak link
	private UUID attachmentSourceUuid;

	private UUID projectUuid;

	/**
	 *
	 */
	public ApplyHistoryEntry() {
		super();
	}

	/**
	 *
	 */
	public ApplyHistoryEntry(String query) {
		super();
		this.query = query;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return this.id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	public ApplyType getType() {
		return type;
	}

	public void setType(ApplyType type) {
		this.type = type;
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		return this.query;
	}

	/**
	 * @param query
	 *            the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return this.user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the commit
	 */
	public Commit getCommit() {
		return this.commit;
	}

	/**
	 * @param commit
	 *            the commit to set
	 */
	public void setCommit(Commit commit) {
		this.commit = commit;
	}

	/**
	 * @return the timestamp
	 */
	public Long getTimestamp() {
		return this.timestamp;
	}

	/**
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the attachmentSourceUuid
	 */
	public UUID getAttachmentSourceUuid() {
		return this.attachmentSourceUuid;
	}

	/**
	 * @param attachmentSourceUuid
	 *            the attachmentSourceUuid to set
	 */
	public void setAttachmentSourceUuid(UUID attachmentSourceUuid) {
		this.attachmentSourceUuid = attachmentSourceUuid;
	}

	/**
	 * @return the projectUuid
	 */
	public UUID getProjectUuid() {
		return this.projectUuid;
	}

	/**
	 * @param projectId
	 *  the projectId to set
	 */
	public void setProjectUuid(UUID projectId) {
		this.projectUuid = projectId;
	}
}
