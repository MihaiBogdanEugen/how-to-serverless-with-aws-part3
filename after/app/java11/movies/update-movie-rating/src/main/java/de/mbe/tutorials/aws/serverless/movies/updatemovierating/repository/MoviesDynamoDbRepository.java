package de.mbe.tutorials.aws.serverless.movies.updatemovierating.repository;

import de.mbe.tutorials.aws.serverless.movies.updatemovierating.repository.models.Movie;
import de.mbe.tutorials.aws.serverless.movies.updatemovierating.repository.models.MovieRating;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MoviesDynamoDbRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String moviesTable;

    public MoviesDynamoDbRepository(DynamoDbClient dynamoDbClient, String moviesTable) {
        this.dynamoDbClient = dynamoDbClient;
        this.moviesTable = moviesTable;
    }

    public Movie updateMovieRating(final MovieRating movieRating) {

        final var updateExpressionLines = new ArrayList<String>();
        final var expressionAttributeValues = new HashMap<String, AttributeValue>();
        final var expressionAttributeNames = new HashMap<String, String>();

        Optional.ofNullable(movieRating.getImdbRating()).ifPresent(value -> {
            updateExpressionLines.add("#i = :imdbRating");
            expressionAttributeValues.put(":imdbRating", AttributeValue.builder().n(value.toString()).build());
            expressionAttributeNames.put("#i", "imdbRating");
        });

        Optional.ofNullable(movieRating.getRottenTomatoesRating()).ifPresent(value -> {
            updateExpressionLines.add("#ro = :rottenTomatoesRating");
            expressionAttributeValues.put(":rottenTomatoesRating", AttributeValue.builder().n(value.toString()).build());
            expressionAttributeNames.put("#ro", "rottenTomatoesRating");
        });

        if (updateExpressionLines.isEmpty()) {
            return null;
        }

        final var updateExpression = "SET " + String.join(", ", updateExpressionLines);

        final var updateItemRequest = UpdateItemRequest.builder()
                .key(Map.of("movieId", AttributeValue.builder().s(movieRating.getMovieId()).build()))
                .tableName(moviesTable)
                .updateExpression(updateExpression)
                .expressionAttributeValues(expressionAttributeValues)
                .expressionAttributeNames(expressionAttributeNames)
                .returnValues(ReturnValue.ALL_NEW)
                .build();

        final var attributes = dynamoDbClient.updateItem(updateItemRequest).attributes();

        return convert(attributes);
    }

    private static Movie convert(final Map<String, AttributeValue> attributes) {

        final var movie = new Movie();

        Optional.ofNullable(attributes.getOrDefault("movieId", null))
                .ifPresent(attribute -> movie.setMovieId(attribute.s()));

        Optional.ofNullable(attributes.getOrDefault("name", null))
                .ifPresent(attribute -> movie.setName(attribute.s()));

        Optional.ofNullable(attributes.getOrDefault("countryOfOrigin", null))
                .ifPresent(attribute -> movie.setCountryOfOrigin(attribute.s()));

        Optional.ofNullable(attributes.getOrDefault("releaseDate", null))
                .ifPresent(attribute -> movie.setReleaseDate(attribute.s()));

        Optional.ofNullable(attributes.getOrDefault("rottenTomatoesRating", null))
                .ifPresent(attribute -> movie.setRottenTomatoesRating(Integer.parseInt(attribute.n())));

        Optional.ofNullable(attributes.getOrDefault("imdbRating", null))
                .ifPresent(attribute -> movie.setImdbRating(Integer.parseInt(attribute.n())));

        return movie;
    }
}
