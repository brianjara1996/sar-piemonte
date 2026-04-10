package it.eng.cct.dem.sar.mw.piemonte.exception;

public class NoLottoException extends SarException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1862608803250343721L;

	public NoLottoException() {
	}

	public NoLottoException(String message) {
		super(message);
	}

	public NoLottoException(Throwable cause) {
		super(cause);
	}

	public NoLottoException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoLottoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
