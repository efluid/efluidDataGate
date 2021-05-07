package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.Commit;
import fr.uem.efluid.model.entities.IndexEntry;

import java.time.LocalDateTime;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class IndexEntryPackage extends SharedPackage<IndexEntry> {

    /**
     * @param name
     * @param exportDate
     */
    public IndexEntryPackage(String name, LocalDateTime exportDate) {
        super(name, exportDate);
    }

    /**
     * @return
     */
    @Override
    public String getVersion() {
        return "1";
    }

    /**
     * @return
     */
    @Override
    protected IndexEntry initContent() {
        return new IndexEntry();
    }

}
