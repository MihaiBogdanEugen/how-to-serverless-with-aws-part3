package de.mbe.tutorials.aws.serverless.movies.getmovie;

import de.mbe.tutorials.aws.serverless.movies.getmovie.repository.models.Movie;
import de.mbe.tutorials.aws.serverless.movies.getmovie.repository.models.MovieInfo;
import de.mbe.tutorials.aws.serverless.movies.getmovie.repository.models.MovieRating;

import java.util.Random;
import java.util.UUID;

public interface TestUtils {

    Random RANDOM = new Random();

    default Movie getRandomMovie(final String movieId) {
        final var movie = new Movie();
        movie.setMovieId(movieId);
        return getRandomMovie(movieId, getRandomMovieInfo(movieId), getRandomMovieRating(movieId));
    }

    default Movie getRandomMovie(final String movieId, final MovieInfo movieInfo, final MovieRating movieRating) {
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

    default MovieInfo getRandomMovieInfo(final String movieId) {
        final var movieInfo = new MovieInfo();
        movieInfo.setMovieId(movieId);
        movieInfo.setName(UUID.randomUUID().toString());
        movieInfo.setCountryOfOrigin(UUID.randomUUID().toString());
        movieInfo.setReleaseDate(UUID.randomUUID().toString());
        return movieInfo;
    }

    default MovieRating getRandomMovieRating(final String movieId) {
        final var movieRating = new MovieRating();
        movieRating.setMovieId(movieId);
        movieRating.setImdbRating(RANDOM.nextInt(100));
        movieRating.setRottenTomatoesRating(RANDOM.nextInt(100));
        return movieRating;
    }
}
