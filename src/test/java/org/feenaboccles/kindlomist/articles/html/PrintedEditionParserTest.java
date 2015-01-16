package org.feenaboccles.kindlomist.articles.html;

import static org.junit.Assert.assertEquals;
import static org.feenaboccles.kindlomist.articles.html.PlainArticleParserTest.DUMMY_URI;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.feenaboccles.kindlomist.articles.PrintEdition;
import org.junit.Test;

public class PrintedEditionParserTest {

	@Test
	public void testValidHtml() throws HtmlParseException, IOException {
		String html = Util.loadFromClassPath("printed-index.html");
		String date = "2012-10-10";
		
		PrintEdition p = new PrintEditionParser(date).parse(DUMMY_URI, html);
		
		assertEquals (date, p.getDateStamp());
		assertEquals (URI.create("http://www.economist.com/news/world-week/21636104-politics-week"), p.getPoliticsThisWeek());
		assertEquals (URI.create("http://www.economist.com/news/world-week/21636084-business-week"), p.getBusinessThisWeek());
		assertEquals (URI.create("http://www.economist.com/news/world-week/21636083-kals-cartoon"), p.getKalsCartoon());
		assertEquals (URI.create("http://www.economist.com/news/letters/21635968-letters-editor"), p.getLetters());
		
		assertEquals (Arrays.<String>asList(new String[] { "Leaders", "Briefing", "United States", "The Americas", "Asia", "China", "Middle East and Africa", "Europe", "Britain", "International", "Special report: Luxury", "Business", "Finance and economics", "Science and technology", "Books and arts", "Obituary" }), p.getOrderedSections());
		
		Map<String, List<URI>> pSections = p.getSections();
		assertEquals (new HashSet<String>(p.getOrderedSections()), pSections.keySet());
		
		int[]    linkCounts   = new int[]    { 5, 12, 8, 6, 1, 10, 4, 8, 5, 6, 5, 4, 3 };
		String[] sectionNames = new String[] { 
			"Middle East and Africa",
			"Finance and economics",
			"United States",
			"Europe",
			"Briefing",
			"Britain",
			"The Americas",
			"Special report: Luxury",
			"Leaders",
			"Books and arts",
			"Asia",
			"China",
			"International"
		};
		
		
		for (int i = 0; i < linkCounts.length; i++) {
			List<URI> articleLinks = pSections.get(sectionNames[i]);
			assertEquals (linkCounts[i], articleLinks.size());
		}
		
		// Check one URI at random
		assertEquals (URI.create("http://www.economist.com/news/special-report/21635765-appreciation-luxury-goes-circles-saintly-or-sinful"), pSections.get("Special report: Luxury").get(1));
		
		System.out.println(p);
	}

	
	// Apparently new URI(null) is fine - need to fix that
}
