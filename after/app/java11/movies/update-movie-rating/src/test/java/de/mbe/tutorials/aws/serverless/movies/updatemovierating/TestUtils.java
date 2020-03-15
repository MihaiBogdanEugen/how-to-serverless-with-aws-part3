package de.mbe.tutorials.aws.serverless.movies.updatemovierating;

import de.mbe.tutorials.aws.serverless.movies.updatemovierating.repository.models.Movie;
import de.mbe.tutorials.aws.serverless.movies.updatemovierating.repository.models.MovieRating;

import java.util.Random;
import java.util.UUID;

public interface TestUtils {

    Random RANDOM = new Random();

    default Movie getRandomMovie(final String movieId) {
        final var movie = new Movie();
        movie.setMovieId(movieId);
        movie.setName(UUID.randomUUID().toString());
        movie.setCountryOfOrigin(UUID.randomUUID().toString());
        movie.setReleaseDate(UUID.randomUUID().toString());
        movie.setImdbRating(RANDOM.nextInt(100));
        movie.setRottenTomatoesRating(RANDOM.nextInt(100));
        return movie;
    }

    default MovieRating getRandomMovieRating(final String movieId) {
        final var movieRating = new MovieRating();
        movieRating.setMovieId(movieId);
        movieRating.setImdbRating(RANDOM.nextInt(100));
        movieRating.setRottenTomatoesRating(RANDOM.nextInt(100));
        return movieRating;
    }
}
