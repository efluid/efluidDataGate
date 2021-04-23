package fr.uem.efluid.tests.inheritance.onValues;


import fr.uem.efluid.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Based on efluid models
 */
@EfluidWorkflowDomain
@ParameterTable(name = "ModeleDeCampagne", tableName = "TMODELEDECAMPAGNE",
        keys = @ParameterKey(value = "ID", type = ColumnType.STRING))
public class ModeleDeCampagne extends EfluidSubRoot {

    private int delaiExecutionPrevu;

    private String typeCampagne;

    private String confidentialite = "AUCUN";

    @ParameterIgnored
    @Deprecated
    private int statutModeleDeCampagne = 12;

    private boolean clotureAutomatique = false;

    private String natureCampagne;

    private boolean executionAutomatiqueEcheances;

    private boolean executionMultiEcheances;

    @ParameterValue("AUTORISERFERMETURESURSELECVIDE")
    private boolean autoriserFermetureSurSelectionVide;

    @ParameterValue("GENERATIONAUTOPLANNINGSUIVANT")
    private boolean generationAutomatiquePlanningSuivant;

    @ParameterIgnored
    private String modeleLotIsole;

    private Set modelesDeLots = new HashSet();

    @ParameterIgnored
    private boolean confidentialiteModifiee = false;

    public int getDelaiExecutionPrevu() {
        return delaiExecutionPrevu;
    }

    public void setDelaiExecutionPrevu(int delaiExecutionPrevu) {
        this.delaiExecutionPrevu = delaiExecutionPrevu;
    }

    public String getTypeCampagne() {
        return typeCampagne;
    }

    public void setTypeCampagne(String typeCampagne) {
        this.typeCampagne = typeCampagne;
    }

    public String getConfidentialite() {
        return confidentialite;
    }

    public void setConfidentialite(String confidentialite) {
        this.confidentialite = confidentialite;
    }

    public int getStatutModeleDeCampagne() {
        return statutModeleDeCampagne;
    }

    public void setStatutModeleDeCampagne(int statutModeleDeCampagne) {
        this.statutModeleDeCampagne = statutModeleDeCampagne;
    }

    public boolean isClotureAutomatique() {
        return clotureAutomatique;
    }

    public void setClotureAutomatique(boolean clotureAutomatique) {
        this.clotureAutomatique = clotureAutomatique;
    }

    public String getNatureCampagne() {
        return natureCampagne;
    }

    public void setNatureCampagne(String natureCampagne) {
        this.natureCampagne = natureCampagne;
    }

    public boolean isExecutionAutomatiqueEcheances() {
        return executionAutomatiqueEcheances;
    }

    public void setExecutionAutomatiqueEcheances(boolean executionAutomatiqueEcheances) {
        this.executionAutomatiqueEcheances = executionAutomatiqueEcheances;
    }

    public boolean isExecutionMultiEcheances() {
        return executionMultiEcheances;
    }

    public void setExecutionMultiEcheances(boolean executionMultiEcheances) {
        this.executionMultiEcheances = executionMultiEcheances;
    }

    public boolean isAutoriserFermetureSurSelectionVide() {
        return autoriserFermetureSurSelectionVide;
    }

    public void setAutoriserFermetureSurSelectionVide(boolean autoriserFermetureSurSelectionVide) {
        this.autoriserFermetureSurSelectionVide = autoriserFermetureSurSelectionVide;
    }

    public boolean isGenerationAutomatiquePlanningSuivant() {
        return generationAutomatiquePlanningSuivant;
    }

    public void setGenerationAutomatiquePlanningSuivant(boolean generationAutomatiquePlanningSuivant) {
        this.generationAutomatiquePlanningSuivant = generationAutomatiquePlanningSuivant;
    }

    public String getModeleLotIsole() {
        return modeleLotIsole;
    }

    public void setModeleLotIsole(String modeleLotIsole) {
        this.modeleLotIsole = modeleLotIsole;
    }

    public Set getModelesDeLots() {
        return modelesDeLots;
    }

    public void setModelesDeLots(Set modelesDeLots) {
        this.modelesDeLots = modelesDeLots;
    }

    public boolean isConfidentialiteModifiee() {
        return confidentialiteModifiee;
    }

    public void setConfidentialiteModifiee(boolean confidentialiteModifiee) {
        this.confidentialiteModifiee = confidentialiteModifiee;
    }
}
