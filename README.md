# made-funicular-stamp-reporter-kotlin
A reporter client for communication with the backend
![logo consisting of the text reporter in an acre](Untitled_Artwork.png)


# Local setup
- Use docker to start postgres and kafka. Postgres is persistent using a volume, Kafka can be configured to be. See docker-compose.yml
- `docker compose up`
- Gradle build to generate stubs for gRPC


# Kafka
### Topic structure
Example (jump into the container): `docker exec -it kafka_container bash`
```
/opt/kafka/bin$ ./kafka-topics.sh --list --bootstrap-server localhost:9092
```
```
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic serial.stamp-dlt
```

### Dead letter topic
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic serial-stamp-dlt --property print.headers=true

## Tricks
```docker compose down --volume```
