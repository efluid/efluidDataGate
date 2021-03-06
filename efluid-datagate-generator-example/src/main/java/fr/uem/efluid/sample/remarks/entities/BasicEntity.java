package fr.uem.efluid.sample.remarks.entities;

import fr.uem.efluid.ParameterTable;

@ParameterTable("T_BASIC_ENTITY")
public class BasicEntity extends MyParentEntityType {

    private String id;

    private String value;

    private String other;

    private String something;

    @Override
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

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String getSomething() {
        return something;
    }

    public void setSomething(String something) {
        this.something = something;
    }
}
