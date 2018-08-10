package fr.uem.efluid.services.types;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class ContentLineDisplay {

	private final String key;
	private final String hrPayload;

	/**
	 * @param key
	 * @param hrPayload
	 */
	public ContentLineDisplay(String key, String hrPayload) {
		super();
		this.key = key;
		this.hrPayload = hrPayload;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * @return the hrPayload
	 */
	public String getHrPayload() {
		return this.hrPayload;
	}

}
