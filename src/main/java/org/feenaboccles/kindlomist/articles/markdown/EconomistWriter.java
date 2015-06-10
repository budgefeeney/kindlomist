package org.feenaboccles.kindlomist.articles.markdown;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.feenaboccles.kindlomist.articles.Economist;
import org.feenaboccles.kindlomist.articles.ImageResolver;
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

	private EconomistWriter() { }

	// TODO Proper title, nicely formated date
	public static void write (Writer writer, Economist issue) throws IOException {
		ImageResolver images = issue.getImages();

		// YAML Header with title etc.
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM d, yyyy");
		LocalDate pubDate = issue.getDateStamp();
		writer.write ("---\n");
		writer.write("title: The Economist, " + fmt.format(pubDate) + "\n");
		writer.write("date: " + pubDate.toString() + "\n");
		writer.write("\n\n");
		writer.write("---\n\n");
		
		if (issue.isTheXmasIssue()) {
			writer.write("# The World this Year\n\n");
		} else {
			writer.write("# The World this Week\n\n");
		}
		new Article("Politics this Week", issue.getPoliticsThisWeek()).write(writer, images);
		
		if (issue.getBusinessThisWeek() != null)
			new Article("Business this Week", issue.getBusinessThisWeek()).write(writer, images);
		
		new Article("KAL's Cartoon", issue.getKalsCartoon()).write(writer, images);
		
		for (String sectionName : issue.getOrderedSections()) {
			writer.write ("# " + sectionName + "\n\n");
			for (PlainArticle article : issue.getSections().get(sectionName)) {
				new Article (article).write(writer, images);
			}
		}

	}
}
