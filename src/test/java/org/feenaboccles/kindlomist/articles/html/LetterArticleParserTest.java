package org.feenaboccles.kindlomist.articles.html;

import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.articles.content.Content;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by bryanfeeney on 11/07/15.
 */
public class LetterArticleParserTest {

    @Test
    public void testOnLetters() throws IOException, HtmlParseException, URISyntaxException {
        String articleText = Util.loadFromClassPath("letters-2.html");

        PlainArticle a = new LetterArticleParser().parse(PlainArticleParserTest.DUMMY_URI, articleText);

        assertEquals("Letters", a.getTitle());
        assertEquals("Our Readers Respond", a.getTopic());
        assertEquals("On prisons, the Energy Charter, coal mining, divestment, China, SNP, British Muslims, Greece", a.getStrap());
        assertFalse(a.getMainImage().isPresent());

        assertEquals(41, a.getBody().size());

        int letterCount = 0;
        for (Content content : a.getBody())
            letterCount += content.getType().equals(Content.Type.LETTER_AUTHOR) ? 1 : 0;

        assertEquals(8, letterCount);
    }
}
