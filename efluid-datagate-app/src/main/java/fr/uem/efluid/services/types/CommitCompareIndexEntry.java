package fr.uem.efluid.services.types;

import java.util.Collection;
import java.util.UUID;

/**
 * Model of diff display for
 *
 * @author elecomte
 * @version 1
 * @since v3.1.11
 */
public abstract class CommitCompareIndexEntry extends PreparedIndexEntry {

    private final Collection<UUID> identifiedCommitUuids;

    protected CommitCompareIndexEntry(Collection<UUID> identifiedCommitUuids) {
        this.identifiedCommitUuids = identifiedCommitUuids;
    }

    public Collection<UUID> getIdentifiedCommitUuids() {
        return identifiedCommitUuids;
    }

    public abstract PreparedIndexEntry getContentByCommit(UUID commitUuid);
}
