package org.feenaboccles.kindlomist.articles;

import java.net.URI;
import java.util.Optional;

/**
 * An article which may have a main image
 */
public interface MainImageArticle extends Article {
	Optional<URI> getMainImage();
}
