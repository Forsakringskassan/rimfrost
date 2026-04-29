# Översikt av rekommenderade tasks vid implementation av ny micro-frontend

NOTE: Se
- https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-fe
- https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-bff

för exempel på implementerad micro-frontend och dess BFF.

## Skapa nytt FE-repo baserat på template

Template för micro-frontend: https://github.com/Forsakringskassan/rimfrost-template-micro-fe

Replace på förekomster av _template_ till regelns namn.  
TODOs i template-filerna ger tips om vad som behöver justeras.

## Skapa nytt BFF-repo baserat på template

Template för micro-frontend BFF: https://github.com/Forsakringskassan/rimfrost-template-micro-fe-bff

Replace på förekomster av _template_ till regelns namn.

## Miljövariabler BFF

| Variabel | Beskrivning |
|----------|-------------|
| `PORT` | Porten som BFF-servern lyssnar på |
| `BE_URL` | Bas-URL till regelns backend-service |
| `BE_RULE_PATH` | Sökvägen till regelns REST-API (t.ex. `regel/rtf-manuell`) |

## FE-implementation

Implementera den Vue-komponent som presenteras för handläggaren vid vald uppgift. Komponenten tar emot `handlaggningId` som prop och kommunicerar med sin BFF via följande endpoints:

| Endpoint | Metod | Body | Motsvararar |
|----------|-------|------|-------------|
| `/api/task` | `POST` | `{ handlaggningId }` | `readData` |
| `/api/task` | `PATCH` | `{ handlaggningId, ... }` | `updateData` |
| `/api/task/done` | `POST` | `{ handlaggningId }` | `done` |

Metoderna `readData`, `updateData` och `done` syftar på motsvarande metoder i regelns backend-service — se [regler/manuell/README.md](../regler/manuell/README.md).

När handläggaren är klar med uppgiften dispatchar FE:n ett `task-done`-event som portalen lyssnar på för att visa notifiering och uppdatera uppgiftslistan.

## BFF-implementation

BFF:en proxar anrop från FE mot regelns backend-service. Utöver de tre kärnendpointsen ovan kan BFF:en exponera ytterligare endpoints vid behov, t.ex. för hämtning av utökad uppgiftsbeskrivning (`POST /api/uppgiftsbeskrivning`).

Parametrar som identifierar resurser skickas alltid i request body — aldrig i URL-sökvägen — för att undvika att känsliga identifierare loggas i server-accessloggar.

## Registrering i portalen

Portalen laddar rätt micro-frontend baserat på uppgiftens `url`-fält som sätts av regelns backend via `config.yaml` (nyckel: `uppgift.path`). Ingen manuell registrering i portalen krävs.
