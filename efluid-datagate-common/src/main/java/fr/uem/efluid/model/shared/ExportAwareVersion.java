package fr.uem.efluid.model.shared;

import fr.uem.efluid.model.Shared;
import fr.uem.efluid.model.UpdateChecked;
import fr.uem.efluid.utils.SharedOutputInputUtils;

/**
 * <p>
 * Definition of a dictionary version : simple inline value
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.2.0
 */
public abstract class ExportAwareVersion<D extends ExportAwareProject> implements Shared, UpdateChecked {

    /**
     * @return
     */
    public abstract String getName();

    /**
     * @return associated project
     */
    public abstract D getProject();

    /**
     * @return
     */
    public abstract String getModelIdentity();

    /**
     * @return
     * @see fr.uem.efluid.model.Shared#serialize()
     */
    @Override
    public String serialize() {

        return SharedOutputInputUtils.newJson()
                .with("uid", getUuid())
                .with("cre", getCreatedTime())
                .with("upd", getUpdatedTime())
                .with("nam", getName())
                .with("pro", getProject().getUuid())
                .with("idn", getModelIdentity())
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
        ExportAwareVersion<?> other = (ExportAwareVersion<?>) obj;
        if (this.getUuid() == null) {
            if (other.getUuid() != null)
                return false;
        } else if (!getUuid().equals(other.getUuid()))
            return false;
        return true;
    }

}
