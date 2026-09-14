## Relaterade repon

Se även [regler/VERSIONING.md](../VERSIONING.md) för hur `HandlaggningUpdate.version` och `ProduceratResultat.version` ska hanteras i en regelimplementation.

### rimfrost-framework-regel-asyncapi

Definierar Kafka-kontraktet för initiering av regler (`regel.requests` / `regel.responses`).

### rimfrost-framework-regel-komplettering-openapi

Definierar det gemensamma REST-API:et för kompletteringsregler — hämtning och registrering av kompletteringsdata samt done-operation.

---

# Översikt av rekommenderade tasks vid implementation av ny kompletteringsregel

## Skapa nytt repo för regelns REST-API

Specificera det API som portalen använder (`GET`/`PATCH` `/komplettering`) för att hämta och uppdatera kompletteringsdata.
Skapa en `openapi.yaml` i det nya repot.

## Skapa nytt repo baserat på template

Template för kompletteringsregel: https://github.com/Forsakringskassan/rimfrost-template-regel-komplettering

Replace på förekomster av `_Template_` till regelns namn.<br>
TODOs i template-filerna ger tips om vad som behöver justeras.

## application.properties

### Konfigurera de Kafka-topics som regeln kopplas till.

De topics som kompletteringsregler behöver konfigurera är:

| Egenskap | Beskrivning                                                                                  |
|----------|----------------------------------------------------------------------------------------------|
| `mp.messaging.incoming.regel-requests.topic` | Regeln konsumerar denna topic för inkommande kompletteringsförfrågningar                     |
| `kafka.source` | Source, används för loggning och spårning                                                    |
| `kafka.subtopic` | Subtopic-suffix som OUL använder för att skicka statusnotifieringar tillbaka till denna regel |
| `kafka.cancelled.topic` | Topic där BPMN publicerar cancelled-events — konsumeras av ramverket för timeout-hantering  |

Svar skickas till den topic som angavs i förfrågans `replyTo`-fält — ingen separat outgoing-topic behöver konfigureras. OUL-uppgifter skapas via REST-adapter, inte via Kafka.

### Konfigurera REST-beroenden

```properties
sid.api.base-url=http://rimfrost-k8s-sid:8080
```

### Konfigurera persistens

```properties
regel.persistence.table-prefix=<din_regel>_komplettering
quarkus.flyway.default-schema=<din_regel>_komplettering
```

Prefixet måste vara unikt per regelimplementation och styr namnsättningen av Flyway-migrerade tabeller.

## config.yaml

YAML-fil för grundläggande konfiguration av regeln (beskrivning, roll, lagrum etc.).
Schema för YAML-filen: https://github.com/Forsakringskassan/rimfrost-framework-regel/blob/main/src/main/resources/schema/regel_schema.yaml

## Regelimplementation

### RegelKompletteringService\<T\>

Implementera ett interface med tre metoder:

```java
@ApplicationScoped
public class MinKompletteringService implements RegelKompletteringService<MinKompletteringData> {

    @Override
    boolean isKompletteringRequired(Handlaggning handlaggning) { ... }

    @Override
    T readSvarData(Handlaggning handlaggning) { ... }

    @Override
    HandlaggningUpdate registerSvar(Handlaggning handlaggning, T request) { ... }
}
```

- `isKompletteringRequired` — returnerar `true` om yrkandet saknar uppgifter. Om `false` skickas svar direkt med `utfall = JA` utan att någon OUL-uppgift skapas.
- `readSvarData` — returnerar den data handläggaren behöver för att registrera svar (används av `GET`-endpointen).
- `registerSvar` — applicerar handläggarens svar på handläggningen och returnerar en `HandlaggningUpdate` redo att persisteras (används av `PATCH`-endpointen).

Typparametern `T` är densamma för `readSvarData` (GET-svar) och `registerSvar` (PATCH-request body).

### RegelKompletteringController\<T\>

Extenda ramverkets abstrakta controller och sätt regelns path:

```java
@Path("/api/min-regel")
public class MinKompletteringController extends RegelKompletteringController<MinKompletteringData> {}
```

Ramverket exponerar automatiskt:

| Endpoint | Beskrivning |
|----------|-------------|
| `GET /{handlaggningId}` | Returnerar svardata för handläggaren |
| `PATCH /{handlaggningId}` | Registrerar handläggarens svar |
| `POST /{handlaggningId}/done` | Markerar komplettering som klar, skickar svar på `replyTo` |

## Timeout-hantering

Timeout triggas av en BPMN cancelled-event som publiceras på `kafka.cancelled.topic`. Ramverket konsumerar denna automatiskt och utför cleanup: avslutar OUL-uppgiften med anledning `BPMN_CANCELLED` och tar bort korrelationstillståndet. Inget felsvar skickas på `replyTo` — BPMN-flödet känner redan till timeouten.

Om en handläggare anropar `POST /{handlaggningId}/done` efter timeout returnerar ramverket HTTP 409, eftersom korrelationstillståndet redan är borttaget.

Om regeln behöver egen cleanup vid timeout (t.ex. avbryta externa anrop) kan den implementera `RegelOulCancelledHandler`. Ramverket anropar den under cancel-flödet innan det tar bort korrelationstillståndet.

## Regelimplementation — test

Implementera tester av `isKompletteringRequired`, `readSvarData` och `registerSvar`.

Det är också möjligt att implementera integrationstester genom att extenda bastest-klassen för kompletteringsregler:<br>
https://github.com/Forsakringskassan/rimfrost-framework-regel-komplettering/blob/main/src/test/java/se/fk/rimfrost/framework/regel/komplettering/AbstractRegelKompletteringTestBase.java
