# Kom igång med Rimfrost

Det här dokumentet riktar sig till nya utvecklare och ger en övergripande förståelse för hur Rimfrost-ramverket är uppbyggt, varför det är strukturerat på det sättet, och hur du som utvecklare skapar din första process eller regel.

---

## Vad är Rimfrost?

Rimfrost är ett ramverk för att bygga beslutsstödjande mikrotjänster hos Försäkringskassan. Det ger en standardiserad, återanvändbar grund för att implementera **processer** och **regler** — de grundläggande byggstenarna i FK:s handläggningsflöden.

Alla repon i ekosystemet har prefixet `rimfrost-`. De delas upp i fyra kategorier:

| Prefix | Syfte |
|--------|-------|
| `rimfrost-framework-*` | Ramverkskod som alla implementationer bygger på |
| `rimfrost-template-*` | Startprojekt att kopiera när man skapar nytt |
| `rimfrost-regel-*` | Faktiska regelimplementationer |
| `rimfrost-process-*` | Faktiska processimplementationer |

Teknikstacken är **Java 21, Quarkus** och **Kogito/jBPM** för processorkestration, med **Kafka** som kommunikationskanal mellan tjänster.

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
- Exponerar REST-interface för Yrkande och Handläggning

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
- Lyssnar på OUL-svar och statussuppdateringar
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

## Skapa en ny process

> Exempelimplementation: `rimfrost-process-vah`

En process orkesterar ett handläggningsflöde. Den startas via ett inkommande Kafka-meddelande, anropar en eller flera regler som subprocesser, och avslutas med ett utgående Kafka-meddelande.

**Steg-för-steg:**

### 1. Skapa repo från template

Kopiera `rimfrost-template-process` och ersätt alla förekomster av `Template` med processens namn.

### 2. Konfigurera Kafka-topics i `application.properties`

```properties
# Inkommande topic som startar processen
mp.messaging.incoming.<DIN_INCOMING_TOPIC>.connector=smallrye-kafka
mp.messaging.incoming.<DIN_INCOMING_TOPIC>.auto.offset.reset=earliest
mp.messaging.incoming.<DIN_INCOMING_TOPIC>.value.deserializer=org.apache.kafka.common.serialization.StringDeserializer

# Utgående topic när processen är klar
mp.messaging.outgoing.<DIN_OUTGOING_TOPIC>.connector=smallrye-kafka
mp.messaging.outgoing.<DIN_OUTGOING_TOPIC>.value.serializer=org.apache.kafka.common.serialization.StringSerializer
```

### 3. Lägg till regelberoenden i `pom.xml`

För varje regel processen ska anropa, lägg till en `<dependency>` till regelns subprocess-artefakt. Kogito-ramverket packar upp BPMN-filerna från dessa beroenden automatiskt.

### 4. Konfigurera `template.bpmn`

Använd VS Code-pluginen **Apache KIE Kogito Bundle** för att redigera BPMN-filer.

- Byt namn på filen till något som speglar processen
- **Start Message-event:** sätt Implementation/Execution Message till `<DIN_INCOMING_TOPIC>`
- **End Message-event:** sätt Implementation/Execution Message till `<DIN_OUTGOING_TOPIC>`
- **Subprocesser:** ersätt placeholder-subprocessen med de regler du vill anropa; ange regelns **Process ID** (definierat i regelns BPMN-fil) som Called Element

### 5. Implementera tester

Utgå från `TemplateContainerSmokeIT.java` i template-repot. Testerna använder TestContainers för att starta en riktig Kafka-broker och mockar regelernas svar med WireMock.

---

## Skapa en ny maskinell regel

> Exempelimplementation: `rimfrost-regel-rtf-maskinell`

En maskinell regel körs helt automatiskt utan mänsklig inblandning. Du implementerar regellogiken, ramverket sköter resten.

### Varje maskinell regel består av två repon

| Repo | Syfte |
|------|-------|
| `rimfrost-regel-<namn>-maskinell` | Regellogiken |
| `rimfrost-regel-<namn>-maskinell-subprocess` | BPMN-subprocessen som processen anropar |

### Steg-för-steg:

#### 1. Skapa subprocess-repo från `rimfrost-template-regel-subprocess`

- Byt `artifactId` i `pom.xml` till något unikt för regeln
- Redigera `template_regel.bpmn`:
  - Sätt ett unikt **Process ID** (detta ID refererar processen till när den anropar regeln)
  - Koppla Throwing Intermediate Message-event till `<DIN_OUTGOING_TOPIC>`
  - Koppla Catching Intermediate Message-event till `<DIN_INCOMING_TOPIC>`
  - Byt namn på BPMN-filen

#### 2. Skapa regelrepo från `rimfrost-template-regel-maskinell`

Ersätt alla förekomster av `Template` med regelns namn.

#### 3. Konfigurera `application.properties`

| Topic | Syfte |
|-------|-------|
| `mp.messaging.incoming.regel-requests.topic` | Regeln tar emot initieringsförfrågan |
| `mp.messaging.outgoing.regel-responses.topic` | Regeln skickar tillbaka utfall |

#### 4. Konfigurera `config.yaml`

