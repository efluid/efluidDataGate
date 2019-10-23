package fr.uem.efluid.system.stubs.entities;

import fr.uem.efluid.system.stubs.ManagedDatabaseAccess;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = ManagedDatabaseAccess.TABLE_ONE)
public class SimulatedTableOnlyKeys {

    @Id
    private Long oneKey;

    private Long otherKey;

    public Long getOneKey() {
        return oneKey;
    }

    public void setOneKey(Long oneKey) {
        this.oneKey = oneKey;
    }

    public Long getOtherKey() {
        return otherKey;
    }

    public void setOtherKey(Long otherKey) {
        this.otherKey = otherKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimulatedTableOnlyKeys that = (SimulatedTableOnlyKeys) o;
        return Objects.equals(oneKey, that.oneKey) &&
                Objects.equals(otherKey, that.otherKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oneKey, otherKey);
    }
}
