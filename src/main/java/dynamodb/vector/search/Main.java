package dynamodb.vector.search;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dynamodb.vector.search.domain.Publication;
import dynamodb.vector.search.domain.PublicationEmbedder;
import dynamodb.vector.search.domain.PublicationSearch;
import dynamodb.vector.search.embedding.CohereTextEmbedder;
import dynamodb.vector.search.persistence.PublicationRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public final class Main {

    private static final String DATASET_RESOURCE = "/publications.json";
    private static final String TABLE_NAME = "publications";
    private static final String USAGE_MESSAGE = "Usage: dynamodb-vector-search <query>";
    private static final String ARGUMENT_SEPARATOR = " ";
    private static final String SEARCHING_MESSAGE = "Searching for: ";
    private static final String RESULT_SEPARATOR = "  ";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Main() {
    }

    public static void main(String... arguments) {
        query(arguments).ifPresentOrElse(Main::indexAndSearch, () -> IO.println(USAGE_MESSAGE));
    }

    private static void indexAndSearch(String query) {
        var textEmbedder = new CohereTextEmbedder(BedrockRuntimeClient.create());
        var publicationEmbedder = new PublicationEmbedder(textEmbedder);
        var repository = new PublicationRepository(DynamoDbClient.create(), TABLE_NAME);

        publications().forEach(publication -> save(repository, publicationEmbedder, publication));

        IO.println(SEARCHING_MESSAGE + query);
        new PublicationSearch(textEmbedder, repository).search(query).forEach(Main::print);
    }

    private static void save(PublicationRepository repository,
                             PublicationEmbedder publicationEmbedder,
                             Publication publication) {
        repository.save(publication, publicationEmbedder.embed(publication));
    }

    private static void print(Publication publication) {
        IO.println(publication.identifier() + RESULT_SEPARATOR + publication.title());
    }

    private static List<Publication> publications() {
        try (var dataset = Main.class.getResourceAsStream(DATASET_RESOURCE)) {
            return OBJECT_MAPPER.readValue(dataset, new TypeReference<List<Publication>>() {
            });
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static Optional<String> query(String... arguments) {
        return Optional.of(String.join(ARGUMENT_SEPARATOR, arguments)).filter(text -> !text.isBlank());
    }
}
