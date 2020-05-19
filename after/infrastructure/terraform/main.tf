locals {

  movies_table_name       = "${var.aws_account_id}-movies-table"
  movie_infos_table_name  = "${var.aws_account_id}-movie-infos-table"
  movie_infos_bucket_name = "${var.aws_account_id}-movie-infos-bucket"

  get_movie_lambda_config = {
    java : {
      handler : "de.mbe.tutorials.aws.serverless.movies.getmovie.FnGetMovie::handleRequest"
      runtime : "java11"
    }
    python : {
      handler : "get-movie/fn_get_movie.handle_request"
      runtime : "python3.8"
    }
    filename : "../../app/packages/${var.code_version}-get-movie.zip"
    layer_filename : "../../app/packages/${var.code_version}-get-movie-layer.zip"
    function_name : "${var.code_version}-get-movie"
    layer_name : "${var.code_version}-get-movie-layer"
    provisioned_concurrent_executions : 3
    memory_size : 1792
  }

  update_movie_info_lambda_config = {
    java : {
      handler : "de.mbe.tutorials.aws.serverless.movies.updatemovieinfo.FnUpdateMovieInfo::handleRequest"
      runtime : "java11"
    }
    python : {
      handler : "update-movie-info/fn_update_movie_info.handle_request"
      runtime : "python3.8"
    }
    filename : "../../app/packages/${var.code_version}-update-movie-info.zip"
    layer_filename : "../../app/packages/${var.code_version}-update-movie-info-layer.zip"
    function_name : "${var.code_version}-update-movie-info"
    layer_name : "${var.code_version}-update-movie-info-layer"
    provisioned_concurrent_executions : 3
    memory_size : 1792
  }

  update_movie_rating_lambda_config = {
    java : {
      handler : "de.mbe.tutorials.aws.serverless.movies.updatemovierating.FnUpdateMovieRating::handleRequest"
      runtime : "java11"
    }
    python : {
      handler : "update-movie-rating/fn_update_movie_rating.handle_request"
      runtime : "python3.8"
    }
    filename : "../../app/packages/${var.code_version}-update-movie-rating.zip"
    layer_filename : "../../app/packages/${var.code_version}-update-movie-rating-layer.zip"
    function_name : "${var.code_version}-update-movie-rating"
    layer_name : "${var.code_version}-update-movie-rating-layer"
    provisioned_concurrent_executions : 3
    memory_size : 1792
  }

  upload_movie_infos_lambda_config = {
    java : {
      handler : "de.mbe.tutorials.aws.serverless.movies.uploadmovieinfos.FnUploadMovieInfos::handleRequest"
      runtime : "java11"
    }
    python : {
      handler : "upload-movie-infos/fn_upload_movie_infos.handle_request"
      runtime : "python3.8"
    }
    filename : "../../app/packages/${var.code_version}-upload-movie-infos.zip"
    layer_filename : "../../app/packages/${var.code_version}-upload-movie-infos-layer.zip"
    function_name : "${var.code_version}-upload-movie-infos"
    layer_name : "${var.code_version}-upload-movie-infos-layer"
    provisioned_concurrent_executions : 3
    memory_size : 1792
  }
}

############################################################################

module movies_table {
  source           = "./modules/dynamo_db"
  name             = local.movies_table_name
  hash_key_name    = "movieId"
  stream_enabled   = false
  stream_view_type = ""
}

module movie_infos_table {
  source           = "./modules/dynamo_db"
  name             = local.movie_infos_table_name
  hash_key_name    = "movieId"
  stream_enabled   = true
  stream_view_type = "NEW_IMAGE"
}

############################################################################

module movie_infos_bucket {
  source = "./modules/s3/bucket"
  name   = local.movie_infos_bucket_name
}

############################################################################

data aws_iam_policy_document iam_assume_role_policy {
  statement {
    effect = "Allow"
    principals {
      identifiers = [
        "lambda.amazonaws.com"
      ]
      type = "Service"
    }
    actions = [
      "sts:AssumeRole"
    ]
  }
}

data aws_iam_policy_document get_movie_lambda_iam_policy_document {
  statement {
    effect = "Allow"
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]
    resources = [
      "*"
    ]
  }
  statement {
    effect = "Allow"
    actions = [
      "xray:PutTraceSegments",
      "xray:PutTelemetryRecords",
      "xray:GetSamplingRules",
      "xray:GetSamplingTargets",
      "xray:GetSamplingStatisticSummaries"
    ]
    resources = [
      "*"
    ]
  }
  statement {
    effect = "Allow"
    actions = [
      "dynamodb:BatchGetItem",
      "dynamodb:GetItem",
      "dynamodb:Query",
      "dynamodb:Scan"
    ]
    resources = [
      module.movies_table.arn
    ]
  }
}

data aws_iam_policy_document update_movie_info_lambda_iam_policy_document {
  statement {
    effect = "Allow"
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]
    resources = [
      "*"
    ]
  }
  statement {
    effect = "Allow"
    actions = [
      "xray:PutTraceSegments",
      "xray:PutTelemetryRecords",
      "xray:GetSamplingRules",
      "xray:GetSamplingTargets",
      "xray:GetSamplingStatisticSummaries"
    ]
    resources = [
      "*"
    ]
  }
  statement {
    effect = "Allow"
    actions = [
      "dynamodb:DescribeStream",
      "dynamodb:GetRecords",
      "dynamodb:GetShardIterator",
      "dynamodb:ListStreams"
    ]
    resources = [
      "${module.movie_infos_table.arn}/stream/*"
    ]
  }
  statement {
    effect = "Allow"
    actions = [
      "dynamodb:BatchWriteItem",
      "dynamodb:PutItem",
      "dynamodb:UpdateItem"
    ]
    resources = [
      module.movies_table.arn
    ]
  }
}

data aws_iam_policy_document update_movie_rating_lambda_iam_policy_document {
  statement {
    effect = "Allow"
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]
    resources = [
      "*"
    ]
  }
  statement {
    effect = "Allow"
    actions = [
      "xray:PutTraceSegments",
      "xray:PutTelemetryRecords",
      "xray:GetSamplingRules",
      "xray:GetSamplingTargets",
      "xray:GetSamplingStatisticSummaries"
    ]
    resources = [
      "*"
    ]
  }
  statement {
    effect = "Allow"
    actions = [
      "dynamodb:BatchWriteItem",
      "dynamodb:PutItem",
      "dynamodb:UpdateItem"
    ]
    resources = [
      module.movies_table.arn
    ]
  }
}

data aws_iam_policy_document upload_movie_infos_lambda_iam_policy_document {
  statement {
    effect = "Allow"
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]
    resources = [
      "*"
    ]
  }
  statement {
    effect = "Allow"
    actions = [
      "xray:PutTraceSegments",
      "xray:PutTelemetryRecords",
      "xray:GetSamplingRules",
      "xray:GetSamplingTargets",
      "xray:GetSamplingStatisticSummaries"
    ]
    resources = [
      "*"
    ]
  }
  statement {
    effect = "Allow"
    actions = [
      "s3:ListBucket",
      "s3:GetBucketLocation"
    ]
    resources = [
      module.movie_infos_bucket.arn
    ]
  }
  statement {
    effect = "Allow"
    actions = [
      "s3:GetObject",
      "s3:GetObjectAcl",
      "s3:GetObjectVersion"
    ]
    resources = [
      "${module.movie_infos_bucket.arn}/*"
    ]
  }
  statement {
    effect = "Allow"
    actions = [
      "dynamodb:BatchWriteItem",
      "dynamodb:PutItem",
      "dynamodb:UpdateItem"
    ]
    resources = [
      module.movie_infos_table.arn
    ]
  }
}

############################################################################

module get_movie_lambda_role {
  source                  = "./modules/iam/role"
  role_name               = "get_movie_lambda_role"
  assume_role_policy_json = data.aws_iam_policy_document.iam_assume_role_policy.json
  policy_name             = "get_movie_lambda_policy"
  policy_json             = data.aws_iam_policy_document.get_movie_lambda_iam_policy_document.json
}

module update_movie_info_lambda_role {
  source                  = "./modules/iam/role"
  role_name               = "update_movie_info_lambda_role"
  assume_role_policy_json = data.aws_iam_policy_document.iam_assume_role_policy.json
  policy_name             = "update_movie_info_lambda_policy"
  policy_json             = data.aws_iam_policy_document.update_movie_info_lambda_iam_policy_document.json
}

module update_movie_rating_lambda_role {
  source                  = "./modules/iam/role"
  role_name               = "update_movie_rating_lambda_role"
  assume_role_policy_json = data.aws_iam_policy_document.iam_assume_role_policy.json
  policy_name             = "update_movie_rating_lambda_policy"
  policy_json             = data.aws_iam_policy_document.update_movie_rating_lambda_iam_policy_document.json
}

