package fr.uem.efluid.services.types;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import fr.uem.efluid.model.entities.AttachmentType;
import fr.uem.efluid.tools.AttachmentProcessor;
import fr.uem.efluid.utils.SharedOutputInputUtils;

/**
 * <p>
 * Manage the content for one attachment in a commit or in a set of commit
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class AttachmentLine implements AttachmentProcessor.Compliant {

	private UUID uuid;
	private String tmpPath;
	private String name;
	private AttachmentType type;

	/**
	 * @param uuid
	 *            the uuid to set
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the uuid
	 */
	@Override
	public UUID getUuid() {
		return this.uuid;
	}

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
	@Override
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
	@Override
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

	/**
	 * @return
	 * @see fr.uem.efluid.tools.AttachmentProcessor.Compliant#getData()
	 */
	@Override
	public byte[] getData() {
		return SharedOutputInputUtils.deserializeDataFromTmpFile(Paths.get(getTmpPath()));
	}
}
