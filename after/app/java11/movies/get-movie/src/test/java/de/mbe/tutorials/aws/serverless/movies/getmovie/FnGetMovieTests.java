package de.mbe.tutorials.aws.serverless.movies.getmovie;

import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mbe.tutorials.aws.serverless.movies.getmovie.repository.MoviesDynamoDbRepository;
import de.mbe.tutorials.aws.serverless.movies.getmovie.repository.models.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public final class FnGetMovieTests {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private MoviesDynamoDbRepository moviesDynamoDbRepository;

    private FnGetMovie fnGetMovie;

    @BeforeEach
    void init() {
        fnGetMovie = new FnGetMovie(moviesDynamoDbRepository);
    }

    @Test
    void correctInputWithExistingIdReturnsOk() throws IOException {

        final var expectMovie = new Movie();
        expectMovie.setMovieId("mv1");
        expectMovie.setName("aaa");
        expectMovie.setCountryOfOrigin("bbb");
        expectMovie.setReleaseDate("1985-12-24");
        expectMovie.setImdbRating(99);
        expectMovie.setRottenTomatoesRating(66);

        when(moviesDynamoDbRepository.getByMovieId(expectMovie.getMovieId())).thenReturn(expectMovie);

        final var input = getCorrectInput(expectMovie.getMovieId());
        final var output = new ByteArrayOutputStream();

        fnGetMovie.handleRequest(input, output, null);

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
        assertEquals(expectMovie, OBJECT_MAPPER.readValue(actualResponse.getBody(), Movie.class));
    }

    @Test
    void correctInputWithUnknownIdReturnsNotFound() throws IOException {

        final var expectMovie = new Movie();
        expectMovie.setMovieId("mv1");

        when(moviesDynamoDbRepository.getByMovieId(expectMovie.getMovieId())).thenReturn(null);

        final var input = getCorrectInput(expectMovie.getMovieId());
        final var output = new ByteArrayOutputStream();

        fnGetMovie.handleRequest(input, output, null);

        assertNotNull(output);
        assertTrue(output.size() > 0);

        final var actualResponse = OBJECT_MAPPER.readValue(output.toString(), APIGatewayProxyResponseUtils.APIGatewayResponse.class);

        assertNotNull(actualResponse);
        assertEquals(404, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getHeaders());
        assertEquals(1, actualResponse.getHeaders().size());
        assertTrue(actualResponse.getHeaders().containsKey("Content-Type"));
        assertEquals("application/json", actualResponse.getHeaders().get("Content-Type"));
        assertNotNull(actualResponse.getBody());
        assertEquals("Movie mv1 not found", actualResponse.getBody());
    }

    @Test
    void correctInputWithFaultyDbReturnsInternalServerError() throws IOException {

        final var expectMovie = new Movie();
        expectMovie.setMovieId("mv1");

        when(moviesDynamoDbRepository.getByMovieId(expectMovie.getMovieId())).thenThrow(AmazonDynamoDBException.class);

        final var input = getCorrectInput(expectMovie.getMovieId());
        final var output = new ByteArrayOutputStream();

        fnGetMovie.handleRequest(input, output, null);

        assertNotNull(output);
        assertTrue(output.size() > 0);

        final var actualResponse = OBJECT_MAPPER.readValue(output.toString(), APIGatewayProxyResponseUtils.APIGatewayResponse.class);

        assertNotNull(actualResponse);
        assertEquals(0, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getHeaders());
        assertEquals(1, actualResponse.getHeaders().size());
        assertTrue(actualResponse.getHeaders().containsKey("Content-Type"));
        assertEquals("application/json", actualResponse.getHeaders().get("Content-Type"));
        assertNotNull(actualResponse.getBody());
        assertEquals("null (Service: null; Status Code: 0; Error Code: null; Request ID: null)", actualResponse.getBody());
    }

    @Test
    void incorrectInputReturnsBadRequestInvalidJson() throws IOException {

        final var input = getIncorrectInput();
        final var output = new ByteArrayOutputStream();

        fnGetMovie.handleRequest(input, output, null);

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
    void incorrectInputReturnsBadRequestInvalidJsonEventNull() throws IOException {

        final var output = new ByteArrayOutputStream();

        fnGetMovie.handleRequest(getIncorrectInputNullEvent(), output, null);

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
        assertEquals("Invalid JSON: Missing or null pathParameters.movieId", actualResponse.getBody());
    }

    @Test
    void incorrectInputReturnsBadRequestValidJsonNoMovieId() throws IOException {

        final var output = new ByteArrayOutputStream();

        fnGetMovie.handleRequest(getCorrectInput(""), output, null);

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
        assertEquals("Invalid JSON: Missing or null pathParameters.movieId", actualResponse.getBody());
    }

    private static InputStream getCorrectInput(final String movieId) {
        final var event = "{\n" +
                "  \"body\": \"eyJ0ZXN0IjoiYm9keSJ9\",\n" +
                "  \"resource\": \"/{proxy+}\",\n" +
                "  \"path\": \"/path/to/resource\",\n" +
                "  \"httpMethod\": \"POST\",\n" +
                "  \"isBase64Encoded\": true,\n" +
                "  \"queryStringParameters\": {\n" +
                "    \"foo\": \"bar\"\n" +
                "  },\n" +
                "  \"multiValueQueryStringParameters\": {\n" +
                "    \"foo\": [\n" +
                "      \"bar\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"pathParameters\": {\n" +
                "    \"movieId\": \"" + movieId + "\"\n" +
                "  },\n" +
                "  \"stageVariables\": {\n" +
                "    \"baz\": \"qux\"\n" +
                "  },\n" +
                "  \"headers\": {\n" +
                "    \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\",\n" +
                "    \"Accept-Encoding\": \"gzip, deflate, sdch\",\n" +
                "    \"Accept-Language\": \"en-US,en;q=0.8\",\n" +
                "    \"Cache-Control\": \"max-age=0\",\n" +
                "    \"CloudFront-Forwarded-Proto\": \"https\",\n" +
                "    \"CloudFront-Is-Desktop-Viewer\": \"true\",\n" +
                "    \"CloudFront-Is-Mobile-Viewer\": \"false\",\n" +
                "    \"CloudFront-Is-SmartTV-Viewer\": \"false\",\n" +
                "    \"CloudFront-Is-Tablet-Viewer\": \"false\",\n" +
                "    \"CloudFront-Viewer-Country\": \"US\",\n" +
                "    \"Host\": \"1234567890.execute-api.eu-central-1.amazonaws.com\",\n" +
                "    \"Upgrade-Insecure-Requests\": \"1\",\n" +
                "    \"User-Agent\": \"Custom User Agent String\",\n" +
                "    \"Via\": \"1.1 08f323deadbeefa7af34d5feb414ce27.cloudfront.net (CloudFront)\",\n" +
                "    \"X-Amz-Cf-Id\": \"cDehVQoZnx43VYQb9j2-nvCh-9z396Uhbp027Y2JvkCPNLmGJHqlaA==\",\n" +
                "    \"X-Forwarded-For\": \"127.0.0.1, 127.0.0.2\",\n" +
                "    \"X-Forwarded-Port\": \"443\",\n" +
                "    \"X-Forwarded-Proto\": \"https\"\n" +
                "  },\n" +
                "  \"multiValueHeaders\": {\n" +
                "    \"Accept\": [\n" +
                "      \"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\"\n" +
                "    ],\n" +
                "    \"Accept-Encoding\": [\n" +
                "      \"gzip, deflate, sdch\"\n" +
                "    ],\n" +
                "    \"Accept-Language\": [\n" +
                "      \"en-US,en;q=0.8\"\n" +
                "    ],\n" +
                "    \"Cache-Control\": [\n" +
                "      \"max-age=0\"\n" +
                "    ],\n" +
                "    \"CloudFront-Forwarded-Proto\": [\n" +
                "      \"https\"\n" +
                "    ],\n" +
                "    \"CloudFront-Is-Desktop-Viewer\": [\n" +
                "      \"true\"\n" +
                "    ],\n" +
                "    \"CloudFront-Is-Mobile-Viewer\": [\n" +
                "      \"false\"\n" +
                "    ],\n" +
                "    \"CloudFront-Is-SmartTV-Viewer\": [\n" +
                "      \"false\"\n" +
                "    ],\n" +
                "    \"CloudFront-Is-Tablet-Viewer\": [\n" +
                "      \"false\"\n" +
                "    ],\n" +
                "    \"CloudFront-Viewer-Country\": [\n" +
                "      \"US\"\n" +
                "    ],\n" +
                "    \"Host\": [\n" +
                "      \"0123456789.execute-api.eu-central-1.amazonaws.com\"\n" +
                "    ],\n" +
                "    \"Upgrade-Insecure-Requests\": [\n" +
                "      \"1\"\n" +
                "    ],\n" +
                "    \"User-Agent\": [\n" +
                "      \"Custom User Agent String\"\n" +
                "    ],\n" +
                "    \"Via\": [\n" +
                "      \"1.1 08f323deadbeefa7af34d5feb414ce27.cloudfront.net (CloudFront)\"\n" +
                "    ],\n" +
                "    \"X-Amz-Cf-Id\": [\n" +
                "      \"cDehVQoZnx43VYQb9j2-nvCh-9z396Uhbp027Y2JvkCPNLmGJHqlaA==\"\n" +
                "    ],\n" +
                "    \"X-Forwarded-For\": [\n" +
                "      \"127.0.0.1, 127.0.0.2\"\n" +
                "    ],\n" +
                "    \"X-Forwarded-Port\": [\n" +
                "      \"443\"\n" +
                "    ],\n" +
                "    \"X-Forwarded-Proto\": [\n" +
                "      \"https\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"requestContext\": {\n" +
                "    \"accountId\": \"123456789012\",\n" +
                "    \"resourceId\": \"123456\",\n" +
                "    \"stage\": \"prod\",\n" +
                "    \"requestId\": \"c6af9ac6-7b61-11e6-9a41-93e8deadbeef\",\n" +
                "    \"requestTime\": \"09/Apr/2015:12:34:56 +0000\",\n" +
                "    \"requestTimeEpoch\": 1428582896000,\n" +
                "    \"identity\": {\n" +
                "      \"cognitoIdentityPoolId\": null,\n" +
                "      \"accountId\": null,\n" +
                "      \"cognitoIdentityId\": null,\n" +
                "      \"caller\": null,\n" +
                "      \"accessKey\": null,\n" +
                "      \"sourceIp\": \"127.0.0.1\",\n" +
                "      \"cognitoAuthenticationType\": null,\n" +
                "      \"cognitoAuthenticationProvider\": null,\n" +
                "      \"userArn\": null,\n" +
                "      \"userAgent\": \"Custom User Agent String\",\n" +
                "      \"user\": null\n" +
                "    },\n" +
                "    \"path\": \"/prod/path/to/resource\",\n" +
                "    \"resourcePath\": \"/{proxy+}\",\n" +
                "    \"httpMethod\": \"POST\",\n" +
                "    \"apiId\": \"1234567890\",\n" +
                "    \"protocol\": \"HTTP/1.1\"\n" +
                "  }\n" +
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
