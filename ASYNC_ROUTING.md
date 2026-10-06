# Asynkron svarsroutning i Rimfrost

Den här guiden vänder sig till utvecklare som implementerar **regler**, **Kogito-flöden som anropar regler**, och **workflow-designers** som arbetar med `rimfrost-service-workflow`.
Den förklarar hur asynkrona svarsdestinationer deklareras, propageras och används genom ramverket — med genomgång av Kafka-meddelandefält, OUL-uppgiftsskapande, workflow-tjänstens DB-persistensmönster och nödvändig tjänstkonfiguration.

---

## Översikt

Det finns ingen hårdkodad svarstopic i ramverket.
Anroparen deklarerar sin svarsdestination per request, och ramverket propagerar den vidare till den plats där svaret slutligen produceras — vilket kan ske omedelbart, efter en DB-persisterad asynkron överlämning, eller efter att en manuell uppgift slutförts.

Beroende på sammanhang förekommer destinationen i en av fyra former:

| Form                                          | Var                                                                                                                                  |
|-----------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| Kafka-payload-fält (`replyTo`)                | Regel requests från Kogito-processer (`rimfrost-framework-regel-asyncapi`)                                                           |
| REST-requests (`reply_topic` i `ProcessInfo`) | OUL-uppgiftsskapande — svarsadress som ekas tillbaka i statusnotifikationer (`rimfrost-service-oul-management-regler-openapi`)       |
| REST-requests (`sub_topic`)                   | OUL-uppgiftsskapande — styr vilket topic OUL publicerar statusnotifikationer till (`rimfrost-service-oul-management-regler-openapi`) |
| DB-persisterat per `handlaggningId`           | Toppnivåprocessflöden via `rimfrost-service-workflow` — `replyTo` anges i REST-förfrågan och lagras i DB, inte i Kafka-meddelandet   |

Det specificerade destinationsvärdet **ändras aldrig** av någon tjänst — det läses bara, lagras och vidarebefordras.

---

## Fältdetaljer

### I regelförfrågningar — payload-datafält

Definierat i `rimfrost-framework-regel-asyncapi`:

```yaml
RegelRequestMessagePayloadData:
  properties:
    replyTo:
      type: string
      description: Topic där svar ska skickas
```

Fältet är en del av meddelandets **payload-data**, inte headern.
En regelimplementation tar emot det via `RegelDataRequest.replyTo()`.

Anledningen till att det finns i payload snarare än i Kafka-headern är att regelförfrågningar härstammar från **Kogito-processer**, som konstruerar meddelanden som CloudEvents och inte sätter Kafka-headers.

I subprocess-baserade flöden sätts `replyTo` av `RegelService.createRegelRequest()` i `rimfrost-framework-process`.
Det löser upp det faktiska topic-namnet från en config-property vars namn skickas in från BPMN-inputen `responseTopicProperty`:

```java
String responseTopicName = config.getValue(responseTopicProperty, String.class);
requestMessageData.setReplyTo(responseTopicName);
```

Topic-namnet konstrueras typiskt från `${PROCESS_ID}`, vilket gör det processinstansspecifikt:

```properties
RTF_MANUELL_RESPONSE_TOPIC_NAME=${PROCESS_ID}-rtf-manuell-responses
```

Subprocessen prenumererar på samma topic för inkommande svar, och property-namnet kopplas till BPMN-uppgiften `Create Request` som `responseTopicProperty`.

Ramverket vidarebefordrar `replyTo` oförändrat och använder det för att routa svaret till rätt topic.
Värdet valideras aldrig och får inget defaultvärde — om det saknas eller är felaktigt går svaret helt enkelt förlorat.

### Vid skapande av OUL-uppgift

När en manuell- eller kompletteringsregel inte kan utvärderas omedelbart och måste vänta på att en manuell uppgift slutförs, skapar regelramverket en OUL-uppgift via REST-API:et definierat i `rimfrost-service-oul-management-regler-openapi`.

Schemat `CreateUppgiftRequest` innehåller två routningsrelaterade fält, båda obligatoriska:

| Fält          | Plats i förfrågan   | Syfte                                                                           |
|---------------|---------------------|---------------------------------------------------------------------------------|
| `sub_topic`   | toppnivåfält        | Talar om för OUL vilket topic-suffix som ska användas för statusnotifikationer  |
| `reply_topic` | inuti `ProcessInfo` | Ursprunglig anroparadress, ekas tillbaka oförändrad av OUL i varje notifikation |

Dessa två fält tjänar helt olika syften:

`sub_topic` styr **vart OUL skickar statusnotifikationer**.
Det är en per-tjänst-konfigurationsproperty (`kafka.subtopic`) konfigurerad i subprocessens `application.properties` och injicerad av regelramverket när create-förfrågan byggs.
OUL lägger till det efter bastopic: `operativt-uppgiftslager-status-notification.<sub_topic>`.
Subprocessen måste prenumerera på exakt det topicnamnet för att ta emot callbacks.

