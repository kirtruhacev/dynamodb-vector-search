package dynamodb.vector.search.fixtures;

import dynamodb.vector.search.domain.Organization;
import dynamodb.vector.search.domain.Contributor;
import dynamodb.vector.search.domain.Publication;
import java.util.List;
import java.util.UUID;

public final class PublicationFixtures {

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

    public static String randomString() {
        return UUID.randomUUID().toString();
    }
}
