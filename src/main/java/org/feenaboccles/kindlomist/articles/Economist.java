package org.feenaboccles.kindlomist.articles;

import static org.feenaboccles.kindlomist.articles.PrintEdition.MAX_SECTION_COUNT;
import static org.feenaboccles.kindlomist.articles.PrintEdition.MAX_SEC_NAME_LEN;
import static org.feenaboccles.kindlomist.articles.PrintEdition.MIN_SECTION_COUNT;
import static org.feenaboccles.kindlomist.articles.PrintEdition.MIN_SEC_NAME_LEN;

import java.io.Serializable;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.ValidationException;
import javax.validation.constraints.Size;

import org.feenaboccles.kindlomist.articles.content.Image;
import org.feenaboccles.kindlomist.valid.Validator;

import lombok.NonNull;
import lombok.Value;
import lombok.Builder;
import cz.jirutka.validator.collection.constraints.EachLength;
import cz.jirutka.validator.collection.constraints.EachPattern;

/**
 * A fully parsed issue of the Economist
 * @author bryanfeeney
 */
@Value
@Builder
public class Economist implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final int MIN_ARTICLES_PER_SECTION = 1;

	private static final String[] CORE_SECTIONS = new String[] {
		"Leaders", "United States", "The Americas", "Asia", "China", "Middle East and Africa", 
		"Europe", "Britain", "Business", "Finance and economics", "Science and technology",
		"Books and arts"
	};

	public static final DayOfWeek PUBLICATION_DAY          = DayOfWeek.THURSDAY;
	public static final int       PUBLICATION_HOUR         = 16;
	public static final DayOfWeek DECLARED_PUBLICATION_DAY = DayOfWeek.SATURDAY;
	
	@NonNull LocalDate dateStamp;
	@NonNull WeeklyDigestArticle politicsThisWeek;
	@NonNull Optional<WeeklyDigestArticle> businessThisWeek;
	@NonNull SingleImageArticle kalsCartoon;
	@NonNull PlainArticle letters;
	
	@NonNull @Size(min=MIN_SECTION_COUNT, max=MAX_SECTION_COUNT)
	Map<String, List<PlainArticle>> sections;

	@NonNull PlainArticle obituary;
	
	@NonNull @Size(min=MIN_SECTION_COUNT, max=MAX_SECTION_COUNT) 
	@EachLength(min=MIN_SEC_NAME_LEN, max=MAX_SEC_NAME_LEN) @EachPattern(regexp=PlainArticle.ECONOMIST_VISIBLE_TEXT)
	List<String> orderedSections;
	
	@NonNull ImageResolver images;
	@NonNull Image coverImage;
	
	public Economist validate() throws ValidationException {
		Validator.INSTANCE.validate(this, "Economist issue");
		
		// Check that the section-articles map and list of section names is consistent
		if (! new HashSet<>(orderedSections).equals (sections.keySet())) // more of a coding bug, this
			throw new ValidationException ("The ordered list of sections has " + new HashSet<>(orderedSections).size() + " distinct elements, but the map of sections to article-lists has just " + sections.size() + " elements");
		
		// Check that we have an appropriate number of articles in each section.
		for (String coreSection : CORE_SECTIONS) {
			List<PlainArticle> sectionArticles = sections.get(coreSection);
			if (sectionArticles == null) 
				throw new ValidationException("The core section '" + coreSection + "' is absent in the list of sections " + sections.keySet());
			if (sectionArticles.size() < MIN_ARTICLES_PER_SECTION)
				throw new ValidationException ("The core section '" + coreSection + "' has only " + sectionArticles.size() + " articles which is less than the minimum of " + MIN_ARTICLES_PER_SECTION);
		}
		
		// TODO Check image resolver has an image for all possible images.
		
		// Check that business this week is present, unless this is the Christmas issue.
		if (! isTheXmasIssue() && ! isThePostXmasIssue())
			if (! businessThisWeek.isPresent())
				throw new ValidationException("The business this week section is missing - this is only permitted for the Christmas issue");

		// Check the cover image
		coverImage.validate();
		
		return this;
	}
	
	public boolean isTheXmasIssue() {
		return PrintEdition.isTheXmasIssue(dateStamp);
	}
	
	/**
	 * The first issue after Xmas. Like the Xmas issue, this may not
	 * have a "Business this Week" page
	 */
	private boolean isThePostXmasIssue() {
		return PrintEdition.isThePostXmasIssue(dateStamp);
	}

	/**
	 * The path to the downloaded cover image
	 */
	public Path getPathToCoverImage() {
		return images.getImagePath(coverImage);
	}
}
