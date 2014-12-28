package org.feenaboccles.kindlomist.articles.html;

import static org.junit.Assert.*;

import java.io.IOException;

import org.feenaboccles.kindlomist.articles.SingleImageArticle;
import org.junit.Test;

public class SingleImageArticleParserTest {

	@Test
	public void testOnValidInput() throws IOException, HtmlParseException {
		String articleText = Util.loadFromClassPath("kal.html");
		
		SingleImageArticle a = new SingleImageArticleParser().parse(articleText);
		
		assertEquals ("http://cdn.static-economist.com/sites/default/files/imagecache/full-width/images/2014/12/articles/main/20141213_wwd000.jpg", a.getMainImage().toASCIIString());
	}
}
