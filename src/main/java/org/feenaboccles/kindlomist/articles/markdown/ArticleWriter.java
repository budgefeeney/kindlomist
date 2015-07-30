package org.feenaboccles.kindlomist.articles.markdown;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.NonNull;

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
// TODO Escape all markdown characters from content.
@Log4j2
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
			article.getMainImage(),
			article.getBody()
		);
	}

	public static void writeSingleArticleSection (@NonNull Writer writer,
												  @NonNull ImageResolver images,
												  @NonNull PlainArticle article) throws IOException {
		write(
			writer,
			images,
			article.getTopic() + ": " + article.getTitle(),
			1,
			Optional.of(article.getStrap()),
			article.getMainImage(),
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
							  @NonNull String   title,
							  @NonNull SingleImageArticle article) throws IOException {
		if (StringUtils.isBlank(title))
			throw new IllegalArgumentException("Can't have an empty title");
		write(
			writer,
			images,
			title,
			Optional.empty(),
			article.getMainImage(),
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
							  @NonNull String   title,
							  @NonNull WeeklyDigestArticle article) throws IOException {
		if (StringUtils.isBlank(title))
			throw new IllegalArgumentException("Can't have an empty title");
		write(
				writer,
				images,
				title,
				Optional.empty(),
				Optional.empty(),
				article.getBody()
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
		write(writer, images, title, 2, strap, mainImage, contents);
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
	 * @param titleRank should the title be 1st, 2nd etc.
	 * @param strap  the article's strap
	 * @param mainImage the article's main image
	 * @param contents the articles contents, may be null.
	 * @throws IOException
	 */
	private static void write (@NonNull Writer writer,
				@NonNull ImageResolver images,
				@NonNull String title,
						 int titleRank,
				@NonNull Optional<String> strap,
				@NonNull Optional<URI> mainImage,
				@NonNull List<Content> contents) throws IOException {

		String titleMarkDown = StringUtils.repeat('#', titleRank);
		writer.write(titleMarkDown + " " + title + "\n\n");
		if (strap.isPresent())
			writer.write ("**" + strap.get() + "**\n\n");
		if (mainImage.isPresent())
			writeMainImage (writer, images, mainImage.get());
		if (! contents.isEmpty())
			for (Content contentItem : contents)
				writeContent (writer, images, contentItem);
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
	private static void writeContent(Writer writer, ImageResolver images, Content content) throws IOException {
		switch (content.getType()) {
			case TEXT:          writeText(writer, (Text) content); break;
			case SUB_HEADING:   writeSubHeading(writer, (SubHeading) content); break;
			case IMAGE:         writeImage(writer, images, (Image) content); break;
			case FOOTNOTE:      writeFootnote(writer, (Footnote) content); break;
			case PULL_QUOTE:    writePullQuote(writer, (PullQuote) content); break;
			case LETTER_AUTHOR: writeLetterAuthor(writer, (LetterAuthor) content); break;
			case REFERENCE:     writeReference(writer, (Reference) content); break;
			default:
				throw new IllegalStateException ("No writer is defined for content of type " + content.getType());
		}
	}

	/**
	 * Writes a piece of text content
	 */
	private static void writeText(Writer writer, Text text) throws IOException {
		writer.write(markdownEscapedContent(text));
		writer.write("\n\n");
	}

	/**
	 * Writes an image
	 */
	private static void writeImage(Writer writer, ImageResolver images, Image image) throws IOException {
		if (images.hasImage(image)) {
			Path path = images.getImagePath(image);
			writeMarkdownImageTag(writer, path, image.getContent());
		}
	}

	/**
	 * Writes a sub-heading
	 */
	private static void writeSubHeading(Writer writer, SubHeading heading) throws IOException {
		writer.write ("### " + markdownEscapedContent(heading));
		writer.write ("\n");
	}

	/**
	 * Writes a footnote
	 */
	private static void writeFootnote(Writer writer, Footnote footnote) throws IOException {
		String content = StringUtils.replace(
				markdownEscapedContent(footnote), " ", "\\ ");

		// We subscript the footnote to make it small. This is why we
		// also have to escape spaces
		writer.write('~');
		writer.write(content);
		writer.write("~\n\n");
	}

	/**
	 * Writes a footnote
	 */
	private static void writeLetterAuthor(Writer writer, LetterAuthor author) throws IOException {
		writer.write('_');
		writer.write(markdownEscapedContent(author));
		writer.write("_\n\n");
	}


	/**
	 * Writes a pull-quote
	 */
	private static void writePullQuote(Writer writer, PullQuote pullQuote) throws IOException {
		writer.write("\n> *\"");
		writer.write(markdownEscapedContent(pullQuote));
		writer.write("\"*");
		writer.write("\n\n");
	}


	/**
	 * Writes a reference
	 */
	private static void writeReference(Writer writer, Reference ref) throws IOException {
		writer.write(markdownEscapedContent(ref.getBefore()));
		writer.write(" [");
		writer.write(markdownEscapedContent(ref.getUrlText()));
		writer.write("](");
		writer.write(ref.getUrlHref());
		writer.write(") ");
		writer.write(markdownEscapedContent(ref.getAfter()));

		writer.write("\n\n");
	}

	private static String markdownEscapedContent(Content content) {
		return markdownEscapedContent(content.getContent());
	}

	private static String markdownEscapedContent(String text) {
		StringBuilder sb = new StringBuilder (text.length());
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			switch (c) {
				case '*':
				case '\\':
				case '_':
				case '>':
				case '#':
				case '(':
					sb.append('\\');
					// deliberate fallthrough
				default:
					sb.append(c);
					break;
			}
		}
		return sb.toString();
	}
}
