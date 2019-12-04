package fr.uem.efluid.cucumber.stubs.entities;

import fr.uem.efluid.cucumber.stubs.ManagedDatabaseAccess;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = ManagedDatabaseAccess.EFLUIDTESTNUMBER)
public class EfluidTestNumber {

    @Id
    private String id;

    private String col1;

    private Long col2;

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

    public Long getCol2() {
        return col2;
    }

    public void setCol2(Long col2) {
        this.col2 = col2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EfluidTestNumber that = (EfluidTestNumber) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(col1, that.col1) &&
                Objects.equals(col2, that.col2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, col1, col2);
    }
}
