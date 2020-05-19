package de.mbe.tutorials.aws.serverless.movies.updatemovieinfo;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.xray.interceptors.TracingInterceptor;
import de.mbe.tutorials.aws.serverless.movies.updatemovieinfo.repository.MoviesDynamoDbRepository;
import de.mbe.tutorials.aws.serverless.movies.updatemovieinfo.repository.models.MovieInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FnUpdateMovieInfo {

    private static final Logger LOGGER = LogManager.getLogger(FnUpdateMovieInfo.class);

    private final MoviesDynamoDbRepository moviesDynamoDbRepository;

    public FnUpdateMovieInfo() {

        final var dynamoDBClient = DynamoDbClient.builder()
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .addExecutionInterceptor(new TracingInterceptor())
                        .build())
                .build();

        final var moviesTable = System.getenv("MOVIES_TABLE");

        moviesDynamoDbRepository = new MoviesDynamoDbRepository(dynamoDBClient, moviesTable);
    }

    public FnUpdateMovieInfo(final MoviesDynamoDbRepository moviesDynamoDbRepository) {
        this.moviesDynamoDbRepository = moviesDynamoDbRepository;
    }

    public Boolean handleRequest(final DynamodbEvent input) {

        try {

            final var movieInfos = getMovieInfos(input);
            LOGGER.info("Updated {} movieInfos", movieInfos.size());

            for (var movieInfo : movieInfos) {
                moviesDynamoDbRepository.updateMovieInfo(movieInfo);
            }

            return reply(200, movieInfos.size());

        } catch (IllegalArgumentException error) {
            return reply(400, error);
        } catch (DynamoDbException error) {
            return reply(error.statusCode(), error);
        } catch (Exception error) {
            return reply(500, error);
        }
    }

    private static List<MovieInfo> getMovieInfos(final DynamodbEvent input) {

        final var newImages = input.getRecords().stream()
                .filter(x -> x.getDynamodb().getNewImage() != null && !x.getDynamodb().getNewImage().isEmpty())
                .map(x -> x.getDynamodb().getNewImage())
                .collect(Collectors.toList());

        final var movieInfos = new ArrayList<MovieInfo>();

        for (final var newImage : newImages) {

            final var movieInfo = new MovieInfo();
            Optional.of(newImage.getOrDefault("movieId", null))
                    .ifPresent(x -> movieInfo.setMovieId(x.getS()));

            Optional.of(newImage.getOrDefault("name", null))
                    .ifPresent(x -> movieInfo.setName(x.getS()));

            Optional.of(newImage.getOrDefault("countryOfOrigin", null))
                    .ifPresent(x -> movieInfo.setCountryOfOrigin(x.getS()));

            Optional.of(newImage.getOrDefault("releaseDate", null))
                    .ifPresent(x -> movieInfo.setReleaseDate(x.getS()));

            movieInfos.add(movieInfo);
        }

        return movieInfos;
    }

    private static <T> Boolean reply(final int statusCode, final T body) {

        var response = false;
        switch (statusCode / 100) {
            case 2:
                LOGGER.info("SUCCESS! statusCode: {}, message: {}", statusCode, body);
                response = true;
                break;
            case 4:
                LOGGER.warn("CLIENT ERROR! statusCode: {}, message: {}", statusCode, body);
                break;
            default:
                LOGGER.error("SERVER ERROR! statusCode: {}, message: {}", statusCode, body);
        }
        return response;
    }
}
