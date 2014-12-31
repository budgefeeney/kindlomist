package org.feenaboccles.kindlomist.articles;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Value;

import org.feenaboccles.kindlomist.articles.content.Content;
import org.feenaboccles.kindlomist.articles.content.Image;
import org.feenaboccles.kindlomist.valid.Validator;

/**
 * An article with a number of short paragraphs, being a digest of the
 * week's stories. Used for "Politics this Week" and "Business this Week"
 */
@Value
public class WeeklyDigestArticle implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final int MAX_IMAGES_PER_DIGEST = 20;
	
	@NotNull @Size(min=1, max=100)
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
		
		int imageCount = 0;
		for (Content content : body) {
			content.validate();
			if (content instanceof Image)
				imageCount++;
		}
		if (imageCount > MAX_IMAGES_PER_DIGEST)
			throw new ValidationException("The number of images in this weekly digest (" + imageCount + ") exceeds the maximum allowed " + MAX_IMAGES_PER_DIGEST);
		
		return this;
	}
}
