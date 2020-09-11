package fr.uem.efluid.tests.inheritance.onValues;

import fr.uem.efluid.*;

/**
 * Based on efluid models
 */
@ParameterTable(name = "ModeleDeCampagneWorkflow", tableName = "TMODELEDECAMPAGNEWKF", excludeInherited = @ParameterInheritance(of = EfluidSubRoot.class))
public class ModeleDeCampagneWorkflow extends ModeleDeCampagne {

    @ParameterValue(name = "COMBINAISON_ID", forTable = "TMODELEDECAMPAGNEWKF")
    @ParameterLink(toTableName = "TCOMBINAISONCOMPLEXE", toColumn = "ID")
    private EfluidCombination combinaison = null;

    public EfluidCombination getCombinaison() {
        return combinaison;
    }

    public void setCombinaison(EfluidCombination combinaison) {
        this.combinaison = combinaison;
    }
}
