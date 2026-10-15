package dynamodb.vector.search.fixtures;

import dynamodb.vector.search.domain.Embedding;
import dynamodb.vector.search.domain.Organization;
import dynamodb.vector.search.domain.Contributor;
import dynamodb.vector.search.domain.Publication;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public final class PublicationFixtures {

    private static final int EMBEDDING_DIMENSIONS = 1024;

    private PublicationFixtures() {
    }

    public static Publication randomPublication() {
        return new Publication(UUID.randomUUID(), randomString(), randomString(),
                               List.of(randomContributor(), randomContributor()));
    }

    public static Contributor randomContributor() {
        return new Contributor(UUID.randomUUID(), randomString(), randomOrganization());
    }

    public static Organization randomOrganization() {
        return new Organization(UUID.randomUUID(), randomString());
    }

    public static Embedding randomEmbedding() {
        var values = IntStream.range(0, EMBEDDING_DIMENSIONS)
                         .mapToObj(dimension -> ThreadLocalRandom.current().nextFloat())
                         .toList();
        return new Embedding(values);
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }
}
