package dynamodb.vector.search.embedding;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dynamodb.vector.search.domain.Embedding;
import dynamodb.vector.search.domain.TextEmbedder;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

public class CohereTextEmbedder implements TextEmbedder {

    private static final String MODEL_ID = "cohere.embed-multilingual-v3";
    private static final String DOCUMENT_INPUT_TYPE = "search_document";
    private static final String TRUNCATE_END = "END";
    private static final String NO_EMBEDDING_MESSAGE = "No embedding returned by model " + MODEL_ID;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final BedrockRuntimeClient client;

    public CohereTextEmbedder(BedrockRuntimeClient client) {
        this.client = client;
    }

    @Override
    public Embedding embedDocument(String text) {
        var request = new CohereEmbeddingRequest(List.of(text), DOCUMENT_INPUT_TYPE, TRUNCATE_END);
        var response = client.invokeModel(invokeModelRequest(request));
        return new Embedding(firstEmbedding(readResponse(response)));
    }

    private static List<Float> firstEmbedding(CohereEmbeddingResponse response) {
        return Optional.ofNullable(response.embeddings())
                   .filter(embeddings -> !embeddings.isEmpty())
                   .map(List::getFirst)
                   .orElseThrow(() -> new IllegalStateException(NO_EMBEDDING_MESSAGE));
    }

    private static InvokeModelRequest invokeModelRequest(CohereEmbeddingRequest request) {
        return InvokeModelRequest.builder()
                   .modelId(MODEL_ID)
                   .body(SdkBytes.fromUtf8String(writeRequest(request)))
                   .build();
    }

    private static String writeRequest(CohereEmbeddingRequest request) {
        try {
            return OBJECT_MAPPER.writeValueAsString(request);
        } catch (JsonProcessingException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static CohereEmbeddingResponse readResponse(InvokeModelResponse response) {
        try {
            return OBJECT_MAPPER.readValue(response.body().asUtf8String(), CohereEmbeddingResponse.class);
        } catch (JsonProcessingException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
