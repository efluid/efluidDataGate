package fr.uem.efluid.utils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class ApplicationException extends RuntimeException {

	private final ErrorType error;

	private final Long timestamp = Long.valueOf(System.currentTimeMillis());

	private String payload;

	/**
	 * 
	 */
	public ApplicationException(ErrorType error) {
		super();
		this.error = error;
	}

	/**
	 * @param error
	 * @param message
	 * @param cause
	 */
	public ApplicationException(ErrorType error, String message, Throwable cause) {
		super(message, cause);
		this.error = error;
	}

	/**
	 * @param error
	 * @param message
	 * @param payload
	 */
	public ApplicationException(ErrorType error, String message, String payload) {
		super(message);
		this.error = error;
		this.payload = payload;
	}

	/**
	 * @param error
	 * @param message
	 * @param cause
	 * @param payload
	 */
	public ApplicationException(ErrorType error, String message, Throwable cause, String payload) {
		super(message, cause);
		this.error = error;
		this.payload = payload;
	}

	/**
	 * @param error
	 * @param message
	 */
	public ApplicationException(ErrorType error, String message) {
		super(message);
		this.error = error;
	}

	/**
	 * @param error
	 * @param cause
	 */
	public ApplicationException(ErrorType error, Throwable cause) {
		super(cause);
		this.error = error;
	}

	/**
	 * @return the error
	 */
	public ErrorType getError() {
		return this.error;
	}

	/**
	 * @return the timestamp
	 */
	public Long getTimestamp() {
		return this.timestamp;
	}

	/**
	 * @return the payload
	 */
	public String getPayload() {
		return this.payload;
	}

	/**
	 * @param payload
	 *            the payload to set
	 */
	public void setPayload(String payload) {
		this.payload = payload;
	}

}
