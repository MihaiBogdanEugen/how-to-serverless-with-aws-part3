output movies_stats_api_deployment_execution_invoke_url {
  value = module.movies_api_deployment.invoke_url
}

output movies_bucket_uri {
  value = "s3://${module.movie_infos_bucket.name}"
}