package fr.uem.efluid.tools;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.utils.FormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
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
            this.dateUpdates.put("DATESUPPRESSION", new ApplicationSpec("current_date", IndexAction.values()));
            this.actorUpdates = new HashMap<>();
            this.actorUpdates.put("ACTEURCREATION", new ApplicationSpec("evt 156444", IndexAction.values()));
            this.actorUpdates.put("ACTEURMODIFICATION", new ApplicationSpec("evt 154654", IndexAction.values()));
            this.actorUpdates.put("ACTEURSUPPRESSION", new ApplicationSpec("evt 189445", IndexAction.values()));
        }

        @Override
        void checkContentIsValid(List<String> errors) {
            super.checkContentIsValid(errors);
            if (this.appliedKeyPatterns == null || this.appliedKeyPatterns.size() == 0) {
                errors.add("At least one key value pattern must be specified. Use \".*\" as default to match all");
            }
            if (this.dateUpdates.values().stream().map(ApplicationSpec::getOnActions).anyMatch(a -> a == null || a.size() == 0)) {
                errors.add("An date update onAction cannot be empty. Specify at least one action or remove the date update spec");
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
                if (this.dateUpdates.values().stream().map(ApplicationSpec::getOnActions).anyMatch(a -> a == null || a.size() == 0)) {
                    errors.add("An date update onAction cannot be empty. Specify at least one action (ADD/REMOVE/UPDATE) or remove the date update spec");
                }
            }
            if (this.appliedValueFilterPatterns != null) {
                if (this.appliedValueFilterPatterns.keySet().stream().anyMatch(StringUtils::isEmpty)) {
                    errors.add("Value filter column name cannot be empty. Use \".*\" as default to match all, or remove all filter patterns");
                }
            }
            if (this.actorUpdates != null) {
                if (this.actorUpdates.keySet().stream().anyMatch(StringUtils::isEmpty)) {
                    errors.add("An actor update column name cannot be empty. Use \".*\" as default to match all");
                }
                if (this.actorUpdates.values().stream().map(ApplicationSpec::getValue).anyMatch(StringUtils::isEmpty)) {
                    errors.add("An actor update value cannot be empty. Specify a valid actor name value");
                }
                if (this.actorUpdates.values().stream().map(ApplicationSpec::getOnActions).anyMatch(a -> a == null || a.size() == 0)) {
                    errors.add("An actor update onAction cannot be empty. Specify at least one action (ADD/REMOVE/UPDATE) or remove the actor update spec");
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
                        ? generatePayloadMatchersFromColumnPatterns(this.appliedValueFilterPatterns.keySet().stream())
                        : Collections.emptyList();
            }

            // Continue only if value pattern may match (check that column at least exists)
            if (matchers.appliedValueColumnMatchers.size() > 0 && matchers.appliedValueColumnMatchers.stream().noneMatch(c -> c.matcher(entry.getPayload()).matches())) {
                return false;
            }


            // Preload patterns for date and actor updates (get columns and compile)
            if (matchers.appliedUpdateColumnMatchers == null) {
                matchers.appliedUpdateColumnMatchers = generatePayloadMatchersFromColumnPatterns(
                        Stream.concat(
                                this.dateUpdates != null ? this.dateUpdates.entrySet().stream()
                                        .filter(v -> v.getValue().getOnActions().contains(entry.getAction()))
                                        .map(Map.Entry::getKey) : Stream.empty(),
                                this.actorUpdates != null ? this.actorUpdates.entrySet().stream()
                                        .filter(v -> v.getValue().getOnActions().contains(entry.getAction()))
                                        .map(Map.Entry::getKey) : Stream.empty()
                        ));
            }

            return matchers.appliedUpdateColumnMatchers.stream().anyMatch(c -> c.matcher(entry.getPayload()).matches());
        }

        boolean isValueFilterMatches(IndexAction action, List<Value> values) {

            MatchersForAction matchers = matchersFor(action);

            if (matchers.appliedValueFilterMatchers == null) {
                matchers.appliedValueFilterMatchers =
                        this.appliedValueFilterPatterns != null
                                ? this.appliedValueFilterPatterns.entrySet().stream()
                                .collect(groupingBy(
                                        Map.Entry::getKey,
                                        mapping(e -> Pattern.compile(e.getValue()), toList())))
                                : new HashMap<>();
            }

            // Match all if no filter
            if (matchers.appliedValueFilterMatchers.size() == 0) {
                return true;
            }

            return values.stream()
                    .filter(v -> matchers.appliedValueFilterMatchers.containsKey(v.getName()))
                    .anyMatch(v -> {
                        String val = v.getValueAsString();
                        return matchers.appliedValueFilterMatchers.get(v.getName()).stream().anyMatch(c -> c.matcher(val).matches());
                    });
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

            public ApplicationSpec() {
                super();
            }

            public ApplicationSpec(String value, IndexAction... actions) {
                super();
                this.value = value;
                this.onActions = Arrays.asList(actions);
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
        }

        private static class MatchersForAction {

            @JsonIgnore
            private List<Pattern> appliedValueColumnMatchers;

            @JsonIgnore
            private List<Pattern> appliedUpdateColumnMatchers;

            @JsonIgnore
            private Map<String, List<Pattern>> appliedValueFilterMatchers;

        }
    }

    /**
     * Transformation model for
     */
    public static class Runner extends Transformer.TransformerRunner<EfluidAuditDataTransformer.Config> {

        private final Map<Pattern, String> mappedReplacedValues;

        private Runner(TransformerValueProvider provider, Config config, DictionaryEntry dict) {
            super(provider, config, dict);
            this.mappedReplacedValues = prepareMappedReplacedValues(config);
        }

        @Override
        public boolean test(PreparedIndexEntry preparedIndexEntry) {
            return this.config.isEntryMatches(preparedIndexEntry);
        }

        @Override
        public void accept(IndexAction action, List<Value> values) {
            if (this.config.isValueFilterMatches(action, values)) {
                // Process on indexed list for replacement support
                for (int i = 0; i < values.size(); i++) {
                    Value val = values.get(i);
                    // Apply only 1 matching rule
                    final int finalI = i;
                    this.mappedReplacedValues.entrySet().stream()
                            .filter(e -> e.getKey().matcher(val.getName()).matches())
                            .findFirst()
                            .ifPresent(e -> values.set(finalI, transformedValue(val, e.getValue())));
                }
            }
        }

        /**
         * Init content for a replacement process on specified columns
         *
         * @param config
         * @return
         */
        private Map<Pattern, String> prepareMappedReplacedValues(Config config) {

            String currentTime = this.provider.getFormatedCurrentTime();

            Map<Pattern, String> replacements = config.getActorUpdates() != null
                    ? new HashMap<>(config.getActorUpdates().entrySet().stream().collect(
                    Collectors.toMap(e -> Pattern.compile(e.getKey()), e -> e.getValue().getValue())))
                    : new HashMap<>();

            if (config.getDateUpdates() != null) {
                config.getDateUpdates().forEach((k, v) -> {
                    if (CURRENT_DATE_EXPR.equals(v)) {
                        replacements.put(Pattern.compile(k), currentTime);
                    } else {
                        replacements.put(Pattern.compile(k), FormatUtils.format(FormatUtils.parseLd(v.getValue()).atStartOfDay()));
                    }
                });
            }

            return replacements;
        }
    }
}
