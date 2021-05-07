package fr.uem.efluid.model.shared;

import fr.uem.efluid.model.Shared;
import fr.uem.efluid.model.SharedDictionary;
import fr.uem.efluid.utils.SharedOutputInputUtils;

import java.time.LocalDateTime;

/**
 * <p>
 * Shared information on top level organization : data is managed in projects
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.2.0
 */
public abstract class ExportAwareProject implements Shared {

    /**
     * @return
     */
    public abstract String getName();

    /**
     * @return
     */
    public abstract int getColor();

    public abstract LocalDateTime getCreatedTime();

    /**
     * @return
     * @see fr.uem.efluid.model.Shared#serialize()
     */
    @Override
    public final String serialize() {

        return SharedOutputInputUtils.newJson()
                .with("uid", getUuid())
                .with("cre", getCreatedTime())
                .with("nam", getName())
                .with("col", getColor())
                .toString();
    }

    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getUuid() == null) ? 0 : getUuid().hashCode());
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
        ExportAwareProject other = (ExportAwareProject) obj;
        if (this.getUuid() == null) {
            if (other.getUuid() != null)
                return false;
        } else if (!getUuid().equals(other.getUuid()))
            return false;
        return true;
    }

}
