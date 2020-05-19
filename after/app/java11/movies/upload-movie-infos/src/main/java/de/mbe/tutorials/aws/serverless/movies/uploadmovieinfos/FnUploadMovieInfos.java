package de.mbe.tutorials.aws.serverless.movies.uploadmovieinfos;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.xray.interceptors.TracingInterceptor;
import de.mbe.tutorials.aws.serverless.movies.uploadmovieinfos.repository.MoviesDynamoDbRepository;
import de.mbe.tutorials.aws.serverless.movies.uploadmovieinfos.services.UploadFromS3ToDynamoDBService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.List;
import java.util.stream.Collectors;

public final class FnUploadMovieInfos {

    private static final Logger LOGGER = LogManager.getLogger(FnUploadMovieInfos.class);

    private final String movieInfosBucket;
    private final UploadFromS3ToDynamoDBService uploadFromS3ToDynamoDBService;

    public FnUploadMovieInfos() {

        final var dynamoDbClient = DynamoDbClient.builder()
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .addExecutionInterceptor(new TracingInterceptor())
                        .build())
                .build();

        final var s3Client = S3Client.builder()
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .addExecutionInterceptor(new TracingInterceptor())
                        .build())
                .build();

        movieInfosBucket = System.getenv("MOVIE_INFOS_BUCKET");
        final var movieInfosTable = System.getenv("MOVIE_INFOS_TABLE");

        final var moviesDynamoDbRepository = new MoviesDynamoDbRepository(dynamoDbClient, movieInfosTable);

        uploadFromS3ToDynamoDBService = new UploadFromS3ToDynamoDBService(s3Client, movieInfosBucket, moviesDynamoDbRepository);
    }

    public FnUploadMovieInfos(final String movieInfosBucket, final UploadFromS3ToDynamoDBService uploadFromS3ToDynamoDBService) {
        this.movieInfosBucket = movieInfosBucket;
        this.uploadFromS3ToDynamoDBService = uploadFromS3ToDynamoDBService;
    }

    public Boolean handleRequest(final S3Event input) {

        try {

            final var objectKeys = getObjectKeys(input, movieInfosBucket);
            final var result = uploadFromS3ToDynamoDBService.upload(objectKeys);
            return reply(200, result);

        } catch (IllegalArgumentException error) {
            return reply(400, error);
        } catch (DynamoDbException | S3Exception error) {
            return reply(error.statusCode(), error);
        } catch (Exception error) {
            return reply(500, error);
        }
    }

    private static List<String> getObjectKeys(final S3Event input, final String bucketName) {

        return input.getRecords().stream()
                .filter(x -> x.getS3().getBucket().getName().equalsIgnoreCase(bucketName))
                .map(x -> x.getS3().getObject().getKey())
                .collect(Collectors.toList());
    }

    private static <T> Boolean reply(final int statusCode, final T body) {

        var response = false;
        switch (statusCode / 100) {
            case 2:
                LOGGER.info("SUCCESS! statusCode: {}, message: {}", statusCode, body);
                response = true;
                break;
            case 4:
                LOGGER.warn("CLIENT ERROR! statusCode: {}, message: {}", statusCode, body);
                break;
            default:
                LOGGER.error("SERVER ERROR! statusCode: {}, message: {}", statusCode, body);
        }
        return response;
    }
}