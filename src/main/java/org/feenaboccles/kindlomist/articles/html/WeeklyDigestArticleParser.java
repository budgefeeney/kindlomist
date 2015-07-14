package org.feenaboccles.kindlomist.articles.html;


import java.net.URI;
import java.util.List;

import javax.validation.ValidationException;

import org.feenaboccles.kindlomist.articles.WeeklyDigestArticle;
import org.feenaboccles.kindlomist.articles.content.Content;
import org.feenaboccles.kindlomist.articles.content.LetterAuthor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


/**
 * 
 * @author bryanfeeney
 *
 */
public class WeeklyDigestArticleParser extends AbstractArticleParser
	implements HtmlParser<WeeklyDigestArticle>{
	
	@Override
	public WeeklyDigestArticle parse(URI articleUri, String html) throws HtmlParseException {
		try {
			Document doc = Jsoup.parse(html);
			Element bodyDiv = findArticleDiv(doc);
			
			List<Content> content = readContent(bodyDiv);
			
			return new WeeklyDigestArticle(articleUri, content).validate();
		}
		catch (ValidationException e)
		{	throw new HtmlParseException("The parse succeeded, but the extracted fields defined an article that failed to validate: " + e.getMessage(), e);
		}
		catch (NullPointerException e)
		{	throw new HtmlParseException("The HTML file does not have the expected structure, certain tags could not be found");
		}
	}

	/**
	 * Reads the {@link Content} of an article from the given
	 * DIV. Supports the conversion of short text to headings, but
	 * not the use of {@link LetterAuthor} tags.
	 */
	protected List<Content> readContent(Element bodyDiv) {
		return readContent(
				bodyDiv,
				/* convertShortTextToHeading = */ true,
				/* permitLetterAuthor = */ false);
	}
}
