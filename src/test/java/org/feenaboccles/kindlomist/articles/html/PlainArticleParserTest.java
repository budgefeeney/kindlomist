package org.feenaboccles.kindlomist.articles.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.articles.content.Content;
import org.feenaboccles.kindlomist.articles.content.Footnote;
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
		
		assertEquals (4, Content.Type.values().length);
		int total = 0, texts = 0, headings = 0, imgs = 0, foots = 0;
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
			case FOOTNOTE:
				foots++;
				break;
			default:
				throw new IllegalStateException ("Unknown content type" + content.getType());
			}
			total++;
		}
		
		assertEquals (27, total);
		assertEquals (2,  headings);
		assertEquals (3,  imgs);
		assertEquals (22, texts);
		assertEquals (0,  foots);
		
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
		
		assertEquals (4, Content.Type.values().length);
		int total = 0, texts = 0, headings = 0, imgs = 0, foots = 0;
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
			case FOOTNOTE:
				foots++;
				break;
			default:
				throw new IllegalStateException ("Unknown content type" + content.getType());
			}
			total++;
		}
		
		assertEquals (24, total);
		assertEquals (2,  headings);
		assertEquals (0,  imgs);
		assertEquals (22, texts);
		assertEquals (0,  foots);
		
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
	
	@Test
	public void testOnSecondGoodInput() throws IOException, HtmlParseException, URISyntaxException {
		String articleText = Util.loadFromClassPath("article2.html");
		
		PlainArticle a = new PlainArticleParser().parse(articleText);

		assertEquals ("Let’s get fiscal", a.getTitle());
		assertEquals ("Buttonwood", a.getTopic());
		assertEquals ("A new book from a prescient economist", a.getStrap());
		assertEquals (URI.create("http://cdn.static-economist.com/sites/default/files/imagecache/full-width/images/print-edition/20141220_FND001_0.jpg"), a.getMainImage());
		
		assertEquals (4, Content.Type.values().length);
		int total = 0, texts = 0, headings = 0, imgs = 0, foots = 0;
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
			case FOOTNOTE:
				foots++;
				break;
			default:
				throw new IllegalStateException ("Unknown content type" + content.getType());
			}
			total++;
		}
		
		assertEquals (11, total);
		assertEquals (0,  headings);
		assertEquals (0,  imgs);
		assertEquals (9,  texts);
		assertEquals (2,  foots);
		
		assertEquals (new Text("WHAT is the Japanese word for Schadenfreude? For much of the late 1990s and early 2000s, Western economists and politicians were happy to lecture the Japanese government about the mistakes it made in the aftermath of its asset bubble. But six years after the collapse of Lehman Brothers, the investment bank whose demise triggered the financial crisis, many Western economies are still struggling to generate decent growth. Their central banks are being forced to keep interest rates close to zero. Yields on government bonds in Europe, as in Japan, have sunk to record lows. Some economists are talking of a new era of “secular stagnation”."), a.getBody().get(0));
		assertEquals (new Text("This makes monetary policy much less effective. The policy of quantitative easing (QE), the creation of money to buy assets, succeeded in expanding the balance-sheets of central banks but did not push up bank lending or boost the amount of money circulating among companies and consumers. That explains why QE has not resulted in the hyperinflation that some feared and also, in Mr Koo’s view, why QE has not been very effective."), a.getBody().get(3));
		assertEquals (new Text("Mr Koo’s case, which he first made in “The Holy Grail of Macroeconomics”, a book published in 2008, has been strengthened by intervening events. However, there are some points that he glosses over. A side-effect of QE is that asset prices have risen sharply in value; that should have repaired corporate and personal balance-sheets but the private sector is still not borrowing. Why not? And he probably does not take the arguments for secular stagnation seriously enough: deteriorating demographics and sluggish productivity growth are important. Growth in the rich world has been slowing for decades. Even politicians with the wisdom of Solomon might have struggled in the circumstances."), a.getBody().get(8));
		assertEquals (new Footnote("* “The Escape From Balance Sheet Recession and the QE Trap: A Hazardous Road for the World Economy”, published by John Wiley"), a.getBody().get(9));
		assertEquals (new Footnote("Economist.com/blogs/buttonwood"), a.getBody().get(10));

	}
	
	
	@Test
	public void testOnThirdGoodInput() throws IOException, HtmlParseException, URISyntaxException {
		String articleText = Util.loadFromClassPath("article3-job-ad.html");
		
		PlainArticle a = new PlainArticleParser().parse(articleText);

		assertEquals ("Don’t shoot", a.getTitle());
		assertEquals ("Policing", a.getTopic());
		assertEquals ("America’s police kill too many people. But some forces are showing how smarter, less aggressive policing gets results", a.getStrap());
		assertNull (a.getMainImage());
		
		assertEquals (4, Content.Type.values().length);
		int total = 0, texts = 0, headings = 0, imgs = 0, foots = 0;
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
			case FOOTNOTE:
				foots++;
				break;
			default:
				throw new IllegalStateException ("Unknown content type" + content.getType());
			}
			total++;
		}
		
		assertEquals (11, total);
		assertEquals (0,  headings);
		assertEquals (0,  imgs);
		assertEquals (9,  texts);
		assertEquals (2,  foots);
		
		assertEquals (new Text("WHAT is the Japanese word for Schadenfreude? For much of the late 1990s and early 2000s, Western economists and politicians were happy to lecture the Japanese government about the mistakes it made in the aftermath of its asset bubble. But six years after the collapse of Lehman Brothers, the investment bank whose demise triggered the financial crisis, many Western economies are still struggling to generate decent growth. Their central banks are being forced to keep interest rates close to zero. Yields on government bonds in Europe, as in Japan, have sunk to record lows. Some economists are talking of a new era of “secular stagnation”."), a.getBody().get(0));
		assertEquals (new Text("This makes monetary policy much less effective. The policy of quantitative easing (QE), the creation of money to buy assets, succeeded in expanding the balance-sheets of central banks but did not push up bank lending or boost the amount of money circulating among companies and consumers. That explains why QE has not resulted in the hyperinflation that some feared and also, in Mr Koo’s view, why QE has not been very effective."), a.getBody().get(3));
		assertEquals (new Text("Mr Koo’s case, which he first made in “The Holy Grail of Macroeconomics”, a book published in 2008, has been strengthened by intervening events. However, there are some points that he glosses over. A side-effect of QE is that asset prices have risen sharply in value; that should have repaired corporate and personal balance-sheets but the private sector is still not borrowing. Why not? And he probably does not take the arguments for secular stagnation seriously enough: deteriorating demographics and sluggish productivity growth are important. Growth in the rich world has been slowing for decades. Even politicians with the wisdom of Solomon might have struggled in the circumstances."), a.getBody().get(8));
		assertEquals (new Footnote("* “The Escape From Balance Sheet Recession and the QE Trap: A Hazardous Road for the World Economy”, published by John Wiley"), a.getBody().get(9));
		assertEquals (new Footnote("Economist.com/blogs/buttonwood"), a.getBody().get(10));

	}
}
