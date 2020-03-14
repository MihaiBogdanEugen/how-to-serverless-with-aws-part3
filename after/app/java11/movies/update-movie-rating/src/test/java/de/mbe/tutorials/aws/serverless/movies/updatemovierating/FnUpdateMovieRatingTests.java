package de.mbe.tutorials.aws.serverless.movies.updatemovierating;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.mbe.tutorials.aws.serverless.movies.updatemovierating.repository.MoviesDynamoDbRepository;
import de.mbe.tutorials.aws.serverless.movies.updatemovierating.repository.models.MovieRating;
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
public final class FnUpdateMovieRatingTests implements TestUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private MoviesDynamoDbRepository moviesDynamoDbRepository;

    private FnUpdateMovieRating fnUpdateMovieRating;

    @BeforeEach
    void init() {
        fnUpdateMovieRating = new FnUpdateMovieRating(moviesDynamoDbRepository);
    }

    @Test
    void correctInputWithExistingIdReturnsOk() throws IOException {

        final var movieId = UUID.randomUUID().toString();
        final var expectedMovieRating = getRandomMovieRating(movieId);

        when(moviesDynamoDbRepository.updateMovieRating(expectedMovieRating)).thenReturn(expectedMovieRating);

        final var input = getCorrectInput(expectedMovieRating);
        final var output = new ByteArrayOutputStream();

        fnUpdateMovieRating.handleRequest(input, output, null);

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

        final var actualMovieRating = OBJECT_MAPPER.readValue(actualResponse.getBody(), MovieRating.class);
        assertEquals(expectedMovieRating, actualMovieRating);
    }

    @Test
    void incorrectInputReturnsBadRequestInvalidJson() throws IOException {

        final var input = getIncorrectInput();
        final var output = new ByteArrayOutputStream();

        fnUpdateMovieRating.handleRequest(input, output, null);

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

        fnUpdateMovieRating.handleRequest(getIncorrectInputNullEvent(), output, null);

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
    void incorrectInputReturnsBadRequestValidJsonWrongBody() throws IOException {

        final var output = new ByteArrayOutputStream();

        fnUpdateMovieRating.handleRequest(getCorrectInputWrongBody(new MovieRating()), output, null);

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

        assertTrue(actualResponse.getBody().startsWith("Unexpected character"));
    }

    @Test
    void correctInputWithFaultyDbReturnsInternalServerError() throws IOException {

        final var movieId = UUID.randomUUID().toString();
        final var expectMovieRating = getRandomMovieRating(movieId);

        doThrow(DynamoDbException.class).when(moviesDynamoDbRepository).updateMovieRating(expectMovieRating);

        final var input = getCorrectInput(expectMovieRating);
        final var output = new ByteArrayOutputStream();

        fnUpdateMovieRating.handleRequest(input, output, null);

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

    private static InputStream getCorrectInput(final MovieRating movieRating) {
        final var event = "{\n" +
                "  \"body\": \"{\\n\\t\\\"movieId\\\": \\\"" + movieRating.getMovieId() + "\\\",\\n\\t\\\"rottenTomatoesRating\\\": " + movieRating.getRottenTomatoesRating() + ",\\n\\t\\\"imdbRating\\\": " + movieRating.getImdbRating() +  "\\n}\",\n" +
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
                "    \"movieId\": \"" + movieRating.getMovieId() + "\"\n" +
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

    private static InputStream getCorrectInputWrongBody(final MovieRating movieRating) {
        final var event = "{\n" +
                "  \"body\": \"{asdadada\\n}\",\n" +
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
                "    \"movieId\": \"" + movieRating.getMovieId() + "\"\n" +
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
