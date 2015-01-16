package org.feenaboccles.kindlomist.articles;

import java.util.List;

import org.feenaboccles.kindlomist.articles.content.Content;

/**
 * An article with a list of {@link Content} items
 */
public interface ContentBasedArticle extends Article {
	public List<Content> getBody();
}
