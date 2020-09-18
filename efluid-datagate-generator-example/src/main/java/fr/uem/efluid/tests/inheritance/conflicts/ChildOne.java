package fr.uem.efluid.tests.inheritance.conflicts;

import fr.uem.efluid.ParameterCompositeValue;
import fr.uem.efluid.ParameterIgnored;
import fr.uem.efluid.ParameterLink;

public class ChildOne extends RootEntity  {

    @ParameterIgnored
    @Deprecated
    private boolean chargerSpecifiqueEDP;

    @ParameterLink(toColumn = { "ID", "ROLE" }, toTableName = "TETAPEWORKFLOW")
    @ParameterCompositeValue({ "ETAPEDUTRAITEMENTDEMASSE_ID", "ETAPEDUTRAITEMENTDEMASSE_ROLE" })
    private OtherOne etapeDuTraitementDeMasse = null; // backref des traitements de masse d'une �tape

    public boolean isChargerSpecifiqueEDP() {
        return chargerSpecifiqueEDP;
    }

    public void setChargerSpecifiqueEDP(boolean chargerSpecifiqueEDP) {
        this.chargerSpecifiqueEDP = chargerSpecifiqueEDP;
    }

    public OtherOne getEtapeDuTraitementDeMasse() {
        return etapeDuTraitementDeMasse;
    }

    public void setEtapeDuTraitementDeMasse(OtherOne etapeDuTraitementDeMasse) {
        this.etapeDuTraitementDeMasse = etapeDuTraitementDeMasse;
    }
}
