package fr.uem.efluid.model.entities;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.shared.ExportAwareDictionaryEntry;
import fr.uem.efluid.utils.SharedOutputInputUtils;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * <p>
 * Definition of a parameter table, in a "big central dictionary of all parameters".
 * Mostly a link between some business related datas which are used for a more user
 * friendly rendering, and some technical datas used to select the right parameters value
 * from managed database.
 * </p>
 * <p>
 * For composite key support a basic set of "extension" of key is supported. The specified
 * ext keys are added to ref key for composite definition. Support limited to 5 used
 * properties for key composition (standard key + 4 ext keys)
 * </p>
 *
 * @author elecomte
 * @version 3
 * @since v0.0.1
 */
@Entity
@Table(name = "dictionary")
public class DictionaryEntry extends ExportAwareDictionaryEntry<FunctionalDomain> {

    @Id
    @Type(type = "uuid-char")
    private UUID uuid;

    @NotNull
    private String parameterName;

    // TODO : add protection against SQL Injection

    @NotNull
    private String tableName;

    private String whereClause;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String selectClause;

    @NotNull
    private String keyName;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ColumnType keyType;

    private String ext1KeyName;

    @Enumerated(EnumType.STRING)
    private ColumnType ext1KeyType;

    private String ext2KeyName;

    @Enumerated(EnumType.STRING)
    private ColumnType ext2KeyType;

    private String ext3KeyName;

    @Enumerated(EnumType.STRING)
    private ColumnType ext3KeyType;

    private String ext4KeyName;

    @Enumerated(EnumType.STRING)
    private ColumnType ext4KeyType;

    @NotNull
    private LocalDateTime createdTime;

    @NotNull
    private LocalDateTime updatedTime;

    private LocalDateTime importedTime;

    @ManyToOne(optional = false)
    private FunctionalDomain domain;

    /**
     * @param uuid shareable identifier
     */
    public DictionaryEntry(UUID uuid) {
        super();
        this.uuid = uuid;
    }

