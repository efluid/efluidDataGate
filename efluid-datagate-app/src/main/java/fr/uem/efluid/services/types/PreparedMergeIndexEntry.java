package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.IndexEntry;

/**
 * <p>
 * If result is null after prepare, then this entry is totaly IGNORED
 * </p>
 * <p>The resolution is processed with basic rules, from :<ul><li>"Their" type of modif</li><li>"Mine" type of modif</li><li>Temporal</li></ul></p>
 * <p>For some resolutions, a "warning message" can be specified if the merge content is not "standard"</p>
 *
 * @author elecomte
 * @version 3
 * @since v0.0.1
 */
public class PreparedMergeIndexEntry extends PreparedIndexEntry {

    private PreparedIndexEntry mine;

    private PreparedIndexEntry their;

    private boolean needAction;

    private String resolutionWarning;

    private String resolutionRule;

    /**
     *
     */
    public PreparedMergeIndexEntry() {
        super();
    }

    public String getResolutionRule() {
        return this.resolutionRule;
    }

    public void setResolutionRule(String resolutionRule) {
        this.resolutionRule = resolutionRule;
    }

    /**
     * @return the mine
     */
    public PreparedIndexEntry getMine() {
        return this.mine;
    }

    /**
     * @param mine the mine to set
     */
    public void setMine(PreparedIndexEntry mine) {
        this.mine = mine;
    }

    /**
     * @return the their
     */
    public PreparedIndexEntry getTheir() {
        return this.their;
    }

    /**
     * @param their the their to set
     */
    public void setTheir(PreparedIndexEntry their) {
        this.their = their;
    }


    /**
     * @return the needAction
     */
    @Override
    public boolean isNeedAction() {
        return this.needAction;
    }

    /**
     * @param needAction the needAction to set
     */
    public void setNeedAction(boolean needAction) {
        this.needAction = needAction;
    }

    public String getResolutionWarning() {
        return this.resolutionWarning;
    }

    public void setResolutionWarning(String resolutionWarning) {
        this.resolutionWarning = resolutionWarning;
    }

    /**
     * Used when reading an imported index content
     *
     * @param existing entry from import
     * @return for merge
     */
    public static PreparedMergeIndexEntry fromImportedEntity(IndexEntry existing) {

        PreparedMergeIndexEntry data = new PreparedMergeIndexEntry();

        completeFromExistingEntity(data, existing);

        return data;
    }

    @Override
    public boolean isSelected() {

        // If no need for action, never selected

        if (isNeedAction()) {
            return super.isSelected();
        }

        return false;
    }
}
