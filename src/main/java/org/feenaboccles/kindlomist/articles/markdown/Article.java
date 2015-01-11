package org.feenaboccles.kindlomist.articles.markdown;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import lombok.Value;

import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.articles.SingleImageArticle;
import org.feenaboccles.kindlomist.articles.WeeklyDigestArticle;
import org.feenaboccles.kindlomist.articles.content.Content;

/**
 * A Markdown writeable article
 * <p>
 * This unifies all the the disparate validated article types 
 * (see implementors of {@link org.feenaboccles.kindlomist.articles.Article})
 * and dispenses with the validation checks. Some fields are merged at
 * this point for writing (e.g. topic getting merged into title), and
 * all are nullable (see e.g. {@link #hasContent()}, {@link #hasMainImage()}
 * and {@link #hasStrap()}).
 */
@Value
public class Article {

	String title;
	String strap;
	
	URI mainImage;
	
	List<Content> content;
	
	
	public Article (PlainArticle article) {
		title = article.getTopic() + ": " + article.getTitle();
		strap = article.getStrap();
		
		mainImage = article.getMainImage();
		content   = article.getBody();
	}
	
	public Article (String title, SingleImageArticle article) {
		this.title = title;
		strap = null;
		
		mainImage = article.getMainImage();
		content   = Collections.emptyList();
	}
	
	public Article (String title, WeeklyDigestArticle article) {
		this.title = title;
		strap = null;
		
		mainImage = null;
		content   = article.getBody();
	}
	
	public boolean hasStrap() {
		return strap != null;
	}
	
	public boolean hasMainImage() { 
		return mainImage != null;
	}
	
	public boolean hasContent() {
		return ! content.isEmpty();
	}
	
}
