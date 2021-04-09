package fr.uem.efluid.cucumber.stubs.entities;

import fr.uem.efluid.cucumber.stubs.ManagedDatabaseAccess;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = ManagedDatabaseAccess.TABLE_THREE_KEYS)
public class SimulatedTableThreeKeys {

    @Id
    private String firstKey;

    private String secondKey;

    private String thirdKey;

    public String getFirstKey() {
        return firstKey;
    }

    public void setFirstKey(String firstKey) {
        this.firstKey = firstKey;
    }

    public String getSecondKey() {
        return secondKey;
    }

    public void setSecondKey(String secondKey) {
        this.secondKey = secondKey;
    }

    public String getThirdKey() {
        return thirdKey;
    }

    public void setThirdKey(String thirdKey) {
        this.thirdKey = thirdKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimulatedTableThreeKeys that = (SimulatedTableThreeKeys) o;
        return Objects.equals(firstKey, that.firstKey) && Objects.equals(secondKey, that.secondKey) && Objects.equals(thirdKey, that.thirdKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstKey, secondKey, thirdKey);
    }
}
