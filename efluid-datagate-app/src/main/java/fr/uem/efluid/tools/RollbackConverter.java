package fr.uem.efluid.tools;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.types.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A convenient converter from a DiffLine to the corresponding rollback DiffLine with all required rules
 *
 * @author elecomte
 * @version 1
 * @since v2.0.17
 */
@Component
public class RollbackConverter {

    @Autowired
    private ManagedValueConverter valueConverter;

    public DiffLine toRollbackLine(DiffLine source) {

        long timestamp = System.currentTimeMillis();

        // Rollback on delete => became an add
        if (source.getAction() == IndexAction.REMOVE) {
            return DiffLine.combined(
                    source.getDictionaryEntryUuid(),
                    source.getKeyValue(),
                    source.getPrevious(),
                    null,
                    IndexAction.ADD,
                    timestamp);
        }

        // Rollback on add => became an delete
        if (source.getAction() == IndexAction.ADD) {
            return DiffLine.combined(
                    source.getDictionaryEntryUuid(),
                    source.getKeyValue(),
                    null,
                    source.getPayload(),
                    IndexAction.REMOVE,
                    timestamp);
        }

        // Other case are update current => previous
        return DiffLine.combined(
                source.getDictionaryEntryUuid(),
                source.getKeyValue(),
                buildRollbackUpdatePayload(source.getPayload(), source.getPrevious()),
                source.getPayload(),
                IndexAction.UPDATE,
                timestamp);
    }

    private String buildRollbackUpdatePayload(String sourcePayload, String sourcePrevious) {
        List<Value> payload = this.valueConverter.expandInternalValue(sourcePayload);
        Map<String, Value> previous = this.valueConverter.expandInternalValue(sourcePrevious).stream().collect(Collectors.toMap(v -> v.getName(), v -> v));

        // Rebuild "null value" new compliant payload for update rollback
        return this.valueConverter.convertToExtractedValue(
                /*
                 * Two possibilities :
                 *  - Was present - keep previous as this
                 *  - Was not present - revert as a null set
                 */
                payload.stream()
                        .map(v -> Objects.requireNonNullElseGet(
                                previous.get(v.getName()),
                                () -> new NullValue(v.getName(), v.getType())))
                        .collect(Collectors.toList()));
    }

    private static class NullValue implements Value {

        private final String name;
        private final ColumnType type;

        private NullValue(String name, ColumnType type) {
            this.name = name;
            this.type = type;
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
            return this.type;
        }
    }
}
