package org.feenaboccles.kindlomist.articles;

import static org.junit.Assert.*;

import org.junit.Test;

public class PlainArticleTest {

	@Test
	public void test() {
		PlainArticle a = PlainArticle.builder()
						.title("My first article")
						.strap("This is a quick article about somethign small")
						.body("Lorem ipsum dolor sit amet")
						.build();
		
		assertEquals ("My first article", a.getTitle());
		assertEquals ("This is a quick article about somethign small", a.getStrap());
		assertEquals ("Lorem ipsum dolor sit amet", a.getBody());
	}

}
