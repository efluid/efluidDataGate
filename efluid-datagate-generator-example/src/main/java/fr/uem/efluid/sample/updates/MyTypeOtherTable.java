package fr.uem.efluid.sample.updates;

import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterTableSet;
import fr.uem.efluid.ParameterValue;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@ParameterTableSet(tables = {
        @ParameterTable(tableName = "MY_TYPE", keyField = "key"),
        @ParameterTable(tableName = "MY_TYPE_CHILD", keyField = "key")
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
