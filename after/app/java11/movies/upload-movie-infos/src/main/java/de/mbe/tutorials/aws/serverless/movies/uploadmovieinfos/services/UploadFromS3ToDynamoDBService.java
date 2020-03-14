package de.mbe.tutorials.aws.serverless.movies.uploadmovieinfos.services;

import de.mbe.tutorials.aws.serverless.movies.uploadmovieinfos.repository.MoviesDynamoDbRepository;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class UploadFromS3ToDynamoDBService {

    private final S3Client s3Client;
    private final String movieInfoBucket;
    private final MoviesDynamoDbRepository moviesDynamoDbRepository;

    public UploadFromS3ToDynamoDBService(final S3Client s3Client, final String movieInfoBucket, final MoviesDynamoDbRepository moviesDynamoDbRepository) {
        this.s3Client = s3Client;
        this.movieInfoBucket = movieInfoBucket;
        this.moviesDynamoDbRepository = moviesDynamoDbRepository;
    }

    public int upload(final List<String> objectKeys) throws IOException {

        var result = 0;
        final var lines = new ArrayList<String>();

        for (final var objectKey : objectKeys) {

            final var request = GetObjectRequest.builder()
                    .bucket(movieInfoBucket)
                    .key(objectKey)
                    .build();

            final var inputStream = s3Client.getObject(request, ResponseTransformer.toInputStream());

            String line;

            try (final var bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                while ((line = bufferedReader.readLine()) != null) {
                    lines.add(line);
                    if (lines.size() == 25) {
                        result += moviesDynamoDbRepository.saveLines(lines);
                        lines.clear();
                    }
                }
            }
        }

        if (!lines.isEmpty()) {
            result += moviesDynamoDbRepository.saveLines(lines);
            lines.clear();
        }

        return result;
    }
}
