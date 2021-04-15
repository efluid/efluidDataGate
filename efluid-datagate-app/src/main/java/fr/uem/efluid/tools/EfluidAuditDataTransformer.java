package fr.uem.efluid.tools;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.utils.FormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * Transformer for Efluid Audit data update at merge
 * <p>
 * Example of simulated query
 * <pre>
 *     update TZONEIHM set ACTEURCREATION = 'evt 156444' , DATECREATION = 'current_date' , ACTEURMODIFICATION = 'current_date' ,
 *     DATEMODIFICATION = 'evt 154654' , ACTEURSUPPRESSION = 'evt 189445' , DATESUPPRESSION = 'current_date' where ID like 'TRA$%' and etatobjet = 0;
 * </pre>
 *
 * @author elecomte
 * @version 2
 * @since v1.2.0
 */
@Component
public class EfluidAuditDataTransformer extends Transformer<EfluidAuditDataTransformer.Config, EfluidAuditDataTransformer.Runner> {

    public static final String CURRENT_DATE_EXPR = "current_date";

    @Autowired
    public EfluidAuditDataTransformer(ManagedValueConverter converter, TransformerValueProvider provider) {
        super(converter, provider);
    }

    @Override
    public String getName() {
        return "EFLUID_AUDIT";
    }

    @Override
    protected Config newConfig() {
        return new Config();
    }

    @Override
    protected Runner runner(Config config, DictionaryEntry dict) {
        return new Runner(getValueProvider(), config, dict);
    }

    /**
     * Specification of configuration
     */
    public static class Config extends Transformer.TransformerConfig {

        private List<String> appliedKeyPatterns;

        private Map<String, String> appliedValueFilterPatterns;

        private Map<String, ApplicationSpec> dateUpdates;

        private Map<String, ApplicationSpec> actorUpdates;

        @JsonIgnore
        private List<Pattern> appliedKeyMatchers;

        @JsonIgnore
        private final Map<IndexAction, MatchersForAction> matchersByAction = new HashMap<>();

        public Config() {
            super();
        }

        public List<String> getAppliedKeyPatterns() {
            return this.appliedKeyPatterns;
        }

        public void setAppliedKeyPatterns(List<String> appliedKeyPatterns) {
            this.appliedKeyPatterns = appliedKeyPatterns;
        }

        public Map<String, String> getAppliedValueFilterPatterns() {
            return this.appliedValueFilterPatterns;
        }

        public void setAppliedValueFilterPatterns(Map<String, String> appliedValueFilterPatterns) {
            this.appliedValueFilterPatterns = appliedValueFilterPatterns;
        }

        public Map<String, ApplicationSpec> getDateUpdates() {
            return this.dateUpdates;
        }

        public void setDateUpdates(Map<String, ApplicationSpec> dateUpdates) {
            this.dateUpdates = dateUpdates;
        }

        public Map<String, ApplicationSpec> getActorUpdates() {
            return this.actorUpdates;
        }

        public void setActorUpdates(Map<String, ApplicationSpec> actorUpdates) {
            this.actorUpdates = actorUpdates;
        }

        @Override
        void populateDefault() {
            super.populateDefault();
            this.appliedKeyPatterns = new ArrayList<>();
            this.appliedKeyPatterns.add("TRA$.*");
            this.appliedValueFilterPatterns = new HashMap<>();
            this.appliedValueFilterPatterns.put("ETATOBJET", "0");
            this.dateUpdates = new HashMap<>();
            this.dateUpdates.put("DATECREATION", new ApplicationSpec("current_date", IndexAction.values()));
            this.dateUpdates.put("DATEMODIFICATION", new ApplicationSpec("current_date", IndexAction.values()));
            this.dateUpdates.put("DATESUPPRESSION", new ApplicationSpec("current_date", "DELETED", "1"));
            this.actorUpdates = new HashMap<>();
            this.actorUpdates.put("ACTEURCREATION", new ApplicationSpec("evt 156444", IndexAction.values()));
            this.actorUpdates.put("ACTEURMODIFICATION", new ApplicationSpec("evt 154654", IndexAction.values()));
            this.actorUpdates.put("ACTEURSUPPRESSION", new ApplicationSpec("evt 189445", "DELETED", "1"));
        }

