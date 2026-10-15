package dynamodb.vector.search.domain;

import java.util.List;
import java.util.UUID;

public record Publication(UUID identifier, String title, String description, List<Contributor> contributors) {

}
