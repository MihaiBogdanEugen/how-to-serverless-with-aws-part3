package de.mbe.tutorials.aws.serverless.movies.getmovie.repository.models;

public final class MovieRating {

    private String movieId;
    private Integer rottenTomatoesRating;
    private Integer imdbRating;

    public MovieRating() { }

    public String getMovieId() {
        return movieId;
    }

    public Integer getRottenTomatoesRating() {
        return rottenTomatoesRating;
    }

    public Integer getImdbRating() {
        return imdbRating;
    }

    public void setMovieId(final String movieId) {
        this.movieId = movieId;
    }

    public void setRottenTomatoesRating(final Integer rottenTomatoesRating) {
        this.rottenTomatoesRating = rottenTomatoesRating;
    }

    public void setImdbRating(final Integer imdbRating) {
        this.imdbRating = imdbRating;
    }
}