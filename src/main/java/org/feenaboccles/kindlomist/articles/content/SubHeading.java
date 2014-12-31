package org.feenaboccles.kindlomist.articles.content;

import javax.validation.ValidationException;
import javax.validation.constraints.Pattern;

import lombok.NonNull;
import lombok.Value;

import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.valid.Validator;
import org.hibernate.validator.constraints.Length;

@Value
public class SubHeading implements Content {

	private static final long serialVersionUID = 1L;

	@NonNull @Length(min=3, max=200) @Pattern(regexp=PlainArticle.ECONOMIST_VISIBLE_TEXT)
	String content;

	@Override
	public Content validate() throws ValidationException {
		Validator.INSTANCE.validate(this, "sub-heading");
		return this;
	}
	
	@Override
	public Type getType() {
		return Type.SUB_HEADING;
	}

}
