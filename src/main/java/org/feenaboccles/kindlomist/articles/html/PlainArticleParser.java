package org.feenaboccles.kindlomist.articles.html;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.validation.ValidationException;

import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.articles.content.Content;
import org.feenaboccles.kindlomist.articles.content.Content.Type;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Takes a HTML page representing a standard Economist article, and parses
 * it into a {@link PlainArticle} object via the {@link #parse(URI, String)}  method.
 * <p>
 * Threadsafe.
 */
public class PlainArticleParser extends AbstractArticleParser
	implements HtmlParser<PlainArticle> {


	@Override
	public PlainArticle parse(URI articleUri, String html) throws HtmlParseException {
		
		try {
			Document doc = Jsoup.parse(html);
			ArticleHeader header = readHeaders(doc);
			
			Element bodyDiv = findArticleDiv(doc);
			Optional<URI> mainImage = readMainImage(bodyDiv);
			
			List<Content> content = readContent(bodyDiv);

			// In some cases the Economist may publish a mini-article lacking either title,
			// such as mini-articles showing a chart with some commentary, or both title and topic,
			// such as job ads. We skip the job ads, but work around the charts with commentary.
			if (content.size() >= 2 && hasAtLeastOneImage(content) 
					&& header.getTitle().isEmpty() && ! header.getTopic().isEmpty()) {
				header.setTitle(header.getTopic());
				header.setTopic("In Brief");
			}
			
			return PlainArticle.builder()
		                       .articleUri(articleUri)
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

	private boolean hasAtLeastOneImage(List<Content> contents) {
		for (Content content : contents)
			if (content.getType() == Type.IMAGE)
				return true;
		return false;
	}
}
