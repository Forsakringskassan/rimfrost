# `config.yaml` – Regelmetadata

Varje regel i Rimfrost — oavsett om den är maskinell eller manuell — måste innehålla en fil `src/main/resources/config.yaml`. Filen innehåller regelns **metadata**: den beskriver vad regeln gör, vilken roll den tillhör, och vilket lagrum som styr den. Innehållet valideras mot ett JSON Schema vid uppstart, och ramverket använder informationen för registrering, loggning och spårbarhet.

Filen ägs av regelteamet och ska versionshanteras tillsammans med regelkoden. Filen ska inte innehålla miljöspecifik konfiguration — sådant hör hemma i `application.properties`.

## De fyra sektionerna

`config.yaml` är uppdelad i fyra obligatoriska sektioner, var och en med ett tydligt ansvarsområde:

| Sektion | Ansvar |
|---|---|
| `uppgift` | Operativ identitet — path, aktivitetsbenämning och version |
| `specifikation` | Verksamhetsbeskrivning — namn, beskrivning, roll och applikationsreferens |
| `regel` | Regellogikens identitet — namn och beskrivning av vad regeln bedömer |
| `lagrum` | Juridisk grund — författning, kapitel, paragraf och giltighetsdatum |

## Exempel

```yaml
uppgift:
  # Stabil UUID som identifierar uppgiften unikt över tid
  id: d435beb0-c1c1-471a-916f-a111db0ef08d
  # Semantisk version på formen "major.minor"
  version: "1.0"
  # URL-path som ramverket använder för routing
  path: /regel/rtf-maskinell
  # Aktivitetsbenämning som visas i handläggningsgränssnittet
  aktivitet: "Bedömma rätten till"

specifikation:
  id: 494726b5-5122-4efc-8a5d-578dcd44df0f
  version: "1.0"
  # Kortfattat namn på uppgiften, används i listor och loggar
  namn: "Har kunden rätt till VAH?"
  # Längre beskrivning av vad uppgiften innebär för handläggaren
  uppgiftbeskrivning: "Kontrollerar om personen är folkbokförd i Sverige samt om personen har ett jobb"
  # Kod för tillämplig verksamhetslogik
  verksamhetslogik: C
  # Den roll som ansvarar för uppgiften
  roll: ANSVARIG_HANDLAGGARE
  # FK:s interna applikations-ID
  applikationsId: 3.5.2
  # Versionsidentifierare för applikationslogiken
  applikationsversion: kontroll_folkbokford_arbetsgivare_1.0

regel:
  id: caf86a5f-9776-4fce-a854-8d365fa75067
  version: "1.0"
  # Regelns kortnamn — syns i spårningsloggar
  namn: "Har kunden arbetsgivare och är folkbokförd"
  # Detaljerad beskrivning av vad regeln kontrollerar
  beskrivning: "Maskinell kontroll som kollar om kunden är folkbokförd i sverige samt kollar om personen har en arbetsgivare"

lagrum:
  id: 6955bd9b-2019-4589-a460-7bcfd8a43eb5
  version: "1.0"
  # Datum från vilket lagrummet gäller (ISO 8601)
  giltigFom: 2010-02-11
  # Namn på författning (lag, förordning etc.)
  forfattning: Husdjursbalken
  kapitel: 4
  paragraf: 7
  stycke: 2
  punkt: 3

# Optionell sektion för utökad beskrivning
utokadUppgiftsbeskrivning:
  beskrivning: "Maskinell kontroll som kollar om kunden är folkbokförd i sverige samt kollar om personen har en arbetsgivare"
```

## Regler och begränsningar

**UUIDs** — fälten `id` i varje sektion ska vara globalt unika UUID v4. Generera ett nytt UUID per sektion när du skapar en ny regel. Återanvänd aldrig ett UUID från en annan regel.

**Version** — formatet är `"major.minor"` (t.ex. `"1.0"` eller `"2.3"`). Öka versionen när innehållet förändras på ett sätt som påverkar ramverkets beteende.

**Utökning** — varje sektion accepterar ett valfritt `metadata`-objekt med godtyckliga nycklar. Använd det för information som inte ryms i standardfälten, men som behöver följa med regelns definition.

**Schemavalidering** — ramverket validerar filen mot `regel_schema.yaml` vid uppstart. Saknade obligatoriska fält eller felaktigt format ger ett startfel. Det fullständiga schemat finns i `rimfrost-framework-regel/src/main/resources/schema/regel_schema.yaml`.