        @Override
        void checkContentIsValid(List<String> errors) {
            super.checkContentIsValid(errors);
            if (this.appliedKeyPatterns == null || this.appliedKeyPatterns.size() == 0) {
                errors.add("At least one key value pattern must be specified. Use \".*\" as default to match all");
            }
            if (this.appliedValueFilterPatterns != null) {
                if (this.appliedValueFilterPatterns.keySet().stream().anyMatch(StringUtils::isEmpty)) {
                    errors.add("Value filter column name cannot be empty. Use \".*\" as default to match all, or remove all filter patterns");
                }
            }
            if ((this.dateUpdates == null || this.dateUpdates.size() == 0) && (this.actorUpdates == null || this.actorUpdates.size() == 0)) {
                errors.add("At least one update on date or actor must be specified.");
            }
            if (this.dateUpdates != null) {
                if (this.dateUpdates.keySet().stream().anyMatch(StringUtils::isEmpty)) {
                    errors.add("A date update column name cannot be empty. Use \".*\" as default to match all");
                }
                if (this.dateUpdates.values().stream().map(ApplicationSpec::getValue).anyMatch(StringUtils::isEmpty)) {
                    errors.add("A date update value cannot be empty. Use \"" + CURRENT_DATE_EXPR + "\" for current date or a fixed date using format \"" + FormatUtils.DATE_FORMAT + "\"");
                } else if (this.dateUpdates.values().stream().map(ApplicationSpec::getValue).anyMatch(v -> !CURRENT_DATE_EXPR.equals(v) && !FormatUtils.canParseLd(v))) {
                    errors.add("A date update value must be \"current_date\" or a fixed date value using format \"" + FormatUtils.DATE_FORMAT + "\"");
                }
                if (this.dateUpdates.values().stream().anyMatch(s -> (s.getOnValues() == null || s.getOnValues().isEmpty()) && (s.getOnActions() == null || s.getOnActions().isEmpty()))) {
                    errors.add("An date update must be specified with onValues or onActions. Specify at least one action (ADD/REMOVE/UPDATE) or one value spec or remove the date update spec");
                }
                if (this.dateUpdates.values().stream().filter(s -> s.getOnValues() != null).flatMap(s -> s.getOnValues().stream()).anyMatch(v -> StringUtils.isEmpty(v.getColumnPattern()) || StringUtils.isEmpty(v.getValuePattern()))) {
                    errors.add("The onValues properties columnPattern and valuePattern cannot be empty. Check dateUpdates");
                }
            }
            if (this.actorUpdates != null) {
                if (this.actorUpdates.keySet().stream().anyMatch(StringUtils::isEmpty)) {
                    errors.add("An actor update column name cannot be empty. Use \".*\" as default to match all");
                }
                if (this.actorUpdates.values().stream().map(ApplicationSpec::getValue).anyMatch(StringUtils::isEmpty)) {
                    errors.add("An actor update value cannot be empty. Specify a valid actor name value");
                }
                if (this.actorUpdates.values().stream().anyMatch(s -> (s.getOnValues() == null || s.getOnValues().isEmpty()) && (s.getOnActions() == null || s.getOnActions().isEmpty()))) {
                    errors.add("An actor update must be specified with onValues or onActions. Specify at least one action (ADD/REMOVE/UPDATE) or one value spec or remove the actor update spec");
                }
                if (this.actorUpdates.values().stream().filter(s -> s.getOnValues() != null).flatMap(s -> s.getOnValues().stream()).anyMatch(v -> StringUtils.isEmpty(v.getColumnPattern()) || StringUtils.isEmpty(v.getValuePattern()))) {
                    errors.add("The onValues properties columnPattern and valuePattern cannot be empty. Check actorUpdates");
                }
            }
        }

