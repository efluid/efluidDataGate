package fr.uem.efluid.transformers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.tools.diff.ManagedValueConverter;
import fr.uem.efluid.utils.ApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.uem.efluid.utils.ErrorType.*;

/**
 * Transformer for region-scoped select of values at import
 *
 * @author elecomte
 * @version 1
 * @since v2.1.16
 */
@Component
public class EfluidRegionDataTransformer extends Transformer<EfluidRegionDataTransformer.Config, EfluidRegionDataTransformer.Runner> {

    @Autowired
    public EfluidRegionDataTransformer(ManagedValueConverter converter, TransformerValueProvider provider) {
        super(converter, provider);
    }

    @Override
    public String getName() {
        return "EFLUID_REGION_SUPPORT";
    }

    @Override
    protected EfluidRegionDataTransformer.Config newConfig() {
        return new EfluidRegionDataTransformer.Config();
    }

    @Override
    protected EfluidRegionDataTransformer.Runner runner(EfluidRegionDataTransformer.Config config, DictionaryEntry dict) {
        return new EfluidRegionDataTransformer.Runner(getValueProvider(), config, dict);
    }

    public static class Config extends Transformer.TransformerConfig {

        private static final String CSV_SEPARATOR = ";";

        private static final String[] SOURCE_COLS = {"DIR", "TABNAME", "OP", "COLS_PK"
                , "SRC_ID1", "SRC_ID2", "SRC_ID3", "SRC_ID4", "SRC_ID5"};

        private static final String SOURCE_TABLE = "TRECOPIEPARAMREFERENTIELDIR";

        private static final String SOURCE_QUERY =
                "SELECT " + String.join(", ", SOURCE_COLS) + " FROM " + SOURCE_TABLE;

        private static final String COUNT_QUERY = "SELECT COUNT(1) FROM " + SOURCE_TABLE;

        private static final String GET_REGION_QUERY =
                "SELECT SITE FROM TAPPLICATIONINFO WHERE PROJET = ?";

        private String project;

        @JsonIgnore
        private transient Set<String> sourcedTables = new HashSet<>();

        @JsonIgnore
        private transient Map<String, List<RegionParameter>> parameters = new HashMap<>();

        public String getProject() {
            return this.project;
        }

        public void setProject(String project) {
            this.project = project;
        }

        Map<String, List<RegionParameter>> getParameters() {
            return this.parameters;
        }

        @Override
        void populateDefault() {
            super.populateDefault();
            this.project = "project";
        }

        @Override
        void checkContentIsValid(List<String> errors) {
            super.checkContentIsValid(errors);
            if (!StringUtils.hasText(this.project)) {
                errors.add("project cannot be empty or missing");
            }
        }

        @Override
        boolean isTableNameMatches(DictionaryEntry dict) {
            return this.sourcedTables.contains(dict.getTableName()) && super.isTableNameMatches(dict);
        }

        @Override
        public boolean isAttachmentPackageSupport() {
            return true;
        }

