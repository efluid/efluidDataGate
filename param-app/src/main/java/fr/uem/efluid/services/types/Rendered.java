package fr.uem.efluid.services.types;

/**
 * <p>
 * Specify that the item is processed in rendering (in screens or rest services, for
 * manual operation)
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface Rendered {

	/**
	 * <p>
	 * If true, the entry is a display only feature : it will be ignored in saving
	 * process, as it is only intended to be used on display
	 * </p>
	 * 
	 * @return
	 */
	boolean isDisplayOnly();

	/**
	 * <p>
	 * Contains a "human readable" payload for rendering
	 * </p>
	 * 
	 * @return
	 */
	String getHrPayload();
}
