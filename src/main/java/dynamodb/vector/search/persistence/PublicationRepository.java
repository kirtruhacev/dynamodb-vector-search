package dynamodb.vector.search.persistence;

import static dynamodb.vector.search.persistence.PublicationTable.PUBLICATION_TYPE_ATTRIBUTE;
import static dynamodb.vector.search.persistence.PublicationTable.VECTOR_INDEX_NAME;
import dynamodb.vector.search.domain.Embedding;
import dynamodb.vector.search.domain.Publication;
import dynamodb.vector.search.domain.PublicationType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.SearchVectorsRequest;

public class PublicationRepository {

    private static final int TOP_K = 100;
    private static final String PUBLICATION_TYPE_PLACEHOLDER = ":publicationType";
    private static final String PUBLICATION_TYPE_CONDITION =
        PUBLICATION_TYPE_ATTRIBUTE + " = " + PUBLICATION_TYPE_PLACEHOLDER;

    private final DynamoDbClient client;
    private final String tableName;

    public PublicationRepository(DynamoDbClient client, String tableName) {
        this.client = client;
        this.tableName = tableName;
    }

    public void save(Publication publication, Embedding embedding) {
        var item = PublicationDao.fromPublication(publication).toItem(embedding);
        client.putItem(PutItemRequest.builder().tableName(tableName).item(item).build());
    }

    public List<Publication> searchPublications(Embedding embedding, PublicationType publicationType) {
        var request = searchVectorsRequest(embedding);
        return publicationsMatching(Optional.ofNullable(publicationType)
                                        .map(type -> filteredBy(request, type))
                                        .orElse(request)
                                        .build());
    }

    private static SearchVectorsRequest.Builder filteredBy(SearchVectorsRequest.Builder request,
                                                           PublicationType publicationType) {
        return request.searchConditionExpression(PUBLICATION_TYPE_CONDITION)
                   .expressionAttributeValues(publicationTypeValue(publicationType));
    }

    private List<Publication> publicationsMatching(SearchVectorsRequest request) {
        return client.searchVectors(request).searchResults().stream()
                   .map(result -> PublicationDao.fromItem(result.item()).toPublication())
                   .toList();
    }

    private static Map<String, AttributeValue> publicationTypeValue(PublicationType publicationType) {
        return Map.of(PUBLICATION_TYPE_PLACEHOLDER, AttributeValue.fromS(publicationType.name()));
    }

    private SearchVectorsRequest.Builder searchVectorsRequest(Embedding embedding) {
        return SearchVectorsRequest.builder()
                   .tableName(tableName)
                   .indexName(VECTOR_INDEX_NAME)
                   .topK(TOP_K)
                   .searchVector(PublicationDao.embeddingNumbers(embedding));
    }

    public Optional<Publication> findByIdentifier(UUID identifier) {
        var request = GetItemRequest.builder().tableName(tableName).key(PublicationDao.keyOf(identifier)).build();
        var response = client.getItem(request);
        return Optional.of(response)
                   .filter(GetItemResponse::hasItem)
                   .map(found -> PublicationDao.fromItem(found.item()).toPublication());
    }
}
