package de.mbe.tutorials.aws.serverless.movies.updatemovieinfo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.xray.interceptors.TracingInterceptor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mbe.tutorials.aws.serverless.movies.updatemovieinfo.repository.MoviesDynamoDbRepository;
import de.mbe.tutorials.aws.serverless.movies.updatemovieinfo.repository.models.MovieInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.amazonaws.util.StringUtils.isNullOrEmpty;

public class FnUpdateMovieInfo implements RequestStreamHandler, APIGatewayProxyResponseUtils {

    private static final Logger LOGGER = LogManager.getLogger(FnUpdateMovieInfo.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {

        try {

            final var movieInfos = getMovieInfos(input);
            LOGGER.info("Updated {} movieInfos", movieInfos.size());

            for (var movieInfo : movieInfos) {
                moviesDynamoDbRepository.updateMovieInfo(movieInfo);
            }

            writeOk(output, Integer.toString(movieInfos.size()));

        } catch (IllegalArgumentException error) {
            writeBadRequest(output, error);
        } catch (DynamoDbException error) {
            writeDynamoDbException(output, error);
        } catch (Exception error) {
            writeInternalServerError(output, error);
        }
    }

    private List<MovieInfo> getMovieInfos(final InputStream input) {

        final var movieInfos = new ArrayList<MovieInfo>();

        final JsonNode event;

        try {
            event = OBJECT_MAPPER.readTree(input);
        } catch (IOException error) {
            throw new IllegalArgumentException("Invalid JSON: " + error.getMessage());
        }

        if (isNodeNullOrEmpty(event)) {
            throw new IllegalArgumentException("Invalid JSON: event is null");
        }

        final var recordsNode = event.findValue("Records");
        if (isNodeNullOrEmpty(recordsNode) || !recordsNode.isArray()) {
            throw new IllegalArgumentException("Invalid JSON: Records node is null or not an array");
        }

        for (final var recordNode : recordsNode) {

            final var dynamodbNode = recordNode.findValue("dynamodb");
            if (isNodeNullOrEmpty(dynamodbNode)) {
                throw new IllegalArgumentException("Invalid JSON: Missing Record.dynamodb node");
            }

            final var newImageNode = dynamodbNode.findValue("NewImage");
            if (isNodeNullOrEmpty(newImageNode)) {
                continue;
            }

            final var movieInfo = new MovieInfo();

            final var movieIdNode = newImageNode.findValue("movieId");
            if (isNodeNullOrEmpty(movieIdNode)) {
                throw new IllegalArgumentException("Invalid JSON: Missing Record.dynamodb.NewImage.movieId node");
            }

            final var movieIdNodeValue = movieIdNode.findValue("S");
            if (isNodeNullOrEmpty(movieIdNodeValue)) {
                throw new IllegalArgumentException("Invalid JSON: Missing Record.dynamodb.NewImage.movieId.S node");
            }

            final var movieId = Optional.of(movieIdNodeValue).map(JsonNode::asText).orElse(null);
            if (isNullOrEmpty(movieId)) {
                throw new IllegalArgumentException("Invalid JSON: Record.dynamodb.NewImage.movieId.S node is null");
            }

            movieInfo.setMovieId(movieId);

            final var nameNode = newImageNode.findValue("name");
            if (isNodeNullOrEmpty(nameNode)) {
                throw new IllegalArgumentException("Invalid JSON: Missing Record.dynamodb.NewImage.name node");
            }

            final var nameNodeValue = nameNode.findValue("S");
            if (isNodeNullOrEmpty(nameNodeValue)) {
                throw new IllegalArgumentException("Invalid JSON: Missing Record.dynamodb.NewImage.name.S node");
            }

            final var name = Optional.of(nameNodeValue).map(JsonNode::asText).orElse(null);
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("Invalid JSON: Record.dynamodb.NewImage.name.S node is null");
            }

            movieInfo.setName(name);

            final var countryOfOriginNode = newImageNode.findValue("countryOfOrigin");
            if (isNodeNullOrEmpty(countryOfOriginNode)) {
                throw new IllegalArgumentException("Invalid JSON: Missing Record.dynamodb.NewImage.countryOfOrigin node");
            }

            final var countryOfOriginNodeValue = countryOfOriginNode.findValue("S");
            if (isNodeNullOrEmpty(countryOfOriginNodeValue)) {
                throw new IllegalArgumentException("Invalid JSON: Missing Record.dynamodb.NewImage.countryOfOrigin.S node");
            }

            final var countryOfOrigin = Optional.of(countryOfOriginNodeValue).map(JsonNode::asText).orElse(null);
            if (isNullOrEmpty(countryOfOrigin)) {
                throw new IllegalArgumentException("Invalid JSON: Record.dynamodb.NewImage.countryOfOrigin.S node is null");
            }

            movieInfo.setCountryOfOrigin(countryOfOrigin);

            final var releaseDateNode = newImageNode.findValue("releaseDate");
            if (isNodeNullOrEmpty(releaseDateNode)) {
                throw new IllegalArgumentException("Invalid JSON: Missing Record.dynamodb.NewImage.releaseDate node");
            }

            final var releaseDateNodeValue = releaseDateNode.findValue("S");
            if (isNodeNullOrEmpty(releaseDateNodeValue)) {
                throw new IllegalArgumentException("Invalid JSON: Missing Record.dynamodb.NewImage.releaseDate.S node");
            }

            final var releaseDate = Optional.of(releaseDateNodeValue).map(JsonNode::asText).orElse(null);
            if (isNullOrEmpty(releaseDate)) {
                throw new IllegalArgumentException("Invalid JSON: Record.dynamodb.NewImage.releaseDate.S node is null");
            }

            movieInfo.setReleaseDate(releaseDate);

            movieInfos.add(movieInfo);
        }

        return movieInfos;
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
