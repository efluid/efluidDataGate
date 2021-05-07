package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.entities.Commit;

/**
 * <p>Export / import definition of commit.</p>
 * <p>
 * A major change was added between version 2 and 3, but compatibility was kept : the index was included "inside" serialized Commit
 * data in export, and, and starting version 3, is now managed as an independent package. But it's still possible to parse
 * the "old" model as it has limited impact on how the data can be processed, only the way it's gathered is modified.
 * </p>
 *
 * @author elecomte
 * @version 3
 * @since v0.0.1
 */
public class CommitPackage extends SharedPackage<Commit> {

    private static final String CURRENT_VERSION = "3";
    private static final String COMPLIANT_VERSION = "2";

    private boolean compatibilityMode = false;

    /**
     * @param name
     * @param exportDate
     */
    public CommitPackage(String name, LocalDateTime exportDate) {
        super(name, exportDate);
    }

    @Override
    public boolean isCompatible(String importVersion) {

        // Compliance with old version but requires specific flag in commit deserialization process
        if (COMPLIANT_VERSION.equals(importVersion)) {
            this.compatibilityMode = true;
            return true;
        }

        // Default rule for other versions
        return super.isCompatible(importVersion);
    }

    /**
     * @return
     */
    @Override
    public String getVersion() {
        return CURRENT_VERSION;
    }

    /**
     * @return
     */
    @Override
    protected Commit initContent() {
        return new Commit(this.compatibilityMode);
    }

    public boolean isCompatibilityMode() {
        return this.compatibilityMode;
    }
}
