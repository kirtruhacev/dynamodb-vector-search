package dynamodb.vector.search.persistence;

import static dynamodb.vector.search.persistence.PublicationTable.VECTOR_INDEX_NAME;
import dynamodb.vector.search.domain.Embedding;
import dynamodb.vector.search.domain.Publication;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.SearchVectorsRequest;

public class PublicationRepository {

    private static final int TOP_K = 100;

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

    public List<Publication> searchPublications(Embedding embedding) {
        var response = client.searchVectors(searchVectorsRequest(embedding));
        return response.searchResults().stream()
                   .map(result -> PublicationDao.fromItem(result.item()).toPublication())
                   .toList();
    }

    private SearchVectorsRequest searchVectorsRequest(Embedding embedding) {
        return SearchVectorsRequest.builder()
                   .tableName(tableName)
                   .indexName(VECTOR_INDEX_NAME)
                   .topK(TOP_K)
                   .searchVector(PublicationDao.embeddingNumbers(embedding))
                   .build();
    }

    public Optional<Publication> findByIdentifier(UUID identifier) {
        var request = GetItemRequest.builder().tableName(tableName).key(PublicationDao.keyOf(identifier)).build();
        var response = client.getItem(request);
        return Optional.of(response)
                   .filter(found -> found.hasItem())
                   .map(found -> PublicationDao.fromItem(found.item()).toPublication());
    }
}
