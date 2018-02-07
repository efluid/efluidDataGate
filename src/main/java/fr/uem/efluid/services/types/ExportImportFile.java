package fr.uem.efluid.services.types;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.springframework.web.multipart.MultipartFile;

import fr.uem.efluid.utils.TechnicalException;

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
	 */
	public ExportImportFile(MultipartFile file, String contentType) {
		super();
		try {
			this.fileData = file.getBytes();
			this.filename = file.getOriginalFilename();
			this.contentType = contentType;
		} catch (IOException e) {
			throw new TechnicalException("Cannot process the imported multipart file", e);
		}
	}

	/**
	 * For export
	 * 
	 * @param tmpFileExported
	 * @param contentType
	 */
	public ExportImportFile(File tmpFileExported, String contentType) {
		super();
		// Readonly access to exported file
		try (RandomAccessFile f = new RandomAccessFile(tmpFileExported, "r")) {

			final int length = (int) f.length();
			final byte[] b = new byte[length];
			f.read(b);

			this.fileData = b;
			this.filename = tmpFileExported.getName();
			this.contentType = contentType;
		} catch (IOException e) {
			throw new TechnicalException("Cannot process the exported temp file", e);
		}
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
