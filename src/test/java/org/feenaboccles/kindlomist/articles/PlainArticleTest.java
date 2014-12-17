package org.feenaboccles.kindlomist.articles;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ValidationException;

import org.junit.Test;

public class PlainArticleTest {
	
	private final static String SAMPLE_TITLE = "My first article";
	private final static String SAMPLE_STRAP = "This is a quick article about somethign small";
	private final static String SAMPLE_BODY  = "";
	
	private final static List<URI> URLS;
	static {
		try {
			URLS = PlainArticle.toUriList(Arrays.<String>asList(new String[] {
				"http://cdn.static-economist.com/sites/default/files/imagecache/full-width/images/2014/12/articles/main/20141220_fnp503.jpg",
				"http://cdn.static-economist.com/sites/default/files/imagecache/original-size/20141220_FNC572.png"}));
		}
		catch (URISyntaxException ue)
		{	ue.printStackTrace();
			throw new IllegalStateException ("The same URLs for the test are not valid URLs: " + ue.getMessage());
		}
	}
		
	
	@Test
	public void testValidNoUrlBuilder() throws ValidationException {
		PlainArticle a = PlainArticle.builder()
						.title(SAMPLE_TITLE)
						.strap(SAMPLE_STRAP)
						.body(SAMPLE_BODY)
						.build().validate();
		
		assertEquals (SAMPLE_TITLE, a.getTitle());
		assertEquals (SAMPLE_STRAP, a.getStrap());
		assertEquals (SAMPLE_BODY, a.getBody());
	}
	
	@Test
	public void testValidWithUrlBuilder() throws ValidationException {
		PlainArticle a = PlainArticle.builder()
						.title(SAMPLE_TITLE)
						.strap(SAMPLE_STRAP)
						.body(SAMPLE_BODY)
						.images(URLS)
						.build();
		
		assertEquals (SAMPLE_TITLE, a.getTitle());
		assertEquals (SAMPLE_STRAP, a.getStrap());
		assertEquals (SAMPLE_BODY, a.getBody());
	}

}
