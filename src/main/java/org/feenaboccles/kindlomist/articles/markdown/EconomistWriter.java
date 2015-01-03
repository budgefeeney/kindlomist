package org.feenaboccles.kindlomist.articles.markdown;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.time.LocalDate;

import org.feenaboccles.kindlomist.articles.Economist;
import org.feenaboccles.kindlomist.articles.PlainArticle;

/**
 * Writes out the Economist issue as a pandoc-compatible Markdown
 * file with YAML metadata.
 *  <p>
 * The precise pandoc incantation required to compile it to epub
 * is then
 * <p>
 * <pre>
 * pandoc -S  --epub-chapter-level 1 --toc --toc-depth 2 -o economist.epub economist.md
 * </pre>
 * @author bryanfeeney
 *
 */
public class EconomistWriter 
{

	// TODO Proper title, nicely formated date
	public void writeEconomist (Writer writer, Economist issue) throws IOException {
		ArticleWriter awriter = new ArticleWriter (writer, issue.getImages());
		
		// YAML Header with title etc.
		DateFormat fmt = DateFormat.getDateInstance(DateFormat.LONG);
		LocalDate pubDate = issue.getDateStamp();
		writer.write ("---");
		writer.write("title: The Economist, " + fmt.format(pubDate));
		writer.write("date: " + pubDate.toString());
		writer.write("\n\n");
		writer.write("---\n\n");
		
		if (issue.isTheXmasIssue()) {
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
