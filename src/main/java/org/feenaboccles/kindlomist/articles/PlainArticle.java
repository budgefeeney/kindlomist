package org.feenaboccles.kindlomist.articles;

import java.util.List;

import lombok.Value;
import lombok.experimental.Builder;

/**
 * A plain Economist article.
 */
@Value
@Builder
public class PlainArticle 
{
	String title;
	String strap;
	String body;
	
	List<String> images;
}
