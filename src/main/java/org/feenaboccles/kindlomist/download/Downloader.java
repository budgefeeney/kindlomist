package org.feenaboccles.kindlomist.download;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
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
import org.feenaboccles.kindlomist.articles.html.*;

/**
 * Encapsulates the logic involved in downloading a full issue of the Ecomonimst
 * @author bryanfeeney
 *
 */
@Log4j2
public class Downloader extends HttpAction {

	private final DateStamp dateStamp;
	private final Email userEmail;
	private final Password password;
	
	private final static int NUM_SIMUL_DOWNLOADS = 6;
	
	/**
	 * @param dateStamp the date-stamp used to identify the particular issue
	 * @param userEmail the username with which to log in
	 * @param password the password to use when logging in.
	 */
	public Downloader(DateStamp dateStamp, Email userEmail, Password password) {
		super(HttpClientBuilder.create()
			   	.setRedirectStrategy(new LaxRedirectStrategy())
			   	.build());
		this.dateStamp = dateStamp;
		this.userEmail = userEmail;
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
			Path tmpImgDir  = Files.createTempDirectory("images-");
			imageResolver   = new ImageResolver(tmpImgDir);
			imageDownloader = new ImageDownloader(client, imageResolver, NUM_SIMUL_DOWNLOADS);
			tmpImgDir.toFile().deleteOnExit();
		}
		catch (IOException e) {
			throw new HttpActionException("Can't create a temporary directory into which images should be downloaded : " + e.getMessage(), e);
		}
		
		// Log in
		log.debug("Logging in to the Economist with username " + userEmail.value());
		if (! new LoginAction (client, userEmail, password).call())
			throw new HttpActionException("Failed to log in to the " + userEmail.value() + " account with the given password");
		
		// Download the table of contents
		log.debug("Downloading the index page for datestamp " + dateStamp + " at URL");
		final Optional<URI> u;
		try {
			u = Optional.of(new URI("http://www.economist.com/printedition/" + dateStamp.value()));
		} catch (URISyntaxException e) {
			throw new HttpActionException("Couldn't construct a valid URL from the date-stamp '" + dateStamp + "' : " + e.getMessage(), e);
		}
		PrintEdition p = fetchAndParse(u.get(), some(URI.create("http://www.economist.com")), new PrintEditionParser(dateStamp));
		
		// Download the special articles (politics this week, Kals cartoon, etc.)
		log.debug("Loading core articles: politics, business, and cartoon");
		SingleImageArticle  kal  = fetchAndParse(p.getKalsCartoon(), u, new SingleImageArticleParser());
		WeeklyDigestArticle pols = fetchAndParseDigest(p.getPoliticsThisWeek(), u);
		Optional<WeeklyDigestArticle> biz =
				p.getBusinessThisWeek().isPresent()
				? Optional.of(fetchAndParseDigest(p.getBusinessThisWeek().get(), u))
				: Optional.empty();
		PlainArticle letters = fetchAndParse(p.getLetters(), u, new LetterArticleParser());
		PlainArticle obit = fetchAndParse(p.getObituary(), u, new PlainArticleParser());


		downloadMainImage(imageDownloader, kal);
		downloadContentImages(imageDownloader, pols);
		biz.ifPresent(b -> downloadContentImages(imageDownloader, b));
		
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

		// Finally this issue's cover-image
		Image coverImage = coverImageFromTimeStamp(dateStamp);
		imageDownloader.launchDownload(coverImage, URI.create("http://www.economist.com/printedition"));

		// Build the issue
		try
		{	imageDownloader.waitForAllDownloadsToComplete(30, TimeUnit.MINUTES);
			return Economist.builder()
						.dateStamp(dateStamp.asLocalDate())
						.politicsThisWeek(pols)
						.businessThisWeek(biz)
						.kalsCartoon(kal)
						.letters(letters)
						.sections(sections)
						.obituary(obit)
						.orderedSections(p.getOrderedSections())
						.images(imageResolver)
						.coverImage(coverImage)
						.build().validate();
		}
		catch (InterruptedException ie) {
			throw new HttpActionException ("Timed out, or was interrupted, while waiting for all images to download " + ie.getMessage(), ie);
		}
	}

	/**
	 * Downloads the cover image for the print edition of the Econoimst
	 * associated with the given date-stamp, which should have the format
	 * yyyy-mm-dd.
	 * @param dateStamp a date-stamp in the format yyyy-mm-dd
	 * @return an Image object representing the cover image of the Economist
	 */
	private static Image coverImageFromTimeStamp(DateStamp dateStamp) {
		return new Image("http://cdn.static-economist.com/sites/default/files/imagecache/print-cover-full/print-covers/" + dateStamp.valueAsNumbersOnly() + "_cuk400.jpg");
	}


	/**
	 * Fetches a weekly news digest page and parses it into a {@link WeeklyDigestArticle}
	 * @param uri the URI of the article being parsed.
	 * @param referrer the  URI of the referrer
	 */
	private WeeklyDigestArticle fetchAndParseDigest (URI uri, Optional<URI> referrer)
			throws HttpActionException, HtmlParseException {
		return fetchAndParse(uri, referrer, new WeeklyDigestArticleParser());
	}

	/**
	 * Fetches a webpage's HTML from the given URL, throwing a {@link HttpActionException}
	 * if an error occurs during the process, and then attempts to parse it into the
	 * appropriate object, throwing a {@link HtmlParseException} if it fails to parse.
	 * @param uri the URI of the article being parsed.
	 * @param referrer the  URI of the referrer
	 * @param parser the object required to parse the fetched HTML into an object
	 * @return the parsed object corresponding to the given HTML
	 */
	private <T> T fetchAndParse (URI uri, Optional<URI> referrer, HtmlParser<T> parser)
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

	
	/** 
	 * Downloads the main article title image for all given articles,
	 * if one exists
	 */
	public void downloadMainImage (ImageDownloader d, MainImageArticle... articles) {
		for (MainImageArticle article : articles)
			if (article.getMainImage().isPresent())
				d.launchDownload(article.getMainImage().get(), article.getArticleUri());
	}
	
	/**
	 * Downloads the inline images in the article content
	 */
	public void downloadContentImages (ImageDownloader d, ContentBasedArticle... articles) {
		for (ContentBasedArticle article : articles)
			article.getBody().stream()
					.filter(content -> content.getType() == Content.Type.IMAGE)
					.forEach(content -> d.launchDownload((Image) content, article.getArticleUri()));
	}
	
	/**
	 * Downloads all the images in the given plain articles
	 */
	public void downloadAllImages (ImageDownloader d, PlainArticle... articles) {
		downloadMainImage (d, articles);
		downloadContentImages (d, articles);
	}

	/**
	 * Converts a non null value to an Optional value.
	 */
	private static <T> Optional<T> some (@NonNull T value) {
		return Optional.of(value);
	}
}
