package fr.uem.efluid.transformers;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.tools.diff.ManagedValueConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Transformer example
 */
@Component
public class UpperCaseTransformer extends ColumnTransformer<UpperCaseTransformer.Config, UpperCaseTransformer.Runner> {

    @Autowired
    public UpperCaseTransformer(ManagedValueConverter converter, TransformerValueProvider provider) {
        super(converter, provider);
    }

    @Override
    public String getName() {
        return "UPPERCASE_TRANSFORMER";
    }

    @Override
    protected UpperCaseTransformer.Config newConfig() {
        return new UpperCaseTransformer.Config();
    }

    @Override
    protected Runner runner(UpperCaseTransformer.Config config, DictionaryEntry dict) {
        return new Runner(getValueProvider(), config, dict);
    }

    public static class Config extends ColumnTransformer.Config {

        public Config(){
            super();
        }
    }

    public static class Runner extends ColumnTransformer.Runner<UpperCaseTransformer.Config> {

        public Runner(TransformerValueProvider provider, UpperCaseTransformer.Config config, DictionaryEntry dict) {
            super(provider, config, dict);
        }

        @Override
        protected String transformValue(String value, ColumnType type) {
            return value.toUpperCase();
        }
    }
}
