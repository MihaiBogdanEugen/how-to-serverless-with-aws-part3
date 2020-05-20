from main.api.api import api_gw_event_handler
from aws_xray_sdk.core import patch_all

patch_all()


def handle_request(event):
    """
    Retrieves a movie record by ID, looks for movieId in the path parameters
    """
    return api_gw_event_handler(event)
