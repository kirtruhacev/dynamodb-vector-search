package dynamodb.vector.search.persistence;

import dynamodb.vector.search.domain.Publication;
import java.util.Optional;
import java.util.UUID;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

public class PublicationRepository {

    private final DynamoDbClient client;
    private final String tableName;

    public PublicationRepository(DynamoDbClient client, String tableName) {
        this.client = client;
        this.tableName = tableName;
    }

    public void save(Publication publication) {
        var item = PublicationDao.fromPublication(publication).toItem();
        client.putItem(PutItemRequest.builder().tableName(tableName).item(item).build());
    }

    public Optional<Publication> findByIdentifier(UUID identifier) {
        var request = GetItemRequest.builder().tableName(tableName).key(PublicationDao.keyOf(identifier)).build();
        var response = client.getItem(request);
        return Optional.of(response)
                   .filter(found -> found.hasItem())
                   .map(found -> PublicationDao.fromItem(identifier, found.item()).toPublication());
    }
}
