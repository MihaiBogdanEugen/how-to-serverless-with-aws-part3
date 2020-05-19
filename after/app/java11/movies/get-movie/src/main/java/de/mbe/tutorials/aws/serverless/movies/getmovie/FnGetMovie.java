package de.mbe.tutorials.aws.serverless.movies.getmovie;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent;
import com.amazonaws.xray.interceptors.TracingInterceptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mbe.tutorials.aws.serverless.movies.getmovie.repository.MoviesDynamoDbRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.Map;

public final class FnGetMovie {

    private static final Logger LOGGER = LogManager.getLogger(FnGetMovie.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final MoviesDynamoDbRepository moviesDynamoDbRepository;

    public FnGetMovie() {

        final var dynamoDBClient = DynamoDbClient.builder()
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .addExecutionInterceptor(new TracingInterceptor())
                        .build())
                .build();

        final var moviesTable = System.getenv("MOVIES_TABLE");

        moviesDynamoDbRepository = new MoviesDynamoDbRepository(dynamoDBClient, moviesTable);
    }

    public FnGetMovie(final MoviesDynamoDbRepository moviesDynamoDbRepository) {
        this.moviesDynamoDbRepository = moviesDynamoDbRepository;
    }

    public APIGatewayV2ProxyResponseEvent handleRequest(final APIGatewayV2ProxyRequestEvent input) {

        try {

            final var movieId = getMovieId(input);
            LOGGER.info("Retrieving movie {}", movieId);

            final var movie = moviesDynamoDbRepository.getMovieById(movieId);
            if (movie == null) {
                return reply(404, "Movie " + movieId + " not found");
            } else {
                return reply(200, movie);
            }

        } catch (IllegalArgumentException error) {
            return reply(400, error);
        } catch (DynamoDbException error) {
            return reply(error.statusCode(), error);
        } catch (Exception error) {
            return reply(500, error);
        }
    }

    private static String getMovieId(final APIGatewayV2ProxyRequestEvent input) {

        final var movieId = input.getPathParameters().getOrDefault("movieId", null);
        if (movieId == null) {
            throw new IllegalArgumentException("Invalid JSON: Missing or null pathParameters.movieId");
        }
        return movieId;
    }

    private static <T> APIGatewayV2ProxyResponseEvent reply(final int statusCode, final T body) {

        switch (statusCode / 100) {
            case 2:
                LOGGER.info("SUCCESS! statusCode: {}, message: {}", statusCode, body);
                break;
            case 4:
                LOGGER.warn("CLIENT ERROR! statusCode: {}, message: {}", statusCode, body);
                break;
            default:
                LOGGER.error("SERVER ERROR! statusCode: {}, message: {}", statusCode, body);
        }

        String bodyAsString;
        try {
            bodyAsString = OBJECT_MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            bodyAsString = e.getMessage();
        }

        final var response = new APIGatewayV2ProxyResponseEvent();
        response.setHeaders(Map.of("Content-Type", "application/json"));
        response.setIsBase64Encoded(false);
        response.setBody(bodyAsString);
        response.setStatusCode(200);
        return response;
    }
}
