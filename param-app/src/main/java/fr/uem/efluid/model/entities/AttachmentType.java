package fr.uem.efluid.model.entities;

/**
 * @author elecomte
 * @since v2.0.0
 * @version 1
 */
public enum AttachmentType {

	MD_FILE(".md", true, false),
	SQL_FILE(".sql", true, true),
	PAR_FILE(".par", false, true),
	ZIP_FILE(".zip", false, false),
	TEXT_FILE(".txt", true, false),
	OTHER(".*", false, false);

	private final String extension;

	private final boolean editable;

	private final boolean runnable;

	private AttachmentType(String ext, boolean editable, boolean runnable) {
		this.extension = ext;
		this.editable = editable;
		this.runnable = runnable;
	}

	/**
	 * @return
	 */
	public String getExtension() {
		return this.extension;
	}

	/**
	 * @return the editable
	 */
	public boolean isEditable() {
		return this.editable;
	}

	/**
	 * @return the runnable
	 */
	public boolean isRunnable() {
		return this.runnable;
	}

	/**
	 * @param contentType
	 * @param fileName
	 * @return
	 */
	public static AttachmentType fromContentTypeAndFileName(String contentType, String fileName) {

		// TODO : use contentType for doubleCheck

		int extPos = fileName.lastIndexOf('.');

		switch (fileName.substring(extPos + 1)) {
		case "sql":
			return SQL_FILE;
		case "md":
			return MD_FILE;
		case "par":
			return PAR_FILE;
		case "zip":
			return ZIP_FILE;
		default:
			return OTHER;
		}
	}
}
