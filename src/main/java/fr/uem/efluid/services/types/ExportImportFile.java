package fr.uem.efluid.services.types;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * Shared model for a file exported or imported. Contains everything supported by
 * application. The
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class ExportImportFile {

	private final byte[] fileData;
	private final String filename;
	private final String contentType;

	/**
	 * For import
	 * 
	 * @param file
	 * @param contentType
	 * @throws IOException
	 */
	public ExportImportFile(MultipartFile file, String contentType) throws IOException {
		super();

		this.fileData = file.getBytes();
		this.filename = file.getOriginalFilename();
		this.contentType = contentType;
	}

	/**
	 * For export
	 * 
	 * @param tmpFileExported
	 * @param contentType
	 * @throws IOException
	 */
	public ExportImportFile(Path tmpFileExported, String contentType) throws IOException {
		super();

		this.fileData = Files.readAllBytes(tmpFileExported);
		this.filename = tmpFileExported.getFileName().toString();
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
}
