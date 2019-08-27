package fr.uem.efluid.model.entities;

import fr.uem.efluid.model.shared.ExportAwareVersion;
import fr.uem.efluid.utils.SharedOutputInputUtils;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * <p>
 * Identified versions in a project. Associated to dictionary data
 * </p>
 * <p>
 * Versioning is not managed as a complete entity tree timestamped model, but reserved only for
 * detail validation and version compare.
 * The content of an updated version is therefore specified using <code>versionContent</code> property,
 * where an "export like" content of the dictionary model is stored
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.2.0
 */
@Entity
@Table(name = "versions")
public class Version extends ExportAwareVersion<Project> {

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

    @ManyToOne(optional = false)
    private Project project;

    private String modelIdentity;

    @Lob // Stored export of version content, generated at init / import
    private String versionContent;

    /**
     * @param uuid
     */
    public Version(UUID uuid) {
        super();
        this.uuid = uuid;
    }

    /**
     *
     */
    public Version() {
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
     * @param uuid the uuid to set
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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
     * @return the createdTime
     */
    @Override
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
     * @return the updatedTime
     */
    @Override
    public LocalDateTime getUpdatedTime() {
        return this.updatedTime;
    }

    /**
     * @param updatedTime the updatedTime to set
     */
    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
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
     * @return the project
     */
    @Override
    public Project getProject() {
        return this.project;
    }

    /**
     * @param project the project to set
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * @return the modelIdentity
     */
    @Override
    public String getModelIdentity() {
        return this.modelIdentity;
    }

    /**
     * @param modelIdentity the modelIdentity to set
     */
    public void setModelIdentity(String modelIdentity) {
        this.modelIdentity = modelIdentity;
    }

    public String getVersionContent() {
        return this.versionContent;
    }

    public void setVersionContent(String versionContent) {
        this.versionContent = versionContent;
    }

    /**
     * @param raw
     * @see fr.uem.efluid.model.Shared#deserialize(java.lang.String)
     */
    @Override
    public void deserialize(String raw) {

        SharedOutputInputUtils.fromJson(raw)
                .applyUUID("uid", v -> setUuid(v))
                .applyLdt("cre", v -> setCreatedTime(v))
                .applyLdt("upd", v -> setUpdatedTime(v))
                .applyString("nam", v -> setName(v))
                .applyUUID("pro", v -> setProject(new Project(v)))
                .applyString("idn", v -> setModelIdentity(v));
    }

}
