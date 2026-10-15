package dynamodb.vector.search.fixtures;

import dynamodb.vector.search.domain.Embedding;
import dynamodb.vector.search.domain.TextEmbedder;

public class RecordingTextEmbedder implements TextEmbedder {

    private final Embedding embedding;
    private String recordedDocumentText;
    private String recordedQueryText;

    public RecordingTextEmbedder(Embedding embedding) {
        this.embedding = embedding;
    }

    @Override
    public Embedding embedDocument(String text) {
        recordedDocumentText = text;
        return embedding;
    }

    @Override
    public Embedding embedQuery(String text) {
        recordedQueryText = text;
        return embedding;
    }

    public String recordedDocumentText() {
        return recordedDocumentText;
    }

    public String recordedQueryText() {
        return recordedQueryText;
    }
}
