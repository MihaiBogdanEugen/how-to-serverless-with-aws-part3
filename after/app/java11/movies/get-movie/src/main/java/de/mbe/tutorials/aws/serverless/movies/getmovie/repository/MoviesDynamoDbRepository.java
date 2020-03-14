package de.mbe.tutorials.aws.serverless.movies.getmovie.repository;

import de.mbe.tutorials.aws.serverless.movies.getmovie.repository.models.Movie;
import de.mbe.tutorials.aws.serverless.movies.getmovie.repository.models.MovieInfo;
import de.mbe.tutorials.aws.serverless.movies.getmovie.repository.models.MovieRating;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.Map;
import java.util.Optional;

public class MoviesDynamoDbRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String movieInfosTable;
    private final String movieRatingsTable;

    public MoviesDynamoDbRepository(final DynamoDbClient dynamoDbClient, final String movieInfosTable, final String movieRatingsTable) {
        this.dynamoDbClient = dynamoDbClient;
        this.movieInfosTable = movieInfosTable;
        this.movieRatingsTable = movieRatingsTable;
    }

    public Movie getMovieById(final String movieId) {

        final var movieInfo = this.getMovieInfoById(movieId);
        final var movieRating = this.getMovieRatingById(movieId);

        if (movieInfo == null && movieRating == null) {
            return null;
        }

        final var movie = new Movie();
        movie.setMovieId(movieId);

        if (movieInfo != null) {
            movie.setName(movieInfo.getName());
            movie.setCountryOfOrigin(movieInfo.getCountryOfOrigin());
            movie.setReleaseDate(movieInfo.getReleaseDate());
        }

        if (movieRating != null) {
            movie.setImdbRating(movieRating.getImdbRating());
            movie.setRottenTomatoesRating(movieRating.getRottenTomatoesRating());
        }

        return movie;
    }

    private MovieInfo getMovieInfoById(final String movieId) {

        final var getItemRequest = GetItemRequest.builder()
                .key(Map.of("movieId", AttributeValue.builder().s(movieId).build()))
                .tableName(movieInfosTable)
                .consistentRead(true)
                .build();

        final var getItemResponse = dynamoDbClient.getItem(getItemRequest);
        if (!getItemResponse.hasItem()) {
            return null;
        }

        final var attributes = getItemResponse.item();
        final var movieInfo = new MovieInfo();
        movieInfo.setMovieId(movieId);

        Optional.ofNullable(attributes.getOrDefault("name", null))
                .ifPresent(attribute -> movieInfo.setName(attribute.s()));

        Optional.ofNullable(attributes.getOrDefault("countryOfOrigin", null))
                .ifPresent(attribute -> movieInfo.setCountryOfOrigin(attribute.s()));

        Optional.ofNullable(attributes.getOrDefault("releaseDate", null))
                .ifPresent(attribute -> movieInfo.setReleaseDate(attribute.s()));

        return movieInfo;
    }

    private MovieRating getMovieRatingById(final String movieId) {

        final var getItemRequest = GetItemRequest.builder()
                .key(Map.of("movieId", AttributeValue.builder().s(movieId).build()))
                .tableName(movieRatingsTable)
                .consistentRead(true)
                .build();

        final var getItemResponse = dynamoDbClient.getItem(getItemRequest);
        if (!getItemResponse.hasItem()) {
            return null;
        }

        final var attributes = getItemResponse.item();
        final var movieRating = new MovieRating();
        movieRating.setMovieId(movieId);

        Optional.ofNullable(attributes.getOrDefault("rottenTomatoesRating", null))
                .ifPresent(attribute -> movieRating.setRottenTomatoesRating(Integer.parseInt(attribute.n())));

        Optional.ofNullable(attributes.getOrDefault("imdbRating", null))
                .ifPresent(attribute -> movieRating.setImdbRating(Integer.parseInt(attribute.n())));

        return movieRating;
    }
}
