package de.mbe.tutorials.aws.serverless.movies.uploadmovieinfos.services;

import com.amazonaws.services.s3.AmazonS3;
import de.mbe.tutorials.aws.serverless.movies.uploadmovieinfos.repository.MoviesDynamoDbRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class UploadFromS3ToDynamoDBService {

    private final AmazonS3 amazonS3;
    private final String movieInfoBucket;
    private final MoviesDynamoDbRepository moviesDynamoDbRepository;

    public UploadFromS3ToDynamoDBService(final AmazonS3 amazonS3, final String movieInfoBucket, final MoviesDynamoDbRepository moviesDynamoDbRepository) {
        this.amazonS3 = amazonS3;
        this.movieInfoBucket = movieInfoBucket;
        this.moviesDynamoDbRepository = moviesDynamoDbRepository;
    }

    public int upload(final List<String> objectKeys) throws IOException {

        var result = 0;
        final var lines = new ArrayList<String>();

        for (final var objectKey : objectKeys) {

            final var s3Object = amazonS3.getObject(movieInfoBucket, objectKey);

            String line;

            try (final var inputStream = s3Object.getObjectContent()) {
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
        }

        if (!lines.isEmpty()) {
            result += moviesDynamoDbRepository.saveLines(lines);
            lines.clear();
        }

        return result;
    }
}