```yaml
uppgift:
  id: <unikt-uuid>
  version: 1
  path: /regel/maskinell
regel:
  namn: "Regelns namn"
  beskrivning: "Vad regeln kontrollerar"
  lagrum: "Relevant lagrum"
```

#### 5. Implementera `RegelService.java`

```java
@ApplicationScoped
public class RegelService extends RegelMaskinellServiceBase {

    @Override
    public RegelMaskinellResult processRegel(RegelMaskinellRequest request) {
        // Hämta handläggningsdata från request
        // Kör regellogiken
        // Returnera RegelMaskinellResult med utfall
    }
}
```

Ramverket hanterar Kafka-konsumtion, hämtning av handläggningsdata och persistering — du fokuserar enbart på regellogiken.

#### 6. Implementera tester

Utöka `RegelMaskinellTest` (från template) och testa `processRegel`-metoden direkt. För bredare integrationstester, utöka `AbstractRegelMaskinellTest` från framework-repot.

---

## Skapa en ny manuell regel

> Exempelimplementation: `rimfrost-regel-rtf-manuell`

En manuell regel kräver att en handläggare tar ställning via portalen. Ramverket skapar en uppgift i OUL och väntar på att handläggaren agerar.

### Varje manuell regel består av tre repon

| Repo | Syfte |
|------|-------|
| `rimfrost-regel-<namn>-manuell` | Regellogiken och REST-API:t |
| `rimfrost-regel-<namn>-manuell-openapi` | OpenAPI-spec för regelns REST-API |
| `rimfrost-regel-<namn>-manuell-subprocess` | BPMN-subprocessen som processen anropar |

### Steg-för-steg:

#### 1. Skapa OpenAPI-spec

Specificera det GET/PATCH-API som portalen anropar för att hämta och uppdatera information om den operativa uppgiften.

#### 2. Skapa subprocess-repo

Samma tillvägagångssätt som för maskinell subprocess (se ovan).

#### 3. Skapa regelrepo från `rimfrost-template-regel-manuell`

Ersätt alla förekomster av `Template` med regelns namn.

#### 4. Konfigurera `application.properties`

Manuella regler behöver fler Kafka-topics än maskinella:

| Topic | Syfte |
|-------|-------|
| `regel-requests` (incoming) | Tar emot initieringsförfrågan |
| `regel-responses` (outgoing) | Skickar tillbaka utfall när klar |
| `operativt-uppgiftslager-requests` (outgoing) | Skapar uppgift i OUL |
| `operativt-uppgiftslager-responses` (incoming) | Svar från OUL på skapad uppgift |
| `operativt-uppgiftslager-status-notification` (incoming) | OUL meddelar statusändringar |
| `operativt-uppgiftslager-status-control` (outgoing) | Regeln uppdaterar uppgiftsstatus i OUL |

#### 5. Implementera `_Template_Service.java`

Implementera interfacet `RegelManuellServiceInterface` med tre metoder:

```java
@ApplicationScoped
public class MinRegelService extends RegelManuellServiceBase {

    @Override
    public ReadDataResponse readData(ReadDataRequest request) {
        // Returnerar den data som portalen visar för handläggaren
    }

    @Override
    public UpdateDataResponse updateData(UpdateDataRequest request) {
        // Hanterar handläggarens uppdateringar av uppgiftsdata
    }

    @Override
    public void done(DoneRequest request) {
        // Städa upp, spara vad som behövs
        // Anropa sendRegelResponse() för att signalera att regeln är klar
        sendRegelResponse(...);
    }
}
```

#### 6. Implementera tester

Utöka `RegelTemplateTest` från template-repot och testa de tre metoderna. Basklassen `RegelTest` från framework-repot ger tillgång till delade testverktyg.

---

## Sammanfattning — checklista för ny regel eller process

### Ny maskinell regel
- [ ] Skapa subprocess-repo från `rimfrost-template-regel-subprocess`
- [ ] Konfigurera unikt Process ID i BPMN-filen
- [ ] Skapa regelrepo från `rimfrost-template-regel-maskinell`
- [ ] Konfigurera `config.yaml` och `application.properties`
- [ ] Implementera `processRegel` i `RegelService.java`
- [ ] Skriv tester
- [ ] Lägg till subprocess som beroende i processen

### Ny manuell regel
- [ ] Skapa OpenAPI-spec-repo
- [ ] Skapa subprocess-repo från `rimfrost-template-regel-subprocess`
- [ ] Konfigurera unikt Process ID i BPMN-filen
- [ ] Skapa regelrepo från `rimfrost-template-regel-manuell`
- [ ] Konfigurera `config.yaml` och `application.properties` (6 topics)
- [ ] Implementera `readData`, `updateData` och `done`
- [ ] Skriv tester
- [ ] Lägg till subprocess som beroende i processen

### Ny process
- [ ] Skapa repo från `rimfrost-template-process`
- [ ] Konfigurera `application.properties` (incoming/outgoing topics)
- [ ] Lägg till regelberoenden i `pom.xml`
- [ ] Konfigurera BPMN-flödet med Start/End-events och regelsubprocesser
- [ ] Skriv integrationstester med TestContainers
