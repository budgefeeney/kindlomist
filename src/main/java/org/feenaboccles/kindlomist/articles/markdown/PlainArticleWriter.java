package org.feenaboccles.kindlomist.articles.markdown;

import java.io.Writer;

import org.feenaboccles.kindlomist.articles.ImageResolver;
import org.feenaboccles.kindlomist.articles.PlainArticle;

/**
 * Writes out a {@link PlainArticle} in Markdown format
 */
public class PlainArticleWriter extends ArticleWriter {

	public PlainArticleWriter(Writer writer, ImageResolver images) {
		super(writer, images);
	}
	
	
	
}
