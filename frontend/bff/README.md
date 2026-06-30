## Relaterade repon

### rimfrost-template-micro-fe-bff

Template för BFF: https://github.com/Forsakringskassan/rimfrost-template-micro-fe-bff

### rimfrost-framework-regel-manuell-openapi

Definierar det gemensamma REST-API:et som BFF:en anropar på regelns backend.

NOTE: Se
- https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-bff
- https://github.com/Forsakringskassan/rimfrost-regel-bekraftabeslut-bff

för exempel på implementerade BFF:er.

## Skapa nytt repo baserat på template

Template för micro-frontend BFF: https://github.com/Forsakringskassan/rimfrost-template-micro-fe-bff

Ersätt förekomster av _template_ med regelns namn.

## Miljövariabler

| Variabel | Beskrivning |
|----------|-------------|
| `PORT` | Porten som BFF-servern lyssnar på |
| `BE_URL` | Bas-URL till regelns backend-service |
| `BE_RULE_PATH` | Sökvägen till regelns REST-API (t.ex. `regel/rtf-manuell`) |

## Implementera endpoints

BFF:en exponerar tre kärnendpoints som micro-frontenden anropar:

| Endpoint | Metod | Body | Motsvararar |
|----------|-------|------|-------------|
| `/api/task` | `POST` | `{ handlaggningId }` | `readData` i regelns backend |
| `/api/task` | `PATCH` | `{ handlaggningId, ... }` | `updateData` i regelns backend |
| `/api/task/done` | `POST` | `{ handlaggningId }` | `done` i regelns backend |

Ytterligare endpoints kan exponeras vid behov, t.ex. `POST /api/uppgiftsbeskrivning` för hämtning av utökad uppgiftsbeskrivning.

## Backend-integration

BFF:en proxar anrop mot regelns backend-service. Integrationspunkterna implementeras i `BackendClient.java`:

https://github.com/Forsakringskassan/rimfrost-template-micro-fe-bff/blob/main/src/main/java/se/fk/github/templatebff/integration/BackendClient.java

Regelns backend-URL konfigureras via miljövariablerna `BE_URL` och `BE_RULE_PATH`.

Se metoderna `readData`, `updateData` och `done` i regelns backend-service — [regler/manuell/README.md](../../regler/manuell/README.md).

## Testa

BFF:en testas med WireMock för att mocka bakänden:

https://github.com/Forsakringskassan/rimfrost-template-micro-fe-bff/blob/main/src/test/java/se/fk/github/templatebff/WireMockTestResource.java
