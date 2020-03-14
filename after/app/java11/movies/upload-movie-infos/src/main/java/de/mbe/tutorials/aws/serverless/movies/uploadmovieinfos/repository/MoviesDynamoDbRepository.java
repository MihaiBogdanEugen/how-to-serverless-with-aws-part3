package de.mbe.tutorials.aws.serverless.movies.uploadmovieinfos.repository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MoviesDynamoDbRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String movieInfosTable;

    public MoviesDynamoDbRepository(final DynamoDbClient dynamoDbClient, final String movieInfosTable) {
        this.dynamoDbClient = dynamoDbClient;
        this.movieInfosTable = movieInfosTable;
    }

    public int saveLines(final List<String> lines) {

        if (lines.isEmpty()) {
            return 0;
        }

        final var writeRequests = new ArrayList<WriteRequest>();

        for (var line : lines) {
            final var parts = line.split(",");
            writeRequests.add(WriteRequest.builder()
                    .putRequest(PutRequest.builder()
                            .item(Map.ofEntries(
                                    new AbstractMap.SimpleEntry<>("movieId", AttributeValue.builder().s(parts[0]).build()),
                                    new AbstractMap.SimpleEntry<>("name", AttributeValue.builder().s(parts[1]).build()),
                                    new AbstractMap.SimpleEntry<>("countryOfOrigin", AttributeValue.builder().s(parts[2]).build()),
                                    new AbstractMap.SimpleEntry<>("releaseDate", AttributeValue.builder().s(parts[3]).build())
                            ))
                            .build())
                    .build());
        }

        var batchItemRequest = BatchWriteItemRequest.builder()
                .requestItems(Map.of(movieInfosTable, writeRequests))
                .build();

        dynamoDbClient.batchWriteItem(batchItemRequest);

        return lines.size();
    }
}
