package de.mbe.tutorials.aws.serverless.movies.updatemovieinfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.mbe.tutorials.aws.serverless.movies.updatemovieinfo.repository.MoviesDynamoDbRepository;
import de.mbe.tutorials.aws.serverless.movies.updatemovieinfo.repository.models.MovieInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FnUpdateMovieInfoTests implements TestUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private MoviesDynamoDbRepository moviesDynamoDbRepository;

    private FnUpdateMovieInfo fnUpdateMovieInfo;

    @BeforeEach
    void init() {
        fnUpdateMovieInfo = new FnUpdateMovieInfo(moviesDynamoDbRepository);
    }

    @Test
    void correctInputReturnsOk() throws IOException {

        final var movieId = UUID.randomUUID().toString();
        final var expectedMovie = getRandomMovie(movieId);
        final var expectedMovieInfo = getRandomMovieInfo(movieId);

        when(moviesDynamoDbRepository.updateMovieInfo(expectedMovieInfo)).thenReturn(expectedMovie);

        final var input = getCorrectInput(expectedMovieInfo);
        final var output = new ByteArrayOutputStream();

        fnUpdateMovieInfo.handleRequest(input, output, null);

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

        assertEquals("1", actualResponse.getBody());
    }

    @Test
    void incorrectInputReturnsBadRequestInvalidJson() throws IOException {

        final var input = getIncorrectInput();
        final var output = new ByteArrayOutputStream();

        fnUpdateMovieInfo.handleRequest(input, output, null);

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

        fnUpdateMovieInfo.handleRequest(getIncorrectInputNullEvent(), output, null);

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

        final var movieId = UUID.randomUUID().toString();
        final var expectMovieInfo = getRandomMovieInfo(movieId);

        doThrow(DynamoDbException.class).when(moviesDynamoDbRepository).updateMovieInfo(expectMovieInfo);

        final var input = getCorrectInput(expectMovieInfo);
        final var output = new ByteArrayOutputStream();

        fnUpdateMovieInfo.handleRequest(input, output, null);

        assertNotNull(output);
        assertTrue(output.size() > 0);

        final var actualResponse = OBJECT_MAPPER.readValue(output.toString(), APIGatewayProxyResponseUtils.APIGatewayResponse.class);

        assertNotNull(actualResponse);

        assertEquals(0, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getHeaders());
        assertEquals(1, actualResponse.getHeaders().size());
        assertEquals("application/json", actualResponse.getHeaders().get("Content-Type"));
        assertNull(actualResponse.getBody());
    }

    private static InputStream getCorrectInput(final MovieInfo movieInfo) {
        final var event = "{\n" +
                "  \"Records\": [\n" +
                "    {\n" +
                "      \"eventID\": \"c4ca4238a0b923820dcc509a6f75849b\",\n" +
                "      \"eventName\": \"INSERT\",\n" +
                "      \"eventVersion\": \"1.1\",\n" +
                "      \"eventSource\": \"aws:dynamodb\",\n" +
                "      \"awsRegion\": \"eu-central-1\",\n" +
                "      \"dynamodb\": {\n" +
                "        \"Keys\": {\n" +
                "          \"movieId\": {\n" +
                "            \"S\": \"" + movieInfo.getMovieId() + "\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"NewImage\": {\n" +
                "          \"name\": {\n" +
                "            \"S\": \"" + movieInfo.getName() + "\"\n" +
                "          },\n" +
                "          \"releaseDate\": {\n" +
                "            \"S\": \"" + movieInfo.getReleaseDate() + "\"\n" +
                "          },\n" +
                "          \"countryOfOrigin\": {\n" +
                "            \"S\": \"" + movieInfo.getCountryOfOrigin() + "\"\n" +
                "          },\n" +
                "          \"movieId\": {\n" +
                "            \"S\": \"" + movieInfo.getMovieId() + "\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"ApproximateCreationDateTime\": 1428537600,\n" +
                "        \"SequenceNumber\": \"4421584500000000017450439091\",\n" +
                "        \"SizeBytes\": 26,\n" +
                "        \"StreamViewType\": \"NEW_AND_OLD_IMAGES\"\n" +
                "      },\n" +
                "      \"eventSourceARN\": \"arn:aws:dynamodb:eu-central-1:123456789012:table/ExampleTableWithStream/stream/2015-06-27T00:48:05.899\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        return new ByteArrayInputStream(event.getBytes());
    }

    private static InputStream getIncorrectInput() {
        final var event = "{\n" +
                "    \"resource\": \"/movies/{movieId}\",\n" +
                "    \"path\": \"/movi";

        return new ByteArrayInputStream(event.getBytes());
    }

    private static InputStream getIncorrectInputNullEvent() {
        final var event = "{ }";

        return new ByteArrayInputStream(event.getBytes());
    }
}
