package de.mbe.tutorials.aws.serverless.movies.updatemovierating;

import de.mbe.tutorials.aws.serverless.movies.updatemovierating.repository.models.MovieRating;

import java.util.Random;

public interface TestUtils {

    Random RANDOM = new Random();

    default MovieRating getRandomMovieRating(final String movieId) {
        final var movieRating = new MovieRating();
        movieRating.setMovieId(movieId);
        movieRating.setImdbRating(RANDOM.nextInt(100));
        movieRating.setRottenTomatoesRating(RANDOM.nextInt(100));
        return movieRating;
    }
}
