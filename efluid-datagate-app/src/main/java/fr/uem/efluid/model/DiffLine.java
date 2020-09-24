package fr.uem.efluid.model;

import fr.uem.efluid.model.entities.IndexAction;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * Minimal model for a diff line
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
public interface DiffLine extends ContentLine, Comparable<DiffLine> {

    UUID getDictionaryEntryUuid();

    IndexAction getAction();

    long getTimestamp();

    String getPrevious();

    static DiffLine combined(UUID dictionaryEntryUuid, String keyValue, String payload, String previous, IndexAction action, long timestamp) {
        return new CombinedDiffLine(dictionaryEntryUuid, keyValue, payload, previous, action, timestamp);
    }

    /**
     * <p>
     * Replay all difflines to get only one with combined results
     * </p>
     */
    static DiffLine combinedOnSameTableAndKey(List<? extends DiffLine> linesOnSameTableSameKey, boolean keepDeleted) {

        if (linesOnSameTableSameKey == null || linesOnSameTableSameKey.size() == 0) {
            return null;
        }

        DiffLine first = linesOnSameTableSameKey.get(0);

        // Only one : simply keep it
        if (linesOnSameTableSameKey.size() == 1) {
            return first;
        }

        IndexAction currentAction = null;
        String currentPayload = null;
        String previousPayload = null;
        long timestamp = 0;

        // Replay, regarding each line action
        for (DiffLine line : linesOnSameTableSameKey) {

            timestamp = line.getTimestamp();

            switch (line.getAction()) {
                case ADD:
                    currentAction = line.getAction();
                    previousPayload = null;
                    currentPayload = line.getPayload();
                    break;
                case UPDATE:
                    // If updating an addition, became a new addition
                    if (currentAction != IndexAction.ADD) {
                        currentAction = line.getAction();
                        previousPayload = currentPayload;
                    }
                    currentPayload = line.getPayload();
                    break;
                case REMOVE:
                default:
                    previousPayload = currentPayload;
                    // If was added in same scope : drop it totaly
                    if (!keepDeleted && currentAction == IndexAction.ADD) {
                        currentAction = null;
                    } else {
                        currentAction = line.getAction();
                        currentPayload = line.getPayload();
                    }
                    break;
            }
        }

        // Produces simulated diffline, with merged replayed value
        return currentAction != null
                ? DiffLine.combined(first.getDictionaryEntryUuid(), first.getKeyValue(), currentPayload, previousPayload, currentAction, timestamp)
                : null;
    }

    /**
     * @author elecomte
     * @version 1
     * @since v0.0.1
     */
    class CombinedDiffLine implements DiffLine {

        private final UUID dictionaryEntryUuid;

        private final String keyValue;

        private final String payload;

        private final String previous;

        private final IndexAction action;

        private final long timestamp;

        CombinedDiffLine(UUID dictionaryEntryUuid, String keyValue, String payload, String previous, IndexAction action, long timestamp) {
            super();
            this.dictionaryEntryUuid = dictionaryEntryUuid;
            this.keyValue = keyValue;
            this.payload = payload;
            this.previous = previous;
            this.action = action;
            this.timestamp = timestamp;
        }

        /**
         * @return the dictionaryEntryUuid
         */
        @Override
        public UUID getDictionaryEntryUuid() {
            return this.dictionaryEntryUuid;
        }

        /**
         * @return the keyValue
         */
        @Override
        public String getKeyValue() {
            return this.keyValue;
        }

        /**
         * @return the payload
         */
        @Override
        public String getPayload() {
            return this.payload;
        }

        /**
         * @return the action
         */
        @Override
        public IndexAction getAction() {
            return this.action;
        }

        /**
         * @return the timestamp
         */
        @Override
        public long getTimestamp() {
            return this.timestamp;
        }

        @Override
        public String getPrevious() {
            return this.previous;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "Combined [dict:" + getDictionaryEntryUuid() + ", chg:" + this.action + "@" + this.keyValue + "|" + this.payload + "]";
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.action == null) ? 0 : this.action.hashCode());
            result = prime * result + ((this.dictionaryEntryUuid == null) ? 0 : this.dictionaryEntryUuid.hashCode());
            result = prime * result + ((this.keyValue == null) ? 0 : this.keyValue.hashCode());
            result = prime * result + ((this.payload == null) ? 0 : this.payload.hashCode());
            return result;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CombinedDiffLine other = (CombinedDiffLine) obj;
            if (this.action != other.action)
                return false;
            if (this.dictionaryEntryUuid == null) {
                if (other.dictionaryEntryUuid != null)
                    return false;
            } else if (!this.dictionaryEntryUuid.equals(other.dictionaryEntryUuid))
                return false;
            if (this.keyValue == null) {
                if (other.keyValue != null)
                    return false;
            } else if (!this.keyValue.equals(other.keyValue))
                return false;
            if (this.payload == null) {
                if (other.payload != null)
                    return false;
            } else if (!this.payload.equals(other.payload))
                return false;
            return true;
        }
    }

    @Override
    default int compareTo(DiffLine o) {
        return Long.compare(getTimestamp(), o.getTimestamp());
    }
}
