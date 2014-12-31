package org.feenaboccles.kindlomist.articles.html;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.apache.commons.lang3.StringUtils;
import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.articles.content.Content;
import org.feenaboccles.kindlomist.articles.content.Footnote;
import org.feenaboccles.kindlomist.articles.content.Image;
import org.feenaboccles.kindlomist.articles.content.SubHeading;
import org.feenaboccles.kindlomist.articles.content.Text;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AbstractArticleParser {

	@Data
	@AllArgsConstructor
	protected
	static class ArticleHeader
	{	String title;
		String topic;
		String strap;
	}

	protected static final String MAIN_IMAGE_DIV_CLASS = "content-image-full";
	protected static final int EXPECTED_IMAGE_COUNT = PlainArticle.MAX_IMAGES_PER_ARTICLE;
	protected static final int EXPECTED_PARAGRAPH_COUNT = 10;

	
	public AbstractArticleParser() {
		super();
	}

	/**
	 * Find the DIV tag that encloses all the article's content, including
	 * headings, main image, paragraphs and inline images.
	 */
	protected Element findArticleDiv(Document doc) {
		return doc.select("article div.main-content").first();
	}

	/**
	 * Find within the article DIV (see {@link #findArticleDiv(Document)}) the
	 * IMG tag that has the main article image. This may return null, as not all
	 * articles have such images
	 */
	protected URI readMainImage(Element bodyDiv) throws URISyntaxException {
		
		URI mainImage = null;
		Elements mainImg = bodyDiv.select("div." + MAIN_IMAGE_DIV_CLASS);
		if (! mainImg.isEmpty()) {
			Elements img = mainImg.first().getElementsByTag("img");
			if (! img.isEmpty())
				mainImage = new URI(img.first().attr("src"));
		}
		return mainImage;
	}

	/**
	 * Finds within the document the title, topic and strap associated with
	 * an article, and returns them all together as a single {@link ArticleHeader}
	 * object
	 */
	protected ArticleHeader readHeaders(Document doc) {
		// Parse the title and the strap
		Element hgroup = doc.getElementsByTag("hgroup").first();
		
		String  title  = childTagText (hgroup, "h3", "");
		String  topic  = childTagText (hgroup, "h2", "");
		String  strap  = childTagText (hgroup, "h1", "");
		
		return new ArticleHeader(title, topic, strap);
	}
	
	/**
	 * Read text from the given child element if it exists, returning
	 * the default value otherwise. If there are multiple child tags,
	 * we return the text associated with the <em>first</em> element
	 * @param parent the parent element amongs whose children we search
	 * for a particular tag
	 * @param childTag the tag-name of the child element
	 * @param defaultValue what to return if the child-tag does not 
	 * exist. 
	 */
	private final static String childTagText(Element parent, String childTag, String defaultValue) {
		Elements es = parent.getElementsByTag(childTag);
		return es.isEmpty()
			? defaultValue
			: es.first().text().trim();
	}
	
	/**
	 * Reads the {@link Content} of an article from the given
	 * DIV.
	 */
	protected List<Content> readContent(Element bodyDiv) {
		List<Content> content = new ArrayList<Content>(EXPECTED_IMAGE_COUNT + EXPECTED_PARAGRAPH_COUNT);
		
		// TODO add subscript support.
		
		// Extract the body text, and any other images.
		String paraText;
		for (Element element : bodyDiv.children()) {
			if (element.nodeName() == "p" && (! (paraText = clean(element.text())).isEmpty())) {
				if (element.className().equals ("xhead")) {
					content.add (new SubHeading (paraText));
				}
				else { // check for a footnote, should all be in a <sup> tag
					Elements sups = element.getElementsByTag("sup");
					if (sups.isEmpty()) {
						content.add(new Text(paraText));
					}
					else {
						String supText = clean(sups.first().text());
						if (supText.equals(paraText)) {
							content.add (new Footnote(supText));
						}
						else {
							content.add (new Text(paraText));
						}
					}
				}
			}
			else if (element.nodeName() == "div" && ! element.className().equals(AbstractArticleParser.MAIN_IMAGE_DIV_CLASS)) {
				Elements imgs = element.getElementsByTag("img");
				if (! imgs.isEmpty())
					content.add (new Image (imgs.first().attr("src")));
			}
		}
		
		return content;
	}

	/**
	 * Trims a string, including unusal space characters
	 */
	private String clean(String text) {
		text = StringUtils.trimToEmpty(text);
		text = text.replaceAll("[\u00a0\u2003]+", ""); // fancy space characters
		
		return text;
	}
}