    /**
     *
     */
    public DictionaryEntry() {
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
     * @return the parameterName
     */
    @Override
    public String getParameterName() {
        return this.parameterName;
    }

    /**
     * @param parameterName the parameterName to set
     */
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    /**
     * @return the tableName
     */
    @Override
    public String getTableName() {
        return this.tableName;
    }

    /**
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * @return the whereClause
     */
    @Override
    public String getWhereClause() {
        return this.whereClause;
    }

    /**
     * @param whereClause the whereClause to set
     */
    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    /**
     * @return the selectClause
     */
    @Override
    public String getSelectClause() {
        return this.selectClause;
    }

    /**
     * @param selectClause the selectClause to set
     */
    public void setSelectClause(String selectClause) {
        this.selectClause = selectClause;
    }

    /**
     * @return the keyName
     */
    @Override
    public String getKeyName() {
        return this.keyName;
    }

    /**
     * @param keyName the keyName to set
     */
    public void setKeyName(String keyName) {
        this.keyName = keyName;
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
     * @return the ext1KeyName
     */
    @Override
    public String getExt1KeyName() {
        return this.ext1KeyName;
    }

    /**
     * @param ext1KeyName the ext1KeyName to set
     */
    public void setExt1KeyName(String ext1KeyName) {
        this.ext1KeyName = ext1KeyName;
    }

    /**
     * @return the ext1KeyType
     */
    @Override
    public ColumnType getExt1KeyType() {
        return this.ext1KeyType;
    }

    /**
     * @param ext1KeyType the ext1KeyType to set
     */
    public void setExt1KeyType(ColumnType ext1KeyType) {
        this.ext1KeyType = ext1KeyType;
    }

    /**
     * @return the ext2KeyName
     */
    @Override
    public String getExt2KeyName() {
        return this.ext2KeyName;
    }

    /**
     * @param ext2KeyName the ext2KeyName to set
     */
    public void setExt2KeyName(String ext2KeyName) {
        this.ext2KeyName = ext2KeyName;
    }

    /**
     * @return the ext2KeyType
     */
    @Override
    public ColumnType getExt2KeyType() {
        return this.ext2KeyType;
    }

    /**
     * @param ext2KeyType the ext2KeyType to set
     */
    public void setExt2KeyType(ColumnType ext2KeyType) {
        this.ext2KeyType = ext2KeyType;
    }

    /**
     * @return the ext3KeyName
     */
    @Override
    public String getExt3KeyName() {
        return this.ext3KeyName;
    }

    /**
     * @param ext3KeyName the ext3KeyName to set
     */
    public void setExt3KeyName(String ext3KeyName) {
        this.ext3KeyName = ext3KeyName;
    }

    /**
     * @return the ext3KeyType
     */
    @Override
    public ColumnType getExt3KeyType() {
        return this.ext3KeyType;
    }

    /**
     * @param ext3KeyType the ext3KeyType to set
     */
    public void setExt3KeyType(ColumnType ext3KeyType) {
        this.ext3KeyType = ext3KeyType;
    }

    /**
     * @return the ext4KeyName
     */
    @Override
    public String getExt4KeyName() {
        return this.ext4KeyName;
    }

    /**
     * @param ext4KeyName the ext4KeyName to set
     */
    public void setExt4KeyName(String ext4KeyName) {
        this.ext4KeyName = ext4KeyName;
    }

    /**
     * @return the ext4KeyType
     */
    @Override
    public ColumnType getExt4KeyType() {
        return this.ext4KeyType;
    }

    /**
     * @param ext4KeyType the ext4KeyType to set
     */
    public void setExt4KeyType(ColumnType ext4KeyType) {
        this.ext4KeyType = ext4KeyType;
    }

    /**
     * @return the domain
     */
    @Override
    public FunctionalDomain getDomain() {
        return this.domain;
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(FunctionalDomain domain) {
        this.domain = domain;
    }

    /**
     * @return the keyType
     */
    @Override
    public ColumnType getKeyType() {
        return this.keyType;
    }

    /**
     * @param keyType the keyType to set
     */
    public void setKeyType(ColumnType keyType) {
        this.keyType = keyType;
    }

    public Stream<String> keyNames() {

        // For composite, use advanced building from iterator
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new EntryKeyNameIterator(this), 0), false);
    }

    public Stream<ColumnType> keyTypes() {

        // For composite, use advanced building from iterator
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new EntryKeyTypeIterator(this), 0), false);
    }

    public boolean isCompositeKey() {
        // If at least ext1 is set => composite
        return this.getExt1KeyName() != null;
    }

    /**
     * @param raw
     * @see fr.uem.efluid.model.Shared#deserialize(java.lang.String)
     */
    @Override
    public void deserialize(String raw) {

        SharedOutputInputUtils.fromJson(raw)
                .applyUUID("uid", this::setUuid)
                .applyLdt("cre", this::setCreatedTime)
                .applyLdt("upd", this::setUpdatedTime)
                .applyUUID("dom", v -> setDomain(new FunctionalDomain(v)))
                .applyString("kna", this::setKeyName)
                .applyString("kty", v -> setKeyType(ColumnType.valueOf(v)))
                .applyString("k1n", this::setExt1KeyName)
                .applyString("k1t", v -> setExt1KeyType(ColumnType.valueOf(v)))
                .applyString("k2n", this::setExt2KeyName)
                .applyString("k2t", v -> setExt2KeyType(ColumnType.valueOf(v)))
                .applyString("k3n", this::setExt3KeyName)
                .applyString("k3t", v -> setExt3KeyType(ColumnType.valueOf(v)))
                .applyString("k4n", this::setExt4KeyName)
                .applyString("k4t", v -> setExt4KeyType(ColumnType.valueOf(v)))
                .applyString("nam", this::setParameterName)
                .applyString("sel", this::setSelectClause)
                .applyString("tab", this::setTableName)
                .applyString("whe", this::setWhereClause);
    }

    /**
     * <p>
     * For easy use of composite key model - process iteration over 5 key properties
     * </p>
     *
     * @author elecomte
     * @version 1
     * @since v0.0.8
     */
    private static abstract class FiveEntryKeyIterator<T> implements Iterator<T> {

        private int max = 0;
        private int pos = 0;

        protected final DictionaryEntry dic;

        FiveEntryKeyIterator(DictionaryEntry dic) {
            super();
            this.dic = dic;

            // Standard key - not composite
            if (!dic.isCompositeKey()) {
                this.max = 1;
            }

            // Composite, search for key defs
            else {
                if (dic.getExt4KeyName() != null) {
                    this.max = 5;
                } else if (dic.getExt3KeyName() != null) {
                    this.max = 4;
                } else if (dic.getExt2KeyName() != null) {
                    this.max = 3;
                } else {
                    this.max = 2;
                }
            }
        }

        /**
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return this.pos < this.max;
        }

        protected abstract T getValue(int index);

        /**
         * @see java.util.Iterator#next()
         */
        @Override
        public T next() {
            this.pos++;
            return getValue(this.pos - 1);
        }

    }

    /**
     * <p>
     * Access to the 5 possible key name in composite key dict entry
     * </p>
     *
     * @author elecomte
     * @version 1
     * @since v0.0.8
     */
    private static final class EntryKeyNameIterator extends FiveEntryKeyIterator<String> {

        EntryKeyNameIterator(DictionaryEntry dic) {
            super(dic);
        }

        @Override
        protected String getValue(int index) {
            switch (index) {
                case 0:
                    return this.dic.getKeyName();
                case 1:
                    return this.dic.getExt1KeyName();
                case 2:
                    return this.dic.getExt2KeyName();
                case 3:
                    return this.dic.getExt3KeyName();
                case 4:
                default:
                    return this.dic.getExt4KeyName();
            }
        }

    }

    /**
     * <p>
     * Like for keyName, but with types
     * </p>
     *
     * @author elecomte
     * @version 1
     * @since v0.0.8
     */
    private static final class EntryKeyTypeIterator extends FiveEntryKeyIterator<ColumnType> {

        EntryKeyTypeIterator(DictionaryEntry dic) {
            super(dic);
        }

        @Override
        protected ColumnType getValue(int index) {
            switch (index) {
                case 0:
                    return this.dic.getKeyType();
                case 1:
                    return this.dic.getExt1KeyType();
                case 2:
                    return this.dic.getExt2KeyType();
                case 3:
                    return this.dic.getExt3KeyType();
                case 4:
                default:
                    return this.dic.getExt4KeyType();
            }
        }
    }
}
