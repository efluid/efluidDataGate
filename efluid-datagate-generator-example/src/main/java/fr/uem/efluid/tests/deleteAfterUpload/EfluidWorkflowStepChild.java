package fr.uem.efluid.tests.deleteAfterUpload;

import fr.uem.efluid.ParameterLink;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterTableSet;
import fr.uem.efluid.ParameterValue;

@ParameterTableSet(domainName = "Workflow", tables = {
        @ParameterTable(name = "WorkflowStep"),
        @ParameterTable(name = "WorkflowStepGenericObject", tableName = "TETAPEWKFOBJGEN"),
})
public class EfluidWorkflowStepChild extends EfluidWorkflowStepRoot {

    @ParameterValue(name = "FUNCTION_ID", forTable = "TETAPEWKFOBJGEN")
    @ParameterLink(toColumn = "ID", toTableName = "TMODELEFONCTION")
    private EfluidFunction function;

    public EfluidFunction getFunction() {
        return function;
    }

    public void setFunction(EfluidFunction function) {
        this.function = function;
    }
}
