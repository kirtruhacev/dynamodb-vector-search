package dynamodb.vector.search.domain;

public interface TextEmbedder {

    Embedding embedDocument(String text);
}