        boolean isEntryMatches(PreparedIndexEntry entry) {

            // Preload patterns for keys (as this)
            if (this.appliedKeyMatchers == null) {
                this.appliedKeyMatchers = this.appliedKeyPatterns != null
                        ? this.appliedKeyPatterns.stream().map(Pattern::compile).collect(toList())
                        : Collections.emptyList();
            }

            // Continue only if key at least match
            if (this.appliedKeyMatchers.size() > 0 && this.appliedKeyMatchers.stream().noneMatch(c -> c.matcher(entry.getKeyValue()).matches())) {
                return false;
            }

            MatchersForAction matchers = matchersFor(entry.getAction());

            // Preload patterns for filter values (get columns and compile)
            if (matchers.appliedValueColumnMatchers == null) {
                matchers.appliedValueColumnMatchers = this.appliedValueFilterPatterns != null
                        ? generatePayloadMatchersFromColumnPatterns(
                        Stream.concat(
                                this.appliedValueFilterPatterns.keySet().stream(),
                                Stream.concat(
                                        this.dateUpdates.values().stream()
                                                .filter(s -> s.getOnValues() != null)
                                                .flatMap(s -> s.getOnValues().stream())
                                                .map(o -> o.columnPattern),
                                        this.actorUpdates.values().stream()
                                                .filter(s -> s.getOnValues() != null)
                                                .flatMap(s -> s.getOnValues().stream())
                                                .map(o -> o.columnPattern)
                                )
                        ))
                        : Collections.emptyList();
            }

            // Continue only if value pattern may match (check that column at least exists)
            return matchers.appliedValueColumnMatchers.size() == 0
                    || matchers.appliedValueColumnMatchers.stream().anyMatch(c -> c.matcher(entry.getPayload()).matches());
        }

        boolean isValueFilterMatches(IndexAction action, List<Value> values) {

            MatchersForAction matchers = matchersFor(action);

            if (matchers.appliedValueFilterMatchers == null) {
                matchers.appliedValueFilterMatchers =
                        this.appliedValueFilterPatterns != null
                                ? this.appliedValueFilterPatterns.entrySet().stream()
                                .collect(groupingBy(
                                        e -> Pattern.compile(e.getKey()),
                                        mapping(e -> Pattern.compile(e.getValue()), toList())))
                                : new HashMap<>();


            }

            // Match all if no filter
            if (matchers.appliedValueFilterMatchers.size() == 0) {
                return true;
            }

            for (Value value : values) {
                String val = value.getValueAsString();

                if (matchers.appliedValueFilterMatchers.entrySet().stream()
                        .filter(e -> e.getKey().matcher(value.getName()).matches())
                        .flatMap(e -> e.getValue().stream())
                        .anyMatch(m -> m.matcher(val).matches())) {
                    return true;
                }
            }

            return false;
        }

        private MatchersForAction matchersFor(IndexAction action) {

            MatchersForAction matchers = this.matchersByAction.get(action);

            // Preload patterns for filter values (get columns and compile)
            if (matchers == null) {
                matchers = new MatchersForAction();
                this.matchersByAction.put(action, matchers);
            }

            return matchers;
        }

        /**
         * Definition of an application of value change for a column. Specify the update value and the IndexAction
         * when the update must be applied
         */
        public static class ApplicationSpec {
            private String value;
            private List<IndexAction> onActions;
            private List<OnValueMatch> onValues;

            public ApplicationSpec() {
                super();
            }

            public ApplicationSpec(String value, IndexAction... actions) {
                super();
                this.value = value;
                this.onActions = Arrays.asList(actions);
            }

            public ApplicationSpec(String value, String onValueName, String onValueValue) {
                super();
                this.value = value;
                this.onValues = Collections.singletonList(new OnValueMatch(onValueName, onValueValue));
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public List<IndexAction> getOnActions() {
                return onActions;
            }

            public void setOnActions(List<IndexAction> onActions) {
                this.onActions = onActions;
            }

            public List<OnValueMatch> getOnValues() {
                return onValues;
            }

            public void setOnValues(List<OnValueMatch> onValues) {
                this.onValues = onValues;
            }
        }

        public static class OnValueMatch {

            private String columnPattern;
            private String valuePattern;

            public OnValueMatch() {
            }

