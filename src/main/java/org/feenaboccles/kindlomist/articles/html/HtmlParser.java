package org.feenaboccles.kindlomist.articles.html;

public interface HtmlParser<T> {
	
	public T parse (String html) throws HtmlParseException;
}
