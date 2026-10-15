package dynamodb.vector.search.embedding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record CohereEmbeddingResponse(List<List<Float>> embeddings) {

}