            public OnValueMatch(String columnPattern, String valuePattern) {
                this.columnPattern = columnPattern;
                this.valuePattern = valuePattern;
            }

            public String getColumnPattern() {
                return columnPattern;
            }

            public void setColumnPattern(String columnPattern) {
                this.columnPattern = columnPattern;
            }

            public String getValuePattern() {
                return valuePattern;
            }

            public void setValuePattern(String valuePattern) {
                this.valuePattern = valuePattern;
            }
        }

        private static class MatchersForAction {

            @JsonIgnore
            private List<Pattern> appliedValueColumnMatchers;

            @JsonIgnore
            private Map<Pattern, List<Pattern>> appliedValueFilterMatchers;

        }

    }

    /**
     * Transformation model for Efluid actor model. Can process values on many rules
     */
    public static class Runner extends Transformer.TransformerRunner<EfluidAuditDataTransformer.Config> {

        private final String currentTime;
        private final Map<IndexAction, SubstituteDefinition> mappedReplacedValues;
        private final List<String> selectedColumns;

        private Runner(TransformerValueProvider provider, Config config, DictionaryEntry dict) {
            super(provider, config, dict);
            this.currentTime = provider.getFormatedCurrentTime();
            this.mappedReplacedValues = prepareMappedReplacedValues(config);
            this.selectedColumns = provider.getDictionaryEntryColumns(dict);
        }

        @Override
        public boolean test(PreparedIndexEntry preparedIndexEntry) {
            return this.config.isEntryMatches(preparedIndexEntry);
        }

        @Override
        public boolean transform(IndexAction action, String key, List<Value> values) {
            if (this.config.isValueFilterMatches(action, values)) {

                Set<String> specifiedColumns = values.stream().map(Value::getName).collect(toSet());

                // Complete values for missing columns
                this.selectedColumns.stream()
                        .filter(c -> !specifiedColumns.contains(c))
                        .forEach(c -> values.add(new MissingValue(c)));

                var matchings = this.mappedReplacedValues.get(action).matchingSubstitutes(values);
                final AtomicBoolean updated = new AtomicBoolean(false);

                // Process on indexed list for replacement support
                for (int i = 0; i < values.size(); i++) {
                    Value val = values.get(i);
                    // Apply only 1 matching rule
                    final int finalI = i;
                    // Process change only for current action - all are init in mappedReplacedValues
                    matchings.stream()
                            .filter(e -> e.matches(val))
                            .findFirst()
                            .ifPresent(e -> {
                                values.set(finalI, e.transform(val));
                                updated.set(true);
                            });
                }

                return updated.get();
            }

            return false;
        }

        /**
         * Init content for a replacement process on specified columns
         *
         * @param config
         * @return
         */
        private Map<IndexAction, SubstituteDefinition> prepareMappedReplacedValues(Config config) {

            Map<IndexAction, SubstituteDefinition> byActions = new HashMap<>();

            for (IndexAction action : IndexAction.values()) {

                SubstituteDefinition allForAction = new SubstituteDefinition();

                if (config.getActorUpdates() != null) {
                    // Default value matcher => On action only
                    config.getActorUpdates().entrySet().stream()
                            .filter(s -> s.getValue().getOnActions() != null && !s.getValue().getOnActions().isEmpty() && s.getValue().getOnActions().contains(action))
                            .forEach(e -> allForAction.computeDefaultSpec(valueNamePredicate(e.getKey()), e.getValue().getValue(), ColumnType.STRING));

                    // Specified values matcher
                    config.getActorUpdates().entrySet().stream()
                            .filter(s -> s.getValue().getOnValues() != null && !s.getValue().getOnValues().isEmpty())
                            .forEach(e -> {
                                var predicate = valueNamePredicate(e.getKey());
                                var replacement = e.getValue().getValue();
                                allForAction.computeEachValuesSpec(e.getValue(), predicate, replacement, ColumnType.STRING);
                            });
                }

                if (config.getDateUpdates() != null) {
                    // Default value matcher => On action only
                    config.getDateUpdates().entrySet().stream()
                            .filter(s -> s.getValue().getOnActions() != null && !s.getValue().getOnActions().isEmpty() && s.getValue().getOnActions().contains(action))
                            .forEach(e -> allForAction.computeDefaultSpec(valueNamePredicate(e.getKey()), dateReplacement(e.getValue()), ColumnType.TEMPORAL));

                    // Specified values matcher
                    config.getDateUpdates().entrySet().stream()
                            .filter(s -> s.getValue().getOnValues() != null && !s.getValue().getOnValues().isEmpty())
                            .forEach(e -> {
                                var predicate = valueNamePredicate(e.getKey());
                                var replacement = dateReplacement(e.getValue());
                                allForAction.computeEachValuesSpec(e.getValue(), predicate, replacement, ColumnType.TEMPORAL);
                            });
                }

                byActions.put(action, allForAction);
            }

            return byActions;
        }

