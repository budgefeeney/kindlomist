package org.feenaboccles.kindlomist.articles.content;

import java.io.Serializable;

import javax.validation.ValidationException;

/**
 * Article content - can come in several forms (e.g. plain-text, inline images, sub-headings)
 * 
 * @author bryanfeeney
 *
 */
public interface Content extends Serializable {

	enum Type {
		TEXT,
		SUB_HEADING,
		IMAGE,
		FOOTNOTE
	}
	
	/**
	 * Gets the text value of the content encapsulated by this object
	 */
	public String getContent();
	
	/**
	 * Checks that this content is valid, throwing a {@link ValidationException}
	 * if not.
	 */
	public Content validate() throws ValidationException;
	
	/**
	 * Returns the type of this content - same as an instanceof check really, but
	 * it's a bit cleaner to code up
	 */
	public Type getType();
}
