package org.feenaboccles.kindlomist.articles.html;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.ValidationException;

import org.feenaboccles.kindlomist.articles.WeeklyDigestArticle;
import org.feenaboccles.kindlomist.articles.content.Content;
import org.feenaboccles.kindlomist.articles.content.Image;
import org.feenaboccles.kindlomist.articles.content.SubHeading;
import org.feenaboccles.kindlomist.articles.content.Text;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * 
 * @author bryanfeeney
 *
 */
public class WeeklyDigestArticleParser extends AbstractArticleParser {

	public WeeklyDigestArticleParser() {
		// TODO Auto-generated constructor stub
	}
	
	public WeeklyDigestArticle parse(String html) throws HtmlParseException {
		try {
			Document doc = Jsoup.parse(html);
			Element bodyDiv = findArticleDiv(doc);
			
			List<Content> content = readContent(bodyDiv);
			
			return WeeklyDigestArticle.builder().body(content).build().validate();
		}
		catch (ValidationException e)
		{	throw new HtmlParseException("The parse succeeded, but the extracted fields defined an article that failed to validate: " + e.getMessage(), e);
		}
		catch (NullPointerException e)
		{	throw new HtmlParseException("The HTML file does not have the expected structure, certain tags could not be found");
		}
	}



}
