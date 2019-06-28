package fr.uem.efluid.system.stubs.entities;

import fr.uem.efluid.system.stubs.ManagedDatabaseAccess;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = ManagedDatabaseAccess.TABLE_SEVEN)
public class SimulatedTableSeven {

    @Id
    private Long id;

    private String businessKey;

    // Not native join = no FK !!!
    private String otherTableValue;

    private String value;

    private boolean enabled;

    public SimulatedTableSeven(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getOtherTableValue() {
        return otherTableValue;
    }

    public void setOtherTableValue(String otherTableValue) {
        this.otherTableValue = otherTableValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimulatedTableSeven that = (SimulatedTableSeven) o;
        return enabled == that.enabled &&
                Objects.equals(id, that.id) &&
                Objects.equals(businessKey, that.businessKey) &&
                Objects.equals(otherTableValue, that.otherTableValue) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, businessKey, otherTableValue, value, enabled);
    }
}
