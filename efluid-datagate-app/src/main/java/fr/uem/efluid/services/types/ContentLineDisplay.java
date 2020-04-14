package fr.uem.efluid.services.types;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class ContentLineDisplay {

	private final String key;
	private final String hrPayload;
	private final String columnName;

	/**
	 * @param key
	 * @param hrPayload
	 */
	public ContentLineDisplay(String key, String hrPayload, String columnName) {
		super();
		this.key = key;
		this.hrPayload = hrPayload;
		this.columnName = columnName;


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
