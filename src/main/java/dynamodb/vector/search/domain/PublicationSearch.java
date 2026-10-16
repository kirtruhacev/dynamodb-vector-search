package dynamodb.vector.search.domain;

import dynamodb.vector.search.persistence.PublicationRepository;
import java.util.List;

public class PublicationSearch {

    private final TextEmbedder textEmbedder;
    private final PublicationRepository publicationRepository;

    public PublicationSearch(TextEmbedder textEmbedder, PublicationRepository publicationRepository) {
        this.textEmbedder = textEmbedder;
        this.publicationRepository = publicationRepository;
    }

    public List<Publication> search(String query, PublicationType publicationType) {
        return publicationRepository.searchPublications(textEmbedder.embedQuery(query), publicationType);
    }
}
