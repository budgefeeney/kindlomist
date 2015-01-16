package org.feenaboccles.kindlomist.download;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.logging.log4j.core.util.Charsets;
import org.feenaboccles.kindlomist.articles.ContentBasedArticle;
import org.feenaboccles.kindlomist.articles.Economist;
import org.feenaboccles.kindlomist.articles.ImageResolver;
import org.feenaboccles.kindlomist.articles.MainImageArticle;
import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.articles.PrintEdition;
import org.feenaboccles.kindlomist.articles.SingleImageArticle;
import org.feenaboccles.kindlomist.articles.WeeklyDigestArticle;
import org.feenaboccles.kindlomist.articles.content.Content;
import org.feenaboccles.kindlomist.articles.content.Image;
import org.feenaboccles.kindlomist.articles.html.HtmlParseException;
import org.feenaboccles.kindlomist.articles.html.HtmlParser;
import org.feenaboccles.kindlomist.articles.html.PlainArticleParser;
import org.feenaboccles.kindlomist.articles.html.PrintEditionParser;
import org.feenaboccles.kindlomist.articles.html.SingleImageArticleParser;
import org.feenaboccles.kindlomist.articles.html.WeeklyDigestArticleParser;
import org.feenaboccles.kindlomist.articles.markdown.EconomistWriter;

/**
 * Encapsulates the logic involved in downloading a full issue of the Ecomonimst
 * @author bryanfeeney
 *
 */
@Log4j2
public class Downloader extends HttpAction {

	private final String dateStamp;
	private final String username;
	private final String password;
	
	private final static int NUM_SIMUL_DOWNLOADS = 6;
	
	/**
	 * @param path the path to which files are downloaded. If null a temporary
	 * directory is created.
	 * @param dateStamp the date-stamp used to identify the particular issue
	 */
	public Downloader(String dateStamp, String username, String password) {
		super(HttpClientBuilder.create()
			   	.setRedirectStrategy(new LaxRedirectStrategy())
			   	.build());
		this.dateStamp = dateStamp;
		this.username  = username;
		this.password  = password;
	}
	
	
	/**
	 * Downloads the full issue.
	 */
	public Economist call() throws HttpActionException, HtmlParseException {
		// Set things up so we can download images.
		ImageResolver   imageResolver;
		ImageDownloader imageDownloader;
		try {
			imageResolver   = new ImageResolver(Files.createTempDirectory("images-"));
			imageDownloader = new ImageDownloader(client, imageResolver, NUM_SIMUL_DOWNLOADS);
		}
		catch (IOException e) {
			throw new HttpActionException("Can't create a temporary directory into which images should be downloaded : " + e.getMessage(), e);
		}
		
		// Log in
		log.debug("Logging in to the Economist with username " + username);
		if (! new LoginAction (client, username, password).call())
			throw new HttpActionException("Failed to log in to the " + username + " account with the given password");
		
		// Download the table of contents
		log.debug("Downloading the index page for datestamp " + dateStamp + " at URL");
		// construct the URL
		final URI u;
		try {
			u = new URI("http://www.economist.com/printedition/" + dateStamp);
		} catch (URISyntaxException e) {
			throw new HttpActionException("Couldn't construct a valid URL from the date-stamp '" + dateStamp + "' : " + e.getMessage(), e);
		}
		PrintEdition p = fetchAndParse(u, URI.create("http://www.economist.com"), new PrintEditionParser(dateStamp));
		
		// Download the special articles (politics this week, Kals cartoon, etc.)
		log.debug("Loading core articles: politics, business, and cartoon");
		SingleImageArticle  kal  = fetchAndParse(p.getKalsCartoon(), u, new SingleImageArticleParser());
		WeeklyDigestArticle pols = fetchAndParse(p.getPoliticsThisWeek(), u, new WeeklyDigestArticleParser());
		WeeklyDigestArticle biz  = fetchAndParseOrNull(p.getBusinessThisWeek(), u, new WeeklyDigestArticleParser());

		downloadMainImage(imageDownloader, kal);
		downloadContentImages(imageDownloader, pols, biz);
		
		// For each of the sections download the section's articles
		Map<String, List<PlainArticle>> sections = new HashMap<>(p.getSections().size());
		for (Map.Entry<String, List<URI>> e : p.getSections().entrySet()) {
			log.debug("Loading articles in section " + e.getKey());
			List<PlainArticle> articles = new ArrayList<>(e.getValue().size());
			sections.put (e.getKey(), articles);
			for (URI articleUri : e.getValue()) {
				if (log.isDebugEnabled())
					log.debug("Fetching article for section " + e.getKey() + " from URI " + articleUri.toASCIIString());
				
				try {
					PlainArticle a = fetchAndParse(articleUri, u, new PlainArticleParser());
					downloadAllImages(imageDownloader, a);
					articles.add(a);
				}
				catch (HtmlParseException hpe) {
					log.warn("Skipping unparseable article - " + hpe.getMessage(), hpe);
					System.err.println ("Skipping unparseable article - " + hpe.getMessage());
					hpe.printStackTrace(System.err);
				}
			}
		}
		
		// Build the issue
		try
		{	imageDownloader.waitForAllDownloadsToComplete(30, TimeUnit.MINUTES);
			return Economist.builder()
						.dateStamp(LocalDate.parse(dateStamp))
						.politicsThisWeek(pols)
						.businessThisWeek(biz)
						.kalsCartoon(kal)
						.sections(sections)
						.orderedSections(p.getOrderedSections())
						.images(imageResolver)
						.build().validate();
		}
		catch (InterruptedException ie) {
			throw new HttpActionException ("Timed out, or was interrupted, while waiting for all images to download " + ie.getMessage(), ie);
		}
	}


