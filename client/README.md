# Clients

## Compile and install database (FIRST!):
In a catalog with database:
```shell
lein compile && lein install
```

## Compile clients
```shell
mvn -q compile
```

## Run clients
### Embedded
```shell
mvn -q exec:java -Dexec.mainClass="embedded.Client"
```
### Multithreaded
1. Run database server. In catalog with database:
```shell
lein compile && lein run
```
2. Run client:
```shell
mvn -q exec:java -Dexec.mainClass="embedded.Client"
```