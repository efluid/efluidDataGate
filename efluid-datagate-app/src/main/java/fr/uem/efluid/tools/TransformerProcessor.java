package fr.uem.efluid.tools;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.services.types.PreparedIndexEntry;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A chained / pre-organized set of transformer for processing an imported commit (at index level). Single use
 *
 * @author elecomte
 * @version 1
 * @since v1.2.0
 */
public class TransformerProcessor {

    private final List<TransformerApply> sortedTransformers;

    public TransformerProcessor(Collection<TransformerApply> transformerApplys) {
        this.sortedTransformers = transformerApplys.stream()
                .sorted(Comparator.comparing(TransformerApply::getPriority).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Entry point for a full transformation on a given dictionary entry
     *
     * @param dict
     * @param mergeDiff
     * @return
     */
    public List<? extends PreparedIndexEntry> transform(DictionaryEntry dict, List<? extends PreparedIndexEntry> mergeDiff) {
        List<? extends PreparedIndexEntry> transformed = mergeDiff;

        // Simple chained process
        for (TransformerApply apply : sortedTransformers) {
            transformed = apply.transform(dict, transformed);
        }

        return transformed;
    }

    /**
     * Note : uses the Transformer dedicated log
     */
    public static class TransformerApply {

        private final String name;
        private final Transformer<?, ?> transformer;
        private final Transformer.TransformerConfig config;
        private final int priority;

        public TransformerApply(String name, Transformer<?, ?> transformer, Transformer.TransformerConfig config, int priority) {
            this.name = name;
            this.transformer = transformer;
            this.config = config;
            this.priority = priority;
        }

        List<? extends PreparedIndexEntry> transform(
                DictionaryEntry dict,
                List<? extends PreparedIndexEntry> mergeDiff) {

            // Do transform
            if (this.transformer.isApplyOnDictionaryEntry(dict, config)) {
                Transformer.LOGGER_TRANSFORMATIONS.info("Transformation is possible for transformer \"{}\" (type {}) on DictionaryEntry table \"{}\"",
                        this.name, this.transformer.getName(), dict.getTableName());
                this.transformer.transform(dict, this.config, mergeDiff);
            }

            // Do NOT transform (log if enabled)
            else if (Transformer.LOGGER_TRANSFORMATIONS.isInfoEnabled()) {
                Transformer.LOGGER_TRANSFORMATIONS.info("No transformation process for transformer \"{}\" (type {}) on DictionaryEntry table \"{}\"",
                        this.name, this.transformer.getName(), dict.getTableName());
            }

            return mergeDiff;
        }

        public int getPriority() {
            return priority;
        }
    }

}
