package fr.uem.efluid.services.types;

import fr.uem.efluid.model.DiffLine;
import org.springframework.data.util.Pair;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Model of diff display for
 *
 * @author elecomte
 * @version 1
 * @since v3.1.11
 */
public class CommitContentCompareIndexEntry extends CommitCompareIndexEntry {

    private final Map<UUID, PreparedIndexEntry> contents;

    private CommitContentCompareIndexEntry(Map<UUID, PreparedIndexEntry> contents) {
        super(contents.keySet());
        this.contents = contents;
    }

    /**
     * <p>
     * For combining process : minimal rendering, with support of hr payload for rendering
     * </p>
     *
     * @param contents        identified variations of content
     * @param tableName       current table
     * @param referenceCommit selected reference commit for rendering
     * @return Content compare entity
     */
    public static CommitContentCompareIndexEntry fromContents(Map<UUID, Pair<DiffLine, String>> contents, String tableName, UUID referenceCommit) {

        Map<UUID, PreparedIndexEntry> prepared = contents.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> PreparedIndexEntry.fromCombined(e.getValue().getFirst(), e.getValue().getSecond(), tableName)));

        CommitContentCompareIndexEntry data = new CommitContentCompareIndexEntry(prepared);

        PreparedIndexEntry reference = prepared.get(referenceCommit);

        // Init from identified reference item
        data.setAction(reference.getAction());
        data.setDictionaryEntryUuid(reference.getDictionaryEntryUuid());
        data.setTableName(tableName);
        data.setKeyValue(reference.getKeyValue());
        data.setHrPayload(reference.getHrPayload());
        data.setTimestamp(reference.getTimestamp());

        return data;
    }

    @Override
    public PreparedIndexEntry getContentByCommit(UUID commitUuid) {
        return this.contents.get(commitUuid);
    }
}
