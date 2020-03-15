package de.mbe.tutorials.aws.serverless.movies.updatemovieinfo.repository.models;

public final class MovieInfo {

    private String movieId;
    private String name;
    private String countryOfOrigin;
    private String releaseDate;

    public MovieInfo() { }

    public MovieInfo(final String movieId, final String name, final String countryOfOrigin, final String releaseDate) {
        this.movieId = movieId;
        this.name = name;
        this.countryOfOrigin = countryOfOrigin;
        this.releaseDate = releaseDate;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MovieInfo movieInfo = (MovieInfo) o;

        if (getMovieId() != null ? !getMovieId().equals(movieInfo.getMovieId()) : movieInfo.getMovieId() != null)
            return false;
        if (getName() != null ? !getName().equals(movieInfo.getName()) : movieInfo.getName() != null) return false;
        if (getCountryOfOrigin() != null ? !getCountryOfOrigin().equals(movieInfo.getCountryOfOrigin()) : movieInfo.getCountryOfOrigin() != null)
            return false;
        return getReleaseDate() != null ? getReleaseDate().equals(movieInfo.getReleaseDate()) : movieInfo.getReleaseDate() == null;
    }

    @Override
    public int hashCode() {
        int result = getMovieId() != null ? getMovieId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getCountryOfOrigin() != null ? getCountryOfOrigin().hashCode() : 0);
        result = 31 * result + (getReleaseDate() != null ? getReleaseDate().hashCode() : 0);
        return result;
    }
}