        private String dateReplacement(Config.ApplicationSpec spec) {
            return CURRENT_DATE_EXPR.equals(spec.getValue()) ?
                    this.currentTime : FormatUtils.format(FormatUtils.parseLd(spec.getValue()).atStartOfDay());
        }

        private static Predicate<Value> valueNamePredicate(String pattern) {
            final Pattern matcher = Pattern.compile(pattern);
            return v -> matcher.matcher(v.getName()).matches();
        }

    }

    /**
     * For columns identified in transformer rules but which could be missing in line we are transforming :
     * specify a "missing" referenced value which could also be transformer with current transformer.
     */
    private static class MissingValue implements Value {

        private final String name;

        private MissingValue(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public byte[] getValue() {
            return null;
        }

        @Override
        public ColumnType getType() {
            return null;
        }
    }

    /**
     * Prepared (complex) substitutes on value list / values
     */
    private static class SubstituteDefinition {

        private final Predicate<List<Value>> DEFAULT = l -> true;

        private final Map<Predicate<List<Value>>, Collection<Substitute>> allForAction;

        private SubstituteDefinition() {
            allForAction = new HashMap<>();
        }

        void computeEachValuesSpec(Config.ApplicationSpec spec, Predicate<Value> valuePredicate, String replacement, ColumnType type) {
            spec.getOnValues().forEach(m -> {
                var listPredicate = valueContentPredicate(m.getColumnPattern(), m.getValuePattern());
                Collection<Substitute> subs = this.allForAction.computeIfAbsent(listPredicate, k -> new HashSet<>());
                subs.add(new Substitute(valuePredicate, replacement, type));
            });
        }

        void computeDefaultSpec(Predicate<Value> valuePredicate, String replacement, ColumnType type) {
            Collection<Substitute> subs = this.allForAction.computeIfAbsent(DEFAULT, k -> new HashSet<>());
            subs.add(new Substitute(valuePredicate, replacement, type));
        }

        Collection<Substitute> matchingSubstitutes(List<Value> values) {
            return this.allForAction.entrySet().stream()
                    .filter(e -> e.getKey().test(values))
                    .flatMap(e -> e.getValue().stream())
                    .collect(Collectors.toList());
        }

        private static class Substitute {
            private final Predicate<Value> predicate;
            private final String value;
            private final ColumnType type;

            private Substitute(Predicate<Value> predicate, String value, ColumnType type) {
                this.predicate = predicate;
                this.value = value;
                this.type = type;
            }

            boolean matches(Value value) {
                return this.predicate.test(value);
            }

            Value transform(Value source) {
                return Transformer.TransformerRunner.transformedValue(source, this.value, this.type);
            }
        }

        private static Predicate<List<Value>> valueContentPredicate(String namePattern, String valuePattern) {
            final Pattern nameMatcher = Pattern.compile(namePattern);
            final Pattern valueMatcher = Pattern.compile(valuePattern);
            return values -> {
                // Basic matching for debug
                for (Value value : values) {
                    if (nameMatcher.matcher(value.getName()).matches()) {
                        if (valueMatcher.matcher(value.getValueAsString()).matches()) {
                            return true;
                        }
                    }
                }
                return false;
            };
        }
    }
}