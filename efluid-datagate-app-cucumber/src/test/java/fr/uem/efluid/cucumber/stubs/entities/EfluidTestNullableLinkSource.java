package fr.uem.efluid.cucumber.stubs.entities;

import fr.uem.efluid.cucumber.stubs.ManagedDatabaseAccess;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = ManagedDatabaseAccess.TTESTNULLLINK_SRC)
public class EfluidTestNullableLinkSource {

    @Id
    private String id;

    private String value;

    private String destBizKey;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDestBizKey() {
        return destBizKey;
    }

    public void setDestBizKey(String destBizKey) {
        this.destBizKey = destBizKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EfluidTestNullableLinkSource test1 = (EfluidTestNullableLinkSource) o;
        return Objects.equals(id, test1.id) &&
                Objects.equals(value, test1.value)&&
                Objects.equals(destBizKey, test1.destBizKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }
}
