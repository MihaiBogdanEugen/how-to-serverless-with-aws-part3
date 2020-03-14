package de.mbe.tutorials.aws.serverless.movies.getmovie;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mbe.tutorials.aws.serverless.movies.getmovie.repository.MoviesDynamoDbRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import static com.amazonaws.util.StringUtils.isNullOrEmpty;

public final class FnGetMovie implements RequestStreamHandler, APIGatewayProxyResponseUtils {

    private static final Logger LOGGER = LogManager.getLogger(FnGetMovie.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final MoviesDynamoDbRepository moviesDynamoDbRepository;

    public FnGetMovie() {
        final var amazonDynamoDB = AmazonDynamoDBClientBuilder
                .standard()
                .withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder()))
                .build();

        final var movieInfosTable = System.getenv("MOVIE_INFOS_TABLE");
        final var movieRatingsTable = System.getenv("MOVIE_RATINGS_TABLE");

        moviesDynamoDbRepository = new MoviesDynamoDbRepository(
                amazonDynamoDB,
                movieInfosTable,
                movieRatingsTable);
    }

    public FnGetMovie(final MoviesDynamoDbRepository moviesDynamoDbRepository) {
        this.moviesDynamoDbRepository = moviesDynamoDbRepository;
    }

    @Override
    public void handleRequest(final InputStream input, final OutputStream output, final Context context) throws IOException {

        try {

            final var movieId = getId(input);
            LOGGER.info("Retrieving movie {}", movieId);

            final var movie = moviesDynamoDbRepository.getByMovieId(movieId);
            if (movie == null) {
                writeNotFound(output, "Movie " + movieId + " not found");
            }

            final var movieAsString = OBJECT_MAPPER.writeValueAsString(movie);
            writeOk(output, movieAsString);

        } catch (IllegalArgumentException error) {
            writeBadRequest(output, error);
        } catch (AmazonDynamoDBException error) {
            writeAmazonDynamoDBException(output, error);
        } catch (Exception error) {
            writeInternalServerError(output, error);
        }
    }

    private String getId(final InputStream input) {

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
        final var id = Optional.ofNullable(pathParameterMapNode)
                .map(x -> x.get("movieId"))
                .map(JsonNode::asText)
                .orElse(null);

        if (isNullOrEmpty(id)) {
            throw new IllegalArgumentException("Invalid JSON: Missing or null pathParameters.movieId");
        }

        return id;
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
