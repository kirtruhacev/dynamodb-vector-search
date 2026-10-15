package dynamodb.vector.search.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import dynamodb.vector.search.fixtures.RecordingTextEmbedder;
import dynamodb.vector.search.persistence.PublicationRepository;
import dynamodb.vector.search.persistence.RecordingDynamoDbClient;
import java.util.List;
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

        new PublicationSearch(textEmbedder, new PublicationRepository(client, TABLE_NAME)).search(QUERY);

        assertThat(textEmbedder.recordedQueryText(), is(equalTo(QUERY)));
        assertThat(searchedVector(client), is(equalTo(EMBEDDING.values())));
    }

    private static List<Float> searchedVector(RecordingDynamoDbClient client) {
        return client.recordedRequest().searchVector().stream().map(AttributeValue::n).map(Float::valueOf).toList();
    }
}