        /**
         * Load the source table as a CSV file and keep it in-memory (requires to load the whole table ...)
         *
         * @param managedSource for managed database attachment loading
         * @return loaded content of table TRECOPIEPARAMREFERENTIELDIR in CSV format
         */
        @Override
        public byte[] exportAttachmentPackageData(JdbcTemplate managedSource) {

            // TODO : do not load in memory but prepare an export source for low memory use at export

            StringBuilder csvInMemory = new StringBuilder();

            // Header
            csvInMemory.append(String.join(CSV_SEPARATOR, SOURCE_COLS)).append("\n");

            // Content
            try {
                managedSource.query(SOURCE_QUERY, (rs) -> {
                    appendRowToCsv(rs, csvInMemory);
                });
            } catch (Exception e) {
                throw new ApplicationException(TRANSFORMER_EFUID_NO_REGION_SOURCE, "cannot get region parameter source in table \""
                        + SOURCE_TABLE + "\" : check managed database", e);
            }

            // Keep data of CSV
            return csvInMemory.toString().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public void importAttachmentPackageData(byte[] attachmentPackageData, JdbcTemplate managedSource, ManagedValueConverter valueConverter) {
            String region = managedSource.query(GET_REGION_QUERY, (rs) -> {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            }, this.project);

            if (region == null) {
                throw new ApplicationException(TRANSFORMER_EFUID_NO_SITE, "no site found with query \""
                        + GET_REGION_QUERY + "\" on specified project " + this.project);
            }

            boolean header = true;
            try (InputStream is = new ByteArrayInputStream(attachmentPackageData)) {
                try (Scanner s = new Scanner(is)) {

                    // Parse CSV data
                    while (s.hasNextLine()) {
                        if (!header) {
                            RegionParameter param = new RegionParameter(valueConverter, s.nextLine().split(CSV_SEPARATOR));

                            // Only matching region
                            if (region.equals(param.getRegion())) {
                                this.parameters
                                        .computeIfAbsent(param.getTable(), k -> new ArrayList<>())
                                        .add(param);
                            }

                            // Track all sources tables
                            this.sourcedTables.add(param.getTable());
                        } else {
                            header = false;
                        }
                    }
                }
            } catch (IOException e) {
                throw new ApplicationException(TRANSFORMER_EFUID_WRONG_REGION_DATA, "cannot read attachment CSV" +
                        " with region config on specified project " + this.project, e);
            }
        }

        @Override
        public String getAttachmentPackageComment(JdbcTemplate managedSource, ManagedValueConverter valueConverter) {
            Long count = managedSource.query(COUNT_QUERY, (rs) -> {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return null;
            });

            return count + " lignes de " + SOURCE_TABLE + " seront prises en compte dans le " +
                    "lot pour la regionalisation";
        }

        private static void appendRowToCsv(ResultSet row, StringBuilder builder) throws SQLException {

            for (int i = 1; i < SOURCE_COLS.length; i++) {
                builder.append(row.getString(i)).append(';');
            }

            // Last with new line for easier use
            builder.append(row.getString(SOURCE_COLS.length)).append('\n');
        }

    }

    public static class Runner extends Transformer.TransformerRunner<EfluidRegionDataTransformer.Config> {

        private final Set<String> matchingKeys;

        public Runner(TransformerValueProvider provider, Config config, DictionaryEntry dict) {
            super(provider, config, dict);
            List<RegionParameter> params = config.getParameters().get(dict.getTableName());
            this.matchingKeys = params != null
                    ? params.stream()
                    .map(RegionParameter::getKey)
                    .collect(Collectors.toSet())
                    : null;
        }

        @Override
        public boolean transform(IndexAction action, String key, List<Value> values) {

            // Already filtered from key, apply change immediately
            values.clear();

            return true;
        }

        @Override
        public boolean test(PreparedIndexEntry preparedIndexEntry) {
            return this.matchingKeys == null || !this.matchingKeys.contains(preparedIndexEntry.getKeyValue());
        }
    }

    private static class RegionParameter {

        private final String region;
        private final String table;
        private final String key;
        // "DIR", "TABNAME", "OP", "COLS_PK", "SRC_ID1", "SRC_ID2", "SRC_ID3", "SRC_ID4", "SRC_ID5"

        RegionParameter(ManagedValueConverter valueConverter, String[] row) {
            this.region = row[0].trim();
            this.table = row[1].trim();
            // OP is not used for now
            //  + SOURCE + DEST
            long pkCount = Stream.of(row[3].trim().split("\\+ "))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .count();
            StringBuilder keyBuild = new StringBuilder();
            for (int i = 0; i < pkCount; i++) {
                valueConverter.appendExtractedKeyValue(keyBuild, row[i + 4].trim(), i > 1);
            }
            this.key = keyBuild.toString();
        }

        public String getRegion() {
            return this.region;
        }

        public String getTable() {
            return this.table;
        }

        public String getKey() {
            return this.key;
        }
    }
}
