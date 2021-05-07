package fr.uem.efluid.model;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.model.shared.ExportAwareDictionaryEntry;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * <p>
 * Definition of a parameter table specified with {@link ParameterTable}
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@SpecifiedWith(ParameterTable.class)
public class ParameterTableDefinition extends ExportAwareDictionaryEntry<ParameterDomainDefinition> {

    private UUID uuid;

    private String parameterName;

    private String tableName;

    private String whereClause;

    private String selectClause;

    private String keyName;

    private ColumnType keyType;

    private String ext1KeyName;

    private ColumnType ext1KeyType;

    private String ext2KeyName;

    private ColumnType ext2KeyType;

    private String ext3KeyName;

    private ColumnType ext3KeyType;

    private String ext4KeyName;

    private ColumnType ext4KeyType;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    private ParameterDomainDefinition domain;

    private transient Set<String> identifiedColumnNames = new HashSet<>();

    private transient final boolean hierarchyTop;

    /**
     *
     */
    public ParameterTableDefinition(boolean hierarchyTop) {
        super();
        this.hierarchyTop = hierarchyTop;
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

    public void setIdentifiedColumnNames(Set<String> identifiedColumnNames) {
        this.identifiedColumnNames = identifiedColumnNames;
    }

    /**
     * Used only for generation
     *
     * @return
     */
    public Set<String> getIdentifiedColumnNames() {
        return this.identifiedColumnNames;
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
     * @return the createdTime
     */
    @Override
    public LocalDateTime getCreatedTime() {
        return this.createdTime;
    }

    /**
     * @param createdTime the createdTime to set
     */
    @Override
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * @return the importedTime
     */
    @Override
    public LocalDateTime getImportedTime() {
        return null;
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
    @Override
    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public void setImportedTime(LocalDateTime time) {
        // Nothing
    }

    /**
     * @return the domain
     */
    @Override
    public ParameterDomainDefinition getDomain() {
        return this.domain;
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(ParameterDomainDefinition domain) {
        this.domain = domain;
    }

    /**
     * @return the keyType
     */
    @Override
    public ColumnType getKeyType() {
        return this.keyType;
    }

    public boolean isHierarchyTop() {
        return this.hierarchyTop;
    }

    /**
     * @param keyType the keyType to set
     */
    public void setKeyType(ColumnType keyType) {
        this.keyType = keyType;
    }

    /**
     * set the keyName for indexed position
     */
    public void setKeyName(int index, String keyName) {

        switch (index) {
            case 0:
                setKeyName(keyName);
                break;
            case 1:
                setExt1KeyName(keyName);
                break;
            case 2:
                setExt2KeyName(keyName);
                break;
            case 3:
                setExt3KeyName(keyName);
                break;
            case 4:
            default:
                setExt4KeyName(keyName);
                break;
        }
    }

    /**
     * set the keyType for indexed position
     */
    public void setKeyType(int index, ColumnType keyType) {

        switch (index) {
            case 0:
                setKeyType(keyType);
                break;
            case 1:
                setExt1KeyType(keyType);
                break;
            case 2:
                setExt2KeyType(keyType);
                break;
            case 3:
                setExt3KeyType(keyType);
                break;
            case 4:
            default:
                setExt4KeyType(keyType);
                break;
        }
    }

    /**
     * @see fr.uem.efluid.model.Shared#deserialize(java.lang.String)
     */
    @Override
    public void deserialize(String raw) {
        // Not implemented
    }

}
