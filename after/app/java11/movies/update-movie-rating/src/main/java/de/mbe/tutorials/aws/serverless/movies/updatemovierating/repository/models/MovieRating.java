package de.mbe.tutorials.aws.serverless.movies.updatemovierating.repository.models;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MovieRating that = (MovieRating) o;

        if (getMovieId() != null ? !getMovieId().equals(that.getMovieId()) : that.getMovieId() != null) return false;
        if (getRottenTomatoesRating() != null ? !getRottenTomatoesRating().equals(that.getRottenTomatoesRating()) : that.getRottenTomatoesRating() != null)
            return false;
        return getImdbRating() != null ? getImdbRating().equals(that.getImdbRating()) : that.getImdbRating() == null;
    }

    @Override
    public int hashCode() {
        int result = getMovieId() != null ? getMovieId().hashCode() : 0;
        result = 31 * result + (getRottenTomatoesRating() != null ? getRottenTomatoesRating().hashCode() : 0);
        result = 31 * result + (getImdbRating() != null ? getImdbRating().hashCode() : 0);
        return result;
    }
}