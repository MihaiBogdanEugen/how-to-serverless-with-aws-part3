package de.mbe.tutorials.aws.serverless.movies.uploadmovieinfos.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
public class MoviesDynamoDbRepositoryTests {

    private static final String MOVIE_INFOS_TABLE = UUID.randomUUID().toString();

    private static DynamoDbClient dynamoDbClient;
    private MoviesDynamoDbRepository moviesDynamoDbRepository;

    @SuppressWarnings("rawtypes")
    @Container
    private static GenericContainer dynamoDbContainer = new GenericContainer<>("amazon/dynamodb-local:1.12.0")
            .withExposedPorts(8000);

    @BeforeAll
    public static void beforeAll() {

        final var address = dynamoDbContainer.getContainerIpAddress();
        final var port = dynamoDbContainer.getFirstMappedPort();
        final var dynamoDbUri = URI.create(String.format("http://%s:%s", address, port));

        dynamoDbClient = DynamoDbClient.builder()
                .endpointOverride(dynamoDbUri)
                .build();

        dynamoDbClient.createTable(CreateTableRequest.builder()
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName("movieId")
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .keySchema(KeySchemaElement.builder()
                        .attributeName("movieId")
                        .keyType(KeyType.HASH)
                        .build())
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(1L)
                        .writeCapacityUnits(1L)
                        .build())
                .tableName(MOVIE_INFOS_TABLE)
                .build());
    }

    @BeforeEach
    void beforeEach() {
        moviesDynamoDbRepository = new MoviesDynamoDbRepository(dynamoDbClient, MOVIE_INFOS_TABLE);
    }

    @Test
    void testSaveLines() {

        final var movieId1 = UUID.randomUUID().toString();
        final var name1 = UUID.randomUUID().toString();
        final var countryOfOrigin1 = UUID.randomUUID().toString();
        final var releaseDate1 = UUID.randomUUID().toString();

        final var movieId2 = UUID.randomUUID().toString();
        final var name2 = UUID.randomUUID().toString();
        final var countryOfOrigin2= UUID.randomUUID().toString();
        final var releaseDate2 = UUID.randomUUID().toString();

        final var movieId3 = UUID.randomUUID().toString();
        final var name3 = UUID.randomUUID().toString();
        final var countryOfOrigin3= UUID.randomUUID().toString();
        final var releaseDate3 = UUID.randomUUID().toString();

        final var lines = List.of(
                String.join(",", List.of(movieId1, name1, countryOfOrigin1, releaseDate1)),
                String.join(",", List.of(movieId2, name2, countryOfOrigin2, releaseDate2)),
                String.join(",", List.of(movieId3, name3, countryOfOrigin3, releaseDate3))
        );

        final var actualResult = moviesDynamoDbRepository.saveLines(lines);

        assertEquals(3, actualResult);

        final var actualLine1 = getMovieInfoById(movieId1);
        assertNotNull(actualLine1);
        assertEquals(4, actualLine1.size());
        assertEquals(name1, actualLine1.get(1));
        assertEquals(countryOfOrigin1, actualLine1.get(2));
        assertEquals(releaseDate1, actualLine1.get(3));

        final var actualLine2 = getMovieInfoById(movieId2);
        assertNotNull(actualLine2);
        assertEquals(4, actualLine2.size());
        assertEquals(name2, actualLine2.get(1));
        assertEquals(countryOfOrigin2, actualLine2.get(2));
        assertEquals(releaseDate2, actualLine2.get(3));

        final var actualLine3 = getMovieInfoById(movieId3);
        assertNotNull(actualLine3);
        assertEquals(4, actualLine3.size());
        assertEquals(name3, actualLine3.get(1));
        assertEquals(countryOfOrigin3, actualLine3.get(2));
        assertEquals(releaseDate3, actualLine3.get(3));
    }

    private List<String> getMovieInfoById(final String movieId) {

        final var getItemRequest = GetItemRequest.builder()
                .key(Map.of("movieId", AttributeValue.builder().s(movieId).build()))
                .tableName(MOVIE_INFOS_TABLE)
                .consistentRead(true)
                .build();

        final var getItemResponse = dynamoDbClient.getItem(getItemRequest);
        if (!getItemResponse.hasItem()) {
            return null;
        }

        final var attributes = getItemResponse.item();
        final var line = new ArrayList<String>();
        line.add(movieId);

        Optional.ofNullable(attributes.getOrDefault("name", null))
                .ifPresent(attribute -> line.add(attribute.s()));

        Optional.ofNullable(attributes.getOrDefault("countryOfOrigin", null))
                .ifPresent(attribute -> line.add(attribute.s()));

        Optional.ofNullable(attributes.getOrDefault("releaseDate", null))
                .ifPresent(attribute -> line.add(attribute.s()));

        return line;
    }
}
