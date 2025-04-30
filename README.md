# made-funicular-postzegel-reporter-kotlin
A reporter client for communication with the backend

# To find out
- Ordering of resubmitted events
- Start mutliple instances of the modulith

# Local setup
You are going to need a Postgres database.
Get one using for example docker
```shell
docker run -p 5432:5432 -e POSTGRES_PASSWORD=password -e POSTGRES_DB=postzegel-reporter --volume pgdata:/var/lib/postgresql/data -d postgres:17
```

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