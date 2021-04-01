package fr.uem.efluid.tools;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EfluidRegionDataTransformer {

    public static class Config extends Transformer.TransformerConfig {

        private static final String[] SOURCE_COLS = {"DIR", "TABNAME", "OP", "COLS_PK"
                , "SRC_ID1", "SRC_ID2", "SRC_ID3", "SRC_ID4", "SRC_ID5"};

        private static final String SOURCE_QUERY =
                "SELECT " + String.join(", ", SOURCE_COLS) + " FROM TRECOPIEPARAMREFERENTIELDIR";

        private String project;

        /**
         * Load the source table as a CSV file and keep it in-memory (requires to load the whole table ...)
         *
         * @param managedSource for managed database attachment loading
         */
        @Override
        public void loadAttachmentPackageData(JdbcTemplate managedSource) {

            // TODO : do not load in memory but prepare an export source for low memory use at export

            StringBuilder csvInMemory = new StringBuilder();

            // Header
            csvInMemory.append(String.join(";", SOURCE_COLS)).append("\n");

            // Content
            managedSource.query(SOURCE_QUERY, (rs) -> {
                appendRowToCsv(rs, csvInMemory);
            });

            // Keep data of CSV
            setAttachmentPackageData(csvInMemory.toString().getBytes(StandardCharsets.UTF_8));
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

        public Runner(TransformerValueProvider provider, Config config, DictionaryEntry dict) {
            super(provider, config, dict);
        }

        @Override
        public void accept(IndexAction action, List<Value> values) {

        }

        @Override
        public boolean test(PreparedIndexEntry preparedIndexEntry) {
            return false;
        }
    }
}
