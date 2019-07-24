package fr.uem.efluid.model.entities;

import fr.uem.efluid.model.Shared;
import fr.uem.efluid.utils.SharedOutputInputUtils;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@Entity
@Table(name = "commits")
public class Commit implements Shared {

    @Id
    @Type(type = "uuid-char")
    private UUID uuid;

    @Column(name = "hashv")
    private String hash;

    @NotBlank
    private String originalUserEmail;

    @Column(name = "comments")
    private String comment;

    @NotNull
    private LocalDateTime createdTime;

    private LocalDateTime importedTime;

    @Enumerated(EnumType.STRING)
    private CommitState state;

    @ManyToOne(optional = false)
    private User user;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "commit_uuid")
    private Collection<IndexEntry> index = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private List<UUID> mergeSources = new ArrayList<>();

    @ManyToOne(optional = false)
    private Project project;

    @ManyToOne(optional = false)
    private Version version;

    @Transient
    private transient boolean refOnly = false;

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
     * @param uuid the uuid to set
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
     * @param originalUserEmail the originalUserEmail to set
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
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @param hash the hash to set
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
     * @param comment the comment to set
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
     * @param createdTime the createdTime to set
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
     * @param importedTime the importedTime to set
     */
    public void setImportedTime(LocalDateTime importedTime) {
        this.importedTime = importedTime;
    }

    /**
     * @return the index
     */
    public Collection<IndexEntry> getIndex() {
        return this.index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(Collection<IndexEntry> index) {
        this.index = index;
    }

    /**
     * @return the state
     */
    public CommitState getState() {
        return this.state;
    }

    /**
     * @param state the state to set
     */
    public void setState(CommitState state) {
        this.state = state;
    }

    /**
     * @return the mergeSources
     */
    public List<UUID> getMergeSources() {
        return this.mergeSources;
    }

    /**
     * @param mergeSources the mergeSources to set
     */
    public void setMergeSources(List<UUID> mergeSources) {
        this.mergeSources = mergeSources;
    }

    /**
     * @return the project
     */
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
     * @return
     */
    public Version getVersion() {
        return this.version;
    }

    /**
     * @param version
     */
    public void setVersion(Version version) {
        this.version = version;
    }

    /**
     * @return the refOnly
     */
    public boolean isRefOnly() {
        return this.refOnly;
    }

    /**
     *
     */
    public void setAsRefOnly() {
        this.refOnly = true;
    }

    /**
     * @return
     * @see fr.uem.efluid.model.Shared#serialize()
     */
    @Override
    public String serialize() {

        // For reference only, doesn't includes index content
        if (this.refOnly) {
            return SharedOutputInputUtils.newJson()
                    .with("uid", getUuid())
                    .with("cre", getCreatedTime())
                    .with("has", getHash())
                    .with("ema", getOriginalUserEmail())
                    .with("pro", getProject().getUuid())
                    .with("ver", getVersion().getUuid())
                    .toString();
        }

        // Else, includes all commit index as sub item
        return SharedOutputInputUtils.newJson()
                .with("uid", getUuid())
                .with("com", getComment())
                .with("cre", getCreatedTime())
                .with("has", getHash())
                .with("ema", getOriginalUserEmail())
                .with("pro", getProject().getUuid())
                .with("ver", getVersion().getUuid())
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
                .applyUUID("uid", this::setUuid)
                .applyString("com", this::setComment)
                .applyLdt("cre", this::setCreatedTime)
                .applyString("has", this::setHash)
                .applyString("ema", this::setOriginalUserEmail)
                .applyString("idx", i -> {
                    // No idx item at all => Ref
                    if (i == null) {
                        setAsRefOnly();
                    }

                    // Empty => Empty idx list
                    else if ("".equals(i)) {
                        // No content
                    }

                    // Process content
                    else {
                        setIndex(Stream.of(i.split("\n")).map(s -> {
                            IndexEntry ent = new IndexEntry();
                            ent.deserialize(s);
                            return ent;
                        }).collect(Collectors.toList()));
                    }
                })
                .applyUUID("pro", v -> setProject(new Project(v)))
                .applyUUID("ver", v -> setVersion(new Version(v)));
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

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Commit [<" + this.uuid + ">\"" + this.comment + "\", by:" + this.originalUserEmail + ", create:" + this.createdTime
                + ", imported:" + this.importedTime + "]";
    }

}
