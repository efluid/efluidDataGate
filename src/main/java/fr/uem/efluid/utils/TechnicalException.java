package fr.uem.efluid.utils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class TechnicalException extends RuntimeException {

	/**
	 * 
	 */
	public TechnicalException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public TechnicalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TechnicalException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public TechnicalException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public TechnicalException(Throwable cause) {
		super(cause);
	}

}