module upload_movie_infos_lambda_role {
  source                  = "./modules/iam/role"
  role_name               = "upload_movie_infos_lambda_role"
  assume_role_policy_json = data.aws_iam_policy_document.iam_assume_role_policy.json
  policy_name             = "upload_movie_infos_lambda_policy"
  policy_json             = data.aws_iam_policy_document.upload_movie_infos_lambda_iam_policy_document.json
}

############################################################################

module get_movie_lambda {
  source                            = "./modules/lambda/function"
  function_name                     = local.get_movie_lambda_config.function_name
  description                       = "Receive a GET request from the API GW and retreive the resource from the DynamoDB table"
  role                              = module.get_movie_lambda_role.arn
  runtime                           = local.get_movie_lambda_config[var.code_version]["runtime"]
  handler                           = local.get_movie_lambda_config[var.code_version]["handler"]
  filename                          = local.get_movie_lambda_config.filename
  layer_name                        = local.get_movie_lambda_config.layer_name
  layer_filename                    = local.get_movie_lambda_config.layer_filename
  provisioned_concurrent_executions = local.get_movie_lambda_config.provisioned_concurrent_executions
  memory_size                       = local.get_movie_lambda_config.memory_size
  env = {
    MOVIES_TABLE = module.movies_table.name
  }
}

module update_movie_info_lambda {
  source                            = "./modules/lambda/function"
  function_name                     = local.update_movie_info_lambda_config.function_name
  description                       = "Update movie info part of a movie in the DynamoDB table"
  role                              = module.update_movie_info_lambda_role.arn
  runtime                           = local.update_movie_info_lambda_config[var.code_version]["runtime"]
  handler                           = local.update_movie_info_lambda_config[var.code_version]["handler"]
  filename                          = local.update_movie_info_lambda_config.filename
  layer_name                        = local.update_movie_info_lambda_config.layer_name
  layer_filename                    = local.update_movie_info_lambda_config.layer_filename
  provisioned_concurrent_executions = local.update_movie_info_lambda_config.provisioned_concurrent_executions
  memory_size                       = local.update_movie_info_lambda_config.memory_size
  env = {
    MOVIES_TABLE = module.movies_table.name
  }
}

module update_movie_rating_lambda {
  source                            = "./modules/lambda/function"
  function_name                     = local.update_movie_rating_lambda_config.function_name
  description                       = "Receive a PATCH request from the API GW and save the resource in the DynamoDB table"
  role                              = module.update_movie_rating_lambda_role.arn
  runtime                           = local.update_movie_rating_lambda_config[var.code_version]["runtime"]
  handler                           = local.update_movie_rating_lambda_config[var.code_version]["handler"]
  filename                          = local.update_movie_rating_lambda_config.filename
  layer_name                        = local.update_movie_rating_lambda_config.layer_name
  layer_filename                    = local.update_movie_rating_lambda_config.layer_filename
  provisioned_concurrent_executions = local.update_movie_rating_lambda_config.provisioned_concurrent_executions
  memory_size                       = local.update_movie_rating_lambda_config.memory_size
  env = {
    MOVIES_TABLE = module.movies_table.name
  }
}

module upload_movie_infos_lambda {
  source                            = "./modules/lambda/function"
  function_name                     = local.upload_movie_infos_lambda_config.function_name
  description                       = "Read movies from an S3 file and dump them into the DynamoDB table"
  role                              = module.upload_movie_infos_lambda_role.arn
  runtime                           = local.upload_movie_infos_lambda_config[var.code_version]["runtime"]
  handler                           = local.upload_movie_infos_lambda_config[var.code_version]["handler"]
  filename                          = local.upload_movie_infos_lambda_config.filename
  layer_name                        = local.upload_movie_infos_lambda_config.layer_name
  layer_filename                    = local.upload_movie_infos_lambda_config.layer_filename
  provisioned_concurrent_executions = local.upload_movie_infos_lambda_config.provisioned_concurrent_executions
  memory_size                       = local.upload_movie_infos_lambda_config.memory_size
  env = {
    MOVIE_INFOS_BUCKET = module.movie_infos_bucket.name
    MOVIE_INFOS_TABLE  = module.movie_infos_table.name
  }
}

############################################################################

module movies_api_gw {
  source      = "./modules/api_gateway/rest_api"
  name        = "movies_api"
  description = "This is the API for the Movies project"
}

