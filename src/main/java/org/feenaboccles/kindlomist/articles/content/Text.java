package org.feenaboccles.kindlomist.articles.content;

import java.util.regex.Matcher;

import javax.validation.ValidationException;
import javax.validation.constraints.Pattern;

import lombok.NonNull;
import lombok.Value;

import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.valid.Validator;
import org.hibernate.validator.constraints.Length;

@Value
public class Text implements Content {

	private static final long serialVersionUID = 1L;
	public static final int MIN_TEXT_LEN =  50; // TODO This is only because of the book reviews which start with the title and author of a book, really I should add a "ReviewSubject" Content.Type
	public static final int MAX_TEXT_LEN = 1500; // there is at least one article with a 1,085 character paragraph
	
	@NonNull 
	@Length(min=MIN_TEXT_LEN, max=MAX_TEXT_LEN) 
	@Pattern(regexp=PlainArticle.ECONOMIST_VISIBLE_TEXT)
	String content;

	@Override
	public Content validate() throws ValidationException {
		try {
			Validator.INSTANCE.validate(this, "text content");
		}
		catch (ValidationException e) {
			String ch = findIssue (java.util.regex.Pattern.compile(PlainArticle.ECONOMIST_VISIBLE_TEXT), content);
			if (ch != null)
				System.out.println ("Illegal text character - " + ch + "  (\\u" + Integer.toHexString(ch.codePointAt(0)) + ")");
			
			throw e;
		}
		return this;
	}
	
	private final static String findIssue(java.util.regex.Pattern pat, String str) {
		Matcher m = pat.matcher(str);
		if (! m.matches())
		{	if (str.length() == 1) {
				return str;
			}
			else {
				String left = str.substring(0, str.length() / 2);
				String rght = str.substring(str.length() / 2);
				
				String leftAns = findIssue(pat, left);
				return leftAns == null
					? findIssue(pat, rght)
					: leftAns;
			}
		}
		else {
			return null;
		}
	}
	
	@Override
	public Type getType() {
		return Type.TEXT;
	}

}

