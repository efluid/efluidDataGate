package fr.uem.efluid.services.types;

import java.nio.file.Path;

import fr.uem.efluid.model.entities.AttachmentType;

/**
 * <p>
 * Manage the content for one attachment in a commit or in a set of commit
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class AttachmentLine {

	private String tmpPath;
	private String name;
	private AttachmentType type;

	/**
	 * @return the tmpPath
	 */
	public String getTmpPath() {
		return this.tmpPath;
	}

	/**
	 * @param tmpPath
	 *            the tmpPath to set
	 */
	public void setTmpPath(String tmpPath) {
		this.tmpPath = tmpPath;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public AttachmentType getType() {
		return this.type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(AttachmentType type) {
		this.type = type;
	}

	/**
	 * @param file
	 * @param tmpPath
	 * @return
	 */
	public static AttachmentLine fromUpload(ExportFile file, Path tmpPath) {

		AttachmentLine line = new AttachmentLine();

		String name = file.getShortName();
		line.setName(name);
		line.setType(AttachmentType.fromContentTypeAndFileName(file.getContentType(), name));
		line.setTmpPath(tmpPath.toString());

		return line;
	}
}
