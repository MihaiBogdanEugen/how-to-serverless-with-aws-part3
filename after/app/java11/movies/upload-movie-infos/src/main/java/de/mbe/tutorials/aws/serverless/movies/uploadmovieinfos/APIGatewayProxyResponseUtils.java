package de.mbe.tutorials.aws.serverless.movies.uploadmovieinfos;

import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public interface APIGatewayProxyResponseUtils {

    Map<String, String> CONTENT_TYPE_APPLICATION_JSON = Map.of("Content-Type", "application/json");

    Logger getLogger();
    ObjectMapper getObjectMapper();

    default void writeOk(final OutputStream output, final String body) throws IOException {
        write(output, 200, body);
    }

    default void writeBadRequest(final OutputStream output, final IllegalArgumentException error) throws IOException {
        write(output, 400, error.getMessage());
    }

    default void writeInternalServerError(final OutputStream output, final Exception error) throws IOException {
        write(output, 404, error.getMessage());
    }

    default void writeAmazonDynamoDBException(final OutputStream output, final AmazonDynamoDBException error) throws IOException {
        write(output, error.getStatusCode(), error.getMessage());
    }

    default void writeAmazonS3Exception(final OutputStream output, final AmazonS3Exception error) throws IOException {
        write(output, error.getStatusCode(), error.getMessage());
    }

    private void write(final OutputStream output, final int statusCode, final String body) throws IOException {
        switch (statusCode / 100) {
            case 2:
                getLogger().info("SUCCESS! statusCode: {}, message: {}", statusCode, body);
                break;
            case 4:
                getLogger().warn("CLIENT ERROR! statusCode: {}, message: {}", statusCode, body);
                break;
            default:
                getLogger().error("SERVER ERROR! statusCode: {}, message: {}", statusCode, body);
        }

        final var gatewayResponse = new APIGatewayResponse(CONTENT_TYPE_APPLICATION_JSON, statusCode, body);
        getObjectMapper().writeValue(output, gatewayResponse);
    }

    default boolean isNodeNullOrEmpty(final JsonNode node) {
        return node == null || node.isNull() || node.isMissingNode();
    }

    final class APIGatewayResponse {

        private String body;
        private Map<String, String> headers;
        private int statusCode;

        public APIGatewayResponse() {

        }

        public APIGatewayResponse(final Map<String, String> headers, final int statusCode, final String body) {
            this.headers = Map.copyOf(headers);
            this.statusCode = statusCode;
            this.body = body;
        }

        public String getBody() {
            return body;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setBody(final String body) {
            this.body = body;
        }

        public void setHeaders(final Map<String, String> headers) {
            this.headers = Map.copyOf(headers);
        }

        public void setStatusCode(final int statusCode) {
            this.statusCode = statusCode;
        }
    }
}
