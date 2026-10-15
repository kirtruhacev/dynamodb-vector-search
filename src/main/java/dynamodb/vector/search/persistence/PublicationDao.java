package dynamodb.vector.search.persistence;

import static dynamodb.vector.search.persistence.PublicationTable.DATA_ATTRIBUTE;
import static dynamodb.vector.search.persistence.PublicationTable.PARTITION_KEY;
import static dynamodb.vector.search.persistence.PublicationTable.SORT_KEY;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dynamodb.vector.search.domain.Publication;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

record PublicationDao(UUID identifier, PublicationData data) {

    private static final String PARTITION_KEY_PREFIX = "PUBLICATION#";
    private static final String SORT_KEY_VALUE = "PUBLICATION";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static PublicationDao fromPublication(Publication publication) {
        var data = new PublicationData(publication.title(), publication.description(), publication.contributors());
        return new PublicationDao(publication.identifier(), data);
    }

    static PublicationDao fromItem(UUID identifier, Map<String, AttributeValue> item) {
        return new PublicationDao(identifier, readData(item.get(DATA_ATTRIBUTE).s()));
    }

    static Map<String, AttributeValue> keyOf(UUID identifier) {
        return Map.of(PARTITION_KEY, AttributeValue.fromS(PARTITION_KEY_PREFIX + identifier),
                      SORT_KEY, AttributeValue.fromS(SORT_KEY_VALUE));
    }

    Map<String, AttributeValue> toItem() {
        var item = new HashMap<>(keyOf(identifier));
        item.put(DATA_ATTRIBUTE, AttributeValue.fromS(writeData(data)));
        return Map.copyOf(item);
    }

    Publication toPublication() {
        return new Publication(identifier, data.title(), data.description(), data.contributors());
    }

    private static String writeData(PublicationData data) {
        try {
            return OBJECT_MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static PublicationData readData(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, PublicationData.class);
        } catch (JsonProcessingException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
