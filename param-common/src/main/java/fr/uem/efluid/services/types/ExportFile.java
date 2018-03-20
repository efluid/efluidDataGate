package fr.uem.efluid.services.types;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.web.multipart.MultipartFile;

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

	/**
	 * @return
	 */
	public MultipartFile toMultipartFile() {

		return new MultipartFile() {

			@Override
			public String getName() {
				return ExportFile.this.getFilename();
			}

			@Override
			public String getOriginalFilename() {
				return ExportFile.this.getFilename();
			}

			@Override
			public String getContentType() {
				return ExportFile.this.getContentType();
			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public long getSize() {
				return ExportFile.this.getSize();
			}

			@Override
			public byte[] getBytes() throws IOException {
				return ExportFile.this.getData();
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return new ByteArrayInputStream(getBytes());
			}

			@Override
			public void transferTo(File dest) throws IOException, IllegalStateException {
				throw new IllegalStateException("Not supported");
			}

		};
	}

}
