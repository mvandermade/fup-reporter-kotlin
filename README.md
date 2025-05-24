# made-funicular-stamp-reporter-kotlin
A reporter client for communication with the backend

# To find out
- Ordering of resubmitted events
- Start multiple instances of the modulith

# Local setup
- Use docker to start postgres and kafka. Postgres is persistent using a volume, Kafka can be configured to be. See docker-compose.yml
- `docker compose up`

# Why not modulith messaging
Because I did not find the retry mechanism useful.
When the service is unavailable, the kafka stream can better stop flowing than retrying after x amount.
In modulith, the messages would pile up and lose their ordering too.

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