package org.feenaboccles.kindlomist.articles.html;

/**
 * Thrown when we fail to parse a HTML file, for whatever reason
 */
public class HtmlParseException extends Exception {

	private static final long serialVersionUID = 1L;

	public HtmlParseException() {
		// TODO Auto-generated constructor stub
	}

	public HtmlParseException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public HtmlParseException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public HtmlParseException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public HtmlParseException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
