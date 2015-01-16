package org.feenaboccles.kindlomist.articles;

import java.io.Serializable;
import java.net.URI;


/**
 * A generic interface for an Economist article. There are no common
 * methods, as -- while certain articles types share certain methods
 * and properties -- these are "mixed in" rather than having a clear
 * hierarchy.
 */
public interface Article extends Serializable {

	/**
	 * The URI of this article itself
	 */
	public URI getArticleUri();
}
