package org.feenaboccles.kindlomist.articles.html;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.logging.log4j.core.util.Charsets;
/**
 * Convenience methods for tests
 *
 */
public class Util {

	private Util() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Reads a UTF-8 encoded text file from the given location, specified as a 
	 * classpath resource (e.g. org/feenaboccles/...)
	 * <p>
	 * Typically used with test resources in the test resources folder (which is
	 * on the root).
	 */
	public static String loadFromClassPath (String classPathUrl) throws IOException {
		ClassLoader ldr = PlainArticleParserTest.class.getClassLoader();
		StringBuilder result = new StringBuilder();
		char[] buf = new char[4096];
		int amt;
		
		try (BufferedReader rdr = new BufferedReader (new InputStreamReader(ldr.getResourceAsStream(classPathUrl), Charsets.UTF_8))) {
			while ((amt = rdr.read(buf)) >= 0)
				result.append(buf, 0, amt);
		}
		
		return result.toString();
	}

}
