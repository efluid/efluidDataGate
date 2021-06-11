package fr.uem.efluid.cucumber.stubs.entities;

import fr.uem.efluid.cucumber.stubs.ManagedDatabaseAccess;

import javax.persistence.*;
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
    @Column(columnDefinition = "BLOB")
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
        return Objects.equals(getKey(), that.getKey()) &&
                Arrays.equals(getData(), that.getData()) &&
                Objects.equals(getSimple(), that.getSimple());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getKey(), getSimple());
        result = 31 * result + Arrays.hashCode(getData());
        return result;
    }
}
