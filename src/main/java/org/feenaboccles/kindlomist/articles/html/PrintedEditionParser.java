package org.feenaboccles.kindlomist.articles.html;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.feenaboccles.kindlomist.articles.PrintEdition;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Takes a page listing all the articles in the current printed edition and
 * extracts from it the full list of articles, returned as a  {@link PrintEdition}
 * object.
 */
public class PrintedEditionParser {

	private static final int EXPECTED_ARTICLES_PER_SECTION = 10;
	private static final int EXPECTED_SECTION_COUNT = 10;

	public PrintedEditionParser() {
		// TODO Auto-generated constructor stub
	}
	
	public PrintEdition parse (String html) throws HtmlParseException {
		try {
			// Declare the variables we're parsing into.
			URI politics = null, biz = null, kal = null, letters = null;
			Map<String, List<URI>> sections = new HashMap<>(EXPECTED_SECTION_COUNT);
			List<String> orderedSectionHeadings = new ArrayList<>(EXPECTED_SECTION_COUNT);
			
			// Start parsing
			Document doc = Jsoup.parse(html);
			Elements secs = doc.select("div.section");
			
			// Read in the intro section, with Politics this week etc.
			Element firstSection = secs.select("div.first").first();
			Elements links = firstSection.getElementsByTag("a");
			for (Element link : links) {
				switch (StringUtils.trimToEmpty(link.text()).toLowerCase()) {
				case "politics this week":
					politics = new URI(link.attr("href"));
					break;
				case "business this week":
					biz = new URI(link.attr("href"));
					break;
				case "kal's cartoon":
					kal = new URI(link.attr("href"));
					break;
				}
			}
			
			// Now read in each section, one by one.
			for (Element sec : secs)
			{	if (sec == firstSection) // skip the first section we processed above
					continue;
			
				// Read the section header
				String sectionHeader = sec.getElementsByTag("h4").first().text();
				
				// Two special cases, the first is letters, which requires a particular page parser
				if ("letters".equals(StringUtils.trimToEmpty(sectionHeader).toLowerCase())) {
					letters = new URI(sec.getElementsByTag("a").first().attr("href"));
					continue;
				}
				
				// the second is Economic indicators, a bunch of flash applets, which we skip
				if ("economic and financial indicators".equals(StringUtils.trimToEmpty(sectionHeader).toLowerCase())) {
					continue;
				}
				
				// Otherwise proceed to create a standard section
				orderedSectionHeadings.add(sectionHeader);
				List<URI> articles = new ArrayList<URI>(EXPECTED_ARTICLES_PER_SECTION);
				sections.put(sectionHeader, articles);
				
				Elements articleLinks = sec.getElementsByTag("a");
				for (Element articleLink : articleLinks)
					if (! StringUtils.equals("Comments", articleLink.attr("title")))
						articles.add (new URI(articleLink.attr("href")));
			}
					
			
			return PrintEdition.builder()
							   .politicsThisWeek(politics)
							   .businessThisWeek(biz)
							   .kalsCartoon(kal)
							   .letters(letters)
							   .sections(sections)
							   .orderedSections(orderedSectionHeadings)
							   .build().validate();
		}
		catch (URISyntaxException e) {
			throw new HtmlParseException("At least one of the extracted article URLs was not in fact a valid URL " + e.getMessage());
		}
	}

}
