package org.feenaboccles.kindlomist.download;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;

import org.apache.http.client.HttpClient;
import org.feenaboccles.kindlomist.articles.ImageResolver;
import org.feenaboccles.kindlomist.articles.content.Image;

/**
 * Downloads images and writes stores them in the given {@link ImageResolver}.
 * Requests are queued up by calling {@link #launchDownload(Image, URI)} or
 * {@link #launchDownload(URI, URI)}. Once all downloads have been "launched"
 * (in reality queued up), call {@link #waitForAllDownloadsToComplete(long, TimeUnit)}
 */
public class ImageDownloader {

	private final ImageResolver   resolver;
	private final HttpClient      client;
	private final ExecutorService executor;
	
	
	/**
	 * Creates a new {@link ImageDownloader}. The given {@link HttpClient} must
	 * support multi-threading
	 */
	public ImageDownloader(HttpClient client, ImageResolver resolver, int numSimultaneousDownloads) {
		super();
		this.client   = client;
		this.resolver = resolver;
		this.executor = Executors.newFixedThreadPool(numSimultaneousDownloads);
	}
	
	public void launchDownload(Image image, URI articleUri) {
		executor.submit(new Downloader (image, articleUri, client, resolver));
	}

	public void launchDownload(URI image, URI articleUri) {
		executor.submit(new Downloader (image, articleUri, client, resolver));
	}
	
	/**
	 * Waits for all downloads to finish. No more jobs can be submitted once
	 * this is called.
	 */
	public void waitForAllDownloadsToComplete(long timeout, TimeUnit units) throws InterruptedException {
		executor.shutdown();
		executor.awaitTermination(timeout, units);
	}
	
	
	// ------------------------------------------------------------------------
	
	@Log4j2
	private final static class Downloader 
	extends HttpAction implements Callable<Path>{
		Image image;
		URI uri;
		URI articleUri;
		ImageResolver resolver;
		
		public Downloader(URI uri, URI articleUri, HttpClient client, ImageResolver resolver) {
			super(client);
			this.image      = null;
			this.uri        = uri;
			this.articleUri = articleUri;
			this.resolver   = resolver;
		}
		
		public Downloader(Image image, URI articleUri, HttpClient client, ImageResolver resolver) {
			super(client);
			this.image      = image;
			this.uri        = null;
			this.articleUri = articleUri;
			this.resolver   = resolver;
		}


		@Override
		public Path call() throws HttpActionException {
			// get the output stream, and the actual image URI
			Path path = null;
			try {
				URI imageUri = image == null ? uri : new URI(image.getContent());
				if (log.isInfoEnabled())
					log.info ("Downloading from " + uri.toASCIIString());
				
				byte[] imageBytes = makeBinaryHttpRequest(imageUri, articleUri);
				
				if (image == null) {
					path = resolver.putImage(image, imageBytes);
				} else {
					path = resolver.putImage(uri, imageBytes);
				}
				
				return path;
			}
			catch (URISyntaxException e) {
				throw new HttpActionException ("The URI associated with the image content - " + image.getContent() + " - does not define a valid URI : " + e.getMessage(), e);
			}
			catch (IOException e) {
				throw new HttpActionException ("The download succeeded but an error occurred when writing the file to " + path + " : " + e.getMessage(), e);
			}
		}
	}
	
}