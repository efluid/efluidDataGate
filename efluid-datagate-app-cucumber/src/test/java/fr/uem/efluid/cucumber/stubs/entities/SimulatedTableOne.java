package fr.uem.efluid.cucumber.stubs.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import fr.uem.efluid.cucumber.stubs.ManagedDatabaseAccess;

import java.util.Objects;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Entity
@Table(name = ManagedDatabaseAccess.TABLE_ONE)
public class SimulatedTableOne {

    @Id
    private Long key;

    private String value;

    private String preset;

    private String something;

    public SimulatedTableOne() {
        super();
    }

    public SimulatedTableOne(Long key) {
        super();
        this.key = key;
    }

    /**
     * @return the key
     */
    public Long getKey() {
        return this.key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(Long key) {
        this.key = key;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the preset
     */
    public String getPreset() {
        return this.preset;
    }

    /**
     * @param preset the preset to set
     */
    public void setPreset(String preset) {
        this.preset = preset;
    }

    /**
     * @return the something
     */
    public String getSomething() {
        return this.something;
    }

    /**
     * @param something the something to set
     */
    public void setSomething(String something) {
        this.something = something;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimulatedTableOne that = (SimulatedTableOne) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(value, that.value) &&
                Objects.equals(preset, that.preset) &&
                Objects.equals(something, that.something);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, preset, something);
    }
}
