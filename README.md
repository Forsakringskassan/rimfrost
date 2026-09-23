# Kom igång med Rimfrost

Det här dokumentet riktar sig till nya utvecklare och ger en övergripande förståelse för hur Rimfrost-ramverket är uppbyggt, varför det är strukturerat på det sättet.

---

## Vad är Rimfrost?

Rimfrost är ett ramverk för att bygga beslutsstödjande mikrotjänster hos Försäkringskassan. Det ger en standardiserad, återanvändbar grund för att implementera **processer** och **regler** — de grundläggande byggstenarna i FK:s handläggningsflöden.

Alla repon i ekosystemet har prefixet `rimfrost-`. De delas upp i nio kategorier:

| Prefix | Syfte |
|--------|-------|
| `rimfrost-framework-*` | Ramverkskod som alla implementationer bygger på |
| `rimfrost-template-*` | Startprojekt att kopiera när man skapar nytt |
| `rimfrost-regel-*` | Faktiska regelimplementationer |
| `rimfrost-process-*` | Faktiska processimplementationer |
| `rimfrost-service-*` | Tjänster som regler kommunicerar med |
| `rimfrost-adapter-*` | REST-klienter för integration mot tjänster |
| `rimfrost-portal-*` | Portal och micro-frontends för handläggare |
| `rimfrost-*-openapi` | OpenAPI-specifikationer (REST-kontrakt) |
| `rimfrost-*-asyncapi` | AsyncAPI-specifikationer (Kafka-kontrakt) |

Teknikstacken är **Java 21, Quarkus** och **Kogito** för processorkestration, med **Kafka** som kommunikationskanal mellan tjänster.

---

## Arkitektur — den stora bilden

Grundidén är att en **process** orkestrerar ett flöde där den ropar på en eller flera **regler**. Reglerna är fristående mikrotjänster som kommunicerar asynkront via Kafka. Varje regel returnerar ett utfall som processen sedan går vidare med.

```
┌─────────────────────────────────────────────────┐
│                  Process (Kogito BPMN)          │
│                                                 │
│   START ──► [Subprocess: Regel A] ──►           │
│             [Subprocess: Regel B] ──► SLUT      │
└─────────┬───────────────┬───────────────────────┘
          │  Kafka        │  Kafka
    ┌─────▼─────┐   ┌─────▼──────┐
    │  Regel A  │   │  Regel B   │
    │ (maskinell│   │  (manuell) │
    │  eller    │   │            │
    │  manuell) │   │            │
    └───────────┘   └────────────┘
```

---

## Ramverket (rimfrost-framework-*)

Du behöver sällan röra ramverkskoden direkt — den konsumeras via Maven-beroenden. Men det är bra att förstå vad varje del gör:

| Repo | Syfte |
|------|-------|
| [`rimfrost-framework-regel`](https://github.com/Forsakringskassan/rimfrost-framework-regel) | Gemensam baskod för alla regeltyper — konfiguration och Kafka-interface |
| [`rimfrost-framework-oul`](https://github.com/Forsakringskassan/rimfrost-framework-oul) | Kommunikation med Operativt uppgiftslager (OUL) |
| [`rimfrost-framework-regel-oul`](https://github.com/Forsakringskassan/rimfrost-framework-regel-oul) | OUL-integration och korrelationslagring för regelkörningar som avslutas i ett separat anrop |
| [`rimfrost-framework-regel-maskinell`](https://github.com/Forsakringskassan/rimfrost-framework-regel-maskinell) | Ramverk för maskinella (automatiserade) regler |
| [`rimfrost-framework-regel-manuell`](https://github.com/Forsakringskassan/rimfrost-framework-regel-manuell) | Ramverk för manuella regler som kräver handläggainteraktion via portal |
| [`rimfrost-framework-regel-komplettering`](https://github.com/Forsakringskassan/rimfrost-framework-regel-komplettering) | Ramverk för kompletteringsregler — Kafka-anrop, OUL-uppgift och REST-gränssnitt mot portal |

### Arvsträd

```
rimfrost-template-regel-maskinell
    └── rimfrost-framework-regel-maskinell
            └── rimfrost-framework-regel

rimfrost-template-regel-manuell
    └── rimfrost-framework-regel-manuell
            └── rimfrost-framework-regel-oul
                    ├── rimfrost-framework-regel
                    └── rimfrost-framework-oul

rimfrost-template-regel-komplettering
    └── rimfrost-framework-regel-komplettering
            └── rimfrost-framework-regel-oul
                    ├── rimfrost-framework-regel
                    └── rimfrost-framework-oul
```

---

## Regler

Det finns tre typer av regler:

- **Maskinell** — helt automatiserad, inga mänskliga beslut. Ramverket tar emot en förfrågan, kör regellogiken, och returnerar ett svar.
- **Manuell** — kräver att en handläggare agerar via en portal (micro-frontend). Ramverket skapar en uppgift i Operativt uppgiftslager (OUL) och väntar på att handläggaren kvitterar den.
- **Komplettering** — hanterar insamling av kompletterande uppgifter från handläggare. Ramverket kontrollerar om komplettering behövs, skapar i så fall en OUL-uppgift och inväntar kvittens.

**Se även:**
- [regler/CONFIG_YAML.md](regler/CONFIG_YAML.md) — konfiguration av regler
- [regler/VERSIONING.md](regler/VERSIONING.md) — versionshantering i regelimplementationer

---

## Externa integrationer

### Team

Kontraktet definieras i `rimfrost-service-team-openapi`.<br>
Ger regler tillgång till:
- teamtillhörighet — vilka team en individ tillhör
- vilka individer som ingår i ett givet team
- om en handläggare har behörighet att hantera ärenden med skyddad identitet

### Skyddad identitet (SID)

Kontraktet definieras i `rimfrost-service-sid-openapi`.<br>
Ger regler möjlighet att:
- kontrollera om en eller flera individer i ett ärende har skyddad identitet

Informationen styr hur ärendet får hanteras och visas för handläggare.

### Arbetsgivare

Kontraktet definieras i `rimfrost-service-arbetsgivare-openapi`.<br>
Ger regler tillgång till t.ex.:
- en persons anställningsinformation
- vilka arbetsgivare personen har eller har haft
- anställningsgrad och period
- specificerad löneinformation för en given tidsperiod

### Folkbokföring

Kontraktet definieras i `rimfrost-service-folkbokforing-openapi`.<br>
Ger regler tillgång till:
- grundläggande personuppgifter för ett personnummer — namn, kön och folkbokföringsadress.

---

## Nästa steg

Beroende på vad du vill skapa finns mer detaljerad information i respektive README:

- **Skapa en process** — se [processer/README.md](processer/README.md)
- **Skapa en manuell regel** — se [regler/manuell/README.md](regler/manuell/README.md)
- **Skapa en maskinell regel** — se [regler/maskinell/README.md](regler/maskinell/README.md)
- **Skapa en kompletteringsregel** — se [regler/komplettering/README.md](regler/komplettering/README.md)
