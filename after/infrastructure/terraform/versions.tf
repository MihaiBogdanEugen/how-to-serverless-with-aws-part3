terraform {
  required_version = "0.12.25"
}

provider aws {
  region  = var.aws_region
  version = "2.62.0"
}