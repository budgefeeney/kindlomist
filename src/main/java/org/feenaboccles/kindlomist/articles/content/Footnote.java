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

	@NonNull @Length(min=10, max=300) @Pattern(regexp=PlainArticle.ECONOMIST_VISIBLE_TEXT)
	String content;

	@Override
	public Content validate() throws ValidationException {
		try {
			Validator.INSTANCE.validate(this, "footnote content");
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
		return Type.FOOTNOTE;
	}

}

