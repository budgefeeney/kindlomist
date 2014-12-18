package org.feenaboccles.kindlomist.articles.html;

import java.io.IOException;
import java.net.URISyntaxException;

import org.feenaboccles.kindlomist.articles.WeeklyDigestArticle;
import org.junit.Test;

public class WeeklyDigestArticleParserTest {


	@Test
	public void testOnPoliticsThisWeek() throws IOException, HtmlParseException, URISyntaxException {
		String articleText = Util.loadFromClassPath("pols-this-week.html");
		
		WeeklyDigestArticle a = new WeeklyDigestArticleParser().parse(articleText);
		
		System.out.println (a.toString());
		
	}
	

	@Test
	public void testOnBusinessThisWeek() throws IOException, HtmlParseException, URISyntaxException {
		String articleText = Util.loadFromClassPath("biz-this-week.html");
		
		WeeklyDigestArticle a = new WeeklyDigestArticleParser().parse(articleText);
		
		System.out.println (a.toString());
		
	}
}
