package fr.uem.efluid.model.shared;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.Shared;
import fr.uem.efluid.model.UpdateChecked;
import fr.uem.efluid.utils.SharedOutputInputUtils;

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
 * @version 2
 * @since v0.0.1
 */
public abstract class ExportAwareDictionaryEntry<D extends ExportAwareFunctionalDomain<?>> implements Shared, UpdateChecked {

    public abstract String getParameterName();

    public abstract String getTableName();

    public abstract String getWhereClause();

    public abstract String getSelectClause();

    public abstract String getKeyName();

    @Override
    public abstract LocalDateTime getCreatedTime();

    public abstract D getDomain();

    public abstract ColumnType getKeyType();

    /**
     * @return
     */
    public abstract String getExt1KeyName();

    /**
     * @return
     */
    public abstract ColumnType getExt1KeyType();

    /**
     * @return
     */
    public abstract String getExt2KeyName();

    /**
     * @return
     */
    public abstract ColumnType getExt2KeyType();

    /**
     * @return
     */
    public abstract String getExt3KeyName();

    /**
     * @return
     */
    public abstract ColumnType getExt3KeyType();

    /**
     * @return
     */
    public abstract String getExt4KeyName();

    /**
     * @return
     */
    public abstract ColumnType getExt4KeyType();

    /**
     * @return
     * @see fr.uem.efluid.model.Shared#serialize()
     */
    @Override
    public final String serialize() {

        return SharedOutputInputUtils.newJson()
                .with("uid", getUuid())
                .with("cre", getCreatedTime())
                .with("upd", getUpdatedTime())
                .with("dom", getDomain().getUuid())
                .with("kna", getKeyName())
                .with("kty", getKeyType())
                .with("k1n", getExt1KeyName())
                .with("k1t", getExt1KeyType())
                .with("k2n", getExt2KeyName())
                .with("k2t", getExt2KeyType())
                .with("k3n", getExt3KeyName())
                .with("k3t", getExt3KeyType())
                .with("k4n", getExt4KeyName())
                .with("k4t", getExt4KeyType())
                .with("nam", getParameterName())
                .with("sel", getSelectClause())
                .with("tab", getTableName())
                .with("whe", getWhereClause())
                .toString();
    }

    /**
     * For testing ...
     *
     * @return all keys names in a list
     */
    public List<String> getAllKeyNames() {

        List<String> keys = new ArrayList<>();

        if (this.getKeyName() != null) {
            keys.add(this.getKeyName());
        }
        if (this.getExt1KeyName() != null) {
            keys.add(this.getExt1KeyName());
        }
        if (this.getExt2KeyName() != null) {
            keys.add(this.getExt2KeyName());
        }
        if (this.getExt3KeyName() != null) {
            keys.add(this.getExt3KeyName());
        }
        if (this.getExt4KeyName() != null) {
            keys.add(this.getExt4KeyName());
        }
        return keys;
    }

    /**
     * For testing ...
     *
     * @return all keys types in a list
     */
    public List<ColumnType> getAllKeyTypes() {

        List<ColumnType> keys = new ArrayList<>();

        if (this.getKeyType() != null) {
            keys.add(this.getKeyType());
        }
        if (this.getExt1KeyType() != null) {
            keys.add(this.getExt1KeyType());
        }
        if (this.getExt2KeyType() != null) {
            keys.add(this.getExt2KeyType());
        }
        if (this.getExt3KeyType() != null) {
            keys.add(this.getExt3KeyType());
        }
        if (this.getExt4KeyType() != null) {
            keys.add(this.getExt4KeyType());
        }
        return keys;
    }

    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.getUuid() == null) ? 0 : this.getUuid().hashCode());
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
        if (!(obj instanceof ExportAwareDictionaryEntry))
            return false;
        ExportAwareDictionaryEntry<?> other = (ExportAwareDictionaryEntry<?>) obj;
        if (this.getUuid() == null) {
            if (other.getUuid() != null)
                return false;
        } else if (!this.getUuid().equals(other.getUuid()))
            return false;
        return true;
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DicEntry [<" + this.getUuid() + "> table:" + this.getTableName()
                + ", where:" + this.getWhereClause() + ", select:" + this.getSelectClause() + ", key:" + this.getKeyName() + "]";
    }
}
