package fr.uem.efluid.tools;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.services.types.Value;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Transformer model for process at column level
 */
public abstract class ColumnTransformer<C extends fr.uem.efluid.tools.ColumnTransformer.Config, R extends ColumnTransformer.Runner<C>> extends Transformer<C, R> {

    public abstract static class Config extends TransformerConfig {

        private List<String> columnNames;

        @JsonIgnore
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

            // TODO : find better solution for performances ...

            // Preload pattern
            if (this.columnMatchers == null) {
                this.columnMatchers = this.columnNames.stream().map(Pattern::compile).collect(Collectors.toList());
            }

            return this.columnMatchers.stream().anyMatch(c -> c.matcher(value.getName()).matches());
        }
    }

    public abstract static class Runner<C extends ColumnTransformer.Config> extends TransformerRunner<C> {

        public Runner(C config, DictionaryEntry dict) {
            super(config, dict);
        }

        @Override
        public String apply(Value value) {

            if (this.config.isColumnNameMatches(value)) {
                return transformValue(value.getValueAsString(), value.getType());
            }

            return value.getValueAsString();
        }

        protected abstract String transformValue(String value, ColumnType type);
    }
}
