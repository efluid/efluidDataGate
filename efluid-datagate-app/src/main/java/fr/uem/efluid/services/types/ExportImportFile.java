package fr.uem.efluid.services.types;

import java.io.IOException;

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
public class ExportImportFile extends ExportFile {

	/**
	 * For import
	 * 
	 * @param file
	 * @param contentType
	 * @throws IOException
	 */
	public ExportImportFile(MultipartFile file, String contentType) throws IOException {
		super(file.getBytes(), file.getOriginalFilename(), contentType);
	}

}
