package org.feenaboccles.kindlomist.articles;

import java.net.URI;

/**
 * An article with a main image
 */
public interface MainImageArticle extends Article {
	public URI getMainImage();
}
