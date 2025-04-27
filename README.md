# made-funicular-postzegel-reporter-kotlin
A reporter client for communication with the backend

# Local setup
You are going to need a Postgres database.
Get one using for example docker
```shell
docker run -p 5432:5432 -e POSTGRES_PASSWORD=password -e POSTGRES_DB=postzegel-reporter --volume pgdata:/var/lib/postgresql/data -d postgres:17
```

# Notes
Watch out when refactoring the name of a listner. This means upon restarting the application the events are not resumed!
This will lead to an error in the console.