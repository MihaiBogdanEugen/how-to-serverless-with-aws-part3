package de.mbe.tutorials.aws.serverless.movies.getmovie.repository;

import de.mbe.tutorials.aws.serverless.movies.getmovie.TestUtils;
import de.mbe.tutorials.aws.serverless.movies.getmovie.repository.models.MovieInfo;
import de.mbe.tutorials.aws.serverless.movies.getmovie.repository.models.MovieRating;
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

    private static final String MOVIE_INFOS_TABLE = UUID.randomUUID().toString();
    private static final String MOVIE_RATINGS_TABLE = UUID.randomUUID().toString();

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
                .tableName(MOVIE_RATINGS_TABLE)
                .build());
    }

    @BeforeEach
    void beforeEach() {
        moviesDynamoDbRepository = new MoviesDynamoDbRepository(dynamoDbClient, MOVIE_INFOS_TABLE, MOVIE_RATINGS_TABLE);
    }

    @Test
    void testGetMovieById() {

        final var movieId = UUID.randomUUID().toString();
        final var movieInfo = getRandomMovieInfo(movieId);
        final var movieRating = getRandomMovieRating(movieId);
        final var expectedMovie = getRandomMovie(movieId, movieInfo, movieRating);

        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(MOVIE_INFOS_TABLE)
                .item(convertToDynamoDBItem(movieInfo))
                .build());

        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(MOVIE_RATINGS_TABLE)
                .item(convertToDynamoDBItem(movieRating))
                .build());

        final var actualMovie = moviesDynamoDbRepository.getMovieById(movieId);

        assertNotNull(actualMovie);
        assertEquals(expectedMovie, actualMovie);
    }

    private static Map<String, AttributeValue> convertToDynamoDBItem(final MovieInfo movieInfo) {
        return Map.ofEntries(
                new AbstractMap.SimpleEntry<>("movieId", AttributeValue.builder().s(movieInfo.getMovieId()).build()),
                new AbstractMap.SimpleEntry<>("name", AttributeValue.builder().s(movieInfo.getName()).build()),
                new AbstractMap.SimpleEntry<>("countryOfOrigin", AttributeValue.builder().s(movieInfo.getCountryOfOrigin()).build()),
                new AbstractMap.SimpleEntry<>("releaseDate", AttributeValue.builder().s(movieInfo.getReleaseDate()).build())
        );
    }

    private static Map<String, AttributeValue> convertToDynamoDBItem(final MovieRating movieRating) {
        return Map.ofEntries(
                new AbstractMap.SimpleEntry<>("movieId", AttributeValue.builder().s(movieRating.getMovieId()).build()),
                new AbstractMap.SimpleEntry<>("imdbRating", AttributeValue.builder().n(movieRating.getImdbRating().toString()).build()),
                new AbstractMap.SimpleEntry<>("rottenTomatoesRating", AttributeValue.builder().n(movieRating.getRottenTomatoesRating().toString()).build())
        );
    }
}
