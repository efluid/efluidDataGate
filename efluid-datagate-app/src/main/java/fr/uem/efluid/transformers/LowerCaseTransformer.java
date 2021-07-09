package fr.uem.efluid.transformers;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.tools.diff.ManagedValueConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Another Transformer example
 */
@Component
public class LowerCaseTransformer extends ColumnTransformer<LowerCaseTransformer.Config, LowerCaseTransformer.Runner> {

    @Autowired
    public LowerCaseTransformer(ManagedValueConverter converter, TransformerValueProvider provider) {
        super( converter, provider);
    }

    @Override
    public String getName() {
        return "LOWERCASE_TRANSFORMER";
    }

    @Override
    protected LowerCaseTransformer.Config newConfig() {
        return new LowerCaseTransformer.Config();
    }

    @Override
    protected Runner runner(LowerCaseTransformer.Config config, DictionaryEntry dict) {
        return new Runner(getValueProvider(), config, dict);
    }

    public static class Config extends ColumnTransformer.Config {

        public Config() {
            super();
        }
    }

    public static class Runner extends ColumnTransformer.Runner<LowerCaseTransformer.Config> {

        public Runner(TransformerValueProvider provider, LowerCaseTransformer.Config config, DictionaryEntry dict) {
            super(provider, config, dict);
        }

        @Override
        protected String transformValue(String value, ColumnType type) {
            return value.toLowerCase();
        }
    }
}
