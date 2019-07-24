package fr.uem.efluid.system.stubs.entities;

import fr.uem.efluid.system.stubs.ManagedDatabaseAccess;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = ManagedDatabaseAccess.EFLUIDTESTPKCOMPOSITE)
public class EfluidTestPkComposite {

    @Id
    private String id;

    private String id2;

    private String col1;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId2() {
        return id2;
    }

    public void setId2(String id2) {
        this.id2 = id2;
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
        EfluidTestPkComposite that = (EfluidTestPkComposite) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(id2, that.id2) &&
                Objects.equals(col1, that.col1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, id2, col1);
    }
}
