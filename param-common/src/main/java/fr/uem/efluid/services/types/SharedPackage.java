package fr.uem.efluid.services.types;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.uem.efluid.model.Shared;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 * @param <T>
 */
public abstract class SharedPackage<T extends Shared> {

	private final String name;

	private final LocalDateTime exportDate;

	private List<T> contents;

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
	public <K extends SharedPackage<T>> K initWithContent(List<T> lcontents) {
		this.contents = lcontents;
		return (K) this;
	}

	/**
	 * @return the content
	 */
	public Stream<T> streamContent() {
		return this.contents.stream();
	}

	/**
	 * @return the content
	 */
	public List<T> getContent() {
		return this.contents;
	}

	/**
	 * <p>
	 * Available only once content is initialized (directly or with deserialize)
	 * </p>
	 * 
	 * @return
	 */
	public int getContentSize() {
		return this.contents != null ? this.contents.size() : 0;
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
	 * @param uncompressPath
	 *            the uncompressPath to set
	 */
	public void setUncompressPath(Path uncompressPath) {
		this.uncompressPath = uncompressPath;
	}

	/**
	 * @param contentRaw
	 */
	public void deserialize(List<String> contentRaw) {
		this.contents = contentRaw.stream().map(this::deserializeOne).collect(Collectors.toList());
	}

	/**
	 * @return
	 */
	public List<String> serialize() {
		return this.contents.stream().map(this::serializeOne).collect(Collectors.toList());
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
	protected T deserializeOne(String rawContent) {
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

}