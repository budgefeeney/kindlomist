package org.feenaboccles.kindlomist.articles;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Builder;

import org.feenaboccles.kindlomist.articles.content.Content;
import org.feenaboccles.kindlomist.valid.Validator;
import org.hibernate.validator.constraints.Length;

/**
 * A plain Economist article.
 */
@Value
@Builder
public class PlainArticle implements Article, MainImageArticle, ContentBasedArticle {
	private static final long serialVersionUID = 1L;
	
	public static final String ECONOMIST_IMAGE_CDN = "cdn.static-economist.com";
	public static final int MAX_IMAGES_PER_ARTICLE = 10;
	public final static String ECONOMIST_VISIBLE_TEXT = "[\\p{Sc}\\p{IsLatin}\\d \\n:;,,\\+\\-\\-—\u2013\\.\"´‘’'“”()\\{\\}\\[\\]’\\.%…!\\?&\\*/\\\\½⅓⅔¼¾⅛⅜⅝⅞†\u02da#]+";
	
	@NonNull
	URI articleUri;
	
	@NotNull @Length(min=4, max=80) @Pattern(regexp=ECONOMIST_VISIBLE_TEXT)
	String title;
	
	@NotNull @Length(min=3, max=80) @Pattern(regexp=ECONOMIST_VISIBLE_TEXT)
	String topic;
	
	@NotNull @Length(min=4, max=160) @Pattern(regexp=ECONOMIST_VISIBLE_TEXT)
	String strap;
	
	@NotNull @Size(min=1, max=100)
	List<Content> body;
	
	URI mainImage;
	
	
	/** 
	 * Checks if this is a valid plain article, throws an exception otherwise
	 * @see Validator#validate(Object, String)
	 * @throws IllegalArgumentException
	 */
	public PlainArticle validate() throws ValidationException
	{	Validator.INSTANCE.validate(this, "article");
		
		if (mainImage != null && ! mainImage.getHost().equals(ECONOMIST_IMAGE_CDN))
			throw new ValidationException("Invalid article: \n\tThe main image URL - " + mainImage + " - accesses an unexpected host.");
		
		if (new HashSet<>(body).size() != body.size())
			throw new ValidationException("Duplicate images or paragraphs in this article");
		
		int imageCount = 0;
		boolean foundFootnote = false;
		for (Content content : body) {
			content.validate();
			if (foundFootnote && content.getType() != Content.Type.FOOTNOTE)
				throw new ValidationException ("Standard content found after the first footnote");
			if (content.getType() == Content.Type.IMAGE)
				imageCount++;
			
			foundFootnote |= content.getType() == Content.Type.FOOTNOTE;
		}
		if (imageCount > MAX_IMAGES_PER_ARTICLE)
			throw new ValidationException("The number of images in this article (" + imageCount + ") exceeds the maximum allowed " + MAX_IMAGES_PER_ARTICLE);
		
		return this;
	}

	
	/**
	 * Converts a list of Strings to a list of URIs. 
	 * @throws URISyntaxException 
	 * 
	 * @todo Consider moving to a different class
	 */
	public static List<URI> toUriList (Collection<String> urlStrings) throws URISyntaxException
	{	List<URI> result = new ArrayList<URI>(urlStrings.size());
		for (String urlString : urlStrings)
			result.add(new URI(urlString));
		
		return result;
	}
}
