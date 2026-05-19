# jpos Maven + JUnit setup

This project uses Maven with JUnit 4 for unit testing.

## Project structure

- `src/main/java` for application source
- `src/test/java` for unit tests

## Run tests

```sh
mvn test
```

## Run a single test class

```sh
mvn -Dtest=Service.Implementation.LoginServiceImplTest test
```

## Remove legacy pre-Maven src files

```sh
chmod +x cleanup-legacy-src.sh
./cleanup-legacy-src.sh
```