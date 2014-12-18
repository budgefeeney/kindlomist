package org.feenaboccles.kindlomist.articles.html;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.validation.ValidationException;

import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.articles.content.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Takes a HTML page representing a standard Economist article, and parses
 * it into a {@link PlainArticle} object via the {@link #parse(String)} method.
 * <p>
 * Threadsafe.
 */
public class PlainArticleParser extends AbstractArticleParser  {

	public PlainArticleParser() {
		;
	}

	public PlainArticle parse(String html) throws HtmlParseException {
		
		
		try {
			Document doc = Jsoup.parse(html);
			ArticleHeader header = readHeaders(doc);
			
			Element bodyDiv = findArticleDiv(doc);
			URI mainImage = readMainImage(bodyDiv);
			
			List<Content> content = readContent(bodyDiv);
			
			return PlainArticle.builder()
				               .title(header.getTitle())
				               .topic(header.getTopic())
				               .strap(header.getStrap())
				               .body(content)
				               .mainImage(mainImage)
				               .build().validate();
		}
		catch (ValidationException e)
		{	throw new HtmlParseException("The parse succeeded, but the extracted fields defined an article that failed to validate: " + e.getMessage(), e);
		}
		catch (URISyntaxException e)
		{	throw new HtmlParseException("The URI extracted for an image in the article is not a valid URI : " + e.getMessage());
		}
		catch (NullPointerException e)
		{	throw new HtmlParseException("The HTML file does not have the expected structure, certain tags could not be found");
		}
		
	}
}
