package fr.uem.efluid.model.entities;

/**
 * @author elecomte
 * @since v2.0.0
 * @version 1
 */
public enum AttachmentType {

	MD_FILE(".md"),
	SQL_FILE(".sql"),
	PAR_FILE(".par"),
	ZIP_FILE(".zip"),
	OTHER(".*");

	private final String extension;

	private AttachmentType(String ext) {
		this.extension = ext;
	}

	/**
	 * @return
	 */
	public String getExtension() {
		return this.extension;
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