module movies_resource {
  source      = "./modules/api_gateway/resource"
  rest_api_id = module.movies_api_gw.id
  parent_id   = module.movies_api_gw.root_resource_id
  path_part   = "movies"
}

module movie_resource {
  source      = "./modules/api_gateway/resource"
  rest_api_id = module.movies_api_gw.id
  parent_id   = module.movies_resource.id
  path_part   = "{movieId}"
}

module get_movie_request_method {
  source        = "./modules/api_gateway/method"
  rest_api_id   = module.movies_api_gw.id
  resource_id   = module.movie_resource.id
  http_method   = "GET"
  authorization = "NONE"
}

module update_movie_rating_request_method {
  source        = "./modules/api_gateway/method"
  rest_api_id   = module.movies_api_gw.id
  resource_id   = module.movie_resource.id
  http_method   = "PATCH"
  authorization = "NONE"
}

module get_movie_request_integration {
  source              = "./modules/api_gateway/integration"
  rest_api_id         = module.movies_api_gw.id
  resource_id         = module.movie_resource.id
  http_method         = module.get_movie_request_method.http_method
  function_invoke_arn = module.get_movie_lambda.invoke_arn
  depends_on_method   = module.get_movie_request_method
}

module update_movie_rating_request_integration {
  source              = "./modules/api_gateway/integration"
  rest_api_id         = module.movies_api_gw.id
  resource_id         = module.movie_resource.id
  http_method         = module.update_movie_rating_request_method.http_method
  function_invoke_arn = module.update_movie_rating_lambda.invoke_arn
  depends_on_method   = module.update_movie_rating_request_method
}

module movies_api_deployment {
  source                   = "./modules/api_gateway/deployment"
  rest_api_id              = module.movies_api_gw.id
  stage_name               = "prod"
  depends_on_integration_1 = module.get_movie_request_integration
  depends_on_integration_2 = module.update_movie_rating_request_integration
}

############################################################################

module allow_movies_bucket_to_invoke_upload_movie_infos_lambda {
  source              = "./modules/lambda/permission/allow_execution_from_s3_bucket"
  bucket_arn          = module.movie_infos_bucket.arn
  function_arn        = module.upload_movie_infos_lambda.arn
  function_alias      = module.upload_movie_infos_lambda.alias_name
  depends_on_bucket   = module.movie_infos_bucket
  depends_on_function = module.upload_movie_infos_lambda
}

module movies_bucket_notification {
  source              = "./modules/s3/notification/object_created"
  bucket_id           = module.movie_infos_bucket.id
  function_arn        = module.upload_movie_infos_lambda.alias_arn
  file_extension      = "csv"
  depends_on_function = module.upload_movie_infos_lambda
  depends_on_bucket   = module.movie_infos_bucket
}

############################################################################

module allow_movies_api_gw_to_invoke_get_movie_lambda {
  source              = "./modules/lambda/permission/allow_execution_from_api_gateway"
  region              = var.aws_region
  account_id          = var.aws_account_id
  api_gw_id           = module.movies_api_gw.id
  resource_path       = module.movie_resource.path
  function_arn        = module.get_movie_lambda.arn
  function_alias      = module.get_movie_lambda.alias_name
  method_http_verb    = module.get_movie_request_method.http_method
  depends_on_function = module.get_movie_lambda
  depends_on_api_gw   = module.movies_api_gw
}

module allow_movies_api_gw_to_invoke_update_movie_rating_lambda {
  source              = "./modules/lambda/permission/allow_execution_from_api_gateway"
  region              = var.aws_region
  account_id          = var.aws_account_id
  api_gw_id           = module.movies_api_gw.id
  resource_path       = module.movie_resource.path
  function_arn        = module.update_movie_rating_lambda.arn
  function_alias      = module.update_movie_rating_lambda.alias_name
  method_http_verb    = module.update_movie_rating_request_method.http_method
  depends_on_function = module.update_movie_rating_lambda
  depends_on_api_gw   = module.movies_api_gw
}

############################################################################

module stream_updates_to_invoke_update_movie_info_lambda {
  source                             = "./modules/lambda/event_source_mapping"
  event_source_arn                   = module.movie_infos_table.stream_arn
  function_name                      = module.update_movie_info_lambda.alias_arn
  batch_size                         = 25
  maximum_batching_window_in_seconds = 3
  starting_position                  = "TRIM_HORIZON"
  depends_on_function                = module.update_movie_info_lambda
  depends_on_event_source            = module.movie_infos_table
}

############################################################################

