package org.feenaboccles.kindlomist.articles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.validation.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class PlainArticleTest {
	
	private final static String SAMPLE_TITLE = "My first article";
	private final static String SAMPLE_STRAP = "This is a quick article about somethign small";
	private final static String SAMPLE_BODY  = "WHEN the first mobile phone call was made in 1973, few members of the public were interested in the new technology. Telecoms companies had to invent reasons to use them—for instance, that they could be used to call a friend to pass the time when stuck in a car during a traffic jam—in order to get sceptical consumers to adopt them. But now, most people in rich countries could not imagine life without one: there are now more active mobile-phone connections in America and Europe than people.\n\nThe rising importance of mobiles—not simply to make calls but to access the internet as well—partly explains why BT, a fixed-line telecoms firm, decided to make a £12.5 billion ($19.6 billion) bid for EE, Britain's biggest mobile operator, on December 15th. BT also hopes that the merger will allow the firm to profitably offer what is know as “quad-play” (a bundle of fixed and mobile phone calls, internet access and television), which will also help keep customers from switching away from its other products.";
	
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
						.images(Collections.emptyList())
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
						.build().validate();
		
		assertEquals (SAMPLE_TITLE, a.getTitle());
		assertEquals (SAMPLE_STRAP, a.getStrap());
		assertEquals (SAMPLE_BODY, a.getBody());
	}
	
	
	@Test
	@SuppressWarnings("unused")
	public void testInvalidTitle() throws ValidationException {
		String[] inputs = new String[] { null, "", " ", "ssd", "😉😗😛😌😢😥😩😠😆", SAMPLE_BODY };
		String[] issues = new String[] {"null", "empty", "too short", "too short", "invalid characters", "too long" };
		
		assertEquals (inputs.length, issues.length);
		
		for (int i = 0; i < inputs.length; i++) {
			try {
				PlainArticle a = PlainArticle.builder()
							.title(inputs[i])
							.strap(SAMPLE_STRAP)
							.body(SAMPLE_BODY)
							.images(URLS)
							.build().validate();
				fail ("Failed to invalidate a title which was " + issues[i]);
			}
			catch (ValidationException e) { ; }
		}
	}	
	
	@Test
	@SuppressWarnings("unused")
	public void testInvalidStrap() throws ValidationException {
		String[] inputs = new String[] { null,   "",     " ",         "ssd",       "😉😗😛😌😢😥😩😠😆",  SAMPLE_BODY };
		String[] issues = new String[] {"null", "empty", "too short", "too short", "invalid characters", "too long" };
		
		assertEquals (inputs.length, issues.length);
		
		for (int i = 0; i < inputs.length; i++) {
			try {
				PlainArticle a = PlainArticle.builder()
							.title(SAMPLE_TITLE)
							.strap(inputs[i])
							.body(SAMPLE_BODY)
							.images(URLS)
							.build().validate();
				fail ("Failed to invalidate a strap which was " + issues[i]);
			}
			catch (ValidationException e) { ; }
		}
	}	
	

	@Test
	@SuppressWarnings("unused")
	public void testInvalidBody() throws ValidationException {
		String[] inputs = new String[] { null,   "",     " ",         "ssd",       SAMPLE_STRAP, "😉😗😛😌😢😥😩😠😆",  StringUtils.repeat("01234567890", 5000) };
		String[] issues = new String[] {"null", "empty", "too short", "too short", "too short",  "invalid characters", "too long" };
		
		assertEquals (inputs.length, issues.length);
		
		for (int i = 0; i < inputs.length; i++) {
			try {
				PlainArticle a = PlainArticle.builder()
							.title(SAMPLE_TITLE)
							.strap(SAMPLE_STRAP)
							.body(inputs[i])
							.images(URLS)
							.build().validate();
				fail ("Failed to invalidate a body which was " + issues[i]);
			}
			catch (ValidationException e) { ; }
		}
	}
	
	@Test
	@SuppressWarnings("unused")
	public void testInvalidUris() throws ValidationException, URISyntaxException {
		try {
			PlainArticle a = PlainArticle.builder()
							.title(SAMPLE_TITLE)
							.strap(SAMPLE_STRAP)
							.body(SAMPLE_BODY)
							.images(PlainArticle.toUriList(Arrays.<String>asList(new String[] {
								"http://www.google.com"
							})))
							.build().validate();
			
			fail ("Failed to detect an image URL exiting the Economist's domain");
		}
		catch (ValidationException e) { ; }
		
		try {
			PlainArticle a = PlainArticle.builder()
							.title(SAMPLE_TITLE)
							.strap(SAMPLE_STRAP)
							.body(SAMPLE_BODY)
							.images(null)
							.build().validate();
			
			fail ("Failed to detect an a null images list");
		}
		catch (ValidationException e) { ; }
		
		List<URI> dupes = new ArrayList<URI>(URLS.size() * 2);
		dupes.addAll(URLS);
		dupes.addAll(URLS);
		
		try {
			PlainArticle a = PlainArticle.builder()
							.title(SAMPLE_TITLE)
							.strap(SAMPLE_STRAP)
							.body(SAMPLE_BODY)
							.images(dupes)
							.build().validate();
			
			fail ("Failed to detect duplicated image URLs");
		}
		catch (ValidationException e) { ; }
		
		int tooMany = PlainArticle.MAX_IMAGES_PER_ARTICLE + 1;
		ArrayList<URI> tooManyImages = new ArrayList<URI>(tooMany);
		for (int i = 0; i < tooMany; i++)
			tooManyImages.add (new URI("http://cdn.static-economist.com/images/" + i + ".png"));
		
		try {
			PlainArticle a = PlainArticle.builder()
							.title(SAMPLE_TITLE)
							.strap(SAMPLE_STRAP)
							.body(SAMPLE_BODY)
							.images(tooManyImages)
							.build().validate();
			
			fail ("Failed to detect too many image URLs");
		}
		catch (ValidationException e) { ; }
	}
}
