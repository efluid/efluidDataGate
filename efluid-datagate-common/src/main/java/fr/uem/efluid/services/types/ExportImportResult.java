package fr.uem.efluid.services.types;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * For easy rendering of details on processed import or export,
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class ExportImportResult<T> {

    private Map<String, ItemCount> counts = new HashMap<>();

    private final T result;

    /**
     * @param result
     */
    public ExportImportResult(T result) {
        this.result = result;
    }

    /**
     * @return the result
     */
    public T getResult() {
        return this.result;
    }

    /**
     * @param type
     * @param added
     * @param modified
     * @param deleted
     */
    public void addCount(String type, long added, long modified, long deleted) {
        this.counts.put(type, new ItemCount(type, added, deleted, modified));
    }

    /**
     * Copy a result counts
     *
     * @param source
     */
    public void copyCounts(ExportImportResult<?> source) {
        source.counts.forEach((k, v) -> {

            ItemCount existing = this.counts.get(k);

            if (existing != null) {
                existing = new ItemCount(existing, v);
            } else {
                existing = v;
            }

            this.counts.put(k, existing);
        });
    }

    /**
     * @return
     */
    public static ExportImportResult<Void> newVoid() {
        return new ExportImportResult<>(null);
    }

    /**
     * @return the counts
     */
    public Map<String, ItemCount> getCounts() {
        return this.counts;
    }

    /**
     * <p>
     * As export/import packages can contains various type of data, manage count detail
     * for each separately
     * </p>
     *
     * @author elecomte
     * @version 1
     * @since v0.0.1
     */
    public static class ItemCount {

        private final String type;
        private final long added;
        private final long deleted;
        private final long modified;

        /**
         * @param type
         * @param added
         * @param deleted
         * @param modified
         */
        ItemCount(String type, long added, long deleted, long modified) {
            super();
            this.type = type;
            this.added = added;
            this.deleted = deleted;
            this.modified = modified;
        }

        ItemCount(ItemCount addLeft, ItemCount addRight) {
            super();
            this.type = addLeft.type;
            this.added = addLeft.added + addRight.added;
            this.deleted = addLeft.deleted + addRight.deleted;
            this.modified = addLeft.modified + addRight.modified;
        }

        /**
         * @return the type
         */
        public String getType() {
            return this.type;
        }

        /**
         * @return the added
         */
        public long getAdded() {
            return this.added;
        }

        /**
         * @return the deleted
         */
        public long getDeleted() {
            return this.deleted;
        }

        /**
         * @return the modified
         */
        public long getModified() {
            return this.modified;
        }

    }
}
