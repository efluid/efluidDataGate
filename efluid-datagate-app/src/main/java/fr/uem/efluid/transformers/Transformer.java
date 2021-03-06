package fr.uem.efluid.transformers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.tools.diff.ManagedValueConverter;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import fr.uem.efluid.utils.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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

        // Use iterator for process and update of the last in one operation
        Iterator<? extends PreparedIndexEntry> entries = mergeDiff.iterator();
        while (entries.hasNext()) {
            PreparedIndexEntry entry = entries.next();

            // Process only when runner can apply
            if (runner.test(entry)) {

                // Expand payload (modifiable list)
                List<Value> extracted = new ArrayList<>(this.converter.expandInternalValue(entry.getPayload()));

                // Apply transform and update line only if changes where detected
                if (runner.transform(entry.getAction(), entry.getKeyValue(), extracted)) {

                    if (LOGGER_TRANSFORMATIONS.isInfoEnabled()) {
                        LOGGER_TRANSFORMATIONS.info("Values processed by transformer {} on entry {}[{}] :\n{}",
                                this.getName(), dict.getTableName(), entry.getKeyValue(), buildTransformationResultForDebug(extracted));
                    }

                    // Drop erased values
                    if (extracted.isEmpty()) {
                        entries.remove();
                    }

                    // Or apply updated one
                    else {
                        // Rebuild payload
                        entry.setPayload(this.converter.convertToExtractedValue(extracted));
                    }
                }

                // Nothing has changed for line even if compliant
                else if (LOGGER_TRANSFORMATIONS.isDebugEnabled()) {
                    LOGGER_TRANSFORMATIONS.debug("No changes from transformer {} on entry {}[{}]",
                            this.getName(), dict.getTableName(), entry.getKeyValue());
                }

            }
        }
    }

    private static String buildTransformationResultForDebug(List<Value> content) {

        if (content.isEmpty()) {
            return "erased line";
        }

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

        // For optional attachment, if any, on current RUNNING config

        /**
         * If true, an attachment package is supported by transformer
         *
         * @return status on Attachment package which can be specified for config
         */
        @JsonIgnore
        public boolean isAttachmentPackageSupport() {
            return false;
        }

        /**
         * Called before export to initialize the attachment package data from the managed source.
         * Default will provide null value
         *
         * @param managedSource for managed database attachment loading
         * @return data or null if no need to process attachment package
         */
        public byte[] exportAttachmentPackageData(JdbcTemplate managedSource) {

            return null;
        }

        /**
         * For transformer with package data support, apply imported one if any.
         *
         * @param attachmentPackageData null, empty string, or prepared attachment package data
         *                              from imported transformer. Any format can be used for
         *                              the transformer support
         * @param managedSource         access to local managed DB for any post loading action
         * @param valueConverter        for any loaded value process with standard converter
         */
        public void importAttachmentPackageData(
                byte[] attachmentPackageData,
                JdbcTemplate managedSource,
                ManagedValueConverter valueConverter) {
            // Default does nothing
        }

        /**
         * If null, no attachment package is supported by transformer
         *
         * @param managedSource  access to local managed DB for any post loading action
         * @param valueConverter for any loaded value process with standard converter
         * @return comment on Attachment package which can be specified for config
         */
        @JsonIgnore
        public String getAttachmentPackageComment(
                JdbcTemplate managedSource,
                ManagedValueConverter valueConverter) {
            return null;
        }

        protected static boolean anyIsEmpty(Collection<String> vals){
            return vals.stream().anyMatch(v -> !StringUtils.hasText(v));
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
            implements Predicate<PreparedIndexEntry> {

        protected final C config;

        protected final DictionaryEntry dict;

        protected final TransformerValueProvider provider;

        public TransformerRunner(TransformerValueProvider provider, C config, DictionaryEntry dict) {
            this.provider = provider;
            this.config = config;
            this.dict = dict;
        }

        /**
         * Process transform on provided list of values
         *
         * @param action current action
         * @param key    line key (can be a composite key in the form "a / b / c"
         * @param values the payload extracted to a list of values, ready to be transformed. The list can be erased to drop a line
         * @return true if an update was done on values
         */
        public abstract boolean transform(IndexAction action, String key, List<Value> values);

        /**
         * Init a transformed value of the same type
         */
        protected static Value transformedValue(Value existing, String newValue) {
            return new TransformedValue(existing, FormatUtils.toBytes(newValue));
        }

        /**
         * Init a transformed value of a specified type
         */
        protected static Value transformedValue(Value existing, String newValue, ColumnType newType) {
            return new TransformedValue(existing, FormatUtils.toBytes(newValue), newType);
        }

        public static final class TransformedValue implements Value {

            private static final String TRANSFORMATION_DISPLAY = " -> ";

            private final Value target;
            private final byte[] modifiedContent;
            private final ColumnType modifiedType;

            private TransformedValue(Value target, byte[] modifiedContent) {
                this.target = target;
                this.modifiedContent = modifiedContent;
                this.modifiedType = target.getType();

                LOGGER.debug("Processed transformation on value from {} to {}", target.getValue(), modifiedContent);
            }

            private TransformedValue(Value target, byte[] modifiedContent, ColumnType type) {
                this.target = target;
                this.modifiedContent = modifiedContent;
                this.modifiedType = type;

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
                return this.modifiedType;
            }

            public String getTransformation() {
                return getName() + " : " + this.target.getValueAsString() + TRANSFORMATION_DISPLAY + getValueAsString();
            }
        }
    }
}