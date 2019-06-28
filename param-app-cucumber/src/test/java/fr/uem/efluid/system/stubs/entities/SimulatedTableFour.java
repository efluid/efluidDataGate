package fr.uem.efluid.system.stubs.entities;

import fr.uem.efluid.system.stubs.ManagedDatabaseAccess;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A table with a join on another table
 *
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Entity
@Table(name = ManagedDatabaseAccess.TABLE_FOUR)
public class SimulatedTableFour {

    @Id
    private String key;

    @ManyToOne
    private SimulatedTableOne otherTable;

    private LocalDateTime contentTime;

    private int contentInt;

    public SimulatedTableFour() {
    }


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

    public SimulatedTableOne getOtherTable() {
        return this.otherTable;
    }

    public void setOtherTable(SimulatedTableOne otherTable) {
        this.otherTable = otherTable;
    }

    public LocalDateTime getContentTime() {
        return this.contentTime;
    }

    public void setContentTime(LocalDateTime contentTime) {
        this.contentTime = contentTime;
    }

    public int getContentInt() {
        return this.contentInt;
    }

    public void setContentInt(int contentInt) {
        this.contentInt = contentInt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimulatedTableFour that = (SimulatedTableFour) o;
        return contentInt == that.contentInt &&
                Objects.equals(key, that.key) &&
                Objects.equals(otherTable, that.otherTable) &&
                Objects.equals(contentTime, that.contentTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, otherTable, contentTime, contentInt);
    }
}
