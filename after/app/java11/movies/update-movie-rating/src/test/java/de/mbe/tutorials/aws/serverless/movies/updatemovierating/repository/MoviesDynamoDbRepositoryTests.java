package de.mbe.tutorials.aws.serverless.movies.updatemovierating.repository;

import de.mbe.tutorials.aws.serverless.movies.updatemovierating.repository.models.MovieRating;
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
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
public class MoviesDynamoDbRepositoryTests {

    private static final Random RANDOM = new Random();
    private static final String MOVIE_RATINGS_TABLE = UUID.randomUUID().toString();

    private static DynamoDbClient dynamoDbClient;

    private static MoviesDynamoDbRepository moviesDynamoDbRepository;

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
                .tableName(MOVIE_RATINGS_TABLE)
                .build());
    }

    @BeforeEach
    void beforeEach() {
        moviesDynamoDbRepository = new MoviesDynamoDbRepository(dynamoDbClient, MOVIE_RATINGS_TABLE);
    }

    @Test
    void testUpdateMovieRating() {

        final var movieId = UUID.randomUUID().toString();
        final var expectedMovieRating = getRandomMovieRating(movieId);

        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(MOVIE_RATINGS_TABLE)
                .item(convertToDynamoDBItem(expectedMovieRating))
                .build());

        expectedMovieRating.setImdbRating(RANDOM.nextInt(100));
        expectedMovieRating.setRottenTomatoesRating(RANDOM.nextInt(100));
        final var actualMovieRating = moviesDynamoDbRepository.updateMovieRating(expectedMovieRating);

        assertNotNull(actualMovieRating);
        assertNotEquals(expectedMovieRating, actualMovieRating);
    }

    private static MovieRating getRandomMovieRating(final String movieId) {
        final var movieRating = new MovieRating();
        movieRating.setMovieId(movieId);
        movieRating.setImdbRating(RANDOM.nextInt(100));
        movieRating.setRottenTomatoesRating(RANDOM.nextInt(100));
        return movieRating;
    }


    private static Map<String, AttributeValue> convertToDynamoDBItem(final MovieRating movieRating) {
        return Map.ofEntries(
                new AbstractMap.SimpleEntry<>("movieId", AttributeValue.builder().s(movieRating.getMovieId()).build()),
                new AbstractMap.SimpleEntry<>("imdbRating", AttributeValue.builder().n(movieRating.getImdbRating().toString()).build()),
                new AbstractMap.SimpleEntry<>("rottenTomatoesRating", AttributeValue.builder().n(movieRating.getRottenTomatoesRating().toString()).build())
        );
    }
}
