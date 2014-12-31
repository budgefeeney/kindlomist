package org.feenaboccles.kindlomist.articles;

import java.net.URI;
import java.util.Map;

import org.feenaboccles.kindlomist.articles.content.Image;

/**
 * Resolves images when parsing files.
 * @author bryanfeeney
 */
public class ImageResolver
{
	Map<Image, byte[]> images;
	Map<URI, byte[]>  imagesByUri;
	
	public byte[] getImage(Image key) {
		return images.get(key);
	}
	
	public byte[] getImage(URI key) { 
		return images.get(key);
	}
	
	public void putImage(Image key, byte[] imageBytes) {
		images.put(key, imageBytes);
	}
	
	public void putImage(URI key, byte[] imageBytes) {
		imagesByUri.put(key, imageBytes);
	}
}
