package fr.uem.efluid.cucumber.stubs.entities;

import fr.uem.efluid.cucumber.stubs.ManagedDatabaseAccess;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = ManagedDatabaseAccess.TTESTNULLLINK_DEST)
public class EfluidTestNullableLinkDestination {

    @Id
    private String techKey;

    private String code;

    private String bizKey;

    public String getTechKey() {
        return techKey;
    }

    public void setTechKey(String techKey) {
        this.techKey = techKey;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBizKey() {
        return bizKey;
    }

    public void setBizKey(String bizKey) {
        this.bizKey = bizKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EfluidTestNullableLinkDestination that = (EfluidTestNullableLinkDestination) o;
        return techKey.equals(that.techKey) &&
                code.equals(that.code) &&
                Objects.equals(bizKey, that.bizKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(techKey, code, bizKey);
    }
}
