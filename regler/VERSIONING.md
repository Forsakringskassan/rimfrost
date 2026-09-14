# Versionshantering

Gäller alla regelimplementationer i rimfrost-ramverket.

## Översikt

Det finns två oberoende versionsfält i systemet. De lever på olika objekt, fyller olika syften
och steppas av olika parter.

| Fält | Objekt | Granularitet | Stegningsansvar |
|------|--------|--------------|-----------------|
| `HandlaggningUpdate.version` | Handläggnings-objektet | En per handläggning | Regeln (vid behov) |
| `ProduceratResultat.version` | Ett enskilt resultat i yrkandet | En per resultatobjekt | Regeln (förmånen) |

De är ortogonala — att steppa det ena har ingen automatisk effekt på det andra.

---

## 1. `HandlaggningUpdate.version` — versionshantering av handläggningsobjektet

### Syfte

Concurrency-hantering av handläggnings-objektet i `rimfrost-service-handlaggning`.
Skyddar mot att en föråldrad skrivning skriver över ändringar som gjorts av en annan part sedan
senaste läsning. Backend persisterar den version den tar emot — den steppar inte automatiskt.
En skrivning som innehåller en version som konfliktar med nuvarande tillstånd avvisas av backend
med ett versionskonflikt-fel.

### Ramverkets ansvar

Ramverket stegar aldrig `HandlaggningUpdate.version`. Alla skrivningar skickar
`handlaggning.version()` oförändrat till backend. Om en regel behöver signalera en meningsfull
tillståndsändring till konsumenter ansvarar regeln själv för att steppa versionen.

Versionskonflikt-fel från backend propageras som explicita fel till anroparen. Ramverket
försöker inte automatiskt göra om en skrivning som avvisats på grund av versionskonflikt.

### Alla regeltyper

| Regeltyp | Skrivsteg | Version i HandlaggningUpdate |
|----------|-----------|------------------------------|
| Manuell (`read()`-flödet) | Middleware skriver underlag | `handlaggning.version()` — oförändrat |
| Manuell (`update()`-flödet) | Regel returnerar HandlaggningUpdate | `handlaggning.version()` — oförändrat |
| Maskinell | Regel returnerar HandlaggningUpdate | `handlaggning.version()` — oförändrat |
| Komplettering (`registerSvar()`) | Regel returnerar HandlaggningUpdate | `handlaggning.version()` — oförändrat |
| Komplettering (`/done`) | Ramverket bygger HandlaggningUpdate | `handlaggning.version()` — oförändrat |

---

## 2. `ProduceratResultat.version` — versionshantering på resultats-nivå

### Syfte

Hanterar versionen av ett enskilt resultatobjekt (ersättning, beslut m.m.) i yrkandets
`produceradeResultat`-lista. Gör det möjligt för konsumenter att upptäcka om ett specifikt
resultat har förändrats sedan de senast läste det, oberoende av handläggningens övergripande version.

### Vem steppar det

Regeln (förmånen) — i `updateData()` för manuella regler, i `processRegel()` för maskinella
regler, i `registerSvar()` för kompletteringsregler, när ett enskilt resultat uppdateras.

### När det steppas

När data som tillhör ett specifikt `ProduceratResultat` ändras — till exempel uppdaterat
`beslutsutfall` eller `avslagsanledning` på en ersättning. Regeln hittar det berörda
resultatet, bygger en ny immutable kopia med ändrade fält och `version + 1`:

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

## 3. Vad regeln ser — alltid senaste versionen, aldrig historik

`produceradeResultat`-listan på `Handlaggning` innehåller **en post per resultat-id** — den
senaste versionen. Ingen historik sparas i listan.

Det spelar ingen roll att ett resultat har ändrats flera gånger tidigare. När en regel tar emot
en `Handlaggning` ser den bara det aktuella tillståndet för varje resultat. `version`-fältet är
en ändringsräknare, inte ett index man kan använda för att hämta äldre versioner.

### Merge-logiken

När en regel returnerar uppdaterade resultat slår ramverket ihop den nya listan
med den befintliga enligt en enkel regel: **samma id → det nya vinner, det gamla kastas**. Resultat
vars id inte berörs förs över oförändrade till det nya yrkandet.

### Konsekvenser för regelutvecklare

- Hitta rätt resultat via `id`, bygg en ny immutable kopia med `version + 1` och returnera den —
  ramverket ersätter det gamla automatiskt via merge-logiken.
- Det finns ingen tillgång till tidigare versioners innehåll. Om historik behövs måste det hanteras
  på annat sätt, t.ex. i `data`-fältet eller via en separat mekanism utanför ramverket.
- `ProduceratResultat.version` är en ändringsräknare som konsumenter kan jämföra med vad de senast
  läste — inte ett historiknummer som ger åtkomst till äldre tillstånd.

---

## Vanliga misstag

**Antagande att ramverket stegar `HandlaggningUpdate.version` automatiskt.**
Ramverket stegar aldrig versionen — den passeras alltid oförändrad. En regel som förväntar sig
att ramverket hanterar versionsstegning kommer inte att steppa versionen alls, och konsumenter
kan inte avgöra om handläggningen har förändrats.

**Glömma att steppa `ProduceratResultat.version` när ett resultat uppdateras.**
Om skrivningen accepteras av backend lagras den version som skickas in. Om versionen inte steppas
kan konsumenter inte avgöra att resultatet har förändrats.

**Behandla de två versionerna som samma sak.**
Det är de inte. Ett enda `update()`-anrop steppar `ProduceratResultat.version` men lämnar
`HandlaggningUpdate.version` på det värde som skrevs av senaste `read()`-anropet.
