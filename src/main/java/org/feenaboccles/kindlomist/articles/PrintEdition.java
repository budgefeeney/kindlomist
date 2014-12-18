package org.feenaboccles.kindlomist.articles;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;
import javax.validation.constraints.Size;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Builder;

import org.feenaboccles.kindlomist.valid.Validator;

import cz.jirutka.validator.collection.constraints.EachLength;
import cz.jirutka.validator.collection.constraints.EachPattern;

/**
 * An object representing the current printed edition of the Economist
 */
@Value
@Builder
public class PrintEdition {

	private static final String ECONOMIST_COM = "economist.com";
	@NonNull URI politicsThisWeek;
	@NonNull URI businessThisWeek;
	@NonNull URI kalsCartoon;
	@NonNull URI letters;
	
	@NonNull @Size(min=5, max=20)
	Map<String, List<URI>> sections;
	
	@NonNull @Size(min=5, max=20) 
	@EachLength(min=4, max=60) @EachPattern(regexp=PlainArticle.ECONOMIST_VISIBLE_TEXT)
	List<String> orderedSections;
	
	
	public PrintEdition validate() throws ValidationException {
		Validator.INSTANCE.validate(this, "print edition article list");
		if (! new HashSet<>(orderedSections).equals (sections.keySet())) // more of a coding bug, this
			throw new ValidationException ("The ordered list of sections has " + new HashSet<>(orderedSections).size() + " distinct elements, but the map of sections to article-lists has just " + sections.size() + " elements");
		
		for (URI u : new URI[] { politicsThisWeek, businessThisWeek, kalsCartoon, letters })
			if (! u.getHost().endsWith(ECONOMIST_COM))
				throw new ValidationException ("One of the following URLs points to a site other than the economist.com: Politics this Week, Business this Week, KAL's Cartoon, and/or Letters");
		
		for (Map.Entry<String, List<URI>> articleList : sections.entrySet())
			for (URI article : articleList.getValue())
				if (! article.getHost().endsWith (ECONOMIST_COM))
					throw new ValidationException ("One of the URLs in the " + articleList.getKey() + " section points to a site other than the economist.com");
		
		return this;
	}

}
