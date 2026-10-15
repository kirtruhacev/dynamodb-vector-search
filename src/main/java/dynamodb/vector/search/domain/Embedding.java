package dynamodb.vector.search.domain;

import java.util.List;

public record Embedding(List<Float> values) {

}
