package org.feenaboccles.kindlomist.articles;

import java.net.URI;
import java.util.HashSet;
import java.util.List;

import javax.validation.ValidationException;
import javax.validation.constraints.Size;

import lombok.NonNull;
import lombok.Value;

import org.feenaboccles.kindlomist.articles.content.Content;
import org.feenaboccles.kindlomist.valid.Validator;

import static org.feenaboccles.kindlomist.articles.content.Content.Type.*;

/**
 * An article with a number of short paragraphs, being a digest of the
 * week's stories. Used for "Politics this Week" and "Business this Week"
 */
@Value
public class WeeklyDigestArticle implements Article, ContentBasedArticle {
	private static final long serialVersionUID = 1L;

	private static final int MAX_IMAGES_PER_DIGEST = 20;
	public static final double MIN_PROP_OF_DIGEST_THAT_IS_TEXT = 0.5;

	@NonNull
	URI articleUri;
	
	@NonNull @Size(min=1, max=100)
	List<Content> body;

	/** 
	 * Checks if this is a valid article, throws an exception otherwise
	 * @see Validator#validate(Object, String)
	 * @throws IllegalArgumentException
	 */
	public WeeklyDigestArticle validate() throws ValidationException
	{	Validator.INSTANCE.validate(this, "weekly digest article");
		
		if (new HashSet<>(body).size() != body.size())
			throw new ValidationException("Duplicate images or paragraphs in this article");

		int textCount  = 0;
		int imageCount = 0;
		for (Content content : body) {
			content.validate();
			if (IMAGE.equals(content.getType()))
				imageCount++;
			if (TEXT.equals(content.getType()))
				textCount++;
		}
		if (imageCount > MAX_IMAGES_PER_DIGEST)
			throw new ValidationException("The number of images in this weekly digest (" + imageCount + ") exceeds the maximum allowed " + MAX_IMAGES_PER_DIGEST);
		if (textCount / (double) body.size() < MIN_PROP_OF_DIGEST_THAT_IS_TEXT)
			throw new ValidationException("Less than 50% of the content in this weekly digest consists of text paragraphs");

		return this;
	}
}
