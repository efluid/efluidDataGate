package fr.uem.efluid.services.types;

/**
 * <p>
 * When the diff process result implies to share some warnings to the user, a
 * <tt>DiffRemark</tt> can be used
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class DiffRemark<T> {

	private final String location;

	private final RemarkType type;

	private final T payload;

	/**
	 * @param type
	 * @param payload
	 */
	public DiffRemark(RemarkType type, String location, T payload) {
		super();
		this.location = location;
		this.type = type;
		this.payload = payload;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return this.location;
	}

	/**
	 * @return the type
	 */
	public RemarkType getType() {
		return this.type;
	}

	/**
	 * @return the payload
	 */
	public T getPayload() {
		return this.payload;
	}

	/**
	 * @author elecomte
	 * @since v0.0.8
	 * @version 1
	 */
	public static enum RemarkType {
		MISSING_ON_UNCHECKED_JOIN
	}
}
