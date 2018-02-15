package fr.uem.efluid.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import fr.uem.efluid.services.types.ExportImportFile;

/**
 * For REST / WEB needs
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class WebUtils {

	public static final String API_ROOT = "/services";

	public static final String DATE_FORMAT = "yyyy-MM-dd";

	public static final String TIME_FORMAT = "HH:mm:ss";

	public static final String DATE_TIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;

	/**
	 * <p>
	 * Read a spring-mvc processed Multipart upload request, and provides it as a standard
	 * {@link ExportImportFile}.
	 * </p>
	 * 
	 * @param request
	 *            the request provided in Spring-mvc Rest controller action on uploads
	 * @return a standard {@link ExportImportFile} with access to content.
	 */
	public static ExportImportFile inputExportImportFile(MultipartHttpServletRequest request) {

		// Assume only one uploaded file param
		String fname = request.getFileNames().next();

		if (fname == null) {
			throw new ApplicationException(ErrorType.UPLOAD_WRG_DATA, "No specified upload name");
		}

		// Get the spring-mvc wrapper to data for the param
		MultipartFile file = request.getFile(fname);

		if (file == null) {
			throw new ApplicationException(ErrorType.UPLOAD_WRG_DATA, "No available file for parameter " + fname);
		}

		// Standard wrapper for spring-mvc data model
		try {
			return new ExportImportFile(file, request.getContentType());
		} catch (IOException e) {
			throw new ApplicationException(ErrorType.UPLOAD_WRG_DATA, "Cannot process data read for imported file " + fname, e);
		}
	}

	/**
	 * Produces a spring-mvc compliant response for the export file
	 * 
	 * @param file
	 *            a standard {@link ExportImportFile} with access to content.
	 * @return spring mvc response
	 */
	public static ResponseEntity<InputStreamResource> outputExportImportFile(ExportImportFile file) {

		// Produces the response body with file content
		return ResponseEntity
				.ok()
				.contentLength(file.getData().length)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new InputStreamResource(new ByteArrayInputStream(file.getData())));
	}

}
