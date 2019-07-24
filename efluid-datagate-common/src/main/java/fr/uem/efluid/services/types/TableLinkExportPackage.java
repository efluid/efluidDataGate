package fr.uem.efluid.services.types;

import java.time.LocalDateTime;

import fr.uem.efluid.model.shared.ExportAwareTableLink;

/**
 * <p>
 * Link package updates :
 * <ul>
 * <li>V1 : Standard link - initial release</li>
 * <li>V2 : Link updated with update date for version checking</li>
 * <li>V3 : Link with composite keys</li>
 * </ul>
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 3
 */
public abstract class TableLinkExportPackage<D extends ExportAwareTableLink<?>> extends SharedPackage<D> {

	public static final String LINKS_EXPORT = "full-links";
	public static final String PARTIAL_LINKS_EXPORT = "partial-links";

	/**
	 * @param name
	 * @param exportDate
	 */
	public TableLinkExportPackage(String name, LocalDateTime exportDate) {
		super(name, exportDate);
	}

	/**
	 * @return
	 */
	@Override
	public final String getVersion() {
		return "3";
	}
}
