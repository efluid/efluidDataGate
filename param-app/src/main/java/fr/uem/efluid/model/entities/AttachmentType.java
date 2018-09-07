package fr.uem.efluid.model.entities;

/**
 * @author elecomte
 * @since v2.0.0
 * @version 1
 */
public enum AttachmentType {

	MD_FILE(".md"),
	SQL_FILE(".sql"),
	OTHER(".*");

	private final String extension;

	private AttachmentType(String ext) {
		this.extension = ext;
	}

	public String getExtension() {
		return this.extension;
	}
}
