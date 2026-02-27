# Webapp

A RESTful backend service built with Java 21 and Spring Boot 3. Handles file uploads to AWS S3 with metadata stored in PostgreSQL. Deployed on AWS EC2 via a custom AMI built with Packer, running behind an autoscaling group and load balancer provisioned with Terraform.

## Tech Stack

- Java 21 (Temurin LTS)
- Spring Boot 3.2
- Spring Data JPA + Hibernate
- Flyway (database migrations)
- PostgreSQL 17 (AWS RDS in production)
- AWS S3 (file storage)
- Micrometer + StatsD (metrics to CloudWatch)
- SLF4J + Logback (logs to /var/log/webapp.log)
- JUnit 5 + Spring MockMvc (testing)
- Packer (AMI builds)
- GitHub Actions (CI/CD)

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /healthz | Health check - writes to DB, returns 200 if healthy |
| POST | /v1/file | Upload a file (multipart/form-data) to S3 |
| GET | /v1/file?id= | Get file metadata by ID |
| DELETE | /v1/file/:id | Delete file from S3 and remove metadata from DB |

### Health Check Behavior

- Returns `200` with cache headers if DB write succeeds
- Returns `400` if query params or request body are present
- Returns `405` for any non-GET method
- Returns `503` if DB is unreachable

## Project Structure

```
src/
  main/
    java/com/csye6225/webapp/
      WebappApplication.java          - entry point
      controller/
        HealthCheckController.java    - GET /healthz
        FileController.java           - POST/GET/DELETE /v1/file
      service/
        S3Service.java                - S3 upload and delete with timing metrics
      entity/
        HealthCheck.java              - JPA entity for health check records
        FileRecord.java               - JPA entity for file metadata
      repository/
        HealthCheckRepository.java
        FileRepository.java
      config/
        AwsConfig.java                - S3Client bean
        RequestMetricsFilter.java     - logs every request, fires StatsD metrics
        GlobalExceptionHandler.java   - catches unhandled exceptions
    resources/
      application.properties          - all config via environment variables
      db/migration/
        V1__init.sql                  - creates health_check table
        V2__add_file_model.sql        - creates file table
  test/
    java/com/csye6225/webapp/
      HealthCheckControllerTest.java  - 4 integration tests for /healthz
    resources/
      application.properties          - H2 in-memory DB for tests, no AWS needed
```

## Running Locally

### Prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL running locally (or Docker)
- AWS credentials configured (for S3)

### Start PostgreSQL with Docker

```bash
docker run --name webapp-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=webapp \
  -p 5432:5432 \
  -d postgres:17
```

### Set environment variables

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/webapp
export AWS_REGION=us-east-1
export AWS_S3_BUCKET=your-bucket-name
```

### Run

```bash
mvn spring-boot:run
```

App starts on port 8080 by default.

## Running Tests

Tests use H2 in-memory database. No PostgreSQL or AWS credentials needed.

```bash
mvn test
```

Expected output:

```
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Building the JAR

```bash
mvn package -DskipTests
```

Produces `target/webapp-1.0.0.jar`

## CI/CD Pipeline

Every push to `main` triggers the pipeline defined in `.github/workflows/ci.yml`.

**On pull request to main:**
- Runs all tests

**On merge to main:**
1. Runs tests
2. Builds fat JAR
3. Runs Packer to bake a new AMI on AWS with the JAR baked in

The Packer step requires these GitHub secrets to be set:
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`

## How Deployment Works

1. Packer builds an Ubuntu 24.04 AMI with Java 21 and the JAR copied to `/opt/app/webapp.jar`
2. Terraform (in the `tf-aws-infrastructure` repo) provisions the full AWS infrastructure
3. EC2 instances launch from that AMI using the autoscaling group
4. On boot, `user_data.sh` runs and:
   - Pulls the DB password from AWS Secrets Manager
   - Writes `/opt/app/.env` with DATABASE_URL, AWS_REGION, AWS_S3_BUCKET
   - Starts the `csye6225` systemd service which runs `java -jar /opt/app/webapp.jar`
5. Flyway runs DB migrations automatically on application startup
6. CloudWatch agent collects logs from `/var/log/webapp.log` and StatsD metrics on port 8125

## Environment Variables

| Variable | Description |
|----------|-------------|
| DATABASE_URL | Full JDBC URL including credentials |
| AWS_REGION | AWS region (e.g. us-east-1) |
| AWS_S3_BUCKET | S3 bucket name for file storage |
| PORT | Server port (default 8080) |

## Infrastructure

The AWS infrastructure for this project lives in the [tf-aws-infrastructure](https://github.com/K-sau07/tf-aws-infrastructure) repo.
