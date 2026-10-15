package dynamodb.vector.search.persistence;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.SearchVectorsRequest;
import software.amazon.awssdk.services.dynamodb.model.SearchVectorsResponse;

public class RecordingDynamoDbClient implements DynamoDbClient {

    private final SearchVectorsResponse response;
    private SearchVectorsRequest recordedRequest;

    public RecordingDynamoDbClient(SearchVectorsResponse response) {
        this.response = response;
    }

    @Override
    public SearchVectorsResponse searchVectors(SearchVectorsRequest request) {
        recordedRequest = request;
        return response;
    }

    public SearchVectorsRequest recordedRequest() {
        return recordedRequest;
    }

    @Override
    public String serviceName() {
        return SERVICE_NAME;
    }

    @Override
    public void close() {
    }
}
