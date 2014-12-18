package org.feenaboccles.kindlomist.articles.content;

import javax.validation.ValidationException;
import javax.validation.constraints.Pattern;

import lombok.NonNull;
import lombok.Value;

import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.valid.Validator;
import org.hibernate.validator.constraints.Length;

@Value
public class Text implements Content {

	@NonNull @Length(min=100, max=1000) @Pattern(regexp=PlainArticle.ECONOMIST_VISIBLE_TEXT)
	String content;

	@Override
	public Content validate() throws ValidationException {
		try {
			Validator.INSTANCE.validate(this, "text content");
		}
		catch (ValidationException e) {
			for (int i = 0; i < content.length(); i++)
				System.out.println (content.charAt(i) + "  \\u" + Integer.toHexString(content.codePointAt(i)));
			
			throw e;
		}
		return this;
	}
	
	@Override
	public Type getType() {
		return Type.TEXT;
	}

}
