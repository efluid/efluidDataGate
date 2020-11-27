package fr.uem.efluid.services.types;

import java.lang.ref.Cleaner;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.uem.efluid.model.Shared;

/**
 * Stream compliant package definition for export or import of every <tt>Shared</tt> content
 *
 * @param <T>
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
public abstract class SharedPackage<T extends Shared> implements Cleaner.Cleanable {

    private final String name;

    private final LocalDateTime exportDate;

    private final AtomicInteger processed = new AtomicInteger();

    private Stream<T> contents;

    private Path uncompressPath;

    /**
     * @param name
     * @param exportDate
     */
    protected SharedPackage(String name, LocalDateTime exportDate) {
        super();
        this.name = name;
        this.exportDate = exportDate;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the exportDate
     */
    public LocalDateTime getExportDate() {
        return this.exportDate;
    }

    /**
     * Init content when creating a new package. Simple inline setter
     *
     * @param lcontents
     * @return
     */
    @SuppressWarnings("unchecked")
    public <K extends SharedPackage<T>> K from(Stream<T> lcontents) {
        this.contents = lcontents;
        this.processed.set(0);
        return (K) this;
    }

    /**
     * @return the content
     */
    public Stream<T> content() {
        return this.contents;
    }

    /**
     * <p>
     * Available only once content is initialized (directly or with deserialize)
     * </p>
     *
     * @return
     */
    public int getProcessedSize() {
        return this.processed.get();
    }

    /**
     * <p>
     * If the export has complementary file, provides them here
     * </p>
     *
     * @return
     */
    public List<Path> getComplementaryFiles() {
        return new ArrayList<>();
    }

    /**
     * @param uncompressPath the uncompressPath to set
     */
    public void setUncompressPath(Path uncompressPath) {
        this.uncompressPath = uncompressPath;
    }

    /**
     * For compatibility validation on version. Default validate that imported version
     * equals package version, but it can be overriden for some packages
     *
     * @param importVersion
     * @return
     */
    public boolean isCompatible(String importVersion) {
        return getVersion().equals(importVersion);
    }

    /**
     * @param contentRaw
     */
    public void deserialize(Stream<String> contentRaw) {
        this.contents = contentRaw
                .peek(i -> this.processed.incrementAndGet())
                .map(this::deserializeOne);
    }

    /**
     * @return
     */
    public Stream<String> serialize() {
        return this.contents
                .peek(i -> this.processed.incrementAndGet())
                .map(this::serializeOne);
    }

    /**
     * @return
     */
    public String getReloadableTypeName() {
        return this.getClass().getName();
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[\"" + this.getName() + "\"|" + this.getExportDate() + "|v\"" + this.getVersion()
                + "\"]";
    }

    /**
     * @return
     */
    public abstract String getVersion();

    /**
     * <p>
     * Extension point for package - item deserialize
     * </p>
     *
     * @param rawContent
     * @return
     */
    public T deserializeOne(String rawContent) {
        T content = initContent();
        content.deserialize(rawContent);
        return content;
    }

    /**
     * <p>
     * Extension point for package - item serialize
     * </p>
     *
     * @param content
     * @return
     */
    protected String serializeOne(T content) {
        return content.serialize();
    }

    /**
     * @return the uncompressPath
     */
    protected Path getUncompressPath() {
        return this.uncompressPath;
    }

    /**
     * @return
     */
    protected abstract T initContent();

    public void clean() {
        if (this.contents != null) {
            this.contents.close();
        }
    }
}