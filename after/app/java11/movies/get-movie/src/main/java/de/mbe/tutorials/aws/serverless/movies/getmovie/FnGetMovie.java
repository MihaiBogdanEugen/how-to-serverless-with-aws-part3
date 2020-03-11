package de.mbe.tutorials.aws.serverless.movies.getmovie;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.mbe.tutorials.aws.serverless.movies.getmovie.repository.MoviesDynamoDbRepository;
import de.mbe.tutorials.aws.serverless.movies.getmovie.utils.APIGatewayV2ProxyResponseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.amazonaws.util.StringUtils.isNullOrEmpty;

public final class FnGetMovie implements RequestHandler<APIGatewayV2ProxyRequestEvent, APIGatewayV2ProxyResponseEvent>, APIGatewayV2ProxyResponseUtils {

    private static final Logger LOGGER = LogManager.getLogger(FnGetMovie.class);

    private final ObjectMapper objectMapper;
    private final MoviesDynamoDbRepository moviesDynamoDbRepository;

    public FnGetMovie() {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        final var amazonDynamoDB = AmazonDynamoDBClientBuilder
                .standard()
                .withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder()))
                .build();

        final var movieInfosTable = System.getenv("MOVIE_INFOS_TABLE");
        final var movieRatingsTable = System.getenv("MOVIE_RATINGS_TABLE");

        moviesDynamoDbRepository = new MoviesDynamoDbRepository(
                amazonDynamoDB,
                movieInfosTable,
                movieRatingsTable);
    }

    @Override
    public APIGatewayV2ProxyResponseEvent handleRequest(APIGatewayV2ProxyRequestEvent request, Context context) {

        LOGGER.info("FnGetMovie.getRemainingTimeInMillis {} ", context.getRemainingTimeInMillis());

        if (!request.getPathParameters().containsKey("movieId") || isNullOrEmpty(request.getPathParameters().get("movieId"))) {
            return badRequest(LOGGER, "Missing path parameter {movieId}");
        }

        final var movieId = request.getPathParameters().get("movieId");
        LOGGER.info("Retrieving movie {}", movieId);

        try {

            final var movie = moviesDynamoDbRepository.getByMovieId(movieId);
            if (movie == null) {
                return notFound(LOGGER, "Movie " + movieId + " not found");
            }

            final var movieAsString = objectMapper.writeValueAsString(movie);
            return ok(LOGGER, movieAsString);

        } catch (AmazonDynamoDBException error) {
            return amazonDynamoDBException(LOGGER, error);
        } catch (Exception error) {
            return internalServerError(LOGGER, error);
        }
    }
}
