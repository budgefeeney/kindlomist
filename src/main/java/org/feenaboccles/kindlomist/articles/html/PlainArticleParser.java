package org.feenaboccles.kindlomist.articles.html;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.ValidationException;

import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.articles.content.Content;
import org.feenaboccles.kindlomist.articles.content.Image;
import org.feenaboccles.kindlomist.articles.content.SubHeading;
import org.feenaboccles.kindlomist.articles.content.Text;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Takes a HTML page representing a standard Economist article, and parses
 * it into a {@link PlainArticle} object via the {@link #parse(String)} method.
 * <p>
 * Threadsafe.
 */
public class PlainArticleParser  {

	private static final String MAIN_IMAGE_DIV_CLASS = "content-image-full";
	private static final int EXPECTED_IMAGE_COUNT = PlainArticle.MAX_IMAGES_PER_ARTICLE;
	private static final int EXPECTED_PARAGRAPH_COUNT = 10;

	public PlainArticleParser() {
		;
	}

	public PlainArticle parse(String html) throws HtmlParseException {
		List<Content> content   = new ArrayList<Content>(EXPECTED_PARAGRAPH_COUNT + EXPECTED_IMAGE_COUNT);
		URI           mainImage = null;
		
		try {
			Document doc = Jsoup.parse(html);
			
			// Parse the title and the strap
			Element hgroup = doc.getElementsByTag("hgroup").first();
			String  title  = hgroup.getElementsByTag("h3").first().text();
			String  topic  = hgroup.getElementsByTag("h2").first().text();
			String  strap  = hgroup.getElementsByTag("h1").first().text();
			
			// Extract the main article image, if any
			Element bodyDiv = doc.select("article div.main-content").first();
			Elements mainImg = bodyDiv.select("div." + MAIN_IMAGE_DIV_CLASS);
			if (! mainImg.isEmpty()) {
				Elements img = mainImg.first().getElementsByTag("img");
				if (! img.isEmpty())
					mainImage = new URI(img.first().attr("src"));
			}
			
			// Extract the body text, and any other images.
			String paraText;
			for (Element element : bodyDiv.children()) {
				if (element.nodeName() == "p" && (! (paraText = element.text().trim()).isEmpty())) {
					if (element.className().equals ("xhead"))
						content.add (new SubHeading (paraText));
					else
						content.add(new Text(paraText));
				}
				else if (element.nodeName() == "div" && ! element.className().equals(MAIN_IMAGE_DIV_CLASS)) {
					Elements imgs = element.getElementsByTag("img");
					if (! imgs.isEmpty())
						content.add (new Image (imgs.first().attr("src")));
				}
			}
			
			
			return PlainArticle.builder()
				               .title(title)
				               .topic(topic)
				               .strap(strap)
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
