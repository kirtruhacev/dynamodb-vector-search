package dynamodb.vector.search.embedding;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import dynamodb.vector.search.domain.Embedding;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

class CohereTextEmbedderTest {

    private static final String MODEL_ID = "cohere.embed-multilingual-v3";
    private static final String TEXT_TO_EMBED = "Title: Vector search in practice";
    private static final List<Float> EMBEDDING_VALUES = List.of(0.1f, -0.2f, 0.3f);

    private RecordingBedrockRuntimeClient client;
    private CohereTextEmbedder embedder;

    @BeforeEach
    void setUp() {
        client = new RecordingBedrockRuntimeClient(embeddingResponse());
        embedder = new CohereTextEmbedder(client);
    }

    @Test
    void shouldRequestDocumentEmbeddingForGivenText() {
        embedder.embedDocument(TEXT_TO_EMBED);

        assertThat(client.recordedRequest().modelId(), is(equalTo(MODEL_ID)));
        assertThat(client.recordedRequest().body().asUtf8String(), is(equalTo(documentEmbeddingBody())));
    }

    @Test
    void shouldReturnEmbeddingFromModelResponse() {
        assertThat(embedder.embedDocument(TEXT_TO_EMBED), is(equalTo(new Embedding(EMBEDDING_VALUES))));
    }

    @Test
    void shouldThrowWhenModelReturnsNoEmbedding() {
        var embedder = new CohereTextEmbedder(new RecordingBedrockRuntimeClient(responseWithoutEmbeddings()));

        var exception = assertThrows(IllegalStateException.class, () -> embedder.embedDocument(TEXT_TO_EMBED));

        assertThat(exception.getMessage(), containsString(MODEL_ID));
    }

    private static String documentEmbeddingBody() {
        return """
            {"texts":["%s"],"input_type":"search_document","truncate":"END"}""".formatted(TEXT_TO_EMBED);
    }

    private static InvokeModelResponse responseWithoutEmbeddings() {
        var body = """
            {"id":"5a1f6b3c","response_type":"embeddings_floats"}""";
        return InvokeModelResponse.builder().body(SdkBytes.fromUtf8String(body)).build();
    }

    private static InvokeModelResponse embeddingResponse() {
        var body = """
            {"id":"5a1f6b3c","response_type":"embeddings_floats","embeddings":[[0.1,-0.2,0.3]]}""";
        return InvokeModelResponse.builder().body(SdkBytes.fromUtf8String(body)).build();
    }
}
