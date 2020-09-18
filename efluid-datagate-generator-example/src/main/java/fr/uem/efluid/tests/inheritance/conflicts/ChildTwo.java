package fr.uem.efluid.tests.inheritance.conflicts;

import fr.uem.efluid.ParameterCompositeValue;
import fr.uem.efluid.ParameterLink;
import fr.uem.efluid.ParameterValue;

public class ChildTwo extends RootEntity {

    private boolean confirmationNecessaire = false;
    private String typeChargementObjetTraite;
    private String typeChargementObjetConnexe;

    @ParameterCompositeValue({"ETAPEDUTRAITEMENTINIT", "ETAPEDUTRAITEMENTINIT_ROLE"})
    @ParameterLink(toColumn = {"ID", "ROLE"}, toTableName = "TETAPEWORKFLOW")
    private OtherOne etapeDuTraitementInitialisation;

    @ParameterCompositeValue({"ETAPEDUTRAITEMENTUNITAIRE_ID", "ETAPEDUTRAITEMENTUNITAIRE_ROLE"})
    @ParameterLink(toColumn = {"ID", "ROLE"}, toTableName = "TETAPEWORKFLOW")
    private OtherOne etapeDuTraitementUnitaire;

    @ParameterCompositeValue({"ETAPEDUTRAITEMENTFILDELEAU_ID", "ETAPEDUTRAITMTFILDELEAU_ROLE"})
    @ParameterLink(toColumn = {"ID", "ROLE"}, toTableName = "TETAPEWORKFLOW")
    private OtherOne etapeDuTraitementFilDeLeau;

    @ParameterValue("TRAITEMENTREGROUPEMENT_ID")
    @ParameterLink(toColumn = "ID", toTableName = "TTRAITEMENTEXECUTIONETAPE")
    private OtherTwo traitementRegroupement;

    public boolean isConfirmationNecessaire() {
        return confirmationNecessaire;
    }

    public void setConfirmationNecessaire(boolean confirmationNecessaire) {
        this.confirmationNecessaire = confirmationNecessaire;
    }

    public String getTypeChargementObjetTraite() {
        return typeChargementObjetTraite;
    }

    public void setTypeChargementObjetTraite(String typeChargementObjetTraite) {
        this.typeChargementObjetTraite = typeChargementObjetTraite;
    }

    public String getTypeChargementObjetConnexe() {
        return typeChargementObjetConnexe;
    }

    public void setTypeChargementObjetConnexe(String typeChargementObjetConnexe) {
        this.typeChargementObjetConnexe = typeChargementObjetConnexe;
    }

    public OtherOne getEtapeDuTraitementInitialisation() {
        return etapeDuTraitementInitialisation;
    }

    public void setEtapeDuTraitementInitialisation(OtherOne etapeDuTraitementInitialisation) {
        this.etapeDuTraitementInitialisation = etapeDuTraitementInitialisation;
    }

    public OtherOne getEtapeDuTraitementUnitaire() {
        return etapeDuTraitementUnitaire;
    }

    public void setEtapeDuTraitementUnitaire(OtherOne etapeDuTraitementUnitaire) {
        this.etapeDuTraitementUnitaire = etapeDuTraitementUnitaire;
    }

    public OtherOne getEtapeDuTraitementFilDeLeau() {
        return etapeDuTraitementFilDeLeau;
    }

    public void setEtapeDuTraitementFilDeLeau(OtherOne etapeDuTraitementFilDeLeau) {
        this.etapeDuTraitementFilDeLeau = etapeDuTraitementFilDeLeau;
    }

    public OtherTwo getTraitementRegroupement() {
        return traitementRegroupement;
    }

    public void setTraitementRegroupement(OtherTwo traitementRegroupement) {
        this.traitementRegroupement = traitementRegroupement;
    }
}
