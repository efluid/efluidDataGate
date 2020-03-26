package fr.uem.efluid.tools;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.services.types.Value;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Transformer example
 */
public class UpperCaseTransformer extends Transformer<UpperCaseTransformer.Config, UpperCaseTransformer.Runner> {

    @Override
    protected Config newConfig() {
        return null;
    }

    @Override
    protected Runner runner(Config config, DictionaryEntry dict) {
        return new Runner(config, dict);
    }

    public static class Config extends Transformer.TransformerConfig {

        private List<String> columnNames;

        private List<Pattern> columnMatchers;

        public List<String> getColumnNames() {
            return columnNames;
        }

        public void setColumnNames(List<String> columnNames) {
            this.columnNames = columnNames;
        }

        @Override
        void populateDefault() {
            super.populateDefault();
            this.columnNames = new ArrayList<>();
            this.columnNames.add(".*");
            this.columnNames.add("COL_.*");
            this.columnNames.add("COL_C");
        }

        @Override
        void checkContentIsValid(List<String> errors) {
            super.checkContentIsValid(errors);
            if (this.columnNames == null || this.columnNames.size() == 0) {
                errors.add("At least one column name must be specified. Use \".*\" as default to match all");
            } else if (this.columnNames.stream().anyMatch(StringUtils::isEmpty)) {
                errors.add("A column name cannot be empty. Use \".*\" as default to match all");
            }
        }

        boolean isColumnNameMatches(Value value) {

            // Preload pattern
            if (this.columnMatchers == null) {
                this.columnMatchers = this.columnNames.stream().map(Pattern::compile).collect(Collectors.toList());
            }

            return this.columnMatchers.stream().anyMatch(c -> c.matcher(value.getName()).matches());
        }
    }

    public static class Runner extends Transformer.TransformerRunner<UpperCaseTransformer.Config> {

        public Runner(Config config, DictionaryEntry dict) {
            super(config, dict);
        }

        @Override
        public String apply(Value value) {

            if (this.config.isColumnNameMatches(value)) {
                return value.getValueAsString().toUpperCase();
            }

            return value.getValueAsString();
        }
    }
}
