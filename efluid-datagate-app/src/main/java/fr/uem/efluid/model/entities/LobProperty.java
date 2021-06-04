package fr.uem.efluid.model.entities;

import static fr.uem.efluid.utils.SharedOutputInputUtils.*;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.*;

import fr.uem.efluid.model.Shared;

/**
 * <p>
 * For LOB properties managed in index : stored in raw format in dedicated entity, using a
 * reference identifier when required
 * </p>
 * <p>
 * Specified as a <tt>Shared</tt> entity, but identified by its hash and associated
 * DictionaryEntry only : so doesn't use UUID for serialization / deserialization. Export
 * is done as "associated file", with one file for each LOB Property
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Entity
@Table(name = "lobs")
public class LobProperty implements Shared {

	@Id
	@GeneratedValue
	private Long id;

	private String hash;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Commit commit;

	@Lob
	@Column(columnDefinition = "BLOB")
	private byte[] data;

	/**
	 * 
	 */
	public LobProperty() {
		super();
	}

	/**
	 * @return the data
	 */
	public byte[] getData() {
		return this.data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(byte[] data) {
		this.data = data;
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
	 * @see fr.uem.efluid.model.Shared#getUuid()
	 */
	@Override
	public UUID getUuid() {
		// Not used
		return null;
	}

	/**
	 * @see fr.uem.efluid.model.Shared#getImportedTime()
	 */
	@Override
	public LocalDateTime getImportedTime() {
		// Not used
		return null;
	}

	/**
	 * @see fr.uem.efluid.model.Shared#serialize()
	 */
	@Override
	public String serialize() {
		return serializeDataAsTmpFile(
				new String[] { this.commit.getUuid().toString(), encodeB64ForFilename(this.getHash()) }, this.data)
						.getFileName().toString();
	}

	/**
	 * @see fr.uem.efluid.model.Shared#deserialize(java.lang.String)
	 */
	@Override
	public void deserialize(String raw) {

		// Raw data is temp file path
		Path path = despecializePath(raw);
		String[] parts = pathNameParts(path);

		// Get from filename
		this.commit = new Commit(UUID.fromString(parts[0]));
		this.hash = decodeB64ForFilename(parts[1]);

		// Get from file content
		this.data = deserializeDataFromTmpFile(path);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.hash == null) ? 0 : this.hash.hashCode());
		return result;
	}

	/**
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
		LobProperty other = (LobProperty) obj;
		if (this.hash == null) {
			if (other.hash != null)
				return false;
		} else if (!this.hash.equals(other.hash))
			return false;
		return true;
	}

}
