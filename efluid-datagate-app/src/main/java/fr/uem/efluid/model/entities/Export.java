package fr.uem.efluid.model.entities;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * A commit export : details on selected commit(s) and associated transformerDef customizations
 *
 * @author elecomte
 * @version 1
 * @since v1.1.0
 */
@Entity
@Table(name = "exports")
public class Export {

    @Id
    @Type(type = "uuid-char")
    private UUID uuid;

    @NotNull
    private String filename;

    @NotNull
    private LocalDateTime createdTime;

    private LocalDateTime downloadedTime;

    @ManyToOne(optional = false)
    private Project project;

    @ManyToOne(optional = false)
    private Commit startCommit;

    @ManyToOne(optional = false)
    private Commit endCommit;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "export_uuid")
    private Collection<ExportTransformer> transformers = new ArrayList<>();

    public Export() {
    }

    public Export(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getDownloadedTime() {
        return downloadedTime;
    }

    public void setDownloadedTime(LocalDateTime downloadedTime) {
        this.downloadedTime = downloadedTime;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Commit getStartCommit() {
        return startCommit;
    }

    public void setStartCommit(Commit startCommit) {
        this.startCommit = startCommit;
    }

    public Commit getEndCommit() {
        return endCommit;
    }

    public void setEndCommit(Commit endCommit) {
        this.endCommit = endCommit;
    }

    public Collection<ExportTransformer> getTransformers() {
        return transformers;
    }

    public void setTransformers(Collection<ExportTransformer> transformers) {
        this.transformers = transformers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Export export = (Export) o;
        return Objects.equals(uuid, export.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
