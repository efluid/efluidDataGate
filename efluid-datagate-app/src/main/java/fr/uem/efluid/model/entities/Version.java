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


    @Lob // Stored export of version content - domains, regenerated at init / import except for commit exports
    @Column(columnDefinition = "CLOB")
    private String domainsContent;

    @Lob // Stored export of version content - dict, regenerated at init / import except for commit exports
    @Column(columnDefinition = "CLOB")
    private String dictionaryContent;

    @Lob // Stored export of version content - links, regenerated at init / import except for commit exports
    @Column(columnDefinition = "CLOB")
    private String linksContent;

    @Lob // Stored export of version content - mappings, regenerated at init / import except for commit exports
    @Column(columnDefinition = "CLOB")
    private String mappingsContent;

    @Transient
    private transient boolean serializeDictionaryContents;

    /**
     * @param uuid forced uuid
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
    @Override
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

    public String getDomainsContent() {
        return domainsContent;
    }

    public void setDomainsContent(String domainsContent) {
        this.domainsContent = domainsContent;
    }

    public String getDictionaryContent() {
        return dictionaryContent;
    }

    public void setDictionaryContent(String dictionaryContent) {
        this.dictionaryContent = dictionaryContent;
    }

    public String getLinksContent() {
        return linksContent;
    }

    public void setLinksContent(String linksContent) {
        this.linksContent = linksContent;
    }

    public String getMappingsContent() {
        return mappingsContent;
    }

    public void setMappingsContent(String mappingsContent) {
        this.mappingsContent = mappingsContent;
    }

    public void setSerializeDictionaryContents(boolean serializeDictionaryContents) {
        this.serializeDictionaryContents = serializeDictionaryContents;
    }

    public boolean isSerializeDictionaryContents() {
        return serializeDictionaryContents;
    }

    /**
     * @see fr.uem.efluid.model.Shared#deserialize(java.lang.String)
     */
    @Override
    public void deserialize(String raw) {

        SharedOutputInputUtils.fromJson(raw)
                .applyUUID("uid", this::setUuid)
                .applyLdt("cre", this::setCreatedTime)
                .applyLdt("upd", this::setUpdatedTime)
                .applyString("nam", this::setName)
                .applyUUID("pro", v -> setProject(new Project(v)))
                .applyString("idn", this::setModelIdentity)
                .applyString("ddo", this::setDomainsContent)
                .applyString("ddi", this::setDictionaryContent)
                .applyString("dli", this::setLinksContent)
                .applyString("dma", this::setMappingsContent);
    }

    /**
     * @return
     * @see fr.uem.efluid.model.Shared#serialize()
     */
    @Override
    public String serialize() {

        // Variation of export for commit export, with dict content
        if (this.serializeDictionaryContents) {

            return SharedOutputInputUtils.newJson()
                    .with("uid", getUuid())
                    .with("cre", getCreatedTime())
                    .with("upd", getUpdatedTime())
                    .with("nam", getName())
                    .with("pro", getProject().getUuid())
                    .with("idn", getModelIdentity())
                    .with("ddo", getDomainsContent())
                    .with("ddi", getDictionaryContent())
                    .with("dli", getLinksContent())
                    .with("dma", getMappingsContent())
                    .toString();
        }

        // No content => Default
        return super.serialize();
    }
}
