# made-funicular-postzegel-reporter-kotlin
A reporter client for communication with the backend

# To find out
- Ordering of resubmitted events
- Start mutliple instances of the modulith

# Local setup
- Use docker to start postgres and kafka. Postgres is persistent using a volume, Kafka can be configured to be. See docker-compose.yml
- `docker compose up`

# Notes
- Watch out when refactoring the name of a listener. This means upon restarting the application the events are not resumed!
This will lead to an error in the console.

- Also do not change the names of domain objects since their Kafka topics must also be renamed too.

## Kafka
Use to externalize events: https://docs.spring.io/spring-modulith/docs/current-SNAPSHOT/reference/html/#events.externalization

### Topic structure
Modulith automatically creates a topic for the package and @Externalized annotated data class.
Example (jump into the container): `docker exec -it kafka_container bas`
```
/opt/kafka/bin$ ./kafka-topics.sh --list --bootstrap-server localhost:9092
```
You'll see:
```
domain.PostzegelCode
```
