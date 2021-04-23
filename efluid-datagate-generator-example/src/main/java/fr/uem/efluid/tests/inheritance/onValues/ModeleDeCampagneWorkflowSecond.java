package fr.uem.efluid.tests.inheritance.onValues;

import fr.uem.efluid.*;

/**
 * Based on efluid models
 */
@ParameterTableSet(
        domainName = EfluidWorkflowDomain.NAME,
        tables = @ParameterTable(
                name = "ModeleDeCampagneWorkflowSecond",
                tableName = "TMODELEDECAMPAGNEWKFSECOND",
                keys = @ParameterKey(value = "ID", type = ColumnType.STRING),
                values = @ParameterValue(name = "COMBINAISON_ID", forTable = "TMODELEDECAMPAGNEWKFSECOND")
        )
)
public class ModeleDeCampagneWorkflowSecond extends ModeleDeCampagne {

    @ParameterLink(withValueName = "COMBINAISON_ID", toTableName = "TCOMBINAISONCOMPLEXE", toColumn = "ID")
    private EfluidCombination combinaison = null;

    public EfluidCombination getCombinaison() {
        return combinaison;
    }

    public void setCombinaison(EfluidCombination combinaison) {
        this.combinaison = combinaison;
    }
}
