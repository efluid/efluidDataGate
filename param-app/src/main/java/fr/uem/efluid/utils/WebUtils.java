package fr.uem.efluid.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.definition.CommonProfileDefinition;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.services.types.ExportFile;
import fr.uem.efluid.services.types.ExportImportFile;
import fr.uem.efluid.services.types.ProjectData;

/**
 * For REST / WEB needs
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class WebUtils {

	private static final Formatter DEFAULT_FORMATTER = new Formatter();

	public static final String TH_FORMATTER = "custom";

	public static final String NAV_BAR = "navBar";

	public static final String PROFIL_TOKEN = "_token_";

	public static final int MAX_BEFORE_SPACER = 8;

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
	public static ExportFile inputExportImportFile(MultipartFile file) {

		if (file == null) {
			throw new ApplicationException(ErrorType.UPLOAD_WRG_DATA, "No available file for parameter files");
		}

		// Standard wrapper for spring-mvc data model
		try {
			return new ExportImportFile(file, file.getContentType());
		} catch (IOException e) {
			throw new ApplicationException(ErrorType.UPLOAD_WRG_DATA, "Cannot process data read for imported file " + file.getName(), e);
		}
	}

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
	 * <p>
	 * Add items for producing a clean pagination bar : create items to display as links
	 * of each available page
	 * </p>
	 * 
	 * @param model
	 */
	public static void addPageNavBarItems(Model model, String pageUri, String search, int currentPage, int pageCount) {

		List<NavBarItem> items = new ArrayList<>();

		String pattern = pageUri + "/%s" + (search != null ? "/" + search : "");

		// No spacer (few pages)
		if (pageCount < MAX_BEFORE_SPACER) {
			NavBarItem.addItems(items, pattern, 0, pageCount, currentPage);
		}

		// With spacer
		else {

			// Current at the beginning : 1 spacer
			if (currentPage < 4) {
				NavBarItem.addItems(items, pattern, 0, 4, currentPage);
				items.add(NavBarItem.spacer());
				NavBarItem.addItems(items, pattern, pageCount - 2, pageCount, currentPage);
			}

			// Current at the end : 1 spacer
			else if (currentPage > pageCount - 4) {
				NavBarItem.addItems(items, pattern, 0, 2, currentPage);
				items.add(NavBarItem.spacer());
				NavBarItem.addItems(items, pattern, pageCount - 4, pageCount, currentPage);
			}

			// Current elsewhere : 2 spacers around
			else {
				NavBarItem.addItems(items, pattern, 0, 2, currentPage);
				items.add(NavBarItem.spacer());
				NavBarItem.addItems(items, pattern, currentPage - 1, currentPage + 2, currentPage);
				items.add(NavBarItem.spacer());
				NavBarItem.addItems(items, pattern, pageCount - 2, pageCount, currentPage);
			}
		}

		model.addAttribute(NAV_BAR, items);
	}

	/**
	 * <p>
	 * Prepare a Pac4j CommonProfile for specified authenticated user during web
	 * authentication
	 * </p>
	 * 
	 * @param user
	 * @return
	 */
	public static CommonProfile webSecurityProfileFromUser(User user) {

		final CommonProfile profile = new CommonProfile();
		profile.setId(user.getLogin());
		profile.addAttribute(Pac4jConstants.USERNAME, user.getLogin());
		profile.addAttribute(CommonProfileDefinition.EMAIL, user.getEmail());
		profile.addAttribute(PROFIL_TOKEN, user.getToken());

		return profile;
	}

	/**
	 * <p>
	 * For paginated search nav bar
	 * </p>
	 * 
	 * @author elecomte
	 * @since v0.0.8
	 * @version 1
	 */
	public static class NavBarItem {

		public final String title;
		public final String uri;
		public final boolean spacer;
		public final boolean selected;

		/**
		 * @param title
		 * @param uri
		 * @param spacer
		 * @param selected
		 */
		private NavBarItem(String title, String uri, boolean spacer, boolean selected) {
			super();
			this.title = title;
			this.uri = uri;
			this.spacer = spacer;
			this.selected = selected;
		}

		/**
		 * @return the title
		 */
		public String getTitle() {
			return this.title;
		}

		/**
		 * @return the uri
		 */
		public String getUri() {
			return this.uri;
		}

		/**
		 * @return the spacer
		 */
		public boolean isSpacer() {
			return this.spacer;
		}

		/**
		 * @return the selected
		 */
		public boolean isSelected() {
			return this.selected;
		}

		/**
		 * @return
		 */
		static NavBarItem spacer() {
			return new NavBarItem(null, null, true, false);
		}

		/**
		 * @param title
		 * @param uri
		 * @param selected
		 * @return
		 */
		static NavBarItem item(String title, String uri, boolean selected) {
			return new NavBarItem(title, uri, false, selected);
		}

		/**
		 * @param items
		 * @param pattern
		 * @param start
		 * @param end
		 * @param currentPage
		 */
		static void addItems(List<NavBarItem> items, String pattern, int start, int end, int currentPage) {

			for (int i = start; i < end; i++) {
				items.add(NavBarItem.item(String.valueOf(i + 1), String.format(pattern, Integer.valueOf(i)), currentPage == i));
			}

		}
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

		public boolean containsProjectData(ProjectData data, List<ProjectData> mayContain) {
			return mayContain.stream().map(ProjectData::getUuid).anyMatch(p -> data.getUuid().equals(p));
		}
	}

}
