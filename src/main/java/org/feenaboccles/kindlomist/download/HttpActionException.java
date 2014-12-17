package org.feenaboccles.kindlomist.download;

/**
 * Thrown when a {@link HttpAction} fails. Encompasses all possible failures,
 * from network failure, to page parsing failure.
 */
public class HttpActionException extends Exception {

	public HttpActionException() {
		// TODO Auto-generated constructor stub
	}

	public HttpActionException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public HttpActionException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public HttpActionException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public HttpActionException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
