package fr.uem.efluid.tests.deleteAfterUpload;

import fr.uem.efluid.ParameterIgnored;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterValue;

/**
 * Based on efluid config
 */
@EfluidWorkflowDomain
@ParameterTable(name = "WorkflowStep", tableName = "TETAPEWORKFLOW")
public class EfluidWorkflowStepRoot extends EfluidObjectRoot {

    private String label;

    private String comment;

    private String labelStatus;

    private int stepNumber;

    private int delay;

    private String reference;

    @ParameterIgnored
    private String stepRoles;

    @ParameterValue(name = "SUPPROBJETTRAITEAUTORISEE")
    private boolean allowedProcessedObjectDelete = false;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getLabelStatus() {
        return labelStatus;
    }

    public void setLabelStatus(String labelStatus) {
        this.labelStatus = labelStatus;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getStepRoles() {
        return stepRoles;
    }

    public void setStepRoles(String stepRoles) {
        this.stepRoles = stepRoles;
    }

    public boolean isAllowedProcessedObjectDelete() {
        return allowedProcessedObjectDelete;
    }

    public void setAllowedProcessedObjectDelete(boolean allowedProcessedObjectDelete) {
        this.allowedProcessedObjectDelete = allowedProcessedObjectDelete;
    }
}
