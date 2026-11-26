# Rimfrost 1.3 — Sammanfattning av förändringar för utvecklare

---

## Breaking changes

### 1. `RegelDataRequest.replyTo()` borttaget (`rimfrost-framework-regel` 1.6.0)

Tidigare inkluderade det anropande flödet ett `replyTo`-fält i Kafka-förfrågan som angav vilket topic regeln skulle skicka sitt svar till. Ramverket dirigerade svar dynamiskt till denna topic. Detta är nu borttaget — svars-topic konfigureras statiskt i regelns egen `application.properties` (`mp.messaging.outgoing.regel-responses.topic`).

**`RegelDataRequest`**: ta bort alla anrop till `request.replyTo()`.

**`RegelRequestHandlerBase.sendResponse()`**: parametern `replyTo` är borttagen från alla overloads:

```java
// Före
sendResponse(handlaggningId, cloudEventData, utfall, request.replyTo());
sendResponse(handlaggningId, cloudEventData, errorInformation, request.replyTo());

// Efter
sendResponse(handlaggningId, cloudEventData, utfall);
sendResponse(handlaggningId, cloudEventData, errorInformation);
```

**Testhjälpklasser**: `RegelKafkaConnector.sendRegelRequest()` och `RegelTestData.newRegelRequestMessagePayload()` tar inte längre emot något `replyTo`-argument.

**Kafka consumer group ID**: utan `replyTo` identifierar varje process och subprocess sig via ett unikt consumer group ID. Varje process och subprocess behöver en explicit `group.id` i `application.properties`, annars riskerar flera tjänster att konsumera varandras meddelanden från det delade topic:

```properties
mp.messaging.incoming.vah-handlaggning-requests.group.id=vah-handlaggning-requests-consumer
```

### 2. Kafka-svar från regler kastar nu exception vid fel (`rimfrost-framework-regel` 1.6.1)

Publicering av regelsvar använde tidigare fire-and-forget. Nu **väntar ramverket på kvittens från Kafka** och kastar `RegelResponseException` vid fel. Detta garanterar leverans men kräver att anroparen hanterar exceptionet.

```java
// Ny exceptionstyp
public class RegelResponseException extends RuntimeException { ... }
```

Ny konfigurationsproperty:

| Egenskap | Default | Syfte |
|----------|--------------|-------|
| `rimfrost.regel.publish-timeout` | `10s` | Max väntetid för Kafka-kvittens på regelsvar |

### 3. `RegelService.createRegelRequest()` — borttaget argument (`rimfrost-framework-process` 2.0.0)

Det tredje argumentet `responseTopicProperty` är borttaget i linje med Breaking change 1. Alla BPMN-servicetasks som anropar metoden behöver uppdateras:

```java
// Före
createRegelRequest(handlaggningId, aktivitetId, "mp.messaging.outgoing.regel-responses.topic");

// Efter
createRegelRequest(handlaggningId, aktivitetId);
```

### 4. Komplettering GET-svar: nytt svarsformat (`rimfrost-framework-regel-komplettering`)

