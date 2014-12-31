package org.feenaboccles.kindlomist.articles.html;

import java.net.URI;
import java.net.URISyntaxException;

import javax.validation.ValidationException;

import org.feenaboccles.kindlomist.articles.SingleImageArticle;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Takes an HTML page representing an article in the Economist consisting solely
 * of a single image (e.g. KAL's cartoon) and parses it (using the )
 * into a {@link SingleImageArticle} object
 * @author bryanfeeney
 *
 */
public class SingleImageArticleParser extends AbstractArticleParser 
	implements HtmlParser<SingleImageArticle> {

	public SingleImageArticleParser() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public SingleImageArticle parse(String html) throws HtmlParseException {
		
		try {
			Document doc = Jsoup.parse(html);
			
			Element bodyDiv   = findArticleDiv(doc);
			URI     mainImage = readMainImage(bodyDiv);
			
			return new SingleImageArticle(mainImage).validate();
		}
		catch (ValidationException e)
		{	throw new HtmlParseException("The parse succeeded, but the extracted image URL failed to validate: " + e.getMessage(), e);
		}
		catch (URISyntaxException e)
		{	throw new HtmlParseException("The URI extracted for an image in the article is not a valid URI : " + e.getMessage());
		}
		catch (NullPointerException e)
		{	throw new HtmlParseException("The HTML file does not have the expected structure, certain tags could not be found");
		}
		
	}
}
