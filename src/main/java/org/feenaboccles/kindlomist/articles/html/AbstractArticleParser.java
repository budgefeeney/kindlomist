package org.feenaboccles.kindlomist.articles.html;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.articles.content.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Contains convenience methods for parsing pages from Economist.com
 */
public class AbstractArticleParser {


	@Data
	@AllArgsConstructor
	protected
	static class ArticleHeader
	{	String title;
		String topic;
		String strap;
	}


	protected static final String CONTENT_IMAGE_DIV_CLASS_PREFIX = "content-image-";
	protected static final String MAIN_IMAGE_DIV_CLASS = CONTENT_IMAGE_DIV_CLASS_PREFIX + "full";
	protected static final int EXPECTED_IMAGE_COUNT = PlainArticle.MAX_IMAGES_PER_ARTICLE / 2;
	protected static final int EXPECTED_PARAGRAPH_COUNT = 10;

	private static final int FOOTNOTES_PER_PARAGRAPH = 2;
	private final static String UNBOLDED_PUNC_CHARS = ":;,,.\"´‘’'“”(){}[]’.%…!? \t\n\r";

	
	public AbstractArticleParser() {
		super();
	}

	/**
	 * Is this image in the body of an article (see {@link #findArticleDiv(Document)}
	 * a part of the content or an ad
	 */
	public boolean isContentImageDivClass (String cssClass) {
		return cssClass.startsWith(CONTENT_IMAGE_DIV_CLASS_PREFIX) && ! cssClass.equals (MAIN_IMAGE_DIV_CLASS);
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
	 *
	 * Returns the image URI and it's caption. Obviously there can be no caption
	 * if there's no image, but there may be an image with no caption
	 */
	protected Pair<Optional<URI>, Optional<String>> readMainImage(Element bodyDiv) throws URISyntaxException {
		
		URI mainImage = null;
		String caption = null;

		Elements mainImg = bodyDiv.select("div." + MAIN_IMAGE_DIV_CLASS);
		if (! mainImg.isEmpty()) {
			Elements img = mainImg.first().getElementsByTag("img");
			if (! img.isEmpty())
				mainImage = new URI(img.first().attr("src"));

			Elements captionCandidates = mainImg.first().getElementsByTag("span");
			for (Element capCand : captionCandidates) {
				if (capCand.hasClass("caption")) {
					caption = capCand.ownText();
				}
			}
		}
		return Pair.of(Optional.ofNullable(mainImage), Optional.ofNullable(caption));
	}

	/**
	 * Finds within the document the title, topic and strap associated with
	 * an article, and return them all together as a single {@link ArticleHeader}
	 * object
	 */
	protected ArticleHeader readHeaders(Document doc) {
		// Parse the title and the strap
		Element hgroup = doc.getElementsByTag("hgroup").first();
		
		String  title  = childTagText (hgroup, "h3", "");
		String  topic  = childTagText(hgroup, "h2", "");
		String  strap  = childTagText(hgroup, "h1", "");
		
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
	private static String childTagText(Element parent, String childTag, String defaultValue) {
		Elements es = parent.getElementsByTag(childTag);
		return es.isEmpty()
			? defaultValue
			: es.first().text().trim();
	}
	
	/**
	 * Checks if a given paragraph tag, despite not containing any CSS, is in 
	 * fact intended to be a heading, by considering its styling. Evidently
	 * some Economist authors just prefer to hit the bold button than the
	 * heading button.
	 */
	private boolean isHeadingInBoldTag (Element elem, String paraText) {
		// sometimes you see headings like "<strong>Sources</strong>:"
		paraText = StringUtils.strip(paraText, UNBOLDED_PUNC_CHARS);
		
		Elements strongs = elem.getElementsByTag("strong");
		if (! strongs.isEmpty()) {
			String strongText = clean(strongs.first().text());
			strongText = StringUtils.strip(strongText, UNBOLDED_PUNC_CHARS);
		
			if (strongText.equals(paraText)) {
				return true;
			}
		}

		Elements bolds = elem.getElementsByTag("b");
		if (! bolds.isEmpty()) {
			String boldText = clean(bolds.first().text());
			boldText = StringUtils.strip(boldText, UNBOLDED_PUNC_CHARS);
		
			if (boldText.equals(paraText)) {
				return true;
			}
		}
		
		return false;
	}


	/**
	 * Reads the {@link Content} of an article from the given
	 * DIV.
	 */
	protected List<Content> readContent(Element bodyDiv, boolean convertShortTextToHeading, boolean permitLetterAuthor) {
		List<Content> content = new ArrayList<>(EXPECTED_IMAGE_COUNT + EXPECTED_PARAGRAPH_COUNT);
		
		int maxAllowedFootnotes = 0; // allow FOOTNOTES_PER_PARAGRAPH per textual paragraph.
		
		// Extract the body text, and any other images.
		String paraText;
		for (Element element : bodyDiv.children()) {
			if (element.nodeName().equalsIgnoreCase("p") && (! (paraText = clean(element.text())).isEmpty())) {
				if (element.className().equals ("xhead")
						|| isHeadingInBoldTag(element, paraText)
						|| (convertShortTextToHeading && paraText.length() < Text.MIN_TEXT_LEN)) {
					content.add (new SubHeading (paraText));
				}
				else { // check for a footnote, should all be in a <sup> tag
					Elements sups = element.getElementsByTag("sup");
					if (sups.isEmpty()) {
						content.add(textOrAuthor(paraText, permitLetterAuthor));
						maxAllowedFootnotes += FOOTNOTES_PER_PARAGRAPH;
					}
					else {
						String supText = clean(sups.first().text());
						if (supText.equals(paraText)) { // the entire paragraph text is in the <sup> tag
							if (sups.first().hasClass("pullquote")) {
								content.add (new PullQuote(supText));
							}
							else {
								content.add(new Footnote(supText));
							}
						}
						else {
							content.add (textOrAuthor(paraText, permitLetterAuthor));
						}
					}
				}
			}
			else if (element.nodeName().equalsIgnoreCase("div") && isContentImageDivClass(element.className())) {
				Elements imgs = element.getElementsByTag("img");
				if (! imgs.isEmpty())
					content.add (new Image (imgs.first().attr("src")));
			}
		}
		
		detectAndEncodeFootnotesMissingMarkup(content, maxAllowedFootnotes);
		
		return content;
	}

	/**
	 * Having verified that the given content is neither a {@link Footnote} nor
	 * a {@link SubHeading}, determine if it's the name of an individual who wrote
	 * a letter to the Economist ({@link  LetterAuthor} or just plain {@link Text}
	 * and return as appropriate. If <tt>permitLetterAuthor</tt> is false, don't
	 * do the check, just convert it to {@link Text}
	 */
	private final Content textOrAuthor (String text, boolean permitLetterAuthor) {
		return permitLetterAuthor
				? text.matches(LetterAuthor.REGEX)
					? new LetterAuthor(text)
					: new Text(text)
				: new Text(text);
	}

	/**
	 * A workaround for the case where there's a rider at the bottom,
	 * that's essentially a footnote, but which is not annotated by a &lt;sup&gt;
	 * tag. We basically recognize footnotes by their shortness, and ensure
	 * that the only thing that can follow a footnote is either another 
	 * footnote or the end of the content
	 * <p>
	 * Note this is an <strong>in-place</strong> transform.
	 * @param content the list of content elements to inspect and maybe
	 * transform (this is mutated in place).
	 * @param maxAllowedFootnotes the maximum allowed footnotes given the content
	 * that has been read in.
	 */
	private void detectAndEncodeFootnotesMissingMarkup(List<Content> content,
			int maxAllowedFootnotes) {
		ListIterator<Content> iter = content.listIterator(content.size());
		
		footnoteLoop:while (iter.hasPrevious() && maxAllowedFootnotes > 0)
		{	Content c = iter.previous();
			switch (c.getType()) {
			case FOOTNOTE:    continue footnoteLoop;
			case LETTER_AUTHOR:
			case SUB_HEADING:
			case IMAGE:       break footnoteLoop; // Footnotes can't come before images, headings or authors
			case TEXT:
				String text = c.getContent();
				if (text.length() >= Text.MIN_TEXT_LEN) {
					break footnoteLoop; // footnotes can't come before valid paragraphs
				} else {
					iter.set(new Footnote(text));
					maxAllowedFootnotes -= (1 + FOOTNOTES_PER_PARAGRAPH);
				}
				break;
			default:
				throw new IllegalStateException ("No code has been written to handle the new content type " + c.getType());
			}
		}
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