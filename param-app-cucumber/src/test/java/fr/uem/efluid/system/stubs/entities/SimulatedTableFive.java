package fr.uem.efluid.system.stubs.entities;

import fr.uem.efluid.system.stubs.ManagedDatabaseAccess;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

/**
 * With clob
 *
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Entity
@Table(name = ManagedDatabaseAccess.TABLE_FIVE)
public class SimulatedTableFive {

    @Id
    private String key;

    @Lob
    private byte[] data;

    private BigDecimal simple;

    /**
     * @return the key
     */
    public String getKey() {
        return this.key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public BigDecimal getSimple() {
        return this.simple;
    }

    public void setSimple(BigDecimal simple) {
        this.simple = simple;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimulatedTableFive that = (SimulatedTableFive) o;
        return Objects.equals(key, that.key) &&
                Arrays.equals(data, that.data) &&
                Objects.equals(simple, that.simple);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(key, simple);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
}
