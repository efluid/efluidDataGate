package fr.uem.efluid.model.entities;

import fr.uem.efluid.model.Shared;
import fr.uem.efluid.utils.SharedOutputInputUtils;
import org.hibernate.annotations.Type;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

import static fr.uem.efluid.utils.SharedOutputInputUtils.*;

/**
 * Model of an active transformer for current project, with configuration and transformer type
 *
 * @author elecomte
 * @version 1
 * @since v1.2.0
 */
@Entity
@Table(name = "transformers")
public class TransformerDef implements Shared {

    @Id
    @Type(type = "uuid-char")
    private UUID uuid;

    @NotNull
    private String name;

    @NotNull
    private LocalDateTime createdTime;

    @NotNull
    private LocalDateTime updatedTime;

    private LocalDateTime importedTime;

    private LocalDateTime deletedTime;

    private int priority;

    @NotNull
    private String type;

    @ManyToOne(optional = false)
    private Project project;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String configuration;

    @Transient
    private transient String customizedConfiguration;

    @Transient
    private transient byte[] attachmentPackage;

    public TransformerDef() {
        super();
    }

    public TransformerDef(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedTime() {
        return this.createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return this.updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public LocalDateTime getImportedTime() {
        return this.importedTime;
    }

    public void setImportedTime(LocalDateTime importedTime) {
        this.importedTime = importedTime;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Project getProject() {
        return this.project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getConfiguration() {
        return this.configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getCustomizedConfiguration() {
        return customizedConfiguration;
    }

    public LocalDateTime getDeletedTime() {
        return this.deletedTime;
    }

    public void setDeletedTime(LocalDateTime deletedTime) {
        this.deletedTime = deletedTime;
    }

    public void setCustomizedConfiguration(String customizedConfiguration) {
        this.customizedConfiguration = customizedConfiguration;
    }

    public byte[] getAttachmentPackage() {
        return attachmentPackage;
    }

    public void setAttachmentPackage(byte[] attachmentPackage) {
        this.attachmentPackage = attachmentPackage;
    }

    @Override
    public String serialize() {

        String attachment = "";

        if (getAttachmentPackage() != null) {
            attachment = serializeDataAsTmpFile(
                    new String[]{"transformer-attachments", encodeB64ForFilename(getUuid().toString())}, getAttachmentPackage())
                    .getFileName().toString();
        }

        String content = SharedOutputInputUtils.newJson()
                .with("uid", getUuid())
                .with("nam", getName())
                .with("cre", getCreatedTime())
                .with("upd", getUpdatedTime())
                .with("pri", getPriority())
                .with("typ", getType())
                .with("pro", getProject().getUuid())
                // Customized has priority but we keep state "customized"
                .with("isc", getCustomizedConfiguration() != null)
                .with("con", getCustomizedConfiguration() != null ? getCustomizedConfiguration() : getConfiguration())
                .with("att", attachment)
                .toString();

        return mergeValues(attachment, content);
    }

    @Override
    public void deserialize(String mixedContent) {

        // For optional Attachment the filename is associated to the content in "mixed" form
        String[] rawParts = splitValues(mixedContent);

        String folder = rawParts[0];
        String content = rawParts[1];

        SharedOutputInputUtils.fromJson(content)
                .applyUUID("uid", this::setUuid)
                .applyString("nam", this::setName)
                .applyLdt("cre", this::setCreatedTime)
                .applyLdt("upd", this::setUpdatedTime)
                .applyInt("pri", this::setPriority)
                .applyString("typ", this::setType)
                .applyUUID("pro", v -> setProject(new Project(v)))
                .applyString("con", this::setConfiguration)
                .applyString("att", f -> {
                    if (StringUtils.hasText(f)) {
                        // Raw data is temp file path
                        Path path = despecializePath(folder + "/" + f);

                        // Get from file content
                        setAttachmentPackage(deserializeDataFromTmpFile(path));
                    }
                });
    }
}
