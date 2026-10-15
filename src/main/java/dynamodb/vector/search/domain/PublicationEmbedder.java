package dynamodb.vector.search.domain;

import static java.util.stream.Collectors.joining;

public class PublicationEmbedder {

    private static final String TITLE_LABEL = "Title: ";
    private static final String CONTRIBUTORS_LABEL = "Contributors: ";
    private static final String INSTITUTIONS_LABEL = "Institutions: ";
    private static final String DESCRIPTION_LABEL = "Description: ";
    private static final String NAME_SEPARATOR = "; ";
    private static final String LINE_SEPARATOR = "\n";

    private final TextEmbedder textEmbedder;

    public PublicationEmbedder(TextEmbedder textEmbedder) {
        this.textEmbedder = textEmbedder;
    }

    public Embedding embed(Publication publication) {
        return textEmbedder.embedDocument(labelledText(publication));
    }

    private static String labelledText(Publication publication) {
        return String.join(LINE_SEPARATOR,
                           TITLE_LABEL + publication.title(),
                           DESCRIPTION_LABEL + publication.description(),
                           CONTRIBUTORS_LABEL + contributorNames(publication),
                           INSTITUTIONS_LABEL + institutionNames(publication));
    }

    private static String contributorNames(Publication publication) {
        return publication.contributors().stream().map(Contributor::name).collect(joining(NAME_SEPARATOR));
    }

    private static String institutionNames(Publication publication) {
        return publication.contributors().stream()
                   .map(Contributor::organization)
                   .map(Organization::name)
                   .distinct()
                   .collect(joining(NAME_SEPARATOR));
    }
}
