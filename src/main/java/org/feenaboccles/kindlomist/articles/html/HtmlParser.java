package org.feenaboccles.kindlomist.articles.html;

import java.net.URI;

/**
 * An interface for any class that can parse a piece of HTML
 * @param <T> the type of object returned by parsing the HTML
 */
public interface HtmlParser<T> {
	
	/**
	 * Parses the HTML of the given article, downloaded from the given URI,
	 * and returns an appropriate object
	 */
	T parse (URI documentUri, String html) throws HtmlParseException;
}
