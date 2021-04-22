package fr.uem.efluid.cucumber.stubs.entities;

import fr.uem.efluid.cucumber.stubs.ManagedDatabaseAccess;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = ManagedDatabaseAccess.TABLE_ALL_NULLABLE)
public class SimulatedTableAllNullable {

    @Id
    private Long id;

    private String businessKey;

    private String something;

    private Integer value;

    public SimulatedTableAllNullable() {
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

    public String getSomething() {
        return something;
    }

    public void setSomething(String something) {
        this.something = something;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimulatedTableAllNullable that = (SimulatedTableAllNullable) o;
        return value == that.value && Objects.equals(id, that.id) && Objects.equals(businessKey, that.businessKey) && Objects.equals(something, that.something);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, businessKey, something, value);
    }
}
