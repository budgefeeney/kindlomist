package org.feenaboccles.kindlomist.articles.markdown;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.NonNull;
import lombok.Value;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.feenaboccles.kindlomist.articles.ImageResolver;
import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.articles.SingleImageArticle;
import org.feenaboccles.kindlomist.articles.WeeklyDigestArticle;
import org.feenaboccles.kindlomist.articles.content.*;

/**
 * Provides methods to write out articles in Markdown format.
 * <p>
 * Since this is created from objects which have already been validated,
 * no validation is undertaken by this class.
 */
@Log4j2
@Value
public class ArticleWriter {

	private ArticleWriter() { }


	/**
	 * Writes the article out to the given writer in Markdown format. For
	 * each image, a local path on disk is found using the {@link ImageResolver}
	 * and used to create a Markdown image tag
	 * @param writer the writer used to write document to
	 * @param images the image resolver, used to match image URIs to local
	 *               paths on disk
	 * @param article the article to write out
	 */
	public static void write (@NonNull Writer writer,
						@NonNull ImageResolver images,
						@NonNull PlainArticle article) throws IOException {
		write(
			writer,
			images,
			article.getTopic() + ": " + article.getTitle(),
			Optional.of(article.getStrap()),
			Optional.of(article.getMainImage()),
			article.getBody()
		);
	}


	/**
	 * Writes the article out to the given writer in Markdown format. For
	 * each image, a local path on disk is found using the {@link ImageResolver}
	 * and used to create a Markdown image tag
	 * @param writer the writer used to write document to
	 * @param images the image resolver, used to match image URIs to local
	 *               paths on disk
	 * @param title the title to give this article when writing it out,
	 *              (e.g. "KAL's Cartoon")
	 * @param article the article to write out
	 */
	public static void write (@NonNull Writer writer,
							  @NonNull ImageResolver images,
							  String   title,
							  @NonNull SingleImageArticle article) throws IOException {
		write(
			writer,
			images,
			title,
			Optional.empty(),
			Optional.of(article.getMainImage()),
			Collections.emptyList()
		);
	}

	/**
	 * Writes the article out to the given writer in Markdown format. For
	 * each image, a local path on disk is found using the {@link ImageResolver}
	 * and used to create a Markdown image tag
	 * @param writer the writer used to write document to
	 * @param images the image resolver, used to match image URIs to local
	 *               paths on disk
	 * @param title the title to give this article when writing it out,
	 *              (e.g. "Politics this Week")
	 * @param article the article to write out
	 */
	public static void write (@NonNull Writer writer,
							  @NonNull ImageResolver images,
							  String   title,
							  @NonNull WeeklyDigestArticle article) throws IOException {
		write(
				writer,
				images,
				title,
				Optional.empty(),
				Optional.empty(),
				Collections.emptyList()
		);
	}


	/**
	 * Writes this article out to the given writer in Markdown format. For
	 * each image, a local path on disk is found using the {@link ImageResolver}
	 * and used to create a Markdown image tag
	 * @param writer the writer used to write document to
	 * @param images the image resolver, used to match image URIs to local
	 *               paths on disk
	 * @param title  the article title, usually a combinator of Economist "topic"
	 *               and title components
	 * @param strap  the article's strap
	 * @param mainImage the article's main image
	 * @param contents the articles contents, may be null.
	 * @throws IOException
	 */
	private static void write (@NonNull Writer writer,
				@NonNull ImageResolver images,
				@NonNull String title,
				@NonNull Optional<String> strap,
				@NonNull Optional<URI> mainImage,
				@NonNull List<Content> contents) throws IOException {
		writer.write("## " + title + "\n\n");
		if (strap.isPresent())
			writer.write ("**" + strap.get() + "**\n\n");
		if (mainImage.isPresent())
			writeMainImage (writer, images, mainImage.get());
		if (! contents.isEmpty())
			for (Content contentItem : contents)
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
	private static void writeMainImage(Writer writer, ImageResolver images, URI image) throws IOException {
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
	private static  void writeMarkdownImageTag(Writer writer, Path path, Object source) throws IOException {
		if (path == null) {
			log.warn("No path for image at " + String.valueOf(source));
		} else {
			writer.write("\n![](" + path.toString() + ")\n\n");
		}
	}

	/**
	 * Writes out the element of content from an article
	 */
	private static void writeContent(Content content) throws IOException {
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
	private static void writeContent(Writer writer, Text text) throws IOException {
		writer.write(text.getContent());
		writer.write("\n\n");
	}

	/**
	 * Writes an image
	 */
	private static void writeContent(Writer writer, ImageResolver images, Image image) throws IOException {
		if (images.hasImage(image)) {
			Path path = images.getImagePath(image);
			writeMarkdownImageTag(writer, path, image.getContent());
		}
	}

	/**
	 * Writes a sub-heading
	 */
	private static void writeContent(Writer writer, SubHeading heading) throws IOException {
		writer.write ("### " + heading.getContent());
		writer.write ("\n");
	}

	/**
	 * Writes a footnote
	 */
	private static void writeContent(Writer writer, Footnote footnote) throws IOException {
		String content = StringUtils.replace(
				footnote.getContent(), " ", "\\ ");

		// We subscript the footnote to make it small. This is why we
		// also have to escape spaces
		writer.write('~');
		writer.write(content);
		writer.write("~\n\n");
	}
	
}
