# Regler

Det finns tre typer av regler i Rimfrost:

| Typ | Beskrivning | Guide |
|-----|-------------|-------|
| [Maskinell](maskinell/README.md) | Helt automatiserad, inga mänskliga beslut | [maskinell/README.md](maskinell/README.md) |
| [Manuell](manuell/README.md) | Kräver att en handläggare agerar via portal | [manuell/README.md](manuell/README.md) |
| [Komplettering](komplettering/README.md) | Hanterar insamling av kompletterande uppgifter | [komplettering/README.md](komplettering/README.md) |

## Asynkron svarsroutning

Alla regeltyper använder ett gemensamt mönster för att deklarera och propagera svarsdestinationen (`replyTo`) genom ramverket.

Se [ASYNC_ROUTING.md](../ASYNC_ROUTING.md) för en fullständig beskrivning av hur `replyTo` deklareras, propageras och används — inklusive OUL-baserade flöden och konfigurationskrav.
