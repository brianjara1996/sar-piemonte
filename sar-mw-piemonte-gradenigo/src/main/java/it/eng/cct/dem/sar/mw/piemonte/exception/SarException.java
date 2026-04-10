package it.eng.cct.dem.sar.mw.piemonte.exception;

public class SarException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1862608803250345721L;

	public SarException() {
	}

	public SarException(String message) {
		super(message);
	}

	public SarException(Throwable cause) {
		super(cause);
	}

	public SarException(String message, Throwable cause) {
		super(message, cause);
	}

	public SarException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
