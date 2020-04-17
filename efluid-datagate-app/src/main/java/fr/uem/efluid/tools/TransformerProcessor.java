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
        this.sortedTransformers = transformerApplys.stream().sorted(Comparator.comparing(TransformerApply::getPriority)).collect(Collectors.toList());
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

    public static class TransformerApply {

        private final Transformer<?, ?> transformer;
        private final Transformer.TransformerConfig config;
        private final int priority;

        public TransformerApply(Transformer<?, ?> transformer, Transformer.TransformerConfig config, int priority) {
            this.transformer = transformer;
            this.config = config;
            this.priority = priority;
        }

        List<? extends PreparedIndexEntry> transform(
                DictionaryEntry dict,
                List<? extends PreparedIndexEntry> mergeDiff) {
            return this.transformer.transform(dict, this.config, mergeDiff);
        }

        public int getPriority() {
            return priority;
        }
    }

}
