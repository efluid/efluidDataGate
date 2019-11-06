package fr.uem.efluid.sample.remarks;

import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterTable;

@ParameterTable(tableName = "T_CHILD_TABLE_TYPE_DROP", excludeInheritedFrom = ParentType.class)
public class InheritingParentTypeTestDrop extends ParentType {

    @ParameterKey
    private String keyField;

    private String value;

    private String other;

    public String getKeyField() {
        return keyField;
    }

    public void setKeyField(String keyField) {
        this.keyField = keyField;
    }

    @Override
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
}
