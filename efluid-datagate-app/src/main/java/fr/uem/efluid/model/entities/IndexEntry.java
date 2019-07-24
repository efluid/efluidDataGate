package fr.uem.efluid.model.entities;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.utils.SharedOutputInputUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

/**
 * <p>
 * Define one line in the index/backlog. Associated to one dictionaryEntry (defining the
 * managed table), with one type of action, and indentified for one commit. Payload
 * content describe "one full line of managed table", in one value line. Column names and
 * type protection are "natural", values are always BASE64 encoded.
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
@Entity
@Table(name = "indexes")
public class IndexEntry implements DiffLine {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private DictionaryEntry dictionaryEntry;

    @NotNull
    @Enumerated(EnumType.STRING)
    private IndexAction action;

    @ManyToOne
    private Commit commit;

    @Lob
    private String payload;

    private String keyValue;

    private long timestamp;

    /**
     *
     */
    public IndexEntry() {
        super();
    }

    /**
     * @return the id
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the dictionaryEntry
     */
    public DictionaryEntry getDictionaryEntry() {
        return this.dictionaryEntry;
    }

    /**
     * @param dictionaryEntry the dictionaryEntry to set
     */
    public void setDictionaryEntry(DictionaryEntry dictionaryEntry) {
        this.dictionaryEntry = dictionaryEntry;
    }

    /**
     * @return the action
     */
    @Override
    public IndexAction getAction() {
        return this.action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(IndexAction action) {
        this.action = action;
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
     * @return the payload
     */
    @Override
    public String getPayload() {
        return this.payload;
    }

    /**
     * @param payload the payload to set
     */
    public void setPayload(String payload) {
        this.payload = payload;
    }

    /**
     * @return the keyValue
     */
    @Override
    public String getKeyValue() {
        return this.keyValue;
    }

    /**
     * @param keyValue the keyValue to set
     */
    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    /**
     * @return the timestamp
     */
    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return
     * @see DiffLine#getDictionaryEntryUuid()
     */
    @Override
    public UUID getDictionaryEntryUuid() {
        return this.dictionaryEntry.getUuid();
    }

    /**
     * For commit serialize
     *
     * @return
     */
    String serialize() {

        return SharedOutputInputUtils.newJson()
                .with("dic", getDictionaryEntryUuid())
                .with("act", getAction())
                .with("key", getKeyValue())
                .with("pay", getPayload())
                .with("tim", getTimestamp())
                .toString();
    }

    /**
     * For commit deserialize
     *
     * @param raw
     */
    void deserialize(String raw) {
        SharedOutputInputUtils.fromJson(raw)
                .applyUUID("dic", u -> setDictionaryEntry(new DictionaryEntry(u)))
                .applyString("act", a -> setAction(IndexAction.valueOf(a)))
                .applyString("key", this::setKeyValue)
                .applyString("pay", this::setPayload)
                .applyString("tim", t -> setTimestamp(Long.parseLong(t)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexEntry that = (IndexEntry) o;
        return Objects.equals(id, that.id) &&
                dictionaryEntry.equals(that.dictionaryEntry) &&
                keyValue.equals(that.keyValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dictionaryEntry, keyValue);
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "IdxEntry [<" + this.id + ">, dict:" + getDictionaryEntryUuid() + ", time:" + this.timestamp + ", chg:" + this.action + "@"
                + this.keyValue + "|" + this.payload + "]";
    }

}
