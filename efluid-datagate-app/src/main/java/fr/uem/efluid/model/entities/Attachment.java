package fr.uem.efluid.model.entities;

import static fr.uem.efluid.utils.SharedOutputInputUtils.deserializeDataFromTmpFile;
import static fr.uem.efluid.utils.SharedOutputInputUtils.despecializePath;
import static fr.uem.efluid.utils.SharedOutputInputUtils.encodeB64ForFilename;
import static fr.uem.efluid.utils.SharedOutputInputUtils.mergeValues;
import static fr.uem.efluid.utils.SharedOutputInputUtils.newJson;
import static fr.uem.efluid.utils.SharedOutputInputUtils.serializeDataAsTmpFile;
import static fr.uem.efluid.utils.SharedOutputInputUtils.splitValues;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.*;

import fr.uem.efluid.model.Shared;
import fr.uem.efluid.tools.attachments.AttachmentProcessor;
import fr.uem.efluid.utils.SharedOutputInputUtils;
import org.hibernate.annotations.Type;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Entity
@Table(name = "attachments")
public class Attachment implements Shared, AttachmentProcessor.Compliant {

    @Id
    @Type(type = "uuid-char")
    private UUID uuid;

    private AttachmentType type;

    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Commit commit;

    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] data;

    private LocalDateTime importedTime;

    private LocalDateTime executeTime;

    @Transient
    private transient String tmpPath;

    /**
     * @return the uuid
     */
    @Override
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
     * @return the type
     */
    @Override
    public AttachmentType getType() {
        return this.type;
    }

    /**
     * @param type the type to set
     */
    public void setType(AttachmentType type) {
        this.type = type;
    }

    /**
     * @return the name
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the commit
     */
    public Commit getCommit() {
        return this.commit;
    }

    /**
     * @param commit the commit to set
     */
    public void setCommit(Commit commit) {
        this.commit = commit;
    }

    /**
     * @return the data
     */
    @Override
    public byte[] getData() {
        return this.data;
    }

    /**
     * @param data the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return the executeTime
     */
    public LocalDateTime getExecuteTime() {
        return this.executeTime;
    }

    /**
     * @param executeTime the executeTime to set
     */
    public void setExecuteTime(LocalDateTime executeTime) {
        this.executeTime = executeTime;
    }

    /**
     * @return the importedTime
     */
    @Override
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
     * @return the tmpPath
     */
    public String getTmpPath() {
        return this.tmpPath;
    }

    /**
     * @param tmpPath the tmpPath to set
     */
    public void setTmpPath(String tmpPath) {
        this.tmpPath = tmpPath;
    }

    /**
     * @see fr.uem.efluid.model.Shared#serialize()
     */
    @Override
    public String serialize() {

        String file = serializeDataAsTmpFile(
                new String[]{this.commit.getUuid().toString(), encodeB64ForFilename(this.getUuid().toString())}, this.data)
                .getFileName().toString();

        String content = newJson()
                .with("uid", getUuid())
                .with("com", getCommit().getUuid())
                .with("typ", getType().name())
                .with("nam", getName())
                .with("fil", file)
                .toString();

        return mergeValues(file, content);
    }

    /**
     * @see fr.uem.efluid.model.Shared#deserialize(java.lang.String)
     */
    @Override
    public void deserialize(String mixedContent) {

        // For Attachment the file is associated to the content in "mixed" form
        String[] rawParts = splitValues(mixedContent);

        String folder = rawParts[0];
        String content = rawParts[1];

        SharedOutputInputUtils.fromJson(content)
                .applyUUID("uid", u -> setUuid(u))
                .applyUUID("com", c -> setCommit(new Commit(c)))
                .applyString("typ", c -> setType(AttachmentType.valueOf(c)))
                .applyString("nam", n -> setName(n))
                .applyString("fil", f -> {
                    // Raw data is temp file path
                    Path path = despecializePath(folder + "/" + f);

                    // Get from file content
                    setData(deserializeDataFromTmpFile(path));

                    // Keep ref to tmp file
                    setTmpPath(f);
                });
    }

    /**
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
        Attachment other = (Attachment) obj;
        if (this.uuid == null) {
            if (other.uuid != null)
                return false;
        } else if (!this.uuid.equals(other.uuid))
            return false;
        return true;
    }

}
