package fr.uem.efluid.model.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import fr.uem.efluid.model.Shared;
import fr.uem.efluid.utils.SharedOutputInputUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Entity
public class Commit implements Shared {

	@Id
	private UUID uuid;

	@NotBlank
	private String hash;

	@NotBlank
	private String originalUserEmail;

	private String comment;

	@NotNull
	private LocalDateTime createdTime;

	private LocalDateTime importedTime;

	private boolean imported;

	@ManyToOne(optional = false)
	private User user;

	@OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	private Collection<IndexEntry> index = new ArrayList<>();

	/**
	 * @param uuid
	 */
	public Commit(UUID uuid) {
		super();
		this.uuid = uuid;
	}

	/**
	 * 
	 */
	public Commit() {
		super();
	}

	/**
	 * @return the uuid
	 */
	@Override
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
	 * @param hash
	 *            the hash to set
	 */
	public void setHash(String hash) {
		this.hash = hash;
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
	@Override
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
	 * @return the imported
	 */
	public boolean isImported() {
		return this.imported;
	}

	/**
	 * @param imported
	 *            the imported to set
	 */
	public void setImported(boolean imported) {
		this.imported = imported;
	}

	/**
	 * @return the index
	 */
	public Collection<IndexEntry> getIndex() {
		return this.index;
	}

	/**
	 * @param index
	 *            the index to set
	 */
	public void setIndex(Collection<IndexEntry> index) {
		this.index = index;
	}

	/**
	 * @return
	 * @see fr.uem.efluid.model.Shared#serialize()
	 */
	@Override
	public String serialize() {

		return SharedOutputInputUtils.newJson()
				.with("uid", getUuid())
				.with("com", getComment())
				.with("cre", getCreatedTime())
				.with("has", getHash())
				.with("ema", getOriginalUserEmail())
				.with("idx", getIndex().stream().map(IndexEntry::serialize).collect(Collectors.joining("\n")))
				.toString();
	}

	/**
	 * @param raw
	 * @see fr.uem.efluid.model.Shared#deserialize(java.lang.String)
	 */
	@Override
	public void deserialize(String raw) {

		SharedOutputInputUtils.fromJson(raw)
				.apply("uid", UUID.class, u -> setUuid(u))
				.apply("com", String.class, c -> setComment(c))
				.apply("cre", LocalDateTime.class, c -> setCreatedTime(c))
				.apply("has", String.class, h -> setHash(h))
				.apply("ema", String.class, e -> setOriginalUserEmail(e))
				.apply("idx", String.class, i -> setIndex(Stream.of(i.split("\n")).map(s -> {
					IndexEntry ent = new IndexEntry();
					ent.deserialize(s);
					return ent;
				}).collect(Collectors.toList())));
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.uuid == null) ? 0 : this.uuid.hashCode());
		return result;
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Commit other = (Commit) obj;
		if (this.uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!this.uuid.equals(other.uuid))
			return false;
		return true;
	}

}
