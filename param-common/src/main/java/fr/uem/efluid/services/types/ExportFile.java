package fr.uem.efluid.services.types;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * <p>
 * Shared model for a file exported. Contains everything supported by application. The
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class ExportFile {

	private final byte[] fileData;
	private final String filename;
	private final String contentType;

	/**
	 * For export
	 * 
	 * @param tmpFileExported
	 * @param contentType
	 * @throws IOException
	 */
	public ExportFile(Path tmpFileExported, String contentType) throws IOException {
		this(Files.readAllBytes(tmpFileExported),
				tmpFileExported.getFileName().toString(),
				contentType);
	}

	/**
	 * @param fileData
	 * @param filename
	 * @param contentType
	 */
	protected ExportFile(byte[] fileData, String filename, String contentType) {
		super();
		this.fileData = fileData;
		this.filename = filename;
		this.contentType = contentType;
	}

	/**
	 * @return
	 */
	public byte[] getData() {
		return this.fileData;
	}

	/**
	 * @return
	 */
	public String getFilename() {
		return this.filename;
	}

	/**
	 * @return
	 */
	public String getContentType() {
		return this.contentType;
	}

	/**
	 * @return
	 */
	public int getSize() {
		return this.fileData.length;
	}

}
