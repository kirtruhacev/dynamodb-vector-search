package dynamodb.vector.search.persistence;

public final class PublicationTable {

    public static final String PARTITION_KEY = "PK0";
    public static final String SORT_KEY = "SK0";
    public static final String DATA_ATTRIBUTE = "data";
    public static final String EMBEDDING_ATTRIBUTE = "embedding";

    private PublicationTable() {
    }
}
