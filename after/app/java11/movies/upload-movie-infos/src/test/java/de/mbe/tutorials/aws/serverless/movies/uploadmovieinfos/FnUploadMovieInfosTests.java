package de.mbe.tutorials.aws.serverless.movies.uploadmovieinfos;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.mbe.tutorials.aws.serverless.movies.uploadmovieinfos.services.UploadFromS3ToDynamoDBService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public final class FnUploadMovieInfosTests {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String MOVIE_INFOS_BUCKET = UUID.randomUUID().toString();
    private static final String INCORRECT_BUCKET = UUID.randomUUID().toString();

    @Mock
    private UploadFromS3ToDynamoDBService uploadFromS3ToDynamoDBService;

    private FnUploadMovieInfos fnUploadMovieInfos;

    @BeforeEach
    void init() {
        fnUploadMovieInfos = new FnUploadMovieInfos(MOVIE_INFOS_BUCKET, uploadFromS3ToDynamoDBService);
    }

    @Test
    void correctInputSingleFileReturnsOk() throws IOException {

        final var firstObjectKey = UUID.randomUUID() + ".csv";
        final var noOfMovies = new Random().nextInt();
        when(uploadFromS3ToDynamoDBService.upload(List.of(firstObjectKey))).thenReturn(noOfMovies);

        final var input = getCorrectInput(firstObjectKey);
        final var output = new ByteArrayOutputStream();

        fnUploadMovieInfos.handleRequest(input, output, null);

        assertNotNull(output);
        assertTrue(output.size() > 0);

        final var actualResponse = OBJECT_MAPPER.readValue(output.toString(), APIGatewayProxyResponseUtils.APIGatewayResponse.class);

        assertNotNull(actualResponse);

        assertEquals(200, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getHeaders());
        assertEquals(1, actualResponse.getHeaders().size());
        assertTrue(actualResponse.getHeaders().containsKey("Content-Type"));

        assertEquals("application/json", actualResponse.getHeaders().get("Content-Type"));
        assertNotNull(actualResponse.getBody());

        assertEquals(Integer.toString(noOfMovies), actualResponse.getBody());
    }

    @Test
    void correctInputMultipleFilesIdReturnsOk() throws IOException {

        final String objectKey1 = UUID.randomUUID() + ".csv";
        final var noOfMovies1 = new Random().nextInt();

        final String objectKey2 = UUID.randomUUID() + ".csv";
        final var noOfMovies2 = new Random().nextInt();

        final String objectKey3 = UUID.randomUUID() + ".csv";
        final var noOfMovies3 = new Random().nextInt();

        final var files = Map.of(
                MOVIE_INFOS_BUCKET, Map.of(
                        objectKey1, noOfMovies1
                ),
                INCORRECT_BUCKET, Map.of(
                        objectKey2, noOfMovies2,
                        objectKey3, noOfMovies3
                )
        );

        when(uploadFromS3ToDynamoDBService.upload(List.of(objectKey1))).thenReturn(noOfMovies1);

        final var input = getCorrectInput(files);
        final var output = new ByteArrayOutputStream();

        fnUploadMovieInfos.handleRequest(input, output, null);

        assertNotNull(output);
        assertTrue(output.size() > 0);

        final var actualResponse = OBJECT_MAPPER.readValue(output.toString(), APIGatewayProxyResponseUtils.APIGatewayResponse.class);

        assertNotNull(actualResponse);

        assertEquals(200, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getHeaders());
        assertEquals(1, actualResponse.getHeaders().size());
        assertTrue(actualResponse.getHeaders().containsKey("Content-Type"));

        assertEquals("application/json", actualResponse.getHeaders().get("Content-Type"));
        assertNotNull(actualResponse.getBody());

        assertEquals(Integer.toString(noOfMovies1), actualResponse.getBody());
    }

    @Test
    void incorrectInputReturnsBadRequestInvalidJson() throws IOException {

        final var input = getIncorrectInput();
        final var output = new ByteArrayOutputStream();

        fnUploadMovieInfos.handleRequest(input, output, null);

        assertNotNull(output);
        assertTrue(output.size() > 0);

        final var actualResponse = OBJECT_MAPPER.readValue(output.toString(), APIGatewayProxyResponseUtils.APIGatewayResponse.class);

        assertNotNull(actualResponse);

        assertEquals(400, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getHeaders());
        assertEquals(1, actualResponse.getHeaders().size());
        assertTrue(actualResponse.getHeaders().containsKey("Content-Type"));

        assertEquals("application/json", actualResponse.getHeaders().get("Content-Type"));
        assertNotNull(actualResponse.getBody());

        assertTrue(actualResponse.getBody().startsWith("Invalid JSON: Unexpected end-of-input in VALUE_STRING"));
    }

    @Test
    void incorrectInputReturnsBadRequestValidJsonNoMovieId() throws IOException {

        final var output = new ByteArrayOutputStream();

        fnUploadMovieInfos.handleRequest(getIncorrectInputNullEvent(), output, null);

        assertNotNull(output);
        assertTrue(output.size() > 0);

        final var actualResponse = OBJECT_MAPPER.readValue(output.toString(), APIGatewayProxyResponseUtils.APIGatewayResponse.class);

        assertNotNull(actualResponse);

        assertEquals(400, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getHeaders());
        assertEquals(1, actualResponse.getHeaders().size());
        assertTrue(actualResponse.getHeaders().containsKey("Content-Type"));

        assertEquals("application/json", actualResponse.getHeaders().get("Content-Type"));
        assertNotNull(actualResponse.getBody());

        assertEquals("Invalid JSON: Records node is null or not an array", actualResponse.getBody());
    }

    @Test
    void correctInputWithFaultyDbReturnsInternalServerError() throws IOException {

        final var firstObjectKey = UUID.randomUUID() + ".csv";
        final var noOfMovies = new Random().nextInt();
        when(uploadFromS3ToDynamoDBService.upload(List.of(firstObjectKey))).thenThrow(DynamoDbException.class);

        final var input = getCorrectInput(firstObjectKey);
        final var output = new ByteArrayOutputStream();

        fnUploadMovieInfos.handleRequest(input, output, null);

        assertNotNull(output);
        assertTrue(output.size() > 0);

        final var actualResponse = OBJECT_MAPPER.readValue(output.toString(), APIGatewayProxyResponseUtils.APIGatewayResponse.class);

        assertNotNull(actualResponse);

        assertEquals(0, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getHeaders());
        assertEquals(1, actualResponse.getHeaders().size());
        assertTrue(actualResponse.getHeaders().containsKey("Content-Type"));

        assertEquals("application/json", actualResponse.getHeaders().get("Content-Type"));
        assertNull(actualResponse.getBody());
    }

    @Test
    void correctInputWithFaultyBucketReturnsInternalServerError() throws IOException {

        final var firstObjectKey = UUID.randomUUID() + ".csv";
        final var noOfMovies = new Random().nextInt();
        when(uploadFromS3ToDynamoDBService.upload(List.of(firstObjectKey))).thenThrow(S3Exception.class);

        final var input = getCorrectInput(firstObjectKey);
        final var output = new ByteArrayOutputStream();

        fnUploadMovieInfos.handleRequest(input, output, null);

        assertNotNull(output);
        assertTrue(output.size() > 0);

        final var actualResponse = OBJECT_MAPPER.readValue(output.toString(), APIGatewayProxyResponseUtils.APIGatewayResponse.class);

        assertNotNull(actualResponse);

        assertEquals(0, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getHeaders());
        assertEquals(1, actualResponse.getHeaders().size());
        assertTrue(actualResponse.getHeaders().containsKey("Content-Type"));

        assertEquals("application/json", actualResponse.getHeaders().get("Content-Type"));
        assertNull(actualResponse.getBody());
    }

    private static InputStream getCorrectInput(final String objectKey) {
        final var event = "{\n" +
                "    \"Records\": [\n" +
                "" + getRecord(MOVIE_INFOS_BUCKET, objectKey) +
                "    ]\n" +
                "}";
        return new ByteArrayInputStream(event.getBytes());
    }

    private static InputStream getCorrectInput(final Map<String, Map<String, Integer>> files) {

        final var records = new ArrayList<String>();

        for (var bucketEntry : files.entrySet()) {
            for (var fileEntry : bucketEntry.getValue().entrySet()) {
                records.add(getRecord(bucketEntry.getKey(), fileEntry.getKey()));
            }
        }

        final var event = "{\n" +
                "    \"Records\": [\n" +
                "" + String.join(", ", records) +
                "    ]\n" +
                "}";

        return new ByteArrayInputStream(event.getBytes());
    }

    private static String getRecord(final String bucketName, final String fileName) {
        return "{\n" +
                "    \"eventVersion\": \"2.0\",\n" +
                "    \"eventSource\": \"aws:s3\",\n" +
                "    \"awsRegion\": \"eu-central-1\",\n" +
                "    \"eventTime\": \"1970-01-01T00:00:00.000Z\",\n" +
                "    \"eventName\": \"ObjectCreated:Put\",\n" +
                "    \"userIdentity\": {\n" +
                "      \"principalId\": \"EXAMPLE\"\n" +
                "    },\n" +
                "    \"requestParameters\": {\n" +
                "      \"sourceIPAddress\": \"127.0.0.1\"\n" +
                "    },\n" +
                "    \"responseElements\": {\n" +
                "      \"x-amz-request-id\": \"EXAMPLE123456789\",\n" +
                "      \"x-amz-id-2\": \"EXAMPLE123/5678abcdefghijklambdaisawesome/mnopqrstuvwxyzABCDEFGH\"\n" +
                "    },\n" +
                "    \"s3\": {\n" +
                "      \"s3SchemaVersion\": \"1.0\",\n" +
                "      \"configurationId\": \"testConfigRule\",\n" +
                "      \"bucket\": {\n" +
                "        \"name\": \"" + bucketName + "\",\n" +
                "        \"ownerIdentity\": {\n" +
                "          \"principalId\": \"EXAMPLE\"\n" +
                "        },\n" +
                "        \"arn\": \"arn:aws:s3:::example-bucket\"\n" +
                "      },\n" +
                "      \"object\": {\n" +
                "        \"key\": \"" + fileName + "\",\n" +
                "        \"size\": 1024,\n" +
                "        \"eTag\": \"0123456789abcdef0123456789abcdef\",\n" +
                "        \"sequencer\": \"0A1B2C3D4E5F678901\"\n" +
                "      }\n" +
                "    }\n" +
                "  }";
    }

    private static InputStream getIncorrectInputNullEvent() {
        final var event = "{ }";

        return new ByteArrayInputStream(event.getBytes());
    }

    private static InputStream getIncorrectInput() {
        final var event = "{\n" +
                "    \"resource\": \"/movies/{movieId}\",\n" +
                "    \"path\": \"/movi";

        return new ByteArrayInputStream(event.getBytes());
    }
}