`reply_topic` innehåller det ursprungliga `replyTo` från regelförfrågan (notera snake_case-namnet).
Det lagras av OUL med uppgiften och ekas tillbaka oförändrat i varje statusnotifikation, men används aldrig för routning av OUL.
När regelramverket tar emot statuscallbacken på sin prenumererade kanal läser det `reply_topic` från payload och använder det för att skicka det slutliga regelsvaret tillbaka till den ursprungliga anroparen.

---

## Scenario 1 — Maskinell regel

- Skicka `request.replyTo()` till `sendResponse()` oförändrat.
- Vid felfall, skicka det till `sendErrorResponse()`.
- Hårdkoda aldrig eller ersätt med ett konfigurerat topic-namn.

```java
@Override
protected void handleRequest(RegelDataRequest request) {
    Utfall utfall = myRuleLogic.evaluate(request);
    sendResponse(
        request.handlaggningId(),
        request.cloudEventData(),
        utfall,
        request.replyTo()
    );
}
```

---

## Scenario 2 — Manuell regel (asynkron, OUL-baserad)

Användningen av `request.replyTo()` följer samma mönster som Scenario 1 — skicka det oförändrat till både lyckade och felaktiga svarsmetoder. Skillnaden är att det här även persisteras i OUL-uppgiftens spec så att ramverket kan hämta det när OUL-statuscallbacken anländer.

- Skicka `request.replyTo()` in i OUL-uppgiftens spec utan modifikation — ramverket persisterar det och hämtar det när OUL-statuscallbacken anländer.
- På felvägar som svarar omedelbart (innan OUL-uppgiftsskapande), anropa ändå `sendErrorResponse()` med `request.replyTo()`.
- Sätt följande i `application.properties` — värdena måste matcha exakt:

```properties
kafka.subtopic=my-regel

mp.messaging.incoming.operativt-uppgiftslager-status-notification.topic=operativt-uppgiftslager-status-notification.my-regel
```

---

## Scenario 3 — Kogito-subprocess som anropar en regel

`replyTo` sätts automatiskt av `RegelService.createRegelRequest()` — behöver ej sättas manuellt.
Det som måste konfigurera i `application.properties` är:

```properties
# Svarstopic — måste vara unikt per subprocess, typiskt prefixat med ${PROCESS_ID}
MY_REGEL_RESPONSE_TOPIC_NAME=${PROCESS_ID}-my-regel-responses

# Prenumerera på det topic för inkommande regelsvar
mp.messaging.incoming.my-regel-responses.topic=${MY_REGEL_RESPONSE_TOPIC_NAME}
```

I BPMN-uppgiften `Create Request`, koppla `responseTopicProperty` till **property-namnet** (inte dess värde):

```
responseTopicProperty = "MY_REGEL_RESPONSE_TOPIC_NAME"
```

`RegelService` löser upp det faktiska topic-namnet från config vid build-time av requesten och skriver in det i `replyTo` i payload.

För workflow-servicemönstret (icke-subprocess), se Scenario 4 nedan.

---

## Scenario 4 — Toppnivåprocess triggad via rimfrost-service-workflow

`rimfrost-service-workflow` fungerar som en REST-till-Kafka-brygga för toppnivå-Kogito-processer.
Till skillnad från regelsubprocesser bärs inte `replyTo` i Kafka-triggermeddelandet — det persisteras till en databastabell nycklad på `handlaggningId` och slås upp när processen slutförs.

Det fullständiga flödet:

1. Extern anropare skickar `POST /yrkande` till `rimfrost-service-workflow` med `replyTo` i REST-request body.
2. Workflow lagrar `replyTo` i tabellen `handlaggning_reply_topic`, nycklad på `handlaggningId`
3. Workflow löser upp Kafka-topic för målprocessen via `rimfrost-service-erbjudande-topic` och skickar ett triggermeddelande — ett CloudEvent med enbart `handlaggningId` i payload, inget `replyTo`
4. Processen körs och skickar sitt svar till det fasta topic `handlaggning-responses`
5. Workflows Kafka-konsument tar emot svaret, slår upp `replyTo` från DB med `handlaggningId`, och routar det slutliga svaret till topic via `sendHandlaggningDone()`

Processen är helt omedveten om `replyTo` — den känner bara till `handlaggningId`.
Det DB-persisterade värdet överlever omstarter: om workflow kraschar mellan steg 3 och 5 går inte `replyTo` förlorat.

För omstart via `POST /handlaggning/{id}/process` anger anroparen ett nytt `replyTo` som skriver över det lagrade värdet.

---
