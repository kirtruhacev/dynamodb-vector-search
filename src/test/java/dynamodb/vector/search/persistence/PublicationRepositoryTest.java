package dynamodb.vector.search.persistence;

import static dynamodb.vector.search.fixtures.PublicationFixtures.randomEmbedding;
import static dynamodb.vector.search.fixtures.PublicationFixtures.randomPublication;
import static dynamodb.vector.search.persistence.PublicationTable.EMBEDDING_ATTRIBUTE;
import static dynamodb.vector.search.persistence.PublicationTable.PARTITION_KEY;
import static dynamodb.vector.search.persistence.PublicationTable.SORT_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import dynamodb.vector.search.domain.Embedding;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.dynamodb.services.local.embedded.DynamoDBEmbedded;
import software.amazon.dynamodb.services.local.shared.access.AmazonDynamoDBLocal;

class PublicationRepositoryTest {

    private static final String TABLE_NAME = "publications";
    private static final Embedding EMBEDDING = new Embedding(List.of(0.1f, -0.2f, 1.6194582E-4f));

    private AmazonDynamoDBLocal database;
    private DynamoDbClient client;
    private PublicationRepository repository;

    @BeforeEach
    void setUp() {
        database = DynamoDBEmbedded.create();
        client = database.dynamoDbClient();
        client.createTable(createTableRequest());
        repository = new PublicationRepository(client, TABLE_NAME);
    }

    @AfterEach
    void tearDown() {
        database.shutdown();
    }

    @Test
    void shouldPersistAndReadBackPublication() {
        var publication = randomPublication();

        repository.save(publication, randomEmbedding());

        assertThat(repository.findByIdentifier(publication.identifier()), is(equalTo(Optional.of(publication))));
    }

    @Test
    void shouldReturnEmptyWhenPublicationDoesNotExist() {
        assertThat(repository.findByIdentifier(UUID.randomUUID()), is(equalTo(Optional.empty())));
    }

    @Test
    void shouldStoreEmbeddingAsListOfNumbers() {
        var publication = randomPublication();

        repository.save(publication, EMBEDDING);

        assertThat(storedNumbers(publication.identifier()), is(equalTo(EMBEDDING.values())));
    }

    private List<Float> storedNumbers(UUID identifier) {
        return storedItem(identifier).get(EMBEDDING_ATTRIBUTE).l().stream()
                   .map(AttributeValue::n)
                   .map(Float::valueOf)
                   .toList();
    }

    private Map<String, AttributeValue> storedItem(UUID identifier) {
        var request = GetItemRequest.builder().tableName(TABLE_NAME).key(PublicationDao.keyOf(identifier)).build();
        return client.getItem(request).item();
    }

    private static CreateTableRequest createTableRequest() {
        return CreateTableRequest.builder()
                   .tableName(TABLE_NAME)
                   .billingMode(BillingMode.PAY_PER_REQUEST)
                   .attributeDefinitions(
                       AttributeDefinition.builder().attributeName(PARTITION_KEY).attributeType(ScalarAttributeType.S).build(),
                       AttributeDefinition.builder().attributeName(SORT_KEY).attributeType(ScalarAttributeType.S).build())
                   .keySchema(
                       KeySchemaElement.builder().attributeName(PARTITION_KEY).keyType(KeyType.HASH).build(),
                       KeySchemaElement.builder().attributeName(SORT_KEY).keyType(KeyType.RANGE).build())
                   .build();
    }
}
