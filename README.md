# How to Serverless with AWS - Part 3

> This is Part 3 in a series of 3 workshops on how to build Serverless applications using AWS Technologies
>
> [Part 1](https://github.com/MihaiBogdanEugen/how-to-serverless-with-aws-part1)
>
> [Part 2](https://github.com/MihaiBogdanEugen/how-to-serverless-with-aws-part2)

## Prerequisites
- [AWS Command Line Interface](https://aws.amazon.com/cli/) installed and properly set up
- access to the Amazon Console GUI
- [Amazon Corretto 11](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/what-is-corretto-11.html)
- [Python 3.8.x](https://www.python.org/) 
- [Terraform 0.12.23](https://www.terraform.io/)

## Introduction

Movies is a fictional application used for the management of movies.

### Data Models

#### Movie Info

| Property          | Type   |
|-------------------|--------|
| movie_id          | String |
| name              | String |
| country_of_origin | String |
| release_date      | String |

#### Movie Rating

| Property               | Type   |
|------------------------|--------|
| movie_id               | String |
| rotten_tomatoes_rating | Number |
| imdb_rating            | Number |

#### Movie

| Property               | Type   |
|------------------------|--------|
| movie_id               | String |
| name                   | String |
| country_of_origin      | String |
| release_date           | String |
| rotten_tomatoes_rating | Number |
| imdb_rating            | Number |

### Data Flows
- batch ingestion: periodically, a new file is uploaded to the S3 bucket, containing all Movie Info objects
- single update: occasionally, ratings (a Movie Rating object) are updated
- data retrieval: frequently, a full Movie model is retrieved  

## Architecture

![Movies Architecture](https://raw.githubusercontent.com/MihaiBogdanEugen/how-to-serverless-with-aws-part3/master/new-movies-architecture.png)