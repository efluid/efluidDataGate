package fr.uem.efluid.services.types;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.IndexEntry;
import fr.uem.efluid.tools.diff.RollbackConverter;

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
public class PreparedRevertIndexEntry extends PreparedIndexEntry {

    private PreparedIndexEntry source;

    /**
     *
     */
    public PreparedRevertIndexEntry() {
        super();
    }


    /**
     * Used when reading an imported index content
     *
     * @param existing entry from import
     * @return for merge
     */
    public static PreparedRevertIndexEntry fromEntityToRevert(IndexEntry existing, RollbackConverter converter) {

        PreparedRevertIndexEntry data = new PreparedRevertIndexEntry();

        completeRevert(data, converter.toRollbackLine(existing));
        data.source = PreparedIndexEntry.fromExistingEntity(existing);
        data.setTimestamp(System.currentTimeMillis());

        return data;
    }

    /**
     * Used when reading an index content
     *
     * @param data preparing IndexEntry def
     * @param roll definition of revert line
     */
    protected static void completeRevert(PreparedRevertIndexEntry data, DiffLine roll) {

        data.setAction(roll.getAction());
        data.setDictionaryEntryUuid(roll.getDictionaryEntryUuid());

        data.setPayload(roll.getPayload());
        data.setPrevious(roll.getPrevious());
        data.setKeyValue(roll.getKeyValue());
        data.setIndexForDiff(roll.getDictionaryEntryUuid() + "_" + roll.getKeyValue());
    }

    public PreparedIndexEntry getSource() {
        return source;
    }

    public void setSource(PreparedIndexEntry source) {
        this.source = source;
    }
}