	/**
	 * Same as {@link #fetchAndParse(URI, URI, HtmlParser)} except that if the target
	 * URI is null, this will return null.
	 */
	private final <T> T fetchAndParseOrNull (URI uri, URI referrer, HtmlParser<T> parser) 
			throws HttpActionException, HtmlParseException {
		return uri == null ? null : fetchAndParse (uri, referrer, parser);
	}

	/**
	 * Fetches a webpage's HTML from the given URL, throwing a {@link HttpActionException}
	 * if an error occurs during the process, and then attempts to parse it into the
	 * appropriate object, throwing a {@link HtmlParseException} if it fails to parse.
	 * @param uri the URI of the image being parsed.
	 * @param referrer the optional URI of the referrer
	 * @param parser the object required to parse the fetched HTML into an object
	 * @return the parsed object corresponding to the given HTML
	 */
	private final <T> T fetchAndParse (URI uri, URI referrer, HtmlParser<T> parser) 
	throws HttpActionException, HtmlParseException {
		try {
			// download the page
			final String contents = makeHttpRequest(uri, referrer);
		
			// parse it and return
			return parser.parse(uri, contents);
		}
		catch (HtmlParseException e) {
			throw new HtmlParseException ("HTML Parse error for URL " + uri.toASCIIString() + " : " + e.getMessage(), e);
		}
	}
	
	public static void main (String[] args) throws IOException, HttpActionException, HtmlParseException {
		String password = Files.readAllLines(Paths.get("/Users/bryanfeeney/Desktop/eco.passwd")).get(0);
		String date = "2015-01-10";
		
		Downloader d = new Downloader(date, "bryan.feeney@gmail.com", password);
		
		Economist economist = d.call();
		try (OutputStream ostream = Files.newOutputStream(Paths.get("/Users/bryanfeeney/Desktop/economist-" + date + ".blob"))) {
			SerializationUtils.serialize(economist, ostream);
		}
		
		try (BufferedWriter wtr = Files.newBufferedWriter(Paths.get("/Users/bryanfeeney/Desktop/economist-" + date + ".md"), Charsets.UTF_8)) {
			EconomistWriter ewtr = new EconomistWriter();
			ewtr.writeEconomist(wtr, economist);
		}
	}
	
	/** 
	 * Downloads the main article title image for all given articles,
	 * if one exists
	 */
	public void downloadMainImage (ImageDownloader d, MainImageArticle... articles) {
		for (MainImageArticle article : articles)
			if (article.getMainImage() != null)
				d.launchDownload(article.getMainImage(), article.getArticleUri());
	}
	
	/**
	 * Downloads the inline images in the article content
	 */
	public void downloadContentImages (ImageDownloader d, ContentBasedArticle... articles) {
		for (ContentBasedArticle article : articles)
			for (Content content : article.getBody())
				if (content.getType() == Content.Type.IMAGE)
					d.launchDownload((Image) content, article.getArticleUri());
	}
	
	/**
	 * Downloads all the images in the given plain articles
	 */
	public void downloadAllImages (ImageDownloader d, PlainArticle... articles) {
		downloadMainImage (d, articles);
		downloadContentImages (d, articles);
	}
}
