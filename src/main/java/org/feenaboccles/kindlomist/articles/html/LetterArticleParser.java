package org.feenaboccles.kindlomist.articles.html;

import org.feenaboccles.kindlomist.articles.content.Content;
import org.feenaboccles.kindlomist.articles.content.LetterAuthor;
import org.jsoup.nodes.Element;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Parses the letters article
 */
public class LetterArticleParser extends PlainArticleParser {

    /**
     * {@inheritDoc}
     * <p>
     * This handles the special case of Letter articles, for which no "title" is
     * defined. It works around the case of having to create another special-case
     * article class.
     * @param header the headers to mutate in place
     * @param content the article content, helps with the decisions
     * @param image the article's main image, if any
     * @param imageCaption the caption associated with the article's
     *                     main image, if any
     * @return the passed in header object, which will have been mutated
     * in-place.
     */
    @Override
    protected ArticleHeader cleanHeaders(ArticleHeader header, List<Content> content, Optional<URI> image, Optional<String> imageCaption) {
        // TODO Preconditions.

        header.setTitle("Letters");
        String tmp = header.getTopic();
        header.setTopic("Our Readers Respond");
        header.setStrap(tmp);

        return header;
    }

    /**
     * Reads the {@link Content} of an article from the given
     * DIV. Does not support the conversion of short text to headings, but
     * does support the use of {@link LetterAuthor} tags.
     */
    protected List<Content> readContent(Element bodyDiv) {
        return readContent(
                bodyDiv,
				/* convertShortTextToHeading = */ false,
				/* permitLetterAuthor = */ true);
    }
}
