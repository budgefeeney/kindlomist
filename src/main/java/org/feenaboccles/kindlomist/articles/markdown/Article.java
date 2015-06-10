package org.feenaboccles.kindlomist.articles.markdown;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.Value;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.feenaboccles.kindlomist.articles.ImageResolver;
import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.articles.SingleImageArticle;
import org.feenaboccles.kindlomist.articles.WeeklyDigestArticle;
import org.feenaboccles.kindlomist.articles.content.*;

/**
 * A Markdown-writeable article
 * <p>
 * This unifies all the the disparate validated article types 
 * (see implementors of {@link org.feenaboccles.kindlomist.articles.Article})
 * and dispenses with the validation checks. Some fields are merged at
 * this point for writing (e.g. topic getting merged into title), and
 * some, such as strap, main-image and contents, may be absent.
 *
 * Since this is created from objects which have already been validated,
 * no validation is undertaken by this class.
 */
@Log4j2
@Value
public class Article {

	String           title; // never empty or null
	Optional<String> strap;
	Optional<URI>    mainImage;
	List<Content>    content; // may be an empty list
	
	
	public Article (PlainArticle article) {
		title = article.getTopic() + ": " + article.getTitle();
		strap = Optional.of(article.getStrap());
		
		mainImage = Optional.of(article.getMainImage());
		content   = article.getBody();
	}
	
	public Article (String title, SingleImageArticle article) {
		this.title = title;
		strap = Optional.empty();
		
		mainImage = Optional.of(article.getMainImage());
		content   = Collections.emptyList();
	}
	
	public Article (String title, WeeklyDigestArticle article) {
		this.title = title;
		strap = Optional.empty();
		
		mainImage = Optional.empty();
		content   = article.getBody();
	}

	/**
	 * Writes this article out to the given writer in Markdown format. For
	 * each image, a local path on disk is found using the {@link ImageResolver}
	 * and used to create a Markdown image tag
	 * @param writer the writer used to write document to
	 * @param images the image resolver, used to match image URIs to local
	 *               paths on disk
	 * @throws IOException
	 */
	void write (Writer writer, ImageResolver images) throws IOException {
		writer.write("## " + title + "\n\n");
		if (strap.isPresent())
			writer.write ("**" + strap.get() + "**\n\n");
		if (mainImage.isPresent())
			writeMainImage (writer, images, mainImage.get());
		if (! content.isEmpty())
			for (Content contentItem : content)
				writeContent (contentItem);
	}

	/**
	 * Writes out the main image as an appropriate Markdown tag
	 * @param writer the writer used to write document to
	 * @param images the image resolver, used to match image URIs to local
	 *               paths on disk.
	 * @param image  the actual image to write out
	 * @throws IOException
	 */
	void writeMainImage(Writer writer, ImageResolver images, URI image) throws IOException {
		if (image != null) {
			Path path = images.getImagePath(image);
			writeMarkdownImageTag(writer, path, image);
		}
	}

	/**
	 * Writes a markdown tag for an image at a given path. The source is the
	 * actual URI of the image downloaded to that path, and can be either a
	 * {@link URI} or a URI wrapped in an {@link Image} tag
	 *
	 * @throws IOException
	 */
	private void writeMarkdownImageTag(Writer writer, Path path, Object source) throws IOException {
		if (path == null) {
			log.warn("No path for image at " + String.valueOf(source));
		} else {
			writer.write ("\n![](" + path.toString() + ")\n\n");
		}
	}

	/**
	 * Writes out the element of content from an article
	 */
	void writeContent(Content content) throws IOException {
		switch (content.getType()) {
			case TEXT:        writeContent ((Text)       content); break;
			case SUB_HEADING: writeContent ((SubHeading) content); break;
			case IMAGE:       writeContent ((Image)      content); break;
			case FOOTNOTE:    writeContent ((Footnote)   content); break;
			default:
				throw new IllegalStateException ("No writer is defined for content of type " + content.getType());
		}
	}

	/**
	 * Writes a piece of text content
	 */
	void writeContent(Writer writer, Text text) throws IOException {
		writer.write(text.getContent());
		writer.write("\n\n");
	}

	/**
	 * Writes an image
	 */
	void writeContent(Writer writer, ImageResolver images, Image image) throws IOException {
		if (images.hasImage(image)) {
			Path path = images.getImagePath(image);
			writeMarkdownImageTag(writer, path, image.getContent());
		}
	}

	/**
	 * Writes a sub-heading
	 */
	void writeContent(Writer writer, SubHeading heading) throws IOException {
		writer.write ("### " + heading.getContent());
		writer.write ("\n");
	}

	/**
	 * Writes a footnote
	 */
	void writeContent(Writer writer, Footnote footnote) throws IOException {
		String content = StringUtils.replace(
				footnote.getContent(), " ", "\\ ");

		// We subscript the footnote to make it small. This is why we
		// also have to escape spaces
		writer.write('~');
		writer.write(content);
		writer.write("~\n\n");
	}
	
}
