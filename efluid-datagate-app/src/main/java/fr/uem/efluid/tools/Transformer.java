package fr.uem.efluid.tools;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import fr.uem.efluid.utils.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.stream.Collectors.toList;

/**
 * Model for transformation of imported data : allows to update at merge some value to complete
 * them regarding some source-specified business rules.
 * <p>
 * THIS BREAK THE GIT PARADIGM : data processed at destination can be DIFFERENT from data on source.
 * The transformation allows for example to complete some missing properties, or to add some configuration
 * values which are related to destination environment
 *
 * @author elecomte
 * @version 2
 * @since v0.3.0
 */
public abstract class Transformer<C extends Transformer.TransformerConfig, R extends Transformer.TransformerRunner<C>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Transformer.class);

    static final Logger LOGGER_TRANSFORMATIONS = LoggerFactory.getLogger("transformer.results");

    private final ManagedValueConverter converter;

    private final TransformerValueProvider provider;

    protected Transformer(ManagedValueConverter converter, TransformerValueProvider provider) {
        this.converter = converter;
        this.provider = provider;
    }

    /**
     * Process a configured spec for
     *
     * @param config
     * @throws ApplicationException
     */
    public void validateConfig(C config) throws ApplicationException {

        List<String> errors = new ArrayList<>();
        config.checkContentIsValid(errors);

        if (errors.size() > 0) {
            throw new ApplicationException(
                    ErrorType.TRANSFORMER_CONFIG_WRONG,
                    "Wrong content of transformer configuration for type " + this.getClass().getName(),
                    String.join(",", errors));
        }
    }

    /**
     * Get the "example" configuration in edit screen
     *
     * @return
     */
    public C getDefaultConfig() {
        C config = newConfig();
        config.populateDefault();
        return config;
    }

    public abstract String getName();

    /**
     * Instantiate the required config type
     *
     * @return
     */
    protected abstract C newConfig();

    /**
     * Check if the given table must be processed by the current transformer regarding table pattern.
     * Default use config table pattern but this can be overridden
     *
     * @param dict
     * @param config
     * @return
     */
    public boolean isApplyOnDictionaryEntry(DictionaryEntry dict, Transformer.TransformerConfig config) {
        return config.isTableNameMatches(dict);
    }

    /**
     * Access to current value provider for needs of transformer processes
     *
     * @return available provider
     */
    protected TransformerValueProvider getValueProvider() {
        return this.provider;
    }

    /**
     * Init corresponding transformer runner for given dict and specified config
     *
     * @param dict
     * @param config
     * @return
     */
    protected abstract R runner(C config, DictionaryEntry dict);

    /**
     * Entry point for global transform process. Process each value independently
     *
     * @param dict
     * @param rawConfig (simplified - no specified type)
     * @param mergeDiff
     * @return
     */
    @SuppressWarnings("unchecked")
    public void transform(
            DictionaryEntry dict,
            TransformerConfig rawConfig,
            List<? extends PreparedIndexEntry> mergeDiff) {

        R runner = runner((C) rawConfig, dict);
        mergeDiff.stream()
                // Process only when runner can apply
                .filter(runner)
                .forEach(l -> {

                    // Expand payload (modifiable list)
                    List<Value> extracted = new ArrayList<>(this.converter.expandInternalValue(l.getPayload()));

                    // Apply transform
                    runner.accept(l.getAction(), extracted);

                    if (LOGGER_TRANSFORMATIONS.isInfoEnabled()) {
                        LOGGER_TRANSFORMATIONS.info("Values processed by transformer {} on DictionaryEntry table \"{}\" :\n{}",
                                this.getName(), dict.getTableName(), buildTransformationResultForDebug(extracted));
                    }

                    // Rebuild payload
                    l.setPayload(this.converter.convertToExtractedValue(extracted));
                });
    }

    private static String buildTransformationResultForDebug(List<Value> content) {

        List<String> results = content.stream()
                .filter(v -> v instanceof TransformerRunner.TransformedValue)
                .map(v -> ((TransformerRunner.TransformedValue) v).getTransformation())
                .collect(Collectors.toList());

        if (results.size() == 0) {
            return "no changes";
        }

        return results.stream().collect(Collectors.joining("\n + ", " + ", ""));
    }

    /**
     * Configuration spec. Specified as json
     *
     * <b>When defining a new TransformerConfig type you must take care that Collection / array properties can be null after deserialization on destination environment</b>
     */
    @JsonInclude(NON_NULL)
    public static abstract class TransformerConfig {

        private String tablePattern;

        protected TransformerConfig() {
            super();
        }

        void populateDefault() {
            this.tablePattern = ".*";
        }

        /**
         * Check specified dict entry over table pattern
         *
         * @param dict
         * @return
         */
        boolean isTableNameMatches(DictionaryEntry dict) {
            return Pattern
                    .compile(this.tablePattern)
                    .matcher(dict.getTableName())
                    .matches();
        }

        /**
         * @param errors
         */
        void checkContentIsValid(List<String> errors) {
            if (this.tablePattern == null) {
                errors.add("tablePattern cannot be empty or missing. Use \"*\" as default");
            }
        }

        public String getTablePattern() {
            return this.tablePattern;
        }

        public void setTablePattern(String tablePattern) {
            this.tablePattern = tablePattern;
        }

        /**
         * Helper to prepare pattern matchers for searching columns into a payload
         *
         * @param columnPatterns
         * @return
         */
        protected List<Pattern> generatePayloadMatchersFromColumnPatterns(Stream<String> columnPatterns) {
            return columnPatterns.map(v -> {
                if (v.equals(".*")) {
                    return v;
                }
                return "^.*" + v + ".*$";
            }).map(Pattern::compile).collect(toList());
        }
    }

    /**
     * <p>
     * Model of runner for transformation. A new runner is instantiated for each DictionaryEntry.
     * Not threadsafe : can keep status on processed values, and keep current dict entry to process.
     * </p>
     * <p>Runner filter "lines" to process and apply to values</p>
     */
    public static abstract class TransformerRunner<C extends Transformer.TransformerConfig>
            implements Predicate<PreparedIndexEntry>, BiConsumer<IndexAction, List<Value>> {

        protected final C config;

        protected final DictionaryEntry dict;

        protected final TransformerValueProvider provider;

        public TransformerRunner(TransformerValueProvider provider, C config, DictionaryEntry dict) {
            this.provider = provider;
            this.config = config;
            this.dict = dict;
        }

        protected Value transformedValue(Value existing, String newValue) {
            return new TransformedValue(existing, FormatUtils.toBytes(newValue));
        }

        public static final class TransformedValue implements Value {

            private static final String TRANSFORMATION_DISPLAY = " -> ";

            private Value target;

            private final byte[] modifiedContent;

            private TransformedValue(Value target, byte[] modifiedContent) {
                this.target = target;
                this.modifiedContent = modifiedContent;

                LOGGER.debug("Processed transformation on value from {} to {}", target.getValue(), modifiedContent);
            }

            @Override
            public String getName() {
                return this.target.getName();
            }

            @Override
            public byte[] getValue() {
                return this.modifiedContent;
            }

            @Override
            public ColumnType getType() {
                return this.target.getType();
            }

            public String getTransformation() {
                return getName() + " : " + this.target.getValueAsString() + TRANSFORMATION_DISPLAY + getValueAsString();
            }
        }
    }
}