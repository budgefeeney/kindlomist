package org.feenaboccles.kindlomist.articles;

import java.net.URI;

import javax.validation.ValidationException;

import lombok.NonNull;
import lombok.Value;

/**
 * Used to represent articles which consist solely of an image. This is used
 * for KAL's cartoon
 */
@Value
public class SingleImageArticle implements Article, MainImageArticle {
	private static final long serialVersionUID = 1L;
	
	@NonNull
	URI articleUri;
	
	@NonNull
	URI mainImage;
	
	public SingleImageArticle validate() {
		if (! mainImage.getHost().equals(PlainArticle.ECONOMIST_IMAGE_CDN))
			throw new ValidationException("The URL for the image in this single-image article points outside the Econoimist domain : " + mainImage.toASCIIString());
		
		return this;
	}
}
