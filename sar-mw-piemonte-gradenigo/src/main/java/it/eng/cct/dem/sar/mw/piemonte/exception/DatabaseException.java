package it.eng.cct.dem.sar.mw.piemonte.exception;

public class DatabaseException extends SarException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1862608803250343721L;

	public DatabaseException() {
	}

	public DatabaseException(String message) {
		super(message);
	}

	public DatabaseException(Throwable cause) {
		super(cause);
	}

	public DatabaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public DatabaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
