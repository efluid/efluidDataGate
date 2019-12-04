package fr.uem.efluid.model.entities;

import fr.uem.efluid.model.shared.ExportAwareTableLink;
import fr.uem.efluid.utils.SharedOutputInputUtils;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@Entity
@Table(name = "link")
public class TableLink extends ExportAwareTableLink<DictionaryEntry> {

    @Id
    @Type(type = "uuid-char")
    private UUID uuid;

    private String columnFrom;

    @NotNull
    private String tableTo;

    private String columnTo;

    private String name;

    private String ext1ColumnTo;

    private String ext2ColumnTo;

    private String ext3ColumnTo;

    private String ext4ColumnTo;

    private String ext1ColumnFrom;

    private String ext2ColumnFrom;

    private String ext3ColumnFrom;

    private String ext4ColumnFrom;

    @NotNull
    private LocalDateTime createdTime;

    @NotNull
    private LocalDateTime updatedTime;

    private LocalDateTime importedTime;

    @ManyToOne(optional = false)
    private DictionaryEntry dictionaryEntry;

    /**
     * @param uuid forced uuid
     */
    public TableLink(UUID uuid) {
        super();
        this.uuid = uuid;
    }

    /**
     *
     */
    public TableLink() {
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
     * @return the columnFrom
     */
    @Override
    public String getColumnFrom() {
        return this.columnFrom;
    }

    /**
     * @param columnFrom the columnFrom to set
     */
    public void setColumnFrom(String columnFrom) {
        this.columnFrom = columnFrom;
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
     * @return the tableTo
     */
    @Override
    public String getTableTo() {
        return this.tableTo;
    }

    /**
     * @param tableTo the tableTo to set
     */
    public void setTableTo(String tableTo) {
        this.tableTo = tableTo;
    }

    /**
     * @return the ext1ColumnTo
     */
    @Override
    public String getExt1ColumnTo() {
        return this.ext1ColumnTo;
    }

    /**
     * @param ext1ColumnTo the ext1ColumnTo to set
     */
    public void setExt1ColumnTo(String ext1ColumnTo) {
        this.ext1ColumnTo = ext1ColumnTo;
    }

    /**
     * @return the ext2ColumnTo
     */
    @Override
    public String getExt2ColumnTo() {
        return this.ext2ColumnTo;
    }

    /**
     * @param ext2ColumnTo the ext2ColumnTo to set
     */
    public void setExt2ColumnTo(String ext2ColumnTo) {
        this.ext2ColumnTo = ext2ColumnTo;
    }

    /**
     * @return the ext3ColumnTo
     */
    @Override
    public String getExt3ColumnTo() {
        return this.ext3ColumnTo;
    }

    /**
     * @param ext3ColumnTo the ext3ColumnTo to set
     */
    public void setExt3ColumnTo(String ext3ColumnTo) {
        this.ext3ColumnTo = ext3ColumnTo;
    }

    /**
     * @return the ext4ColumnTo
     */
    @Override
    public String getExt4ColumnTo() {
        return this.ext4ColumnTo;
    }

    /**
     * @param ext4ColumnTo the ext4ColumnTo to set
     */
    public void setExt4ColumnTo(String ext4ColumnTo) {
        this.ext4ColumnTo = ext4ColumnTo;
    }

    /**
     * @return the ext1ColumnFrom
     */
    @Override
    public String getExt1ColumnFrom() {
        return this.ext1ColumnFrom;
    }

    /**
     * @param ext1ColumnFrom the ext1ColumnFrom to set
     */
    public void setExt1ColumnFrom(String ext1ColumnFrom) {
        this.ext1ColumnFrom = ext1ColumnFrom;
    }

    /**
     * @return the ext2ColumnFrom
     */
    @Override
    public String getExt2ColumnFrom() {
        return this.ext2ColumnFrom;
    }

    /**
     * @param ext2ColumnFrom the ext2ColumnFrom to set
     */
    public void setExt2ColumnFrom(String ext2ColumnFrom) {
        this.ext2ColumnFrom = ext2ColumnFrom;
    }

    /**
     * @return the ext3ColumnFrom
     */
    @Override
    public String getExt3ColumnFrom() {
        return this.ext3ColumnFrom;
    }

    /**
     * @param ext3ColumnFrom the ext3ColumnFrom to set
     */
    public void setExt3ColumnFrom(String ext3ColumnFrom) {
        this.ext3ColumnFrom = ext3ColumnFrom;
    }

    /**
     * @return the ext4ColumnFrom
     */
    @Override
    public String getExt4ColumnFrom() {
        return this.ext4ColumnFrom;
    }

    /**
     * @param ext4ColumnFrom the ext4ColumnFrom to set
     */
    public void setExt4ColumnFrom(String ext4ColumnFrom) {
        this.ext4ColumnFrom = ext4ColumnFrom;
    }

    /**
     * @return the columnTo
     */
    @Override
    public String getColumnTo() {
        return this.columnTo;
    }

    /**
     * @param columnTo the columnTo to set
     */
    public void setColumnTo(String columnTo) {
        this.columnTo = columnTo;
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
     * @return the dictionaryEntry
     */
    @Override
    public DictionaryEntry getDictionaryEntry() {
        return this.dictionaryEntry;
    }

    /**
     * @param dictionaryEntry the dictionaryEntry to set
     */
    public void setDictionaryEntry(DictionaryEntry dictionaryEntry) {
        this.dictionaryEntry = dictionaryEntry;
    }

    public void setColumnFrom(int index, String col) {

        switch (index) {
            case 0:
                setColumnFrom(col);
                break;
            case 1:
                setExt1ColumnFrom(col);
                break;
            case 2:
                setExt2ColumnFrom(col);
                break;
            case 3:
                setExt3ColumnFrom(col);
                break;
            case 4:
            default:
                setExt4ColumnFrom(col);
                break;
        }
    }

    public void setColumnTo(int index, String col) {

        switch (index) {
            case 0:
                setColumnTo(col);
                break;
            case 1:
                setExt1ColumnTo(col);
                break;
            case 2:
                setExt2ColumnTo(col);
                break;
            case 3:
                setExt3ColumnTo(col);
                break;
            case 4:
            default:
                setExt4ColumnTo(col);
                break;
        }
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
                .applyString("cfr", this::setColumnFrom)
                .applyString("cto", this::setColumnTo)
                .applyString("tto", this::setTableTo)
                .applyString("cf1", this::setExt1ColumnFrom)
                .applyString("cf2", this::setExt2ColumnFrom)
                .applyString("cf3", this::setExt3ColumnFrom)
                .applyString("cf4", this::setExt4ColumnFrom)
                .applyString("ct1", this::setExt1ColumnTo)
                .applyString("ct2", this::setExt2ColumnTo)
                .applyString("ct3", this::setExt3ColumnTo)
                .applyString("ct4", this::setExt4ColumnTo)
                .applyUUID("dic", v -> setDictionaryEntry(new DictionaryEntry(v)));
    }

}
