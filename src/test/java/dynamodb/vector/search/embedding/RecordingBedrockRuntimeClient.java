package dynamodb.vector.search.embedding;

import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

class RecordingBedrockRuntimeClient implements BedrockRuntimeClient {

    private final InvokeModelResponse response;
    private InvokeModelRequest recordedRequest;

    RecordingBedrockRuntimeClient(InvokeModelResponse response) {
        this.response = response;
    }

    @Override
    public InvokeModelResponse invokeModel(InvokeModelRequest request) {
        recordedRequest = request;
        return response;
    }

    InvokeModelRequest recordedRequest() {
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
