# Versionshantering

Gäller alla regelimplementationer i rimfrost-ramverket.

## Översikt

Regler ansvarar för att steppa version-fältet på alla objekt de modifierar.
Ramverket stegar aldrig version-fält automatiskt.

Nedan beskrivs exempel på två version-fält som regelimplementationer hanterar:

| Fält | Objekt | Granularitet | Stegningsansvar |
|------|--------|--------------|-----------------|
| `HandlaggningUpdate.version` | Handläggnings-objektet | En per handläggning | Regeln (vid behov) |
| `ProduceratResultat.version` | Ett enskilt resultat i yrkandet | En per resultatobjekt | Regeln (förmånen) |

---

## 1. `HandlaggningUpdate.version` — versionshantering av handläggningsobjektet

### Syfte

Concurrency-hantering av handläggnings-objektet i `rimfrost-service-handlaggning`.
Skyddar mot att en föråldrad skrivning skriver över ändringar som gjorts av en annan part sedan
senaste läsning. Backend persisterar den version den tar emot — den steppar inte automatiskt.
En skrivning som innehåller en version som konfliktar med nuvarande tillstånd avvisas av backend
med ett versionskonflikt-fel.

### Ramverkets ansvar

Ramverket stegar aldrig `HandlaggningUpdate.version` — värdet kopieras alltid oförändrat från
det mottagna `Handlaggning`-objektet. Om en regel behöver signalera en meningsfull
tillståndsändring till konsumenter ansvarar regeln själv för att steppa versionen.

Versionskonflikt-fel från backend propageras som explicita fel till anroparen. Ramverket
försöker inte automatiskt göra om en skrivning som avvisats på grund av versionskonflikt.

### Hur regler bumpar HandlaggningUpdate.version

När en regel vill signalera en meningsfull tillståndsändring bygger den sin `HandlaggningUpdate`
med `handlaggning.version() + 1`:

```java
return ImmutableHandlaggningUpdate.builder()
      .from(handlaggning)
      .version(handlaggning.version() + 1)   // regeln steppar
      // övriga fält...
      .build();
```

Skrivningar som inte kräver versionsstegning skickar `handlaggning.version()` oförändrat.

---

## 2. `ProduceratResultat.version` — versionshantering på resultats-nivå

### Syfte

Hanterar versionen av ett enskilt resultatobjekt (ersättning, beslut m.m.) i yrkandets
`produceradeResultat`-lista. Gör det möjligt för konsumenter att upptäcka om ett specifikt
resultat har förändrats sedan de senast läste det, oberoende av handläggningens övergripande version.

### Hur regler bumpar ProduceratResultat.version

När data som tillhör ett specifikt `ProduceratResultat` ändras — till exempel uppdaterat
`beslutsutfall` eller `avslagsanledning` på en ersättning — hittar regeln det berörda
resultatet, och bygger en ny immutable kopia med ändrade fält och `version + 1`:

```java
// Exempel från RtfService.java
return ImmutableProduceratResultat.builder()
      .from(produceratResultat)                        // kopiera alla befintliga fält
      .version(produceratResultat.version() + 1)       // regeln steppar
      .avslagsanledning(updateErsattning.getAvslagsanledning())
      .data(updatedData)
      .build();
```

Det uppdaterade resultatet placeras sedan i ett nytt `Yrkande` (via
`RegelUtils.createYrkandeWithUpdatedProduceradeResultat`) och bärs med i den `HandlaggningUpdate`
som regeln returnerar.

### Vad en regelimplementation inte behöver göra

En regel som enbart läser data (implementerar `readData()`) rör aldrig `ProduceratResultat.version`.
En regel som inte muterar något enskilt resultat lämnar likaså alla
`ProduceratResultat.version`-värden oförändrade.

---

## 3. Vad regeln ser i `produceradeResultat` — alltid senaste versionen, aldrig historik

`produceradeResultat`-listan på `Handlaggning` innehåller **en post per resultat-id** — den
senaste versionen. Ingen historik sparas i listan.

Det spelar ingen roll att ett resultat har ändrats flera gånger tidigare. När en regel tar emot
en `Handlaggning` ser den bara det aktuella tillståndet för varje resultat. `version`-fältet är
en ändringsräknare, inte ett index man kan använda för att hämta äldre versioner.

### Merge-logiken

När en regel returnerar uppdaterade resultat slår ramverket ihop den nya listan
med den befintliga enligt en enkel regel: **samma id → det nya vinner, det gamla kastas**. Resultat
vars id inte berörs förs över oförändrade till det nya yrkandet.


