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
			if (isEssayArticle(html)) {
				return parseEssay(articleUri, html);
			}

			html = cleanUpDodgyHtml(html);

			// Now parse the cleaned document normally
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
		{	throw new HtmlParseException("The article at " + articleUri + " has the right structure, but invalid content " + e.getMessage(), e);
		}
		catch (URISyntaxException e)
		{	throw new HtmlParseException("The URI extracted for an image in the article is not a valid URI : " + e.getMessage());
		}
		catch (NullPointerException e)
		{	throw new HtmlParseException("The HTML file does not have the expected structure, certain tags could not be found");
		}
	}

	private static boolean isEssayArticle(String html) {
		return html.contains("es-section");
	}

	/**
	 * There is occasionally some really dodgy things in Economist
	 * HTML, as writers fight against the CMS they have to use
 	 */
	private static String cleanUpDodgyHtml(String html) {
		return html.replaceAll("<br>\\s*<br>", "</p><p>");
	}

	/**
	 * Parses what is essentially a plain article, but laid out in the essay
	 * style. Such articles tend to crop up in, e.g., the Christmas
	 * Special issue
	 *
	 */
	public PlainArticle parseEssay(URI articleUri, String html) throws HtmlParseException {
		html = cleanUpDodgyHtml(html);

		// Now parse the cleaned document normally
		Document doc = Jsoup.parse(html);

		Element section = doc.getElementsByTag("section")
				.stream()
				.filter(e -> e.className().equals("es-section"))
				.findFirst()
				.orElseThrow(() -> new HtmlParseException("This is an essay article without an essay-content section"));

		try {
			Element header = section.getElementsByClass("es-image-wrap").first();
			Element title = header.getElementsByTag("h2").first();

			final Optional<URI> mainImageUri;
			Element mainImageElem = header.getElementsByClass("imagefield-field_essay_main_image").first();
			if (mainImageElem != null) {
				mainImageUri = Optional.of(new URI(mainImageElem.attr("src")));
			} else {
				mainImageUri = Optional.empty();
			}

			Element strap = section.getElementsByClass("es-rubric").first();

			Element contentDiv = section.getElementsByClass("es-content").first();
			List<Content> content = readContent(contentDiv);

			return PlainArticle.builder()
					.articleUri(articleUri)
					.title(title.text())
					.topic("Essay")
					.strap(strap.text())
					.body(content)
					.mainImage(mainImageUri)
					.build().validate();

		} catch (ValidationException e) {
			throw new HtmlParseException("The article at " + articleUri + " has the right structure, but invalid content " + e.getMessage(), e);
		} catch (URISyntaxException e) {
			throw new HtmlParseException("The article at " + articleUri + " specifies an invalid URI for it's main image : " + e.getMessage(), e);
		} catch (NullPointerException e) {
			throw new HtmlParseException("The article at " + articleUri + " did not specify all expected tags", e);
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
	 * Return true if the given article is a mini-article. The two kinds of mini
	 * article are
	 * <ul>
	 *     <li>An image and a single paragraph after it</li>
	 *     <li>An image, a single paragraph, and a list of short bullets</li>
	 * </ul>
	 *
	 * The image may be the main article image, or if that is absent, the first element of content.
	 */
	private static boolean isMiniArticle (Optional<URI> mainImage, List<Content> content) {
		int firstText = -1;
		if (mainImage.isPresent() && content.size() > 0) {
			firstText = 0;
		} else if (content.size() > 1 && content.get(0).getType().equals(Type.IMAGE)) {
			firstText = 1;
		}

		// Didn't find the image, not a mini-article
		if (firstText < 0) {
			return false;
		}

		// Found an image, and it's followed by just one normal paragraph,
		// or one normal paragraph and several "short-ish" ones, as if in a
		// list of bullet points.
		if (! content.get(firstText).getType().equals(Type.TEXT)) {
			return false;
		}

		// Validate that there are no more content elements, or if there are,
		// that they are all short-ish texts
		for (int i = firstText + 1; i < content.size(); i++) {
			Content c = content.get(i);
			if (! c.getType().equals(Type.TEXT)) {
				return false;
			}
			if (c.getContent().length() > SHORTISH_TEXT_LEN) {
				return false;
			}
		}
		return true;
	}
	private final static int SHORTISH_TEXT_LEN = 250;
}
