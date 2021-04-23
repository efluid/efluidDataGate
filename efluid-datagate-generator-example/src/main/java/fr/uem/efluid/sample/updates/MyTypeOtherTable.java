package fr.uem.efluid.sample.updates;

import fr.uem.efluid.*;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@ParameterTableSet(tables = {
        @ParameterTable(tableName = "MY_TYPE", keys = @ParameterKey(value = "KEY", type = ColumnType.STRING)),
        @ParameterTable(tableName = "MY_TYPE_CHILD", keys = @ParameterKey(value = "KEY", type = ColumnType.STRING))
}
)
public class MyTypeOtherTable extends MyType {

    @ParameterValue
    private Long localChildValue;

    protected static final String NOT_TO_MAP_AS_A_VALUE = "must be ignored";

    public Long getLocalChildValue() {
        return this.localChildValue;
    }

    public void setLocalChildValue(Long localChildValue) {
        this.localChildValue = localChildValue;
    }
}
