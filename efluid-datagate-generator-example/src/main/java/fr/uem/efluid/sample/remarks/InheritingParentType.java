package fr.uem.efluid.sample.remarks;

import fr.uem.efluid.ParameterKey;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;

@ParameterTable(tableName = "T_CHILD_TABLE_TYPE")
public class InheritingParentType extends ParentType {

    @ParameterKey
    private String keyField;

    @ParameterValue
    private String value;

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
}
