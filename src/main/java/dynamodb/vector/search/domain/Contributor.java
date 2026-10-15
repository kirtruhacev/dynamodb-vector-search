package dynamodb.vector.search.domain;

import java.util.UUID;

public record Contributor(UUID identifier, String name, Organization organization) {

}
