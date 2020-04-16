package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.Export;

import java.util.UUID;

/**
 * Rendering of a prepared export, ready to download (will download as <tt>name</tt> from given <tt>uuid</tt>)
 *
 * @author elecomte
 * @version 1
 * @since v1.1.0
 */
public class CommitExportDisplay {

    private final UUID uuid;

    private final String filename;

    public CommitExportDisplay(Export export) {
        this.uuid = export.getUuid();
        this.filename = export.getFilename();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getFilename() {
        return filename;
    }
}