`GET /{handlaggningId}` returnerar nu `RegelKompletteringReadResponse<T>` i stället för enbart `T`. Se avsnittet [Informationsaccess i komplettering GET-svar](#informationsaccess-i-komplettering-get-svar) för detaljer. Regelimplementationen påverkas inte — enbart BFF:er som anropar endpoint:en behöver uppdateras.

### 5. `SakfragaStallningstagande` omdöpt till `SakfragaStallningstagandeContainer` (`rimfrost-framework-regel` 1.6.0)

Typen har bytt namn i handlaggning-modellen och i `RegelUtils`. Regler som använder dessa typer direkt behöver uppdatera imports och typreferenser:

```java
// Före
import se.fk.rimfrost.framework.handlaggning.model.SakfragaStallningstagande;
import se.fk.rimfrost.framework.handlaggning.model.ImmutableSakfragaStallningstagande;

// Efter
import se.fk.rimfrost.framework.handlaggning.model.SakfragaStallningstagandeContainer;
import se.fk.rimfrost.framework.handlaggning.model.ImmutableSakfragaStallningstagandeContainer;
```

Metoderna i `RegelUtils` (`createSakfragaStallningstagande()`, `mergeSakfragorStallningstaganden()`) har uppdaterade signaturer men samma namn.

---

## Nya funktioner för processutvecklare

### Processvy (`rimfrost-framework-process` 2.0.0+)

Processvyn ger handläggaren en livebild av var i ett flöde ett ärende befinner sig. I stället för att behöva tolka råa BPMN-tillstånd ser handläggaren en namngiven, ordnad lista med steg — med tydlig markering för vilket steg som pågår, vilka som är klara och vilka som återstår. Detta visas i handläggarportalen genom att portalen anropar processens `/processvy/steg`-endpoint med aktuellt BPMN-aktivitets-ID.

Det är viktigt att skilja på de två delarna:

- **`processvy.url`** är *inte* det som exponerar endpoint:en. Det är URL:en som skickas med i regelanrop och vidare till OUL-uppgiften, så att portalen vet att uppgiften har en processvy och vilken mikrofrontend som ska laddas.
- **`processbeskrivning.json`** är det som styr endpoint:en `GET /processvy/steg` — ramverket exponerar den alltid, men returnerar `404` om filen saknas.

Flödet i portalen ser ut så här:
1. Portalen läser `processvyUrl` (t.ex. `/process/vah`) från OUL-uppgiften
2. Portalen tar sista ledet (`vah`) och laddar `process-vah-fe` som mikrofrontend
3. Mikrofrontenden anropar `process-vah-bff GET /api/steg?aktivitetId=…`
4. BFF:en anropar processens `GET /processvy/steg?aktivitetId=…`

Två saker krävs för att aktivera processvyn:

**1. Ange URL:en i `application.properties`** (signalerar till portalen att uppgiften har en processvy):

```properties
processvy.url=/process/vah
```

**2. Skapa `src/main/resources/processbeskrivning.json`** (aktiverar endpoint:en med faktisk steginformation).

Exempel med tre steg, där ett steg täcker två BPMN-aktiviteter:

```json
{
  "processId": "vah",
  "processnamn": "Vård av husdjur",
  "steg": [
    {
      "id": "kontrollera-ratt-till-forsakring",
      "rubrik": "Kontrollera rätt till försäkring",
      "beskrivning": "Handläggaren kontrollerar att den försäkrade har rätt till ersättning.",
      "aktiviteter": [
        "d4ab4820-68d9-41e0-abe1-cd8f9865d275"
      ]
    },
    {
      "id": "bedom-medicinsk-information",
      "rubrik": "Bedöm medicinsk information",
      "beskrivning": "Handläggaren bedömer om inlämnad medicinsk information styrker behovet.",
      "aktiviteter": [
        "1c9e7a54-3d2b-4f6a-8e1c-0a9b8c7d6e5f",
        "7f3a1b2c-4d5e-6f7a-8b9c-0d1e2f3a4b5c"
      ]
    },
    {
      "id": "fatta-beslut",
      "rubrik": "Fatta beslut",
      "beskrivning": "Handläggaren fattar beslut och kommunicerar utfallet till den försäkrade.",
      "aktiviteter": [
        "9e8d7c6b-5a4f-3e2d-1c0b-9a8f7e6d5c4b"
      ]
    }
  ]
}
```

Ramverket exponerar automatiskt `GET /processvy/steg?aktivitetId={uuid}`. Om `processbeskrivning.json` saknas returnerar endpoint:en `404` utan fel.

Regler som anropas av en process med processvy konfigurerat tar automatiskt emot URL:en i `RegelDataRequest`:

```java
@Nullable String processvyUrl = request.processvyUrl();
```

Fältet är `@Nullable` — förfrågningar från processer utan `processvy.url` konfigurerat sätter inte detta värde.

### Stöd för sub-steg (`rimfrost-framework-process` 2.1.0)

Steg kan nu kapslas i upp till **3 nivåer** (toppnivå + 2 sub-stegsnivåer). Ett steg har antingen `aktiviteter` *eller* `steg`, aldrig båda.

```json
{
  "id": "bedom-ratt-till-ersattning",
  "rubrik": "Bedöm rätt till ersättning",
  "steg": [
    {
      "id": "kontrollera-forsakring",
      "rubrik": "Kontrollera försäkring",
      "aktiviteter": ["d4ab4820-68d9-41e0-abe1-cd8f9865d275"]
    }
  ]
}
```

Alla steg-ID:n måste vara unika i hela trädstrukturen.

### JSON-schemavalidering av processbeskrivning

Ramverket validerar `processbeskrivning.json` vid uppstart. Schemat finns i `src/main/resources/schema/processbeskrivning.schema.json`. Valideringen kontrollerar obligatoriska fält, UUID-format för aktivitets-ID:n, djupbegränsning och okända fält (stavfel upptäcks).

Ett återanvändbart CI-arbetsflöde finns tillgängligt: `.github/workflows/validate-processbeskrivning.yaml`.

### Testhjälpklass `ProcessbeskrivningVerifier`

Verifierar att alla BPMN-aktivitets-ID:n finns med i `processbeskrivning.json`:

```java
@Test
void processbeskrivning_should_contain_all_aktiviteter() {
    ProcessbeskrivningVerifier.assertAllAktiviteterInProcessbeskrivning("vah.bpmn");
}
```

Lägg till `rimfrost-framework-process:tests` som testberoende för att använda klassen.

### Ny metod: `RegelService.createRegelOulCancelled()`

Ny hjälpmetod för processer som behöver avboka en OUL-uppgift när ett flöde avbryts. Anropas från BPMN-servicetasks i processimplementationer — inte från regelkod.

```java
RegelOulCancelledMessagePayloadData createRegelOulCancelled(String handlaggningId);
```

---

## Modifierad regeltyp: Komplettering (`rimfrost-framework-regel-komplettering`)

Ramverkskomponent för regler som behöver inhämta kompletterande information från en sökande via en handläggare. Introducerades i `rimfrost-1_2`. Nyheterna i 1.3 är identitetsprenumerationen och informationsaccess i GET-svaret.

### Hur det fungerar

En kompletteringsregel tar emot en Kafka-förfrågan, kontrollerar om yrkandedata redan är fullständigt och antingen:
- svarar direkt med `utfall = JA` (ingen information saknas), eller
- skapar en OUL-uppgift för handläggaren, väntar och svarar med `utfall = JA` när handläggaren markerat kompletteringen som klar.

Båda vägarna resulterar i `utfall = JA` — det anropande flödet behöver inte särskilja dem.

### Vad regelutvecklare implementerar

Implementera `RegelKompletteringService<T>` där `T` är den OpenAPI-genererade datatypen för kompletteringen:

```java
@ApplicationScoped
public class RtfKompletteringService
        implements RegelKompletteringService<RtfKompletteringData> {

    @Override
    public boolean isKompletteringRequired(Handlaggning handlaggning) {
        // returnera true om attribut saknas i yrkandedata
    }

    @Override
    public RtfKompletteringData readSvarData(Handlaggning handlaggning) {
        // returnera data som visas för handläggaren i GET /{handlaggningId}
    }

    @Override
    public HandlaggningUpdate registerSvar(Handlaggning handlaggning, RtfKompletteringData request) {
        // applicera handläggarens svar och returnera uppdateringen som ska persisteras
    }
}
```

Ramverket exponerar automatiskt `GET/PATCH/POST /{handlaggningId}` för handläggarportalen och hanterar OUL-uppgiftens livscykel, åtkomstkontroll och Kafka-svar.

### Nytt i 1.3: identitetsprenumeration

Regler kan välja att prenumerera på identitetsändringar genom att överskriva `getPrenumerationsIdentiteter()`. När en icke-tom lista returneras prenumererar ramverket på identitetsändringshändelser och avslutar kompletteringen automatiskt om yrkandekontrollen godkänns efter en identitetsuppdatering.

```java
@Override
public List<Idtyp> getPrenumerationsIdentiteter(Handlaggning handlaggning) {
    return List.of(new Idtyp(handlaggning.yrkande().personnummer(), IdtypTyp.PERSONNUMMER));
}
```

En tom lista (standardbeteendet) behåller tidigare beteende — ingen prenumeration skapas. I så fall är det enda sättet att lösa kompletteringen att handläggaren manuellt slutför OUL-uppgiften; det finns ingen extern händelse som utvärderar `isKompletteringRequired()` på nytt.

### Informationsaccess i komplettering GET-svar

FK kräver att handläggarens åtkomst till ärendedata loggas som en informationsaccesshändelse. I stället för att varje regels BFF ska behöva samla ihop denna data på egen hand bundlar ramverket den automatiskt i GET-svaret.

`GET /{handlaggningId}` returnerar nu `RegelKompletteringReadResponse<T>` i stället för enbart `T`:

```json
{
  "data": { /* regelspecifikt svar från readSvarData() */ },
  "informationsaccess": {
    "uppgiftId": "...",
    "regelId": "...",
    "regelVersion": "...",
    "anvandare": {
      "typId": "PERSONAL_IDENTITY_NUMBER",
      "varde": "..."
    }
  }
}
```

Regelimplementationen påverkas inte — `readSvarData()` implementeras på samma sätt som tidigare. BFF:er som anropar endpoint:en behöver däremot uppdateras för att hantera det nya svarsformatet.

---

## Konfigurationsöversikt

| Ramverk | Egenskap | Default | Notering |
|---------|----------|--------------|---------|
| `rimfrost-framework-regel` | `rimfrost.regel.publish-timeout` | `10s` | Timeout för Kafka-kvittens på regelsvar |
| `rimfrost-framework-process` | `processvy.url` | (inget) | Valfritt; nytt i 1.3 |

---

## Driftsättning: databas per process

*Detta är inte nytt i 1.3, men värt att dokumentera eftersom integrationsmiljön (`rimfrost-kubernetes`) nu konfigurerar det explicit.*

Kogito använder JDBC-persistens mot PostgreSQL och hanterar sitt eget databasschema via KIE Flyway vid uppstart. Varje process måste därför ha ett eget isolerat schema — antingen en dedikerad databas, eller ett unikt PostgreSQL-schema inom en delad databas.

I integrationsmiljön delar VAH och VAB en gemensam databas men separeras via `currentSchema` i JDBC-URL:en:

```
jdbc:postgresql://host:5432/rimfrost-test?currentSchema=vah
jdbc:postgresql://host:5432/rimfrost-test?currentSchema=vab
```

Utan isolation riskerar Kogitos Flyway-migrationer att krocka mellan processer.

---

## Checklista för migrering

### Regelimplementationer

- [ ] Ta bort alla referenser till `RegelDataRequest.replyTo()`
- [ ] Hantera (eller propagera) `RegelResponseException` från `sendResponse()`
- [ ] Byt ut `SakfragaStallningstagande` mot `SakfragaStallningstagandeContainer` i imports och typreferenser
- [ ] Läs valfritt `RegelDataRequest.processvyUrl()` om processens URL behövs

### BFF:er mot kompletteringsregler

- [ ] Uppdatera parsning av `GET /{handlaggningId}` — svaret är nu insvept i `RegelKompletteringReadResponse<T>`, se avsnittet *Informationsaccess i komplettering GET-svar*

### Processimplementationer

- [ ] Lägg till `group.id` för incoming handlaggning-requests-konsumenten i `application.properties` (gäller även subprocesser)
- [ ] Uppdatera alla anrop till `RegelService.createRegelRequest()` — ta bort det tredje argumentet
- [ ] Lägg valfritt till `processvy.url` i `application.properties` för att aktivera processvyn
- [ ] Lägg valfritt till `processbeskrivning.json` för att aktivera processvy-endpoint:en
- [ ] Lägg valfritt till `ProcessbeskrivningVerifier`-test för BPMN-konsistens
