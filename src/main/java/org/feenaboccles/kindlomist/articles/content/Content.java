package org.feenaboccles.kindlomist.articles.content;

import javax.validation.ValidationException;

/**
 * Article content - can come in several forms (e.g. plain-text, inline images, sub-headings)
 * 
 * @author bryanfeeney
 *
 */
public interface Content {

	/**
	 * Gets the text value of the content encapsulated by this object
	 */
	public String getContent();
	
	/**
	 * Checks that this content is valid, throwing a {@link ValidationException}
	 * if not.
	 */
	public Content validate() throws ValidationException;
}
