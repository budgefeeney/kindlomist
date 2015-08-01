package org.feenaboccles.kindlomist.articles.content;

import lombok.NonNull;
import lombok.Value;
import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.valid.Validator;
import org.hibernate.validator.constraints.Length;

import javax.validation.ValidationException;
import javax.validation.constraints.Pattern;

@Value
public class LetterAuthor implements Content {

	private static final long serialVersionUID = 1L;
	// Names are always in upper case. It can be any sequence of initials and words,
	// so long as it ends with a word
	public static final String REGEX = "(?:\\p{Lu}\\.?|\\p{Lu}{2,})+\\p{Lu}{2,}" + PlainArticle.ECONOMIST_VISIBLE_TEXT;

	@NonNull @Length(min=10, max=300) @Pattern(regexp=REGEX)
	String content;

	@Override
	public LetterAuthor validate() throws ValidationException {
		Validator.INSTANCE.validate(this, "letter author");
		return this;
	}
	
	@Override
	public Type getType() {
		return Type.LETTER_AUTHOR;
	}

}

