resource aws_dynamodb_table table {
  billing_mode     = var.billing_mode
  hash_key         = var.hash_key_name
  name             = var.name
  read_capacity    = var.read_capacity
  write_capacity   = var.write_capacity
  stream_enabled   = var.stream_enabled
  stream_view_type = var.stream_view_type

  attribute {
    name = var.hash_key_name
    type = var.hash_key_type
  }

  server_side_encryption {
    enabled = var.enable_encryption_at_rest
  }
}