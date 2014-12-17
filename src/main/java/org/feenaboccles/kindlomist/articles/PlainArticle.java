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

import lombok.Value;
import lombok.experimental.Builder;

import org.feenaboccles.kindlomist.valid.Validator;
import org.hibernate.validator.constraints.Length;

/**
 * A plain Economist article.
 */
@Value
@Builder
public class PlainArticle 
{
	
	static final String ECONOMIST_IMAGE_CDN = "cdn.static-economist.com";

	public static final int MAX_IMAGES_PER_ARTICLE = 10;

	final static String ECONOMIST_VISIBLE_TEXT = "[\\p{Sc}\\p{IsLatin}\\d \\n:;,\\-—\\.'“”()\\[\\]]+";
	
	@NotNull @Length(min=4, max=80) @Pattern(regexp=ECONOMIST_VISIBLE_TEXT)
	String title;
	
	@NotNull @Length(min=4, max=160) @Pattern(regexp=ECONOMIST_VISIBLE_TEXT)
	String strap;
	
	@NotNull @Length(min=100, max=50000) @Pattern(regexp=ECONOMIST_VISIBLE_TEXT)
	String body;
	
	@NotNull @Size(min=0, max=MAX_IMAGES_PER_ARTICLE) 
	List<URI> images;
	
	
	/** 
	 * Checks if this is a valid Person, throws an exception otherwise
	 * @see Validator#validate(Object, String)
	 * @throws IllegalArgumentException
	 */
	public PlainArticle validate() throws ValidationException
	{	Validator.INSTANCE.validate(this, "article");
		
		int uid = -1; for (URI u : images) {
			++uid;
			if (! u.getHost().equals(ECONOMIST_IMAGE_CDN))
				throw new ValidationException("Invalid article: \n\tThe " + uid + "-th URL of " + images.size() + " accesses an unexpected host " + u.toString());
		}
		
		if (new HashSet<URI>(images).size() != images.size())
			throw new ValidationException("Duplicate images in this article");
			
		
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
