package de.mbe.tutorials.aws.serverless.movies.updatemovieinfo;

import de.mbe.tutorials.aws.serverless.movies.updatemovieinfo.repository.models.Movie;
import de.mbe.tutorials.aws.serverless.movies.updatemovieinfo.repository.models.MovieInfo;

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

    default MovieInfo getRandomMovieInfo(final String movieId) {
        final var movieInfo = new MovieInfo();
        movieInfo.setMovieId(movieId);
        movieInfo.setName(UUID.randomUUID().toString());
        movieInfo.setCountryOfOrigin(UUID.randomUUID().toString());
        movieInfo.setReleaseDate(UUID.randomUUID().toString());
        return movieInfo;
    }
}
