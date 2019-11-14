package fr.uem.efluid.model.shared;

import fr.uem.efluid.model.Shared;
import fr.uem.efluid.model.UpdateChecked;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * <p>
 * Common features for referenced items with composite key support
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public interface CompositeRefSupport<D extends ExportAwareDictionaryEntry<?>> extends Shared, UpdateChecked {

    /**
     * @return
     */
    String getName();

    /**
     * @return
     */
    String getColumnFrom();

    String getTableTo();

    String getColumnTo();

    /**
     * @return the dictionaryEntry
     */
    D getDictionaryEntry();

    /**
     * @return
     */
    String getExt1ColumnTo();

    /**
     * @return
     */
    String getExt2ColumnTo();

    /**
     * @return
     */
    String getExt3ColumnTo();

    /**
     * @return
     */
    String getExt4ColumnTo();

    /**
     * @return
     */
    String getExt1ColumnFrom();

    /**
     * @return
     */
    String getExt2ColumnFrom();

    /**
     * @return
     */
    String getExt3ColumnFrom();

    /**
     * @return
     */
    String getExt4ColumnFrom();

    /**
     * @return
     */
    default Stream<String> columnFroms() {

        // For composite, use advanced building from iterator
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new ColumnFromIterator(this), 0), false);
    }

    /**
     * @return
     */
    default Stream<String> columnTos() {

        // For composite, use advanced building from iterator
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new ColumnToIterator(this), 0), false);
    }

    /**
     * @return
     */
    default boolean isCompositeKey() {
        return getExt1ColumnFrom() != null || getExt1ColumnTo() != null;
    }

    /**
     * <p>
     * For easy use of composite key model
     * </p>
     *
     * @author elecomte
     * @version 1
     * @since v0.0.8
     */
    final class ColumnFromIterator implements Iterator<String> {

        private int max = 0;
        private int pos = 0;

        private final CompositeRefSupport<?> lin;

        /**
         * @param lin
         */
        public ColumnFromIterator(CompositeRefSupport<?> lin) {
            super();
            this.lin = lin;

            // Standard key - not composite
            if (!lin.isCompositeKey()) {
                this.max = 1;
            }

            // Composite, search for key defs
            else {
                if (lin.getExt4ColumnFrom() != null) {
                    this.max = 5;
                } else if (lin.getExt3ColumnFrom() != null) {
                    this.max = 4;
                } else if (lin.getExt2ColumnFrom() != null) {
                    this.max = 3;
                } else {
                    this.max = 2;
                }
            }
        }

        /**
         * @return
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return this.pos < this.max;
        }

        /**
         * @return
         * @see java.util.Iterator#next()
         */
        @Override
        public String next() {

            switch (this.pos) {
                case 0:
                    this.pos++;
                    return this.lin.getColumnFrom();
                case 1:
                    this.pos++;
                    return this.lin.getExt1ColumnFrom();
                case 2:
                    this.pos++;
                    return this.lin.getExt2ColumnFrom();
                case 3:
                    this.pos++;
                    return this.lin.getExt3ColumnFrom();
                case 4:
                default:
                    this.pos++;
                    return this.lin.getExt4ColumnFrom();
            }
        }

    }

    /**
     * <p>
     * For easy use of composite key model
     * </p>
     *
     * @author elecomte
     * @version 1
     * @since v0.0.8
     */
    final class ColumnToIterator implements Iterator<String> {

        private int max = 0;
        private int pos = 0;

        private final CompositeRefSupport<?> lin;

        /**
         * @param lin
         */
        public ColumnToIterator(CompositeRefSupport<?> lin) {
            super();
            this.lin = lin;

            // Standard key - not composite
            if (!lin.isCompositeKey()) {
                this.max = 1;
            }

            // Composite, search for key defs
            else {
                if (lin.getExt4ColumnTo() != null) {
                    this.max = 5;
                } else if (lin.getExt3ColumnTo() != null) {
                    this.max = 4;
                } else if (lin.getExt2ColumnTo() != null) {
                    this.max = 3;
                } else {
                    this.max = 2;
                }
            }
        }

        /**
         * @return
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return this.pos < this.max;
        }

        /**
         * @return
         * @see java.util.Iterator#next()
         */
        @Override
        public String next() {

            switch (this.pos) {
                case 0:
                    this.pos++;
                    return this.lin.getColumnTo();
                case 1:
                    this.pos++;
                    return this.lin.getExt1ColumnTo();
                case 2:
                    this.pos++;
                    return this.lin.getExt2ColumnTo();
                case 3:
                    this.pos++;
                    return this.lin.getExt3ColumnTo();
                case 4:
                default:
                    this.pos++;
                    return this.lin.getExt4ColumnTo();
            }
        }

    }
}
