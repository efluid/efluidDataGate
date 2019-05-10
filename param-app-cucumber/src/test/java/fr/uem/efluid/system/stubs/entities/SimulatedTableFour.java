package fr.uem.efluid.system.stubs.entities;

import fr.uem.efluid.system.stubs.ManagedDatabaseAccess;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

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

    /**
     * @return
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.key == null) ? 0 : this.key.hashCode());
        return result;
    }

    /**
     * @param obj
     * @return
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimulatedTableFour _other = (SimulatedTableFour) obj;
        if (this.key == null) {
            if (_other.key != null)
                return false;
        } else if (!this.key.equals(_other.key))
            return false;
        return true;
    }
}
