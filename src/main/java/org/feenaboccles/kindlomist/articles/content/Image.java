package org.feenaboccles.kindlomist.articles.content;

import java.net.URI;
import java.net.URISyntaxException;

import javax.validation.ValidationException;

import lombok.NonNull;
import lombok.Value;

import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.valid.Validator;
import org.hibernate.validator.constraints.Length;

@Value
public final class Image implements Content {

	private static final long serialVersionUID = 1L;

	@NonNull @Length(min = 10, max=1000)
	String content;

	@Override
	public Image validate() throws ValidationException {
		Validator.INSTANCE.validate(this, "image URL");
		
		try {
			if (! new URI(content).getHost().equals (PlainArticle.ECONOMIST_IMAGE_CDN))
				throw new ValidationException("The given image URL  - " + content + " - is not hosted by the Economist CDN");
		}
		catch (URISyntaxException e) {
			throw new ValidationException ("The given image URL  - " + content + " - does not constitute a valid URL : " + e.getMessage(), e);
		}
		
		return this;
	}
	
	@Override
	public Type getType() {
		return Type.IMAGE;
	}

}
