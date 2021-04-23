package fr.uem.efluid.sample.remarks;

import fr.uem.efluid.*;

@ParameterTableSet(tables = {
        @ParameterTable(
                name = "EtapeWorkflowObjetGeneriqueSubOne",
                tableName = "T_ETAPE_WFL_SUB",
                keys = @ParameterKey(value = "KEY", type = ColumnType.ATOMIC),
                values = {
                        @ParameterValue("VALUE"),
                        @ParameterValue("EXTENDED")
                }),
        @ParameterTable(
                name = "EtapeWorkflowObjetGeneriqueSubTwo",
                tableName = "T_ETAPE_WFL_GEN",
                keys = @ParameterKey(value = "KEY", type = ColumnType.ATOMIC),
                values = @ParameterValue("EXTENDED")
        )
}, useAllFields = false)
public class EtapeWorkflowObjetGenerique extends EtapeWorkflow {

    private String extended;

    public String getExtended() {
        return extended;
    }

    public void setExtended(String extended) {
        this.extended = extended;
    }
}
