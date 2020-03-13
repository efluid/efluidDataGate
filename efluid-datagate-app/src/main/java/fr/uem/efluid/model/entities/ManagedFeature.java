package fr.uem.efluid.model.entities;

import fr.uem.efluid.services.Feature;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Entity
@Table(name = "features")
public class ManagedFeature {

    @Id
    @Enumerated(EnumType.STRING)
    private Feature feature;

    private boolean enabled;

    @NotNull
    private LocalDateTime updatedTime;

    public ManagedFeature() {
        super();
    }

    public ManagedFeature(Feature feature) {
        super();
        this.feature = feature;
    }

    /**
     * @return the feature
     */
    public Feature getFeature() {
        return this.feature;
    }

    /**
     * @param feature the feature to set
     */
    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the updatedTime
     */
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
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.feature == null) ? 0 : this.feature.hashCode());
        return result;
    }

    /**
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
        ManagedFeature other = (ManagedFeature) obj;
        return this.feature == other.feature;
    }
}
