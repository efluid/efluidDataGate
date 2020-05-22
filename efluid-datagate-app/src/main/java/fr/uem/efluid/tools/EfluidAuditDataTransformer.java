package fr.uem.efluid.tools;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.utils.FormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * @version 1
 * @since v1.2.0
 */
@Component
public class EfluidAuditDataTransformer extends Transformer<EfluidAuditDataTransformer.Config, EfluidAuditDataTransformer.Runner> {

    public static final String CURRENT_DATE_EXPR = "current_date";

    @Autowired
    public EfluidAuditDataTransformer(ManagedValueConverter converter) {
        super(converter);
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
        return new Runner(config, dict);
    }

    public static class Config extends Transformer.TransformerConfig {

        private List<String> appliedKeyPatterns;

        private Map<String, String> appliedValueFilterPatterns;

        private Map<String, String> dateUpdates;

        private Map<String, String> actorUpdates;

        @JsonIgnore
        private List<Pattern> appliedKeyMatchers;

        @JsonIgnore
        private List<Pattern> appliedValueColumnMatchers;

        @JsonIgnore
        private List<Pattern> appliedUpdateColumnMatchers;

        @JsonIgnore
        private Map<String, List<Pattern>> appliedValueFilterMatchers;

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

        public Map<String, String> getDateUpdates() {
            return this.dateUpdates;
        }

        public void setDateUpdates(Map<String, String> dateUpdates) {
            this.dateUpdates = dateUpdates;
        }

        public Map<String, String> getActorUpdates() {
            return this.actorUpdates;
        }

        public void setActorUpdates(Map<String, String> actorUpdates) {
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
            this.dateUpdates.put("DATECREATION", "current_date");
            this.dateUpdates.put("DATEMODIFICATION", "current_date");
            this.dateUpdates.put("DATESUPPRESSION", "current_date");
            this.actorUpdates = new HashMap<>();
            this.actorUpdates.put("ACTEURCREATION", "evt 156444");
            this.actorUpdates.put("ACTEURMODIFICATION", "evt 154654");
            this.actorUpdates.put("ACTEURSUPPRESSION", "evt 189445");
        }

        @Override
        void checkContentIsValid(List<String> errors) {
            super.checkContentIsValid(errors);
            if (this.appliedKeyPatterns == null || this.appliedKeyPatterns.size() == 0) {
                errors.add("At least one key value pattern must be specified. Use \".*\" as default to match all");
            }
            if ((this.dateUpdates == null || this.dateUpdates.size() == 0) && (this.actorUpdates == null || this.actorUpdates.size() == 0)) {
                errors.add("At least one update on date or actor must be specified.");
            }
            if (this.dateUpdates != null) {
                if (this.dateUpdates.keySet().stream().anyMatch(StringUtils::isEmpty)) {
                    errors.add("A date update column name cannot be empty. Use \".*\" as default to match all");
                }
                if (this.dateUpdates.values().stream().anyMatch(StringUtils::isEmpty)) {
                    errors.add("A date update value cannot be empty. Use \"" + CURRENT_DATE_EXPR + "\" for current date or a fixed date using format \"" + FormatUtils.DATE_FORMAT + "\"");
                } else if (this.dateUpdates.values().stream().anyMatch(v -> !CURRENT_DATE_EXPR.equals(v) && !FormatUtils.canParseLd(v))) {
                    errors.add("A date update value must be \"current_date\" or a fixed date value using format \"" + FormatUtils.DATE_FORMAT + "\"");
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
                if (this.actorUpdates.values().stream().anyMatch(StringUtils::isEmpty)) {
                    errors.add("An actor update value cannot be empty. Specify a valid actor name value");
                }
            }
        }

        boolean isEntryMatches(PreparedIndexEntry entry) {

            // Preload patterns for keys (as this)
            if (this.appliedKeyMatchers == null) {
                this.appliedKeyMatchers = this.appliedKeyPatterns.stream()
                        .map(Pattern::compile)
                        .collect(toList());
            }

            // Continue only if key at least match
            if (this.appliedKeyMatchers.size() > 0 && this.appliedKeyMatchers.stream().noneMatch(c -> c.matcher(entry.getKeyValue()).matches())) {
                return false;
            }

            // Preload patterns for filter values (get columns and compile)
            if (this.appliedValueColumnMatchers == null) {
                this.appliedValueColumnMatchers = generatePayloadMatchersFromColumnPatterns(this.appliedValueFilterPatterns.keySet().stream());
            }

            // Continue only if value pattern may match (check that column at least exists)
            if (this.appliedValueColumnMatchers.size() > 0 && this.appliedValueColumnMatchers.stream().noneMatch(c -> c.matcher(entry.getPayload()).matches())) {
                return false;
            }


            // Preload patterns for date and actor updates (get columns and compile)
            if (this.appliedUpdateColumnMatchers == null) {
                this.appliedUpdateColumnMatchers = generatePayloadMatchersFromColumnPatterns(
                        Stream.concat(this.dateUpdates.keySet().stream(), this.actorUpdates.keySet().stream()));
            }

            return this.appliedUpdateColumnMatchers.stream().anyMatch(c -> c.matcher(entry.getPayload()).matches());
        }

        boolean isValueFilterMatches(List<Value> values) {
            if (this.appliedValueFilterMatchers == null) {
                this.appliedValueFilterMatchers = this.appliedValueFilterPatterns.entrySet().stream()
                        .collect(groupingBy(
                                Map.Entry::getKey,
                                mapping(e -> Pattern.compile(e.getValue()), toList())));
            }

            // Match all if no filter
            if (this.appliedValueFilterMatchers.size() == 0) {
                return true;
            }

            return values.stream()
                    .filter(v -> this.appliedValueFilterMatchers.containsKey(v.getName()))
                    .anyMatch(v -> {
                        String val = v.getValueAsString();
                        return this.appliedValueFilterMatchers.get(v.getName()).stream().anyMatch(c -> c.matcher(val).matches());
                    });
        }
    }

    /**
     * Transformation model for
     */
    public static class Runner extends Transformer.TransformerRunner<EfluidAuditDataTransformer.Config> {

        private final Map<Pattern, String> mappedReplacedValues;

        private Runner(Config config, DictionaryEntry dict) {
            super(config, dict);
            this.mappedReplacedValues = prepareMappedReplacedValues(config);
        }

        @Override
        public boolean test(PreparedIndexEntry preparedIndexEntry) {
            return this.config.isEntryMatches(preparedIndexEntry);
        }

        @Override
        public void accept(List<Value> values) {
            if (this.config.isValueFilterMatches(values)) {
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

            String currentTime = FormatUtils.format(LocalDateTime.now());

            Map<Pattern, String> replacements = new HashMap<>(config.getActorUpdates().entrySet().stream().collect(
                    Collectors.toMap(e -> Pattern.compile(e.getKey()), Map.Entry::getValue)));

            config.getDateUpdates().forEach((k, v) -> {
                if (CURRENT_DATE_EXPR.equals(v)) {
                    replacements.put(Pattern.compile(k), currentTime);
                } else {
                    replacements.put(Pattern.compile(k), FormatUtils.format(FormatUtils.parseLd(v).atStartOfDay()));
                }
            });

            return replacements;
        }
    }
}
