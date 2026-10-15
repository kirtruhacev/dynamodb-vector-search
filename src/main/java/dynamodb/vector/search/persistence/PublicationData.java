package dynamodb.vector.search.persistence;

import dynamodb.vector.search.domain.Contributor;
import java.util.List;

record PublicationData(String title, String description, List<Contributor> contributors) {

}
