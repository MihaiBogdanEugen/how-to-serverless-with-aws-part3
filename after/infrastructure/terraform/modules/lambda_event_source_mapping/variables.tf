variable starting_position {
  type = string
}

variable function_name {
  type = string
}

variable event_source_arn {
  type = string
}

variable batch_size {
  type    = number
  default = 25
}

variable maximum_batching_window_in_seconds {
  type    = number
  default = 5
}
