package fr.uem.efluid.sample.remarks;

import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterTableSet;
import fr.uem.efluid.ParameterValue;

@ParameterTableSet(tables = {
        @ParameterTable(name = "EtapeWorkflowObjetGeneriqueSubOne", tableName = "T_ETAPE_WFL_SUB", keyField = "key",
                values = {
                        @ParameterValue("value"),
                        @ParameterValue("extended")
                }),
        @ParameterTable(name = "EtapeWorkflowObjetGeneriqueSubTwo", tableName = "T_ETAPE_WFL_GEN", keyField = "key",
                values = {
                        @ParameterValue("extended")
                })
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
