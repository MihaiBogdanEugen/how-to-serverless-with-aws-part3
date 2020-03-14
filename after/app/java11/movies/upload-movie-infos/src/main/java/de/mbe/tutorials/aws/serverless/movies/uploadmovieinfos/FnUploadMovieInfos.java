package de.mbe.tutorials.aws.serverless.movies.uploadmovieinfos;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mbe.tutorials.aws.serverless.movies.uploadmovieinfos.repository.MoviesDynamoDbRepository;
import de.mbe.tutorials.aws.serverless.movies.uploadmovieinfos.services.UploadFromS3ToDynamoDBService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.amazonaws.util.StringUtils.isNullOrEmpty;

public final class FnUploadMovieInfos implements RequestStreamHandler, APIGatewayProxyResponseUtils {

    private static final Logger LOGGER = LogManager.getLogger(FnUploadMovieInfos.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String movieInfosBucket;
    private final UploadFromS3ToDynamoDBService uploadFromS3ToDynamoDBService;

    public FnUploadMovieInfos() {

        final var amazonDynamoDB = AmazonDynamoDBClientBuilder
                .standard()
                .withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder()))
                .build();

        final var amazonS3 = AmazonS3ClientBuilder
                .standard()
                .withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder()))
                .build();

        movieInfosBucket = System.getenv("MOVIE_INFOS_BUCKET");
        final var movieInfosTable = System.getenv("MOVIE_INFOS_TABLE");

        final var moviesDynamoDbRepository = new MoviesDynamoDbRepository(amazonDynamoDB, movieInfosTable);

        uploadFromS3ToDynamoDBService = new UploadFromS3ToDynamoDBService(amazonS3, movieInfosBucket, moviesDynamoDbRepository);
    }

    public FnUploadMovieInfos(final String movieInfosBucket, final UploadFromS3ToDynamoDBService uploadFromS3ToDynamoDBService) {
        this.movieInfosBucket = movieInfosBucket;
        this.uploadFromS3ToDynamoDBService = uploadFromS3ToDynamoDBService;
    }

    @Override
    public void handleRequest(final InputStream input, final OutputStream output, final Context context) throws IOException {

        try {

            final var objectKeys = getObjectKeys(input);
            final var result = uploadFromS3ToDynamoDBService.upload(objectKeys);
            writeOk(output, Integer.toString(result));

        } catch (IllegalArgumentException error) {
            writeBadRequest(output, error);
        } catch (AmazonDynamoDBException error) {
            writeAmazonDynamoDBException(output, error);
        } catch (AmazonS3Exception error) {
            writeAmazonS3Exception(output, error);
        } catch (Exception error) {
            writeInternalServerError(output, error);
        }
    }

    private List<String> getObjectKeys(final InputStream input) {

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

        final var objectKeys = new ArrayList<String>();

        for (final var recordNode : recordsNode) {

            final var s3Node = recordNode.findValue("s3");
            if (isNodeNullOrEmpty(s3Node)) {
                throw new IllegalArgumentException("Invalid JSON: Missing Record.s3 node");
            }

            final var bucketNode = s3Node.findValue("bucket");
            if (isNodeNullOrEmpty(bucketNode)) {
                throw new IllegalArgumentException("Invalid JSON: Missing Record.s3.bucket node");
            }

            final var bucketNameNode = bucketNode.findValue("name");
            if (isNodeNullOrEmpty(bucketNode)) {
                throw new IllegalArgumentException("Invalid JSON: Missing Record.s3.bucket.name node");
            }

            final var bucketName = Optional.of(bucketNameNode).map(JsonNode::asText).orElse(null);
            if (isNullOrEmpty(bucketName)) {
                throw new IllegalArgumentException("Invalid JSON: Record.s3.bucket.name node is null");
            }

            if (!bucketName.equalsIgnoreCase(movieInfosBucket)) {
                continue;
            }

            final var objectNode = s3Node.findValue("object");
            if (isNodeNullOrEmpty(objectNode)) {
                throw new IllegalArgumentException("Invalid JSON: Missing Record.s3.object node");
            }

            final var objectKeyNode = objectNode.findValue("key");
            if (isNodeNullOrEmpty(objectKeyNode)) {
                throw new IllegalArgumentException("Invalid JSON: Missing Record.s3.object.key node");
            }

            final var objectKey = Optional.of(objectKeyNode).map(JsonNode::asText).orElse(null);
            if (isNullOrEmpty(objectKey)) {
                throw new IllegalArgumentException("Invalid JSON: Record.s3.object.key node is null");
            }

            objectKeys.add(objectKey);
        }

        return objectKeys;
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