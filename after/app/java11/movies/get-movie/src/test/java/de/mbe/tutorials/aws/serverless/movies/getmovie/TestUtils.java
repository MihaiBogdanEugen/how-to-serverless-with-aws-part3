package de.mbe.tutorials.aws.serverless.movies.getmovie;

import de.mbe.tutorials.aws.serverless.movies.getmovie.repository.models.Movie;

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
}
