package fr.uem.efluid.tools;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author elecomte
 * @version 1
 * @since v0.3.0
 */
public abstract class Transformer<C extends Transformer.TransformerConfig, R extends Transformer.TransformerRunner<C>> {

    @Autowired
    private ManagedValueConverter converter;

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
    public boolean isApplyOnDictionaryEntry(DictionaryEntry dict, C config) {
        return config.isTableNameMatches(dict);
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
    public List<? extends PreparedIndexEntry> transform(
            DictionaryEntry dict,
            TransformerConfig rawConfig,
            List<? extends PreparedIndexEntry> mergeDiff) {

        C config = (C) rawConfig;

        if (isApplyOnDictionaryEntry(dict, config)) {
            R runner = runner(config, dict);
            mergeDiff.forEach(l -> l.setPayload(
                    // Rebuild payload ...
                    this.converter.convertToExtractedValue(
                            // From expanded payload ...
                            this.converter.expandInternalValue(l.getPayload()).stream()
                                    .collect(Collectors.toMap(Value::getName,
                                            runner, // Transforming each values ...
                                            (a, b) -> a,
                                            LinkedHashMap::new)))));
        }

        return mergeDiff;
    }

    /**
     * Configuration spec. Specified as json
     */
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
    }

    /**
     * Model of runner for transformation. A new runner is instantiated for each DictionaryEntry.
     * Not threadsafe : can keep status on processed values, and keep current dict entry to process.
     */
    public static abstract class TransformerRunner<C extends Transformer.TransformerConfig> implements Function<Value, String> {

        protected final C config;

        protected final DictionaryEntry dict;

        public TransformerRunner(C config, DictionaryEntry dict) {
            this.config = config;
            this.dict = dict;
        }
    }
}