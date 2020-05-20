from json import load as json_load
from unittest import TestCase
from unittest import mock, main as unittest_main

from fn_get_movie import handle_request


def get_api_gateway_proxy_request():
    with open('resources/api_gateway_proxy_request.json') as f:
        return json_load(f)


def get_api_gateway_proxy_response():
    with open('resources/api_gateway_proxy_response.json') as f:
        return json_load(f)


class TestHandler(TestCase):

    @mock.patch('fn_get_movie.api_gw_event_handler')
    def test_lambda_handler(self, mock_api_gw_event_handler):
        mock_api_gw_event_handler.return_value = get_api_gateway_proxy_response()

        self.assertEqual(handle_request(get_api_gateway_proxy_request()), get_api_gateway_proxy_response())


if __name__ == '__main__':
    unittest_main()
