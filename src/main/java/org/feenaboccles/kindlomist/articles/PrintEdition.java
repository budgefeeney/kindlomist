package org.feenaboccles.kindlomist.articles;

import java.io.Serializable;
import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;
import javax.validation.constraints.Size;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Builder;

import org.feenaboccles.kindlomist.download.DateStamp;
import org.feenaboccles.kindlomist.valid.Validator;

import cz.jirutka.validator.collection.constraints.EachLength;
import cz.jirutka.validator.collection.constraints.EachPattern;

/**
 * An object representing the current printed edition of the Economist
 */
@Value
@Builder
public class PrintEdition implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final String ECONOMIST_COM = "economist.com";
	public  static final int    MIN_SECTION_COUNT = 5;
	public  static final int    MAX_SECTION_COUNT = 20;
	public  static final int    MIN_SEC_NAME_LEN  = 4;
	public  static final int    MAX_SEC_NAME_LEN  = 60;
	
	
	@NonNull DateStamp dateStamp;
	@NonNull URI politicsThisWeek;
	         URI businessThisWeek; // for the xmas edition only, this is skipped
	@NonNull URI kalsCartoon;
	@NonNull URI letters;
	@NonNull URI obituary;
	
	@NonNull @Size(min=MIN_SECTION_COUNT, max=MAX_SECTION_COUNT)
	Map<String, List<URI>> sections;
	
	@NonNull @Size(min=MIN_SECTION_COUNT, max=MAX_SECTION_COUNT) 
	@EachLength(min=MIN_SEC_NAME_LEN, max=MAX_SEC_NAME_LEN) @EachPattern(regexp=PlainArticle.ECONOMIST_VISIBLE_TEXT)
	List<String> orderedSections;
	
	
	public PrintEdition validate() throws ValidationException {
		Validator.INSTANCE.validate(this, "print edition article list");
		
		// Check that the section-articles map and list of section names are consistent
		if (! new HashSet<>(orderedSections).equals (sections.keySet())) // more of a coding bug, this
			throw new ValidationException ("The ordered list of sections has " + new HashSet<>(orderedSections).size() + " distinct elements, but the map of sections to article-lists has just " + sections.size() + " elements");
		
		// Check all the URIs for images and articles lie within the Economist domain
		for (URI u : new URI[] { politicsThisWeek, businessThisWeek, kalsCartoon, letters, obituary })
			if (u != null && ! u.getHost().endsWith(ECONOMIST_COM))
				throw new ValidationException ("One of the following URLs points to a site other than the economist.com: Politics this Week, Business this Week, KAL's Cartoon, Letters and/or Obituary");
		
		for (Map.Entry<String, List<URI>> articleList : sections.entrySet())
			for (URI article : articleList.getValue())
				if (! article.getHost().endsWith (ECONOMIST_COM))
					throw new ValidationException ("One of the URLs in the " + articleList.getKey() + " section points to a site other than the economist.com");
		
		// Check that business this week is present, unless this is the Christmas issue.
		if (! isTheXmasIssue(dateStamp.asLocalDate()) && ! isThePostXmasIssue(dateStamp.asLocalDate()))
			if (businessThisWeek == null)
				throw new ValidationException("The business this week section cannot be null, except for the Christmas issue");
		
		return this;
	}

	/**
	 * Returns true if this is the Christmas issue. Amongst other things, this
	 * is the only issue not to contain a "business this week" section
	 * <p>
	 * The Xmas issue is published the last Thursday before Christmas day
	 * @param dateStamp the issue's date-stamp, in the YYYY-MM-DD format
	 */
	public static boolean isTheXmasIssue (LocalDate dateStamp) {
		if (dateStamp.getMonth() != Month.DECEMBER)
			return false;
		
		LocalDate xmas = LocalDate.of(dateStamp.getYear(), 12, 25);
		DayOfWeek xmasDayOfWeek = xmas.getDayOfWeek();
		switch (xmasDayOfWeek) {
		case MONDAY: 
		case TUESDAY:
		case WEDNESDAY:
		case THURSDAY:
			return dateStamp.isAfter (xmas.minusDays(4 + xmasDayOfWeek.getValue()));
		default:
			return dateStamp.isAfter (xmas.minusDays(xmasDayOfWeek.getValue() - 4));
		}
	}

	/**
	 * Returns true if this is the first issue after Xmas.
	 * This may not contain a "business this week" section
	 */
	static boolean isThePostXmasIssue (String dateStampText) {
		return isThePostXmasIssue(LocalDate.parse(dateStampText));
	}
	
	/**
	 * Returns true if this is the first issue after Xmas.
	 * This may not contain a "business this week" section
	 */
	public static boolean isThePostXmasIssue (LocalDate dateStamp) {
		return dateStamp.getMonth() == Month.JANUARY
			&& dateStamp.isBefore(LocalDate.of(dateStamp.getYear(), 1, 7));
	}
}
