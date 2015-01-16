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
public class PrintEditionParser implements HtmlParser<PrintEdition> {

	private static final int EXPECTED_ARTICLES_PER_SECTION = 10;
	private static final int EXPECTED_SECTION_COUNT = 10;

	private final String dateStamp;
	
	/**
	 * Creates a new {@link PrintEditionParser}
	 * @param dateStamp the date-stamp assigned to the parsed print
	 * edition - used to determine if it's an Xmas issue or not.
	 */
	public PrintEditionParser(String dateStamp) {
		this.dateStamp = dateStamp;
	}
	
	@Override
	public PrintEdition parse (URI articleUri, String html) throws HtmlParseException {
		try {
			// Declare the variables we're parsing into.
			URI politics = null, biz = null, kal = null, letters = null, obit = null;
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
				case "the world this year":
				case "the world this week":
					politics = toUri(link.attr("href"));
					break;
				case "business this week":
					biz = toUri(link.attr("href"));
					break;
				case "kal's cartoon":
					kal = toUri(link.attr("href"));
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
					letters = toUri(sec.getElementsByTag("a").first().attr("href"));
					continue;
				}
				else if ("obituary".equals(StringUtils.trimToEmpty(sectionHeader).toLowerCase())) {
					obit = toUri(sec.getElementsByTag("a").first().attr("href"));
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
						articles.add (toUri(articleLink.attr("href")));
			}
					
			return PrintEdition.builder()
					           .dateStamp(dateStamp)
							   .politicsThisWeek(politics)
							   .businessThisWeek(biz)
							   .kalsCartoon(kal)
							   .letters(letters)
							   .sections(sections)
							   .orderedSections(orderedSectionHeadings)
							   .obituary(obit)
							   .build().validate();
		}
		catch (URISyntaxException e) {
			throw new HtmlParseException("At least one of the extracted article URLs was not in fact a valid URL " + e.getMessage());
		}
	}

	/**
	 * Converts a string to a URI. Prepends Economist.com to the URI if it's
	 * missing a host
	 * @throws URISyntaxException 
	 */
	static final URI toUri (String url) throws URISyntaxException {
		return StringUtils.left(url, 4).toUpperCase().equals("HTTP")
				? new URI (url)
				: new URI ("http://www.economist.com" + url);
	}
}
