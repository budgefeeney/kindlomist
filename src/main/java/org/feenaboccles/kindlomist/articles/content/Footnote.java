package org.feenaboccles.kindlomist.articles.content;

import javax.validation.ValidationException;
import javax.validation.constraints.Pattern;

import lombok.NonNull;
import lombok.Value;

import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.valid.Validator;
import org.hibernate.validator.constraints.Length;

@Value
public class Footnote implements Content {

	private static final long serialVersionUID = 1L;

	@NonNull @Length(min=10, max=300) @Pattern(regexp=PlainArticle.ECONOMIST_VISIBLE_TEXT)
	String content;

	@Override
	public Footnote validate() throws ValidationException {
		Validator.INSTANCE.validate(this, "footnote content");
		return this;
	}
	
	@Override
	public Type getType() {
		return Type.FOOTNOTE;
	}

}

