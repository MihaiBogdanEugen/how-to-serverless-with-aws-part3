package de.mbe.tutorials.aws.serverless.movies.updatemovieinfo.repository.models;

public final class Movie {

    private String movieId;
    private String name;
    private String countryOfOrigin;
    private String releaseDate;
    private Integer rottenTomatoesRating;
    private Integer imdbRating;

    public Movie() { }

    public String getMovieId() {
        return movieId;
    }

    public String getName() {
        return name;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public String getReleaseDate() {
        return releaseDate;
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

    public void setName(final String name) {
        this.name = name;
    }

    public void setCountryOfOrigin(final String countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
    }

    public void setReleaseDate(final String releaseDate) {
        this.releaseDate = releaseDate;
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

        Movie movie = (Movie) o;

        if (getMovieId() != null ? !getMovieId().equals(movie.getMovieId()) : movie.getMovieId() != null) return false;
        if (getName() != null ? !getName().equals(movie.getName()) : movie.getName() != null) return false;
        if (getCountryOfOrigin() != null ? !getCountryOfOrigin().equals(movie.getCountryOfOrigin()) : movie.getCountryOfOrigin() != null)
            return false;
        if (getReleaseDate() != null ? !getReleaseDate().equals(movie.getReleaseDate()) : movie.getReleaseDate() != null)
            return false;
        if (getRottenTomatoesRating() != null ? !getRottenTomatoesRating().equals(movie.getRottenTomatoesRating()) : movie.getRottenTomatoesRating() != null)
            return false;
        return getImdbRating() != null ? getImdbRating().equals(movie.getImdbRating()) : movie.getImdbRating() == null;
    }

    @Override
    public int hashCode() {
        int result = getMovieId() != null ? getMovieId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getCountryOfOrigin() != null ? getCountryOfOrigin().hashCode() : 0);
        result = 31 * result + (getReleaseDate() != null ? getReleaseDate().hashCode() : 0);
        result = 31 * result + (getRottenTomatoesRating() != null ? getRottenTomatoesRating().hashCode() : 0);
        result = 31 * result + (getImdbRating() != null ? getImdbRating().hashCode() : 0);
        return result;
    }
}