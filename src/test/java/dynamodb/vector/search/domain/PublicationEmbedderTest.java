package dynamodb.vector.search.domain;

import static dynamodb.vector.search.fixtures.PublicationFixtures.randomEmbedding;
import static dynamodb.vector.search.fixtures.PublicationFixtures.randomOrganization;
import static dynamodb.vector.search.fixtures.PublicationFixtures.randomPublication;
import static dynamodb.vector.search.fixtures.PublicationFixtures.randomPublicationType;
import static dynamodb.vector.search.fixtures.PublicationFixtures.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import dynamodb.vector.search.fixtures.RecordingTextEmbedder;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PublicationEmbedderTest {

    @Test
    void shouldEmbedLabelledPublicationText() {
        var publication = randomPublication();
        var textEmbedder = new RecordingTextEmbedder(randomEmbedding());

        new PublicationEmbedder(textEmbedder).embed(publication);

        assertThat(textEmbedder.recordedDocumentText(), is(equalTo(labelledText(publication))));
    }

    @Test
    void shouldListEachInstitutionOnceWhenContributorsShareOrganization() {
        var organization = randomOrganization();
        var textEmbedder = new RecordingTextEmbedder(randomEmbedding());

        new PublicationEmbedder(textEmbedder).embed(publicationWithContributorsFrom(organization));

        assertThat(textEmbedder.recordedDocumentText(), endsWith("Institutions: " + organization.name()));
    }

    @Test
    void shouldReturnEmbeddingFromTextEmbedder() {
        var embedding = randomEmbedding();
        var textEmbedder = new RecordingTextEmbedder(embedding);

        var generated = new PublicationEmbedder(textEmbedder).embed(randomPublication());

        assertThat(generated, is(equalTo(embedding)));
    }

    private static Publication publicationWithContributorsFrom(Organization organization) {
        var contributors = List.of(new Contributor(UUID.randomUUID(), randomString(), organization),
                                   new Contributor(UUID.randomUUID(), randomString(), organization));
        return new Publication(UUID.randomUUID(), randomString(), randomString(), randomPublicationType(),
                               contributors);
    }

    private static String labelledText(Publication publication) {
        var contributors = publication.contributors();
        return "Title: " + publication.title() + "\n"
               + "Description: " + publication.description() + "\n"
               + "Contributors: " + contributors.getFirst().name() + "; " + contributors.getLast().name() + "\n"
               + "Institutions: " + contributors.getFirst().organization().name() + "; "
               + contributors.getLast().organization().name();
    }
}
