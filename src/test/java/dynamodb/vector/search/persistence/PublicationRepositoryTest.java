package dynamodb.vector.search.persistence;

import static dynamodb.vector.search.fixtures.PublicationFixtures.randomEmbedding;
import static dynamodb.vector.search.fixtures.PublicationFixtures.randomPublication;
import static dynamodb.vector.search.persistence.PublicationTable.EMBEDDING_ATTRIBUTE;
import static dynamodb.vector.search.persistence.PublicationTable.PARTITION_KEY;
import static dynamodb.vector.search.persistence.PublicationTable.PUBLICATION_TYPE_ATTRIBUTE;
import static dynamodb.vector.search.persistence.PublicationTable.SORT_KEY;
import static dynamodb.vector.search.persistence.PublicationTable.VECTOR_INDEX_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import dynamodb.vector.search.domain.Embedding;
import dynamodb.vector.search.domain.Publication;
import dynamodb.vector.search.domain.PublicationType;
import java.util.HashMap;
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
import software.amazon.awssdk.services.dynamodb.model.SearchResultItem;
import software.amazon.awssdk.services.dynamodb.model.SearchVectorsRequest;
import software.amazon.awssdk.services.dynamodb.model.SearchVectorsResponse;
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

    @Test
    void shouldStorePublicationTypeAsTopLevelAttribute() {
        var publication = randomPublication();

        repository.save(publication, randomEmbedding());

        assertThat(storedItem(publication.identifier()),
                   hasEntry(PUBLICATION_TYPE_ATTRIBUTE,
                            AttributeValue.fromS(publication.publicationType().name())));
    }

    @Test
    void shouldSearchForPublications() {
        var searchClient = new RecordingDynamoDbClient(SearchVectorsResponse.builder().build());

        new PublicationRepository(searchClient, TABLE_NAME).searchPublications(EMBEDDING, null);

        assertThat(searchClient.recordedRequest(), is(equalTo(expectedSearchRequest())));
    }

    @Test
    void shouldFilterSearchByPublicationType() {
        var searchClient = new RecordingDynamoDbClient(SearchVectorsResponse.builder().build());

        new PublicationRepository(searchClient, TABLE_NAME).searchPublications(EMBEDDING, PublicationType.REPORT);

        assertThat(searchClient.recordedRequest(), is(equalTo(expectedFilteredSearchRequest())));
    }

    @Test
    void shouldMapSearchResultsToPublications() {
        var publication = randomPublication();
        var searchClient = new RecordingDynamoDbClient(searchResponseWith(publication));

        var found = new PublicationRepository(searchClient, TABLE_NAME).searchPublications(EMBEDDING, null);

        assertThat(found, is(equalTo(List.of(publication))));
    }

    private static SearchVectorsRequest expectedSearchRequest() {
        return SearchVectorsRequest.builder()
                   .tableName(TABLE_NAME)
                   .indexName(VECTOR_INDEX_NAME)
                   .topK(100)
                   .searchVector(AttributeValue.fromN("0.1"),
                                 AttributeValue.fromN("-0.2"),
                                 AttributeValue.fromN("1.6194582E-4"))
                   .build();
    }

    private static SearchVectorsRequest expectedFilteredSearchRequest() {
        return expectedSearchRequest().toBuilder()
                   .searchConditionExpression("publicationType = :publicationType")
                   .expressionAttributeValues(Map.of(":publicationType", AttributeValue.fromS("REPORT")))
                   .build();
    }

    private static SearchVectorsResponse searchResponseWith(Publication publication) {
        return SearchVectorsResponse.builder()
                   .searchResults(SearchResultItem.builder().item(projectedItem(publication)).score(0.9).build())
                   .build();
    }

    private static Map<String, AttributeValue> projectedItem(Publication publication) {
        var item = new HashMap<>(PublicationDao.fromPublication(publication).toItem(EMBEDDING));
        item.remove(EMBEDDING_ATTRIBUTE);
        return Map.copyOf(item);
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
