package fr.uem.efluid.system.stubs.entities;

import fr.uem.efluid.system.stubs.ManagedDatabaseAccess;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = ManagedDatabaseAccess.TTESTMULTIDATATYPE)
public class EfluidTestMultiDataType {

    @Id
    private String id;

    //Varchar 255
    private String col1;

    //Varchar 40000
    private String col2;

    //Number
    private Long col3;

    //Date
    private LocalDate col4;

    //Timestamp
    private LocalDateTime col5;

    //Char
    private String col6;

    //CLOB
    private String col7;


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

    public String getCol2() {
        return col2;
    }

    public void setCol2(String col2) {
        this.col2 = col2;
    }

    public Long getCol3() {
        return col3;
    }

    public void setCol3(Long col3) {
        this.col3 = col3;
    }

    public LocalDate getCol4() {
        return col4;
    }

    public void setCol4(LocalDate col4) {
        this.col4 = col4;
    }

    public LocalDateTime getCol5() {
        return col5;
    }

    public void setCol5(LocalDateTime col5) {
        this.col5 = col5;
    }

    public String getCol6() {
        return col6;
    }

    public void setCol6(String col6) {
        this.col6 = col6;
    }

    public String getCol7() {
        return col7;
    }

    public void setCol7(String col7) {
        this.col7 = col7;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EfluidTestMultiDataType that = (EfluidTestMultiDataType) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(col1, that.col1) &&
                Objects.equals(col2, that.col2) &&
                Objects.equals(col3, that.col3) &&
                Objects.equals(col4, that.col4) &&
                Objects.equals(col5, that.col5) &&
                Objects.equals(col6, that.col6) &&
                Objects.equals(col7, that.col7);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, col1, col2, col3, col4, col5, col6, col7);
    }
}
