package fr.uem.efluid.tests.inheritance.conflicts;

import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterTable;

@ParameterTable("TETAPEWORKFLOW")
public class OtherOne {

    @ParameterKey
    private String id;

    @ParameterKey
    private String role;

    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
