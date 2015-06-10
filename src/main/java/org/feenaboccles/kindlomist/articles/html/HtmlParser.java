package org.feenaboccles.kindlomist.articles.html;

import java.net.URI;

public interface HtmlParser<T> {
	
	/**
	 * Parses the HTML of the given article, downloaded from the given URI,
	 * and returns an appropriate object
	 */
	T parse (URI documentUri, String html) throws HtmlParseException;
}
