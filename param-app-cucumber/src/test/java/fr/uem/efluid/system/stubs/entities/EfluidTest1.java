package fr.uem.efluid.system.stubs.entities;

import fr.uem.efluid.system.stubs.ManagedDatabaseAccess;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = ManagedDatabaseAccess.TTEST1)
public class EfluidTest1 {

    @Id
    private String id;

    private String col1;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCol1() {
        return col1;
    }

    public void setCol1(String col1) {
        this.col1 = col1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EfluidTest1 test1 = (EfluidTest1) o;
        return Objects.equals(id, test1.id) &&
                Objects.equals(col1, test1.col1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, col1);
    }
}
