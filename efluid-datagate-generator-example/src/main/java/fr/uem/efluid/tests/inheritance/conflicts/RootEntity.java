package fr.uem.efluid.tests.inheritance.conflicts;

import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;

import java.time.LocalDateTime;

@ParameterTable("SAME_TABLE")
public class RootEntity {

    @ParameterKey
    private int key;

    private String value;

    private String something;

    @ParameterValue("OTHER_VALUE")
    private LocalDateTime otherValue;

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSomething() {
        return something;
    }

    public void setSomething(String something) {
        this.something = something;
    }

    public LocalDateTime getOtherValue() {
        return otherValue;
    }

    public void setOtherValue(LocalDateTime otherValue) {
        this.otherValue = otherValue;
    }
}
