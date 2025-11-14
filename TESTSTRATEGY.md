# Teststrategi för rimfrost

Sammanfattning av teststrategier för de olika typerna av komponenterna i rimfrost

## End-to-end

Integrationstest av alla rimfrost-komponenter deployade i minikube.
Syftet är att verifiera full integration genom att köra ett smkoke-test (normalfall)
av en komplett sekvens av flödet (dvs inga mockade komponenter).

Automatiserat smoke-test implementeras i
https://github.com/Forsakringskassan/rimfrost-kubernetes/tree/main/src/it/java/fk/rimfrost

## Frontend

TBD

## OpenAPI-spec & AsyncAPI-spec repositories

Validering av specar utförs i och med den kodgenerering som workflow genomför.
Ingen verifiering utöver detta planeras.

## Fristående services

Med fristående services menas komponenter som inte är huvudflöden eller implementation av regler.
Exempel på detta är:
* rimfrost-folkbokford
* rimfrost-arbetsgivare
* rimfrost-operativt-uppgiftslager

### Unittest

Normalfall & viktiga felfall implementeras som automatiserade tester för att köras av workflow
vid commits och med _mvn test_.<br>

HTTP-endpoints kan testas med _io.quarkus.test.junit.QuarkusTest_,
se https://quarkus.io/guides/getting-started-testing#testing-a-specific-endpoint
och exempelvis: https://github.com/Forsakringskassan/rimfrost-arbetsgivare/blob/main/src/test/se/fk/github/rimfrost/arbetsgivare/Arbetsgivare.java

Kafka-topics kan testas med InMemoryConnector, se https://quarkus.io/guides/kafka#testing-without-a-broker

### Integrationstest

(ofta benämnda komponenttester utanför FK)

Syftet med dessa tester är verifiering av paketerad komponent, dvs en docker image.
Fokus bör vara på happy-path smoketesting för att verifiera container-uppstart & enkla normalfall av komponenten.

NOTE: Kan vi modifiera strategin så att samma underlag för docker imagen
används vid lokalt test som vid release workflow?<br>
(Idag har vi src/main/docker med underlag som endast används vid lokalt bygge)

## Regler

Samma strategi som för _fristående services_ ovan?

## Process (flöde)

TBD