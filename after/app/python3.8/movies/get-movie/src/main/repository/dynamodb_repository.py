import logging

logger = logging.getLogger()
logger.setLevel(logging.INFO)

init_done = False
dynamo_db = None
dynamo_db_movies_table = None


def __init_dynamo_db_repository():

    """
    Lazy initialization for all DynamoDB related resources (boto3 dynamodb resource and boto3 dynamodb table)
    """

    global dynamo_db, dynamo_db_movies_table, init_done

    import os

    table_name = os.getenv("MOVIES_TABLE")

    if not init_done:
        dynamo_db = dynamo_db if dynamo_db else __init_dynamo_db()
        dynamo_db_movies_table = dynamo_db_movies_table if dynamo_db_movies_table else dynamo_db.Table(table_name)
        init_done = True


def __init_dynamo_db():

    """
    Initializes the boto3 dynamodb resource
    """

    import os
    import boto3

    aws_region = os.getenv("AWS_REGION")
    movies_table = os.getenv("MOVIES_TABLE")
    enable_mocks = os.getenv("ENABLE_MOCKS")

    try:
        if enable_mocks:
            return boto3.resource(
                "dynamodb",
                region_name=aws_region,
                endpoint_url="http://localhost:8005",
                aws_access_key_id="test_id",
                aws_secret_access_key="test_access_key",
            )
        else:
            return boto3.resource(
                "dynamodb",
                region_name=aws_region
            )
    except Exception as e:
        logger.error(f"Unable to initialize DynamoDB resource for AWS region {aws_region} and table {movies_table}. Error: {e}")
        return None


def get_movie_by_id(movie_id):

    """
    Retrieves the movie record with specified identifier
    """

    __init_dynamo_db_repository()

    response = dynamo_db_movies_table.get_item(
        Key={
            "movieId": movie_id
        },
    )

    if "Item" in response:
        return response["Item"]

    return None
