from main.repository.dynamodb_repository import get_movie_by_id

import logging

logger = logging.getLogger()
logger.setLevel(logging.INFO)


def __reply(status_code, body):

    """
    Returns a reply object compatible with the API Gateway Proxy integration
    """

    import json

    status_type = status_code / 100

    if status_type == 2:
        logger.info(f"SUCCESS! statusCode: {status_code}, message: {body}")
    elif status_type == 4:
        logger.error(f"CLIENT ERROR! statusCode: {status_code}, message: {body}")
    else:
        logger.error(f"SERVER ERROR! statusCode: {status_code}, message: {body}")

    return {
        "headers": {
            "Content-Type": "application/json"
        },
        "statusCode": status_code,
        "body": json.dumps(body)
    }


def api_gw_event_handler(event):

    """
    Handles an API Gateway proxy integration event, relies on pathParameters.movieId
    """

    if "movieId" not in event["pathParameters"]:
        return __reply(400, "Invalid JSON: Missing or null pathParameters.movieId")

    movie_id = event["pathParameters"]["movieId"]

    logger.info(f"Retrieving movie {movie_id}")

    try:
        movie = get_movie_by_id(movie_id)
    except Exception as e:
        return __reply(500, str(e))

    if movie is None:
        return __reply(404, f"Movie {movie_id} not found")

    return __reply(200, movie)
