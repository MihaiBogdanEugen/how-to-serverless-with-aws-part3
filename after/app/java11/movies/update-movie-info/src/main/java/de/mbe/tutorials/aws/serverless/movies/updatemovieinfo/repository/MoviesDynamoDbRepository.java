package de.mbe.tutorials.aws.serverless.movies.updatemovieinfo.repository;

import de.mbe.tutorials.aws.serverless.movies.updatemovieinfo.repository.models.Movie;
import de.mbe.tutorials.aws.serverless.movies.updatemovieinfo.repository.models.MovieInfo;
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

    public Movie updateMovieInfo(final MovieInfo movieInfo) {

        final var updateExpressionLines = new ArrayList<String>();
        final var expressionAttributeValues = new HashMap<String, AttributeValue>();
        final var expressionAttributeNames = new HashMap<String, String>();

        Optional.ofNullable(movieInfo.getName()).ifPresent(value -> {
            updateExpressionLines.add("#n = :name");
            expressionAttributeValues.put(":name", AttributeValue.builder().s(value).build());
            expressionAttributeNames.put("#n", "name");
        });

        Optional.ofNullable(movieInfo.getCountryOfOrigin()).ifPresent(value -> {
            updateExpressionLines.add("#c = :countryOfOrigin");
            expressionAttributeValues.put(":countryOfOrigin", AttributeValue.builder().s(value).build());
            expressionAttributeNames.put("#c", "countryOfOrigin");
        });

        Optional.ofNullable(movieInfo.getReleaseDate()).ifPresent(value -> {
            updateExpressionLines.add("#r = :releaseDate");
            expressionAttributeValues.put(":releaseDate", AttributeValue.builder().s(value).build());
            expressionAttributeNames.put("#r", "releaseDate");
        });

        if (updateExpressionLines.isEmpty()) {
            return null;
        }

        final var updateExpression = "SET " + String.join(", ", updateExpressionLines);

        final var updateItemRequest = UpdateItemRequest.builder()
                .key(Map.of("movieId", AttributeValue.builder().s(movieInfo.getMovieId()).build()))
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

        Optional.ofNullable(attributes.getOrDefault("id", null))
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
