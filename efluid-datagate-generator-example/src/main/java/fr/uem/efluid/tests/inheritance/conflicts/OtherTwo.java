package fr.uem.efluid.tests.inheritance.conflicts;

import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterTable;

@ParameterTable("TTRAITEMENTEXECUTIONETAPE")
public class OtherTwo {

    @ParameterKey
    private String id;

    private String value;

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
}
