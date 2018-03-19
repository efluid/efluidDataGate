package fr.uem.efluid.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import fr.uem.efluid.services.types.ExportFile;
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

	private static final Formatter DEFAULT_FORMATTER = new Formatter();

	public static final String TH_FORMATTER = "custom";

	/**
	 * <p>
	 * Read a spring-mvc processed Multipart upload request, and provides it as a standard
	 * {@link ExportFile}.
	 * </p>
	 * 
	 * @param request
	 *            the request provided in Spring-mvc Rest controller action on uploads
	 * @return a standard {@link ExportFile} with access to content.
	 */
	public static ExportFile inputExportImportFile(MultipartHttpServletRequest request) {

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
	 * Produces a spring-mvc compliant response for the binary data
	 * 
	 * @param data
	 *            to output as file
	 * @return spring mvc response
	 */
	public static ResponseEntity<InputStreamResource> outputData(byte[] data) {

		// Produces the response body with file content
		return ResponseEntity
				.ok()
				.contentLength(data.length)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new InputStreamResource(new ByteArrayInputStream(data)));
	}

	/**
	 * Produces a spring-mvc compliant response for the export file
	 * 
	 * @param file
	 *            a standard {@link ExportFile} with access to content.
	 * @return spring mvc response
	 */
	public static ResponseEntity<InputStreamResource> outputExportImportFile(ExportFile file) {

		// Produces the response body with file content
		return outputData(file.getData());
	}

	/**
	 * <p>
	 * Add tools to use in templates
	 * </p>
	 * 
	 * @param model
	 */
	public static void addTools(Model model) {
		model.addAttribute(TH_FORMATTER, DEFAULT_FORMATTER);
	}

	/**
	 * Usefull for easy access in Thymeleaf template
	 * 
	 * @author elecomte
	 * @since v0.0.1
	 * @version 1
	 */
	public static class Formatter {

		public String format(LocalDateTime date) {
			return FormatUtils.format(date);
		}
	}

}
