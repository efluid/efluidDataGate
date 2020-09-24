package fr.uem.efluid.services.types;

import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.IndexAction;

import java.util.Objects;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class RollbackLine {

    private final DiffLine source;

    /**
     * @param source
     */
    public RollbackLine(DiffLine source) {
        super();
        this.source = source;
    }

    /**
     * @return the current
     */
    public DiffLine getSource() {
        return this.source;
    }

    /**
     * <p>
     * Convert the rollback data spec to a combined diff which can be ran as usual
     * (managed DB updates are always processed from DiffLine). So use basic rules to
     * define how the rollback can be applied :
     * <ul>
     * <li>If current data is null and previous are not, parameter was deleted =&gt; rollback
     * is an add, to set back previous values</li>
     * <li>If current is not null but previous, parameter was added =&gt; rollback is a
     * delete</li>
     * <li>If both are present, it was an update =&gt; rollback is an other "reverted"
     * update</li>
     * </ul>
     * </p>
     *
     * @return merged data from rollback to make it as a diffLine
     */
    public DiffLine toCombinedDiff() {

        long timestamp = System.currentTimeMillis();

        // Rollback on delete => became an add
        if (this.source.getAction() == IndexAction.REMOVE) {
            return DiffLine.combined(
                    this.source.getDictionaryEntryUuid(),
                    this.source.getKeyValue(),
                    this.source.getPrevious(),
                    null,
                    IndexAction.ADD,
                    timestamp);
        }

        // Rollback on add => became an delete
        if (this.source.getAction() == IndexAction.ADD) {
            return DiffLine.combined(
                    this.source.getDictionaryEntryUuid(),
                    this.source.getKeyValue(),
                    null,
                    this.source.getPayload(),
                    IndexAction.REMOVE,
                    timestamp);
        }

        // Other case are update current => previous
        return DiffLine.combined(
                this.source.getDictionaryEntryUuid(),
                this.source.getKeyValue(),
                this.source.getPrevious(),
                this.source.getPayload(),
                IndexAction.UPDATE,
                timestamp);
    }

}
