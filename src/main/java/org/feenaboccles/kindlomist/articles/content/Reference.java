package org.feenaboccles.kindlomist.articles.content;

import lombok.NonNull;
import lombok.Value;
import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.valid.Validator;
import org.hibernate.validator.constraints.Length;

import javax.validation.ValidationException;
import javax.validation.constraints.Pattern;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A short piece of text describing a URL. Basically a reference of the form
 * "See this article"
 */
@Value
public class Reference implements Content {

    public static final int MAX_NON_URL_TEXT = 50;
    public static final int MIN_VISIBLE_TEXT_LEN = 10;

    @NonNull
    @Length(min=0, max=50)
    String before;

    @NonNull
    @Length(min=4, max=100)
    @Pattern(regexp= PlainArticle.ECONOMIST_VISIBLE_TEXT)
    String urlText;

    @NonNull
    @Length(min=20, max=150)
    @Pattern(regexp= PlainArticle.ECONOMIST_VISIBLE_TEXT)
    String urlHref;

    @NonNull
    @Length(min=0, max=50)
    String after;

    @Override
    public String getContent() {
        return before + " " + urlText + " " + after;
    }

    @Override
    public Type getType() {
        return Type.REFERENCE;
    }


    @Override
    public Reference validate() throws ValidationException {
        try {
            Validator.INSTANCE.validate(this, "reference-text content");

            String fullContent = getContent();
            if (fullContent.length() < MIN_VISIBLE_TEXT_LEN) {
                throw new ValidationException("Error validating reference-text content: not enough text either side of the URL");
            }
            if (! fullContent.matches(PlainArticle.ECONOMIST_VISIBLE_TEXT)) {
                throw new ValidationException("Error validating reference-text content: invalid character detected.");
            }
            try {
                new URI(urlHref);
            } catch (URISyntaxException e) {
                throw new ValidationException("Error validating reference-text content: the wrapped URL is not a proper URL : " + e.getMessage());
            }
        } catch (ValidationException e) {
            System.out.println(e.getMessage());
            throw e;
        }
        return this;
    }
}
