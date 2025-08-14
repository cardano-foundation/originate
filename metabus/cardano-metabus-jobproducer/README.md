# cardano-metabus-api

# For develoment
**Step 1:**: cd to `originate folder` and run this command to build target jars:
```
./mvnw clean install -DskipTests
```

**Step 2:** cd to `metabus folder` and run this command to start the relevant services:

```
docker compose --env-file ../.env.dev up --build pg4keycloak keycloak metabus-jobproducer state-storage zookeeper kafka kafdrop -d
```


**Step 3:** Run or debug Spring Boot cardano-metabus-api project in ide and start developing (you can stop unrelevant services).

# For running the whole project as docker container:
**Step 1:** cd to `metabus/cardano-metabus-api folder`, type:

```
./mvnw clean install -DskipTests
```

**Step 2:** cd to `metabus folder` and type:

```
docker compose --env-file ../.env.dev up --build pg4keycloak keycloak metabus-jobproducer state-storage zookeeper kafka kafdrop metabus-api -d
```
