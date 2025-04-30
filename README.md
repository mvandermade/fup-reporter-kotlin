# made-funicular-postzegel-reporter-kotlin
A reporter client for communication with the backend

# Local setup
You are going to need a Postgres database.
Get one using for example docker
```shell
docker run -p 5432:5432 -e POSTGRES_PASSWORD=password -e POSTGRES_DB=postzegel-reporter --volume pgdata:/var/lib/postgresql/data -d postgres:17
```

# Notes
Watch out when refactoring the name of a listener. This means upon restarting the application the events are not resumed!
This will lead to an error in the console.
https://spring.io/blog/2023/09/22/simplified-event-externalization-with-spring-modulith
Also do not change the names of domain objects since their Kafka topics must also be renamed too.


## Kafka
It seems that adding the dependencies now guarantees ordering of missed messages (* unsure! only seen it once)

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