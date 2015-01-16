package org.feenaboccles.kindlomist.articles;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.feenaboccles.kindlomist.articles.content.Image;

/**
 * Resolves images when parsing files. Threadsafe
 * @author bryanfeeney
 */
public class ImageResolver implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private final static int EXPECTED_ARTICLE_COUNT      = 100;
	private final static int EXPECTED_IMAGES_PER_ARTICLE = 2;
	
	private final Map<Image, Path> images;
	private final Map<URI, Path>  imagesByUri;
	private final Optional<Path> downloadDirectory;
	private final AtomicInteger imageCounter = new AtomicInteger(0);
	
	/**
	 * Creates an {@link ImageResolver} to which paths can be
	 * assigned and retrieved, but without a specific
	 * download directory, which means that the 
	 */
	public ImageResolver() {
		this(Optional.empty());
	}
	

	public ImageResolver(Path downloadDirectory) {
		this(Optional.of(downloadDirectory));
	}
	
	private ImageResolver(Optional<Path> downloadDirectory) {
		this.images            = new ConcurrentHashMap<>(EXPECTED_ARTICLE_COUNT * EXPECTED_IMAGES_PER_ARTICLE);
		this.imagesByUri       = new ConcurrentHashMap<>(EXPECTED_ARTICLE_COUNT);
		this.downloadDirectory = downloadDirectory;
	}
	
	public Path getImage(Image key) {
		return images.get(key);
	}
	
	public Path getImage(URI key) { 
		return images.get(key);
	}
	
	public boolean hasImage(URI key) {
		return imagesByUri.containsKey(key);
	}
	
	public boolean hasImage(Image key) {
		return images.containsKey(key);
	}
	
	public void putImage(Image key, Path imageFile) {
		imageCounter.incrementAndGet();
		images.put(key, imageFile);
	}
	
	public void putImage(URI key, Path imageFile) {
		imageCounter.incrementAndGet();
		imagesByUri.put(key, imageFile);
	}
	
	public Path putImage(Image key, byte[] imageContents) throws IOException {
		imageCounter.incrementAndGet();
		try {
			return putImageBytes(key, new URI(key.getContent()), images, imageContents);
		}
		catch (URISyntaxException e) {
			throw new IOException ("The URI associated with the image content - " + key.getContent() + " - does not define a valid URI : " + e.getMessage(), e);
		}
	}
	
	public Path putImage(URI key, byte[] imageContents) throws IOException {
		imageCounter.incrementAndGet();
		return putImageBytes(key, key, imagesByUri, imageContents);
	}
	

	
	/**
	 * Creates a file in which an image's content could be 
	 * written, adds it to this lookup, and returns an
	 * open stream to write the content should be written
	 * straightaway, and then closed.
	 */
	private synchronized <K> Path putImageBytes (K key, URI uri, Map<K, Path> map, byte[] bytes) throws IOException {
		Path outputPath = generateImagePath(uri);
		map.put(key, outputPath);
		Files.write(outputPath, bytes, StandardOpenOption.CREATE_NEW);
		return outputPath;
	}

	/**
	 * Create a path to a new file to which the bytes of an image
	 * can be saved.
	 */
	private Path generateImagePath(URI key) {
		if (! downloadDirectory.isPresent())
			throw new IllegalStateException("This image resolver was not created with a download directory, so downloads cannot be performed.");
		
		int imageCount   = imageCounter.incrementAndGet();
		String imageFile = "images-" + imageCount;
			
		switch (StringUtils.substringAfterLast(key.getPath(), ".").toLowerCase()) {
		case "jpg":
		case "jpeg":
			imageFile += ".jpg"; break;
		case "gif":
			imageFile += ".gif"; break;
		case "png":
			imageFile += ".png"; break;
		default:
			throw new IllegalArgumentException("This does not appear to be a URI pointing to an image file, as it doesn't end with a recognisable extension (.jpg, .jpeg, .gif, .png) : " + key.toASCIIString());
		}
		
		return downloadDirectory.get().resolve(imageFile);
	}

}
