package dynamodb.vector.search.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import dynamodb.vector.search.fixtures.RecordingTextEmbedder;
import dynamodb.vector.search.persistence.PublicationRepository;
import dynamodb.vector.search.persistence.RecordingDynamoDbClient;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.SearchVectorsResponse;

class PublicationSearchTest {

    private static final String TABLE_NAME = "publications";
    private static final String QUERY = "vector search in practice";
    private static final Embedding EMBEDDING = new Embedding(List.of(0.1f, -0.2f));

    @Test
    void shouldSearchPublicationsMatchingQuery() {
        var textEmbedder = new RecordingTextEmbedder(EMBEDDING);
        var client = new RecordingDynamoDbClient(SearchVectorsResponse.builder().build());

        new PublicationSearch(textEmbedder, new PublicationRepository(client, TABLE_NAME)).search(QUERY, null);

        assertThat(textEmbedder.recordedQueryText(), is(equalTo(QUERY)));
        assertThat(searchedVector(client), is(equalTo(EMBEDDING.values())));
    }

    @Test
    void shouldSearchPublicationsOfGivenType() {
        var client = new RecordingDynamoDbClient(SearchVectorsResponse.builder().build());
        var search = new PublicationSearch(new RecordingTextEmbedder(EMBEDDING),
                                           new PublicationRepository(client, TABLE_NAME));

        search.search(QUERY, PublicationType.BOOK);

        assertThat(client.recordedRequest().searchConditionExpression(),
                   is(equalTo("publicationType = :publicationType")));
        assertThat(client.recordedRequest().expressionAttributeValues(),
                   is(equalTo(Map.of(":publicationType", AttributeValue.fromS("BOOK")))));
    }

    private static List<Float> searchedVector(RecordingDynamoDbClient client) {
        return client.recordedRequest().searchVector().stream().map(AttributeValue::n).map(Float::valueOf).toList();
    }
}
