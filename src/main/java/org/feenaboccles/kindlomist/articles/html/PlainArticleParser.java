package org.feenaboccles.kindlomist.articles.html;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.validation.ValidationException;

import org.apache.commons.lang3.tuple.Pair;
import org.feenaboccles.kindlomist.articles.Article;
import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.articles.content.Content;
import org.feenaboccles.kindlomist.articles.content.Content.Type;
import org.feenaboccles.kindlomist.articles.content.LetterAuthor;
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

	public static final String MINI_ARTICLE_STRAP = "A brief overview";

	@Override
	public PlainArticle parse(URI articleUri, String html) throws HtmlParseException {
		try {
			Document doc = Jsoup.parse(html);
			ArticleHeader header = readHeaders(doc);
			
			Element bodyDiv = findArticleDiv(doc);
			Pair<Optional<URI>, Optional<String>> mainImageCap = readMainImage(bodyDiv);
			
			List<Content> content = readContent(bodyDiv);

			// There is a parse bug, which I can't track, where sometimes the first element
			// of content is confused with the main image. This is patched here.
			if (mainImageMatchesFirstContent(mainImageCap.getLeft(), content)) {
				content.remove(0);
			}

			header = cleanHeaders(header, content, mainImageCap.getLeft(), mainImageCap.getRight());
			
			return PlainArticle.builder()
		                       .articleUri(articleUri)
				               .title(header.getTitle())
				               .topic(header.getTopic())
				               .strap(header.getStrap())
				               .body(content)
				               .mainImage(mainImageCap.getLeft())
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

	/**
	 * Reads the {@link Content} of an article from the given
	 * DIV. Does not support the conversion of short text to headings, or
	 * the use of {@link LetterAuthor} tags.
	 */
	protected List<Content> readContent(Element bodyDiv) {
		return readContent(
				bodyDiv,
				/* convertShortTextToHeading = */ false,
				/* permitLetterAuthor = */ false);
	}

	/**
	 * Reads the headers, and performs any adjustments necessary for special cast
	 * articles.
	 *
	 * This is an <strong>in-place</strong> mutation
	 */
	protected ArticleHeader cleanHeaders(ArticleHeader header, List<Content> content, Optional<URI> image, Optional<String> imageCaption) {
		// In some cases the Economist may publish a mini-article lacking either title,
		// such as mini-articles showing a chart with some commentary, or both title and topic,
		// such as job ads. We skip the job ads, but work around the charts with commentary.
		if (headerLacksTitleOnly(header)) {
			if (isMiniArticle(image, content)) {
				header.setTitle(header.getTopic());
				header.setTopic(header.getStrap());
				header.setStrap(MINI_ARTICLE_STRAP);
			}
			// And then sometimes the article starts with a main image whose
			// caption is meant to serve as the strap
			else if (image.isPresent() && imageCaption.isPresent()) {
				header.setTitle(header.getTopic());
				header.setTopic(header.getStrap());
				header.setStrap(imageCaption.get());
			}
		}

		return header;
	}

	/**
	 * Returns true if the main image is present, and the content has more than one element,
	 * and the first element is an image exactly the same as the mainImgUri
	 */
	private boolean mainImageMatchesFirstContent(Optional<URI> mainImgUri, List<Content> content) {
		return content.size() > 1
				&& mainImgUri.isPresent()
				&& content.get(0).getType().equals(Type.IMAGE)
				&& content.get(0).getContent().equals(mainImgUri.get().toASCIIString());
	}

	private boolean headerLacksTitleOnly(ArticleHeader header) {
		return header.getTitle().isEmpty() && !header.getTopic().isEmpty() && !header.getStrap().isEmpty();
	}

	/**
	 * Return true if the given article is a mini-article, i.e. it has one image,
	 * and one paragraph after that image. The image may be the main article
	 * image, or if that is absent, the first element of content.
	 */
	private static boolean isMiniArticle (Optional<URI> mainImage, List<Content> content) {
		if (mainImage.isPresent()) {
			return content.size() == 1 && content.get(0).getType().equals(Type.TEXT);
		} else {
			return content.size() == 2
					&& content.get(0).getType().equals(Type.IMAGE)
					&& content.get(1).getType().equals(Type.TEXT);
		}
	}
}
