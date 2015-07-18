package org.feenaboccles.kindlomist.articles.content;

import java.io.Serializable;

import javax.validation.ValidationException;

/**
 * Article content - can come in several forms (e.g. paragraph-text, inline images, sub-headings)
 * @author bryanfeeney
 *
 * This is basically a poor man's version of a sum-type (case-class in Scala parlance)
 */
public interface Content extends Serializable {

	enum Type {
		TEXT,
		SUB_HEADING,
		IMAGE,
		FOOTNOTE,
		PULL_QUOTE,
		LETTER_AUTHOR,
		REFERENCE
	}
	
	/**
	 * Gets the text value of the content encapsulated by this object
	 */
	String getContent();
	
	/**
	 * Checks that this content is valid, throwing a {@link ValidationException}
	 * if not.
	 */
	Content validate() throws ValidationException;
	
	/**
	 * Returns the type of this content - same as an instanceof check really, but
	 * it's a bit cleaner to code up
	 */
	Type getType();
}
