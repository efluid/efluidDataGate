package fr.uem.efluid.model.entities;

import fr.uem.efluid.model.Shared;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Model of an active transformer for current project, with configuration and transformer type
 */
@Entity
@Table(name = "transformers")
public class TransformerDesc implements Shared {

    @Id
    @Type(type = "uuid-char")
    private UUID uuid;

    @NotNull
    private LocalDateTime createdTime;

    @NotNull
    private LocalDateTime updatedTime;

    private LocalDateTime importedTime;

    @NotNull
    private String type;

    @ManyToOne(optional = false)
    private Project project;

    @Lob
    private String configuration;

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public LocalDateTime getImportedTime() {
        return importedTime;
    }

    public void setImportedTime(LocalDateTime importedTime) {
        this.importedTime = importedTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Override
    public String serialize() {
        return null;
    }

    @Override
    public void deserialize(String raw) {

    }
}
