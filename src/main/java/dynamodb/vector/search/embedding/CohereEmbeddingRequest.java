package dynamodb.vector.search.embedding;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

record CohereEmbeddingRequest(List<String> texts, @JsonProperty("input_type") String inputType, String truncate) {

}
