variable starting_position {
  type    = string
  default = "LATEST"
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

variable depends_on_function {
  type = any
}

variable depends_on_event_source {
  type = any
}
