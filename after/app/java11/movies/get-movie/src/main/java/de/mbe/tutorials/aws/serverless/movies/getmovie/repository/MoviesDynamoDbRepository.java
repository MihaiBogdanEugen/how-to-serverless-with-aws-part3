package de.mbe.tutorials.aws.serverless.movies.getmovie.repository;

import de.mbe.tutorials.aws.serverless.movies.getmovie.repository.models.Movie;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.Map;
import java.util.Optional;

public class MoviesDynamoDbRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String moviesTable;

    public MoviesDynamoDbRepository(final DynamoDbClient dynamoDbClient, final String moviesTable) {
        this.dynamoDbClient = dynamoDbClient;
        this.moviesTable = moviesTable;
    }

    public Movie getMovieById(final String movieId) {

        final var getItemRequest = GetItemRequest.builder()
                .key(Map.of("movieId", AttributeValue.builder().s(movieId).build()))
                .tableName(moviesTable)
                .consistentRead(true)
                .build();

        final var getItemResponse = dynamoDbClient.getItem(getItemRequest);
        if (!getItemResponse.hasItem()) {
            return null;
        }

        final var attributes = getItemResponse.item();
        final var movie = new Movie();
        movie.setMovieId(movieId);

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
