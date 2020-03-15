package de.mbe.tutorials.aws.serverless.movies.getmovie.repository;

import de.mbe.tutorials.aws.serverless.movies.getmovie.TestUtils;
import de.mbe.tutorials.aws.serverless.movies.getmovie.repository.models.Movie;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
public class MoviesDynamoDbRepositoryTests implements TestUtils {

    private static final String MOVIES_TABLE = UUID.randomUUID().toString();

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
                .tableName(MOVIES_TABLE)
                .build());
    }

    @BeforeEach
    void beforeEach() {
        moviesDynamoDbRepository = new MoviesDynamoDbRepository(dynamoDbClient, MOVIES_TABLE);
    }

    @Test
    void testGetMovieById() {

        final var movieId = UUID.randomUUID().toString();
        final var expectedMovie = getRandomMovie(movieId);

        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(MOVIES_TABLE)
                .item(convertToDynamoDBItem(expectedMovie))
                .build());

        final var actualMovie = moviesDynamoDbRepository.getMovieById(movieId);

        assertNotNull(actualMovie);
        assertEquals(expectedMovie, actualMovie);
    }

    private static Map<String, AttributeValue> convertToDynamoDBItem(final Movie movie) {
        return Map.ofEntries(
                new AbstractMap.SimpleEntry<>("movieId", AttributeValue.builder().s(movie.getMovieId()).build()),
                new AbstractMap.SimpleEntry<>("name", AttributeValue.builder().s(movie.getName()).build()),
                new AbstractMap.SimpleEntry<>("countryOfOrigin", AttributeValue.builder().s(movie.getCountryOfOrigin()).build()),
                new AbstractMap.SimpleEntry<>("releaseDate", AttributeValue.builder().s(movie.getReleaseDate()).build()),
                new AbstractMap.SimpleEntry<>("imdbRating", AttributeValue.builder().n(movie.getImdbRating().toString()).build()),
                new AbstractMap.SimpleEntry<>("rottenTomatoesRating", AttributeValue.builder().n(movie.getRottenTomatoesRating().toString()).build())
        );
    }
}
