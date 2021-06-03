package fr.uem.efluid.cucumber.stubs.entities;

import fr.uem.efluid.cucumber.stubs.ManagedDatabaseAccess;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = ManagedDatabaseAccess.TRECOPIEPARAMREFERENTIELDIR)
public class EfluidParamForRegion {

    @Id
    @GeneratedValue
    private int id;

    @Column(name = "DIR")
    private String dir;

    @Column(name = "TABNAME")
    private String tabname;

    @Column(name = "OP")
    private String op;

    @Column(name = "COLS_PK")
    private String colsPk;

    @Column(name = "SRC_ID1")
    private String srcId1;

    @Column(name = "SRC_ID2")
    private String srcId2;

    @Column(name = "SRC_ID3")
    private String srcId3;

    @Column(name = "SRC_ID4")
    private String srcId4;

    @Column(name = "SRC_ID5")
    private String srcId5;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getTabname() {
        return tabname;
    }

    public void setTabname(String tabname) {
        this.tabname = tabname;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getColsPk() {
        return colsPk;
    }

    public void setColsPk(String colsPk) {
        this.colsPk = colsPk;
    }

    public String getSrcId1() {
        return srcId1;
    }

    public void setSrcId1(String srcId1) {
        this.srcId1 = srcId1;
    }

    public String getSrcId2() {
        return srcId2;
    }

    public void setSrcId2(String srcId2) {
        this.srcId2 = srcId2;
    }

    public String getSrcId3() {
        return srcId3;
    }

    public void setSrcId3(String srcId3) {
        this.srcId3 = srcId3;
    }

    public String getSrcId4() {
        return srcId4;
    }

    public void setSrcId4(String srcId4) {
        this.srcId4 = srcId4;
    }

    public String getSrcId5() {
        return srcId5;
    }

    public void setSrcId5(String srcId5) {
        this.srcId5 = srcId5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EfluidParamForRegion that = (EfluidParamForRegion) o;
        return id == that.id && Objects.equals(dir, that.dir) && Objects.equals(tabname, that.tabname) && Objects.equals(op, that.op) && Objects.equals(colsPk, that.colsPk) && Objects.equals(srcId1, that.srcId1) && Objects.equals(srcId2, that.srcId2) && Objects.equals(srcId3, that.srcId3) && Objects.equals(srcId4, that.srcId4) && Objects.equals(srcId5, that.srcId5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dir, tabname, op, colsPk, srcId1, srcId2, srcId3, srcId4, srcId5);
    }
}
