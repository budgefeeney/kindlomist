package org.feenaboccles.kindlomist.articles;

import java.io.Serializable;


/**
 * A generic interface for an Economist article. There are no common
 * methods, as -- while certain articles types share certain methods
 * and properties -- these are "mixed in" rather than having a clear
 * hierarchy.
 * @author bryanfeeney
 *
 */
public interface Article extends Serializable {

}
