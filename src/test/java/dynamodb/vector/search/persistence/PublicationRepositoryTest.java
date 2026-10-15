package dynamodb.vector.search.persistence;

import static dynamodb.vector.search.fixtures.PublicationFixtures.randomPublication;
import static dynamodb.vector.search.persistence.PublicationTable.PARTITION_KEY;
import static dynamodb.vector.search.persistence.PublicationTable.SORT_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.dynamodb.services.local.embedded.DynamoDBEmbedded;
import software.amazon.dynamodb.services.local.shared.access.AmazonDynamoDBLocal;

class PublicationRepositoryTest {

    private static final String TABLE_NAME = "publications";

    private AmazonDynamoDBLocal database;
    private PublicationRepository repository;

    @BeforeEach
    void setUp() {
        database = DynamoDBEmbedded.create();
        var client = database.dynamoDbClient();
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

        repository.save(publication);

        assertThat(repository.findByIdentifier(publication.identifier()), is(equalTo(Optional.of(publication))));
    }

    @Test
    void shouldReturnEmptyWhenPublicationDoesNotExist() {
        assertThat(repository.findByIdentifier(UUID.randomUUID()), is(equalTo(Optional.empty())));
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
