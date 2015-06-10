package org.feenaboccles.kindlomist.articles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import javax.validation.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.feenaboccles.kindlomist.articles.content.Content;
import org.feenaboccles.kindlomist.articles.content.Image;
import org.feenaboccles.kindlomist.articles.content.SubHeading;
import org.feenaboccles.kindlomist.articles.content.Text;
import org.junit.Test;

public class PlainArticleTest {
	
	private final static String SAMPLE_TITLE = "My first article";
	private final static String SAMPLE_TOPIC = "Stuff";
	private final static String SAMPLE_STRAP = "This is a quick article about somethign small";
	private final static List<Content> SAMPLE_BODY  = Arrays.asList(new Text ("WHEN the first mobile phone call was made in 1973, few members of the public were interested in the new technology. Telecoms companies had to invent reasons to use them—for instance, that they could be used to call a friend to pass the time when stuck in a car during a traffic jam—in order to get sceptical consumers to adopt them. But now, most people in rich countries could not imagine life without one: there are now more active mobile-phone connections in America and Europe than people."),
			new SubHeading("Rising Importance"),
			new Image("http://cdn.static-economist.com/sites/default/files/imagecache/original-size/20141220_FNC572.png"),
			new Text ("The rising importance of mobiles—not simply to make calls but to access the internet as well—partly explains why BT, a fixed-line telecoms firm, decided to make a £12.5 billion ($19.6 billion) bid for EE, Britain's biggest mobile operator, on December 15th. BT also hopes that the merger will allow the firm to profitably offer what is know as “quad-play” (a bundle of fixed and mobile phone calls, internet access and television), which will also help keep customers from switching away from its other products."));
	private final static URI MAIN_IMAGE =  URI.create("http://cdn.static-economist.com/sites/default/files/imagecache/original-size/20141220_FNC572.png");

		
	
	@Test
	public void testValidNoUrlBuilder() throws ValidationException {
		PlainArticle a = PlainArticle.builder()
						.title(SAMPLE_TITLE)
						.topic(SAMPLE_TOPIC)
						.strap(SAMPLE_STRAP)
						.body(SAMPLE_BODY)
						.mainImage(null)
						.build().validate();
		
		assertEquals (SAMPLE_TITLE, a.getTitle());
		assertEquals (SAMPLE_STRAP, a.getStrap());
		assertEquals (SAMPLE_BODY, a.getBody());
	}
	
	@Test
	public void testValidWithUrlBuilder() throws ValidationException {
		PlainArticle a = PlainArticle.builder()
						.title(SAMPLE_TITLE)
						.topic(SAMPLE_TOPIC)
						.strap(SAMPLE_STRAP)
						.body(SAMPLE_BODY)
						.mainImage(Optional.of(MAIN_IMAGE))
						.build().validate();
		
		assertEquals (SAMPLE_TITLE, a.getTitle());
		assertEquals (SAMPLE_STRAP, a.getStrap());
		assertEquals (SAMPLE_BODY, a.getBody());
	}
	
	
	@Test
	@SuppressWarnings("unused")
	public void testInvalidTitle() throws ValidationException {
		String[] inputs = new String[] { null, "", " ", "ssd", "😉😗😛😌😢😥😩😠😆", StringUtils.join(SAMPLE_BODY, '\n') };
		String[] issues = new String[] {"null", "empty", "too short", "too short", "invalid characters", "too long" };
		
		assertEquals (inputs.length, issues.length);
		
		for (int i = 0; i < inputs.length; i++) {
			try {
				PlainArticle a = PlainArticle.builder()
							.title(inputs[i])
							.topic(SAMPLE_TOPIC)
							.strap(SAMPLE_STRAP)
							.body(SAMPLE_BODY)
							.mainImage(Optional.of(MAIN_IMAGE))
							.build().validate();
				fail ("Failed to invalidate a title which was " + issues[i]);
			}
			catch (ValidationException e) {  }
		}
	}	
	
	@Test
	@SuppressWarnings("unused")
	public void testInvalidTopic() throws ValidationException {
		String[] inputs = new String[] { null, "", " ", "ss", "😉😗😛😌😢😥😩😠😆", StringUtils.join(SAMPLE_BODY, '\n') };
		String[] issues = new String[] {"null", "empty", "too short", "too short", "invalid characters", "too long" };
		
		assertEquals (inputs.length, issues.length);
		
		for (int i = 0; i < inputs.length; i++) {
			try {
				PlainArticle a = PlainArticle.builder()
							.title(SAMPLE_TITLE)
							.topic(inputs[i])
							.strap(SAMPLE_STRAP)
							.body(SAMPLE_BODY)
							.mainImage(Optional.of(MAIN_IMAGE))
							.build().validate();
				fail ("Failed to invalidate a title which was " + issues[i]);
			}
			catch (ValidationException e) {  }
		}
		
		// verify the small topic is okay
		PlainArticle a = PlainArticle.builder()
				.title(SAMPLE_TITLE)
				.topic("Fed")
				.strap(SAMPLE_STRAP)
				.body(SAMPLE_BODY)
				.mainImage(Optional.of(MAIN_IMAGE))
				.build().validate(); // will throw an exception if not
	}	
	
	@Test
	@SuppressWarnings("unused")
	public void testInvalidStrap() throws ValidationException {
		String[] inputs = new String[] { null,   "",     " ",         "ssd",       "😉😗😛😌😢😥😩😠😆",  StringUtils.join(SAMPLE_BODY, '\n') };
		String[] issues = new String[] {"null", "empty", "too short", "too short", "invalid characters", "too long" };
		
		assertEquals (inputs.length, issues.length);
		
		for (int i = 0; i < inputs.length; i++) {
			try {
				PlainArticle a = PlainArticle.builder()
							.title(SAMPLE_TITLE)
							.topic(SAMPLE_TOPIC)
							.strap(inputs[i])
							.body(SAMPLE_BODY)
							.mainImage(Optional.of(MAIN_IMAGE))
							.build().validate();
				fail ("Failed to invalidate a strap which was " + issues[i]);
			}
			catch (ValidationException e) {  }
		}
	}	
	

	@Test
	@SuppressWarnings("unused")
	public void testEmptyBody() throws ValidationException {
		
		try {
			PlainArticle a = PlainArticle.builder()
						.title(SAMPLE_TITLE)
						.topic(SAMPLE_TOPIC)
						.strap(SAMPLE_STRAP)
						.body(Collections.emptyList())
						.mainImage(Optional.of(MAIN_IMAGE))
						.build().validate();
			fail ("Failed to invalidate a body which had no content");
		}
		catch (ValidationException e) {  }
		
		try {
			PlainArticle a = PlainArticle.builder()
						.title(SAMPLE_TITLE)
						.topic(SAMPLE_TOPIC)
						.strap(SAMPLE_STRAP)
						.body(null)
						.mainImage(Optional.of(MAIN_IMAGE))
						.build().validate();
			fail ("Failed to invalidate a body which had null instead of a list");
		}
		catch (ValidationException e) {  }
	}
	
	
	@Test
	@SuppressWarnings("unused")
	public void testInvalidMainImage () throws ValidationException, URISyntaxException {
		try {
			PlainArticle a = PlainArticle.builder()
							.title(SAMPLE_TITLE)
							.topic(SAMPLE_TOPIC)
							.strap(SAMPLE_STRAP)
							.body(SAMPLE_BODY)
							.mainImage(Optional.of(URI.create("http://www.google.com")))
							.build().validate();
			
			fail ("Failed to detect an image URL exiting the Economist's domain");
		}
		catch (ValidationException e) {  }
		
		// Verify that no main image is okay
		PlainArticle a = PlainArticle.builder()
							.title(SAMPLE_TITLE)
							.topic(SAMPLE_TOPIC)
							.strap(SAMPLE_STRAP)
							.body(SAMPLE_BODY)
							.mainImage(null)
							.build().validate();
			
	}
	
	@Test
	@SuppressWarnings("unused")
	public void testInvalidBodyText() throws ValidationException {
		String[] inputs = new String[] { null,   "",     " ",         "ssd",       SAMPLE_STRAP, "😉😗😛😌😢😥😩😠😆",  StringUtils.repeat("01234567890", 5000) };
		String[] issues = new String[] {"null", "empty", "too short", "too short", "too short",  "invalid characters", "too long" };
		
		assertEquals (inputs.length, issues.length);
		
		for (int i = 0; i < inputs.length; i++) {
			try {
				PlainArticle a = PlainArticle.builder()
							.title(SAMPLE_TITLE)
							.topic(SAMPLE_TOPIC)
							.strap(SAMPLE_STRAP)
							.body(Arrays.asList(new Content[] { new Text(inputs[i]) } ))
							.mainImage(Optional.of(MAIN_IMAGE))
							.build().validate();
				fail ("Failed to invalidate a body paragraph which was " + issues[i]);
			}
			catch (ValidationException e)  {  }
			catch (NullPointerException e) {  }
		}
	}
	
	@Test
	@SuppressWarnings("unused")
	public void testInvalidBodySubHeading() throws ValidationException {
		String[] inputs = new String[] { null,   "",     " ",         "ssd",       "😉😗😛😌😢😥😩😠😆",  StringUtils.repeat("01234567890", 20) };
		String[] issues = new String[] {"null", "empty", "too short", "too short", "invalid characters", "too long" };
		
		assertEquals (inputs.length, issues.length);
		
		for (int i = 0; i < inputs.length; i++) {
			try {
				PlainArticle a = PlainArticle.builder()
							.title(SAMPLE_TITLE)
							.topic(SAMPLE_TOPIC)
							.strap(SAMPLE_STRAP)
							.body(Arrays.asList(new Content[] { new SubHeading(inputs[i]) } ))
							.mainImage(Optional.of(MAIN_IMAGE))
							.build().validate();
				fail ("Failed to invalidate a body paragraph which was " + issues[i]);
			}
			catch (ValidationException e)  {  }
			catch (NullPointerException e) {  }
		}
		
		// Verify shortish headings are still okay
		PlainArticle a = PlainArticle.builder()
				.title(SAMPLE_TITLE)
				.topic(SAMPLE_TOPIC)
				.strap(SAMPLE_STRAP)
				.body(Arrays.asList(new Content[] { new SubHeading("Whither Osborne") } ))
				.mainImage(Optional.of(MAIN_IMAGE))
				.build().validate(); // will throw an exception if not
	}

	
	@Test
	@SuppressWarnings("unused")
	public void testInvalidImageContent() throws ValidationException {
		String[] inputs = new String[] { null,   "",     " ",      "ssd",            "http://www.google.com" };
		String[] issues = new String[] {"null", "empty", "blank", "not a valid URL", "external URL",   };
		
		assertEquals (inputs.length, issues.length);
		
		for (int i = 0; i < inputs.length; i++) {
			try {
				PlainArticle a = PlainArticle.builder()
							.title(SAMPLE_TITLE)
							.topic(SAMPLE_TOPIC)
							.strap(SAMPLE_STRAP)
							.body(Arrays.asList(new Content[] { new Image(inputs[i]) } ))
							.mainImage(Optional.of(MAIN_IMAGE))
							.build().validate();
				fail ("Failed to invalidate a body image which was " + issues[i]);
			}
			catch (ValidationException e)  {  }
			catch (NullPointerException e) {  }
		}
		
		
		List<Content> dupes = new ArrayList<>(2);
		dupes.add(new Image(MAIN_IMAGE.toASCIIString()));
		dupes.add(new Image(MAIN_IMAGE.toASCIIString()));
		dupes.add(new Text("Lorem ispum sit dolor amet ispum sit dolor amet ispum sit dolor amet ispum sit dolor amet ispum sit dolor amet ispum sit dolor amet ispum sit dolor amet ispum sit dolor amet ispum sit dolor amet ispum sit dolor amet"));
		
		try {
			PlainArticle a = PlainArticle.builder()
							.title(SAMPLE_TITLE)
							.topic(SAMPLE_TOPIC)
							.strap(SAMPLE_STRAP)
							.body(dupes)
							.mainImage(Optional.of(MAIN_IMAGE))
							.build().validate();
			
			fail ("Failed to detect duplicated image URLs in body");
		}
		catch (ValidationException e) {  }
		
		int tooMany = PlainArticle.MAX_IMAGES_PER_ARTICLE + 1;
		ArrayList<Content> tooManyImages = new ArrayList<>(tooMany);
		for (int i = 0; i < tooMany; i++)
			tooManyImages.add (new Image("http://cdn.static-economist.com/images/" + i + ".png"));
		
		try {
			PlainArticle a = PlainArticle.builder()
							.title(SAMPLE_TITLE)
							.topic(SAMPLE_TOPIC)
							.strap(SAMPLE_STRAP)
							.body(tooManyImages)
							.mainImage(Optional.of(MAIN_IMAGE))
							.build().validate();
			
			fail ("Failed to detect too many image URLs");
		}
		catch (ValidationException e) {  }
	}
}
