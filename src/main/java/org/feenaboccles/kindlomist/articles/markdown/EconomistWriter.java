package org.feenaboccles.kindlomist.articles.markdown;

import java.io.IOException;
import java.io.Writer;

import org.feenaboccles.kindlomist.articles.Economist;
import org.feenaboccles.kindlomist.articles.PlainArticle;
import org.feenaboccles.kindlomist.articles.PrintEdition;

public class EconomistWriter 
{

	// TODO Proper title, nicely formated date
	// TODO The World this week / this year - the isXmasIssue test
	public void writeEconomist (Writer writer, Economist issue) throws IOException {
		ArticleWriter awriter = new ArticleWriter (writer, issue.getImages());
		
		writer.write("The Economist\n\n");
		writer.write(issue.getDateStamp().toString());
		writer.write("\n\n");
		writer.write("------\n\n");
		
		if (PrintEdition.isTheXmasIssue(issue.getDateStamp())) {
			writer.write("# The World this Year\n\n");
		} else {
			writer.write("# The World this Week\n\n");
		}
		awriter.write(new Article("Politics this Week", issue.getPoliticsThisWeek()));
		
		if (issue.getBusinessThisWeek() != null)
			awriter.write(new Article("Business this Week", issue.getBusinessThisWeek()));
		
		awriter.write(new Article("KAL's Cartoon", issue.getKalsCartoon()));
		
		for (String sectionName : issue.getOrderedSections()) {
			writer.write ("# " + sectionName + "\n\n");
			for (PlainArticle article : issue.getSections().get(sectionName)) {
				awriter.write (new Article (article));
			}
		}

	}
	
	
	
	
}
