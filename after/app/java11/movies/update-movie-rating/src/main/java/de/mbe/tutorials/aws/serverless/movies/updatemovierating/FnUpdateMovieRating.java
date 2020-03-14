package de.mbe.tutorials.aws.serverless.movies.updatemovierating;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mbe.tutorials.aws.serverless.movies.updatemovierating.repository.MoviesDynamoDbRepository;
import de.mbe.tutorials.aws.serverless.movies.updatemovierating.repository.models.MovieRating;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import static com.amazonaws.util.StringUtils.isNullOrEmpty;

public final class FnUpdateMovieRating implements RequestStreamHandler, APIGatewayProxyResponseUtils {

    private static final Logger LOGGER = LogManager.getLogger(FnUpdateMovieRating.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final MoviesDynamoDbRepository moviesDynamoDbRepository;

    public FnUpdateMovieRating() {

        final var amazonDynamoDB = AmazonDynamoDBClientBuilder
                .standard()
                .withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder()))
                .build();

        final var movieRatingsTable = System.getenv("MOVIE_RATINGS_TABLE");

        moviesDynamoDbRepository = new MoviesDynamoDbRepository(amazonDynamoDB, movieRatingsTable);
    }

    public FnUpdateMovieRating(final MoviesDynamoDbRepository moviesDynamoDbRepository) {
        this.moviesDynamoDbRepository = moviesDynamoDbRepository;
    }

    @Override
    public void handleRequest(final InputStream input, final OutputStream output, final Context context) throws IOException {

        try {

            final var movieRating = getMovieRating(input);
            LOGGER.info("Patching movie {}", movieRating.getMovieId());

            moviesDynamoDbRepository.updateMovieRating(movieRating);
            writeOk(output, "SUCCESS");

        } catch (IllegalArgumentException error) {
            writeBadRequest(output, error);
        } catch (AmazonDynamoDBException error) {
            writeAmazonDynamoDBException(output, error);
        } catch (Exception error) {
            writeInternalServerError(output, error);
        }
    }

    private MovieRating getMovieRating(final InputStream input) throws JsonProcessingException {

        final JsonNode event;

        try {
            event = OBJECT_MAPPER.readTree(input);
        } catch (IOException error) {
            throw new IllegalArgumentException("Invalid JSON: " + error.getMessage());
        }

        if (isNodeNullOrEmpty(event)) {
            throw new IllegalArgumentException("Invalid JSON: event is null");
        }

        final var pathParameterMapNode = event.findValue("pathParameters");
        final var movieId = Optional.ofNullable(pathParameterMapNode)
                .map(x -> x.get("movieId"))
                .map(JsonNode::asText)
                .orElse(null);

        if (isNullOrEmpty(movieId)) {
            throw new IllegalArgumentException("Invalid JSON: Missing or null pathParameters.movieId");
        }

        final var bodyMapNode = event.findValue("body");
        final var body = Optional.ofNullable(bodyMapNode)
                .map(JsonNode::asText)
                .orElse(null);

        if (isNullOrEmpty(body)) {
            throw new IllegalArgumentException("Invalid JSON: Missing or null body");
        }

        final var movieRating = OBJECT_MAPPER.readValue(body, MovieRating.class);
        movieRating.setMovieId(movieId);
        return movieRating;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}
