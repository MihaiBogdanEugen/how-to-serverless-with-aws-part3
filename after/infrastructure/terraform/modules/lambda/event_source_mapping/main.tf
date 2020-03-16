resource aws_lambda_event_source_mapping event_source_mapping {
  event_source_arn                   = var.event_source_arn
  function_name                      = var.function_name
  starting_position                  = var.starting_position
  batch_size                         = var.batch_size
  maximum_batching_window_in_seconds = var.maximum_batching_window_in_seconds
  depends_on                         = [var.depends_on_function, var.depends_on_event_source]
}