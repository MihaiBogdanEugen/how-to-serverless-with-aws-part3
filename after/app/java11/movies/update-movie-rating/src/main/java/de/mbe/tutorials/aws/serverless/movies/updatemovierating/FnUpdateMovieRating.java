package de.mbe.tutorials.aws.serverless.movies.updatemovierating;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent;
import com.amazonaws.xray.interceptors.TracingInterceptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mbe.tutorials.aws.serverless.movies.updatemovierating.repository.MoviesDynamoDbRepository;
import de.mbe.tutorials.aws.serverless.movies.updatemovierating.repository.models.MovieRating;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;

import static com.amazonaws.util.StringUtils.isNullOrEmpty;

public final class FnUpdateMovieRating implements RequestHandler<APIGatewayV2ProxyRequestEvent, APIGatewayV2ProxyResponseEvent> {

    private static final Logger LOGGER = LogManager.getLogger(FnUpdateMovieRating.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final MoviesDynamoDbRepository moviesDynamoDbRepository;

    public FnUpdateMovieRating() {

        final var dynamoDBClient = DynamoDbClient.builder()
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .addExecutionInterceptor(new TracingInterceptor())
                        .build())
                .build();

        final var moviesTable = System.getenv("MOVIES_TABLE");

        moviesDynamoDbRepository = new MoviesDynamoDbRepository(dynamoDBClient, moviesTable);
    }

    public FnUpdateMovieRating(final MoviesDynamoDbRepository moviesDynamoDbRepository) {
        this.moviesDynamoDbRepository = moviesDynamoDbRepository;
    }

    @Override
    public APIGatewayV2ProxyResponseEvent handleRequest(final APIGatewayV2ProxyRequestEvent input, final Context context) {

        try {

            final var movieRating = getMovieRating(input);
            LOGGER.info("Patching movie {}", movieRating.getMovieId());

            final var movie = moviesDynamoDbRepository.updateMovieRating(movieRating);
            return reply(200, movie);

        } catch (IllegalArgumentException error) {
            return reply(400, error);
        } catch (DynamoDbException error) {
            return reply(error.statusCode(), error);
        } catch (Exception error) {
            return reply(500, error);
        }
    }

    private static MovieRating getMovieRating(final APIGatewayV2ProxyRequestEvent input) throws JsonProcessingException {

        final var movieId = input.getPathParameters().getOrDefault("movieId", null);
        if (isNullOrEmpty(movieId)) {
            throw new IllegalArgumentException("Invalid JSON: Missing or null pathParameters.movieId");
        }

        final var movieRating = OBJECT_MAPPER.readValue(input.getBody(), MovieRating.class);
        movieRating.setMovieId(movieId);
        return movieRating;
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
