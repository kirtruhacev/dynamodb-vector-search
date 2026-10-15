package dynamodb.vector.search.fixtures;

import dynamodb.vector.search.domain.Embedding;
import dynamodb.vector.search.domain.TextEmbedder;

public class RecordingTextEmbedder implements TextEmbedder {

    private final Embedding embedding;
    private String recordedText;

    public RecordingTextEmbedder(Embedding embedding) {
        this.embedding = embedding;
    }

    @Override
    public Embedding embedDocument(String text) {
        recordedText = text;
        return embedding;
    }

    public String recordedText() {
        return recordedText;
    }
}
