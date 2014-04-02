FHIR
====

Status
------

Incomplete and experimental.


Getting Started
---------------

Clone this repository, start MongoDB, and run

    mvn package
    java -Dserver.port=8080 -Dspring.data.mongodb.uri=mongodb://localhost:27017/fhir -jar target/FHIR.jar
