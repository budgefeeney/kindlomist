package org.feenaboccles.kindlomist.articles.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.articles.content.Content;
import org.feenaboccles.kindlomist.articles.content.Image;
import org.feenaboccles.kindlomist.articles.content.SubHeading;
import org.feenaboccles.kindlomist.articles.content.Text;
import org.junit.Test;

public class PlainArticleParserTest {

	
	
	@Test
	public void testOnGoodInput() throws IOException, HtmlParseException, URISyntaxException {
		String articleText = Util.loadFromClassPath("article.html");
		
		PlainArticle a = new PlainArticleParser().parse(articleText);
		
		assertEquals ("Don’t shoot", a.getTitle());
		assertEquals ("Policing", a.getTopic());
		assertEquals ("America’s police kill too many people. But some forces are showing how smarter, less aggressive policing gets results", a.getStrap());
		assertEquals (new URI("http://cdn.static-economist.com/sites/default/files/imagecache/full-width/images/print-edition/20141213_USP001_0.jpg"), a.getMainImage());
		
		assertEquals (3, Content.Type.values().length);
		int total = 0, texts = 0, headings = 0, imgs = 0;
		for (Content content : a.getBody()) {
			switch (content.getType()) {
			case TEXT:
				texts++;
				break;
			case SUB_HEADING:
				headings++;
				break;
			case IMAGE:
				imgs++;
				break;
			default:
				throw new IllegalStateException ("Unknown body type");
			}
			total++;
		}
		
		assertEquals (27, total);
		assertEquals (2,  headings);
		assertEquals (3,  imgs);
		assertEquals (22, texts);
		
		assertEquals (new Text("IN THE basement of St Gregory’s church in Crown Heights, a Brooklyn neighbourhood where kosher pizzerias compete with jerk-chicken shacks for business, the officers of the 77th precinct are giving away colouring books for children. “Police officers are your friends,” the book’s title proclaims. Around the city, protests at the decision not to prosecute the officer who choked Eric Garner to death suggested that plenty of New Yorkers did not agree."), a.getBody().get(0));
		assertEquals (new Text("Even with these changes, “There is at least one crazy cop in every precinct,” says a retired NYPD officer. Everyone else knows who they are, but they are impossible to sack until they do something really stupid. The officer who choked Mr Garner had been sued for wrongful arrest, and was accused of ordering two black men to strip naked in the street for a search. (He denied it, and one case was settled.) Reformers think the procedure for sacking bullies in uniform should be much swifter. Those who enforce the law should also obey it."), a.getBody().get(26));
		assertEquals (new Text("Under Barack Obama’s administration, the department currently has 27 active cases, looking at city forces such as Seattle’s or Cleveland’s and also at some individual sheriffs’ departments. Though the DoJ finds that, even in the worst departments, most shootings are justified, they also show that the shooting of unarmed people who pose no threat is disturbingly common."), a.getBody().get(12));
		assertEquals (new Image("http://cdn.static-economist.com/sites/default/files/imagecache/original-size/images/print-edition/20141213_USC577.png"), a.getBody().get(11));
		assertEquals (new SubHeading("The body count in Albuquerque"), a.getBody().get(13));
		
		
		System.out.println (a.getTopic() + ": " + a.getTitle());
	}

	@Test
	public void testOnGoodInputWithNoImages() throws IOException, HtmlParseException, URISyntaxException {
		String articleText = Util.loadFromClassPath("article.html");
		articleText = articleText.replaceAll("(</?)img", "$1bfg");
		
		PlainArticle a = new PlainArticleParser().parse(articleText);
		
		assertEquals ("Don’t shoot", a.getTitle());
		assertEquals ("Policing", a.getTopic());
		assertEquals ("America’s police kill too many people. But some forces are showing how smarter, less aggressive policing gets results", a.getStrap());
		assertNull (a.getMainImage());
		
		assertEquals (3, Content.Type.values().length);
		int total = 0, texts = 0, headings = 0, imgs = 0;
		for (Content content : a.getBody()) {
			switch (content.getType()) {
			case TEXT:
				texts++;
				break;
			case SUB_HEADING:
				headings++;
				break;
			case IMAGE:
				imgs++;
				break;
			default:
				throw new IllegalStateException ("Unknown body type");
			}
			total++;
		}
		
		assertEquals (24, total);
		assertEquals (2,  headings);
		assertEquals (0,  imgs);
		assertEquals (22, texts);
		
		assertEquals (new Text("IN THE basement of St Gregory’s church in Crown Heights, a Brooklyn neighbourhood where kosher pizzerias compete with jerk-chicken shacks for business, the officers of the 77th precinct are giving away colouring books for children. “Police officers are your friends,” the book’s title proclaims. Around the city, protests at the decision not to prosecute the officer who choked Eric Garner to death suggested that plenty of New Yorkers did not agree."), a.getBody().get(0));
		assertEquals (new Text("Even with these changes, “There is at least one crazy cop in every precinct,” says a retired NYPD officer. Everyone else knows who they are, but they are impossible to sack until they do something really stupid. The officer who choked Mr Garner had been sued for wrongful arrest, and was accused of ordering two black men to strip naked in the street for a search. (He denied it, and one case was settled.) Reformers think the procedure for sacking bullies in uniform should be much swifter. Those who enforce the law should also obey it."), a.getBody().get(23));
		assertEquals (new Text("Under Barack Obama’s administration, the department currently has 27 active cases, looking at city forces such as Seattle’s or Cleveland’s and also at some individual sheriffs’ departments. Though the DoJ finds that, even in the worst departments, most shootings are justified, they also show that the shooting of unarmed people who pose no threat is disturbingly common."), a.getBody().get(10));
		assertEquals (new SubHeading("The body count in Albuquerque"), a.getBody().get(11));
		
		
		System.out.println (a.getTopic() + ": " + a.getTitle());
	}
	
	@Test
	@SuppressWarnings("unused")
	public void testOnBrokenInput() throws IOException, HtmlParseException, URISyntaxException {
		String articleText = Util.loadFromClassPath("article.html");
		articleText = articleText.replaceAll("(</?)div", "$1Spud");
		
		try {
			PlainArticle a = new PlainArticleParser().parse(articleText);
			fail ("Somehow generated an articel despite the HTML file being corrputed");
		}
		catch (HtmlParseException e) { ; }
		// any other exception should be an error here.
		
	}
	
}
