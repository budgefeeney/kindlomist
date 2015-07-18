package org.feenaboccles.kindlomist.articles.content;

import lombok.NonNull;
import lombok.Value;
import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.valid.Validator;
import org.hibernate.validator.constraints.Length;

import javax.validation.ValidationException;
import javax.validation.constraints.Pattern;

@Value
public class PullQuote implements Content {

	private static final long serialVersionUID = 1L;

	@NonNull @Length(min=10, max=200) @Pattern(regexp=PlainArticle.ECONOMIST_VISIBLE_TEXT)
	String content;

	@Override
	public PullQuote validate() throws ValidationException {
		Validator.INSTANCE.validate(this, "pull-quote content");
		return this;
	}
	
	@Override
	public Type getType() {
		return Type.PULL_QUOTE;
	}

}

