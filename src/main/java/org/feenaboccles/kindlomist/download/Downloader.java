package org.feenaboccles.kindlomist.download;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;

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

	private final DateStamp dateStamp;
	private final UserName username;
	private final Password password;
	
	private final static int NUM_SIMUL_DOWNLOADS = 6;
	
	/**
	 * @param dateStamp the date-stamp used to identify the particular issue
	 * @param username the username with which to log in
	 * @param password the password to use when logging in.
	 */
	public Downloader(DateStamp dateStamp, UserName username, Password password) {
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
			Path tmpImgDir  = Files.createTempDirectory("images-");
			imageResolver   = new ImageResolver(tmpImgDir);
			imageDownloader = new ImageDownloader(client, imageResolver, NUM_SIMUL_DOWNLOADS);
			tmpImgDir.toFile().deleteOnExit();
		}
		catch (IOException e) {
			throw new HttpActionException("Can't create a temporary directory into which images should be downloaded : " + e.getMessage(), e);
		}
		
		// Log in
		log.debug("Logging in to the Economist with username " + username.value());
		if (! new LoginAction (client, username, password).call())
			throw new HttpActionException("Failed to log in to the " + username.value() + " account with the given password");
		
		// Download the table of contents
		log.debug("Downloading the index page for datestamp " + dateStamp + " at URL");
		final URI u;
		try {
			u = new URI("http://www.economist.com/printedition/" + dateStamp.value());
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
						.sections(sections)
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
	 * Same as {@link #fetchAndParse(URI, URI, HtmlParser)} except that if the target
	 * URI is null, this will return null.
	 */
	private <T> T fetchAndParseOrNull (URI uri, URI referrer, HtmlParser<T> parser)
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
	private <T> T fetchAndParse (URI uri, URI referrer, HtmlParser<T> parser)
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
	public void downloadCoverImage (ImageDownloader d, Image coverImage) {
		d.launchDownload(coverImage);
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
	

	
	public static void main (String[] args) throws IOException, HttpActionException, HtmlParseException, InterruptedException {
		String password = Files.readAllLines(Paths.get("/Users/bryanfeeney/Desktop/eco.passwd")).get(0);
		DateStamp date = DateStamp.of("2015-06-06");
		
		Downloader d = new Downloader(date, UserName.of("bryan.feeney@gmail.com"), Password.of(password));
		
		Economist economist = d.call();
//		try (OutputStream ostream = Files.newOutputStream(Paths.get("/Users/bryanfeeney/Desktop/economist-" + date + ".blob"))) {
//			SerializationUtils.serialize(economist, ostream);
//		}
		
		String mdPathStr   = "/Users/bryanfeeney/Desktop/economist-" + date + ".md";
		String epubPathStr = "/Users/bryanfeeney/Desktop/economist-" + date + ".epub";
		try (BufferedWriter wtr = Files.newBufferedWriter(Paths.get(mdPathStr), Charsets.UTF_8)) {
			EconomistWriter ewtr = new EconomistWriter();
			ewtr.writeEconomist(wtr, economist);
		}
		
		// Execute the pandoc conversion
		Path coverImagePath = economist.getImages().getImagePath(economist.getCoverImage());
		Runtime rt = Runtime.getRuntime();
		Process p  = rt.exec("/Users/bryanfeeney/.cabal/bin/pandoc -S  --epub-chapter-level 1 --toc --toc-depth 2 -o " + epubPathStr + " --epub-cover-image " + coverImagePath.toString() + " " + mdPathStr);
		p.waitFor();
	}
}
