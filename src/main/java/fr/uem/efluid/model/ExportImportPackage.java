package fr.uem.efluid.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public abstract class ExportImportPackage<T extends Shared> {

	private final String name;
	private final LocalDateTime exportDate;

	private List<T> contents;

	/**
	 * @param name
	 * @param exportDate
	 */
	public ExportImportPackage(String name, LocalDateTime exportDate) {
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
	 * @return the content
	 */
	public List<T> getContent() {
		return this.contents;
	}

	/**
	 * @param contentRaw
	 */
	public void deserialize(List<String> contentRaw) {

		this.contents = contentRaw.stream().map(s -> {
			T content = initContent();
			content.deserialize(s);
			return content;
		}).collect(Collectors.toList());

	}

	/**
	 * @return
	 */
	public List<String> serialize() {

		return this.contents.stream().map(Shared::serialize).collect(Collectors.toList());
	}

	/**
	 * @return
	 */
	public abstract String getVersion();

	/**
	 * @return
	 */
	protected abstract T initContent();

}
