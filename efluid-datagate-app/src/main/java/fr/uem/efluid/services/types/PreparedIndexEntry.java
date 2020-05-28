package fr.uem.efluid.services.types;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.IndexEntry;

import java.util.UUID;

/**
 * <p>Rendering of a diff line for commit preparation. Provides access to display values for the rendering of the diff, and many
 * selectors for the building of the final commit index / selection of lines to rollback</p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
public class PreparedIndexEntry implements DiffLine, Rendered {

    private String indexForDiff;

    private Long id;

    private UUID dictionaryEntryUuid;

    // Only for paginated display needs - the value is in most case not initialized
    private String tableName;

    // Only for paginated display needs - the value is in most case not initialized
    private String domainName;

    private IndexAction action;

    private String keyValue;

    private String payload;

    private String hrPayload;

    private long timestamp;

    private boolean selected;

    private boolean rollbacked;

    private UUID commitUuid;

    /**
     *
     */
    public PreparedIndexEntry() {
        super();
    }

    public String getIndexForDiff() {
        return indexForDiff;
    }

    public void setIndexForDiff(String indexForDiff) {
        this.indexForDiff = indexForDiff;
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
     * @return the dictionaryEntryUuid
     */
    @Override
    public UUID getDictionaryEntryUuid() {
        return this.dictionaryEntryUuid;
    }

    /**
     * @param dictionaryEntryUuid the dictionaryEntryUuid to set
     */
    public void setDictionaryEntryUuid(UUID dictionaryEntryUuid) {
        this.dictionaryEntryUuid = dictionaryEntryUuid;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
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
     * @return the hrPayload
     */
    @Override
    public String getHrPayload() {
        return this.hrPayload;
    }

    /**
     * @param hrPayload the hrPayload to set
     */
    public void setHrPayload(String hrPayload) {
        this.hrPayload = hrPayload;
    }

    /**
     * @return the selected
     */
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * @return the rollbacked
     */
    public boolean isRollbacked() {
        return this.rollbacked;
    }

    /**
     * Default is always true for diff
     *
     * @return
     */
    public boolean isNeedAction() {
        return true;
    }

    /**
     * @param rollbacked the rollbacked to set
     */
    public void setRollbacked(boolean rollbacked) {
        this.rollbacked = rollbacked;
    }

    /**
     * @return the commitUuid
     */
    public UUID getCommitUuid() {
        return this.commitUuid;
    }

    /**
     * @param commitUuid the commitUuid to set
     */
    public void setCommitUuid(UUID commitUuid) {
        this.commitUuid = commitUuid;
    }

    /**
     * @return
     * @see fr.uem.efluid.services.types.Rendered#getCombinedKey()
     */
    @Override
    public String getCombinedKey() {
        return this.keyValue;
    }

    @Override
    public String toString() {
        return "PreparedIndexEntry{" +
                "indexForDiff=" + indexForDiff +
                ", id=" + id +
                ", dictionaryEntryUuid=" + dictionaryEntryUuid +
                ", action=" + action +
                ", keyValue='" + keyValue + '\'' +
                ", payload='" + payload + '\'' +
                ", hrPayload='" + hrPayload + '\'' +
                ", timestamp=" + timestamp +
                ", selected=" + selected +
                ", rollbacked=" + rollbacked +
                ", commitUuid=" + commitUuid +
                '}';
    }

    /**
     * Used only when creating a new one as index are immutable
     *
     * @param data
     * @return
     */
    public static IndexEntry toEntity(PreparedIndexEntry data) {

        IndexEntry entry = new IndexEntry();

        entry.setAction(data.getAction());
        entry.setDictionaryEntry(new DictionaryEntry(data.getDictionaryEntryUuid()));
        entry.setKeyValue(data.getKeyValue());
        entry.setPayload(data.getPayload());
        entry.setTimestamp(data.getTimestamp()); // Can have one ?

        return entry;
    }

    /**
     * <p>
     * For combining process : minimal rendering, with support of hr payload for rendering
     * </p>
     *
     * @param combined
     * @param hrPayload
     * @return
     */
    public static PreparedIndexEntry fromCombined(DiffLine combined, String hrPayload) {

        PreparedIndexEntry data = new PreparedIndexEntry();

        data.setAction(combined.getAction());
        data.setDictionaryEntryUuid(combined.getDictionaryEntryUuid());
        data.setPayload(combined.getPayload());
        data.setKeyValue(combined.getKeyValue());
        data.setHrPayload(hrPayload);
        data.setTimestamp(combined.getTimestamp());
        data.setIndexForDiff(combined.getDictionaryEntryUuid() + "_" + combined.getKeyValue());

        return data;
    }

    /**
     * Used when reading an index content
     *
     * @param existing
     * @return
     */
    public static PreparedIndexEntry fromExistingEntity(IndexEntry existing) {

        PreparedIndexEntry data = new PreparedIndexEntry();

        completeFromExistingEntity(data, existing);

        return data;
    }

    /**
     * Used when reading an index content
     *
     * @param data
     * @param existing
     */
    protected static void completeFromExistingEntity(PreparedIndexEntry data, IndexEntry existing) {

        data.setAction(existing.getAction());

        if (existing.getDictionaryEntry() != null) {
            data.setDictionaryEntryUuid(existing.getDictionaryEntry().getUuid());
        }

        data.setPayload(existing.getPayload());
        data.setId(existing.getId());
        data.setKeyValue(existing.getKeyValue());
        data.setCommitUuid(existing.getCommit() != null ? existing.getCommit().getUuid() : null);
        data.setTimestamp(existing.getTimestamp());
        data.setIndexForDiff(existing.getDictionaryEntryUuid() + "_" + existing.getKeyValue());
    }

}
