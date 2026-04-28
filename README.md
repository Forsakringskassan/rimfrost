# Kom igång med Rimfrost

Det här dokumentet riktar sig till nya utvecklare och ger en övergripande förståelse för hur Rimfrost-ramverket är uppbyggt, varför det är strukturerat på det sättet.

---

## Vad är Rimfrost?

Rimfrost är ett ramverk för att bygga beslutsstödjande mikrotjänster hos Försäkringskassan. Det ger en standardiserad, återanvändbar grund för att implementera **processer** och **regler** — de grundläggande byggstenarna i FK:s handläggningsflöden.

Alla repon i ekosystemet har prefixet `rimfrost-`. De delas upp i sex kategorier:

| Prefix | Syfte |
|--------|-------|
| `rimfrost-framework-*` | Ramverkskod som alla implementationer bygger på |
| `rimfrost-template-*` | Startprojekt att kopiera när man skapar nytt |
| `rimfrost-regel-*` | Faktiska regelimplementationer |
| `rimfrost-process-*` | Faktiska processimplementationer |
| `rimfrost-service-*` | Tjänster som regler kommunicerar med |
| `rimfrost-framework-*-adapter` | REST-klienter för integration mot tjänster |

Teknikstacken är **Java 21, Quarkus** och **Kogito** för processorkestration, med **Kafka** som kommunikationskanal mellan tjänster.

### Tjänster (rimfrost-service-*)

Tjänsterna är de bakgrundssystem som regler hämtar data från eller interagerar med. De exponerar REST- och/eller Kafka-API:er och har egna specifikationer i tillhörande `-openapi`- och `-asyncapi`-repon.

### Adaptrar (rimfrost-framework-*-adapter)

Adaptrarna är återanvändbara REST-klienter paketerade som Maven-bibliotek. En regel lägger till en adapter som beroende för att prata med en tjänst — utan att behöva implementera HTTP-kommunikationen själv.

---

## Arkitektur — den stora bilden

Grundidén är att en **process** orkesterar ett flöde där den ropar på en eller flera **regler**. Reglerna är fristående mikrotjänster som kommunicerar asynkront via Kafka. Varje regel returnerar ett utfall som processen sedan går vidare med.

```
┌─────────────────────────────────────────────────┐
│                  Process (Kogito BPMN)           │
│                                                  │
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

**Regler** finns i två varianter:

- **Maskinell** — helt automatiserad, inga mänskliga beslut. Ramverket tar emot en förfrågan, kör regellogiken, och returnerar ett svar.
- **Manuell** — kräver att en handläggare agerar via en portal (micro-frontend). Ramverket skapar en uppgift i Operativt uppgiftslager (OUL) och väntar på att handläggaren kvitterar den.

---

## Ramverket (rimfrost-framework-*)

Du behöver sällan röra ramverkskoden direkt — den konsumeras via Maven-beroenden. Men det är bra att förstå vad varje del gör:

### rimfrost-framework-regel

Baskod som är gemensam för **alla** regler, oavsett om de är maskinella eller manuella.

- Läser in regelns konfiguration från `config.yaml`
- Exponerar Kafka-interface för request/response (regelinitiering och avslut)

### rimfrost-framework-regel-maskinell

Bygger på `rimfrost-framework-regel` och lägger till det som är specifikt för maskinella regler:

- Hanterar inkommande regelförfrågan från Kafka
- Hämtar handläggningsdata
- Anropar din regellogik via den abstrakta metoden `processRegel`
- Sparar resultat och skickar tillbaka svar på Kafka

Du som implementatör behöver bara implementera `processRegel`.

### rimfrost-framework-regel-manuell

Bygger på `rimfrost-framework-regel` och `rimfrost-framework-oul` och lägger till det som krävs för manuella regler:

- Initierar ny regel och skapar uppgift i OUL
- Lyssnar på OUL-svar och statusuppdateringar
- Hämtar handläggningsdata
- Exponerar REST-API som portalen (micro-frontend) anropar

Du som implementatör behöver implementera `readData`, `updateData` och `done`.

### rimfrost-framework-oul

Hanterar kommunikationen med **Operativt uppgiftslager** — det system där handläggarnas uppgifter lever.

- Kafka request/response för skapande av operativa uppgifter
- REST-interface för Done-operationer

### Arvsträd

```
rimfrost-template-regel-maskinell
    └── rimfrost-framework-regel-maskinell
            └── rimfrost-framework-regel

rimfrost-template-regel-manuell
    └── rimfrost-framework-regel-manuell
            ├── rimfrost-framework-regel
            └── rimfrost-framework-oul
```

---

## Nästa steg

Beroende på vad du vill skapa finns mer detaljerad information i respektive README:

- **Skapa en process** — se [processer/README.md](processer/README.md)
- **Skapa en manuell regel** — se [regler/manuell/README.md](regler/manuell/README.md)
- **Skapa en maskinell regel** — se [regler/maskinell/README.md](regler/maskinell/README.md)
