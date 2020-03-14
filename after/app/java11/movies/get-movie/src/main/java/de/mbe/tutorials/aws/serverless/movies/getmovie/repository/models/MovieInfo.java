package de.mbe.tutorials.aws.serverless.movies.getmovie.repository.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "movie_infos")
public final class MovieInfo {

    @DynamoDBHashKey
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
}
