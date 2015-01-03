package org.feenaboccles.kindlomist.articles.markdown;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.feenaboccles.kindlomist.articles.ImageResolver;
import org.feenaboccles.kindlomist.articles.content.Content;
import org.feenaboccles.kindlomist.articles.content.Footnote;
import org.feenaboccles.kindlomist.articles.content.Image;
import org.feenaboccles.kindlomist.articles.content.SubHeading;
import org.feenaboccles.kindlomist.articles.content.Text;

/**
 * Abstract class with helper methods for writing out article
 * content
 */
public class ArticleWriter {

	private final Writer writer;
	private final ImageResolver images;
	
	public ArticleWriter(Writer writer, ImageResolver images) {
		super();
		this.writer = writer;
		this.images = images;
	}
	
	void write (Article article) throws IOException {
		writer.write("## " + article.getTitle() + "\n\n");
		if (article.hasStrap())
			writer.write ("**" + article.getStrap() + "**\n\n");
		if (article.hasMainImage())
			writeMainImage (article.getMainImage());
		if (article.hasContent())
			for (Content content : article.getContent())
				writeContent (content);
	}
	
	/**
	 * Writes out the main image.
	 */
	void writeMainImage (URI image) {
		; // do nothing
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
	void writeContent(Text text) throws IOException {
		writer.write(text.getContent());
		writer.write("\n\n");
	}
	
	/**
	 * Writes an image
	 */
	void writeContent(Image image) throws IOException {
		; // TODO Actually do this.
	}
	
	/**
	 * Writes a sub-heading
	 */
	void writeContent(SubHeading heading) throws IOException {
		writer.write ("### " + heading.getContent());
		writer.write ("\n");
	}
	
	/**
	 * Writes a footnote
	 */
	void writeContent(Footnote footnote) throws IOException {
		String content = StringUtils.replace(
							footnote.getContent(), " ", "\\ ");
		
		// We subscript the footnote to make it small. This is why we
		// also have to escape spaces
		writer.write('~');
		writer.write(content);
		writer.write("~\n\n");
	}
}