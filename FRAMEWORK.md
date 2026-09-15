## Regelramverk — beroenden

```mermaid
graph TD

    rimfrost-template-regel-manuell -->|uses| rimfrost-framework-regel-manuell
    rimfrost-template-regel-komplettering -->|uses| rimfrost-framework-regel-komplettering

    rimfrost-template-regel-maskinell -->|uses| rimfrost-framework-regel-maskinell
    rimfrost-framework-regel-maskinell -->|uses| rimfrost-framework-regel

    rimfrost-framework-regel-manuell -->|uses| rimfrost-framework-regel-oul
    rimfrost-framework-regel-komplettering -->|uses| rimfrost-framework-regel-oul
    rimfrost-framework-regel-oul -->|uses| rimfrost-framework-regel
    rimfrost-framework-regel-oul -->|uses| rimfrost-framework-oul

```

## Portal och micro-frontends

```mermaid
graph TD

    rimfrost-template-micro-fe -->|används som template för| regel-fe["rimfrost-regel-*-fe"]
    rimfrost-template-micro-fe-bff -->|används som template för| regel-bff["rimfrost-regel-*-bff"]

    rimfrost-portal-handlaggare -->|laddar| regel-fe
    rimfrost-portal-bff -->|proxar mot| OUL["Operativt uppgiftslager"]
    regel-bff -->|proxar mot| regel-backend["rimfrost-regel-*"]

```

### rimfrost-portal-handlaggare

Vue-baserad portalapplikation för handläggare. Listar tilldelade uppgifter och laddar rätt micro-frontend per uppgiftstyp baserat på uppgiftens `url`-fält från Operativt uppgiftslager.

### rimfrost-portal-bff

BFF (Backend For Frontend) för portalen. Proxar anrop mot Operativt uppgiftslager och hanterar hämtning och tilldelning av operativa uppgifter.

### rimfrost-template-micro-fe

Template för micro-frontend-implementation av manuella regler. Varje regel skapar ett eget repo baserat på denna template och implementerar en Vue-komponent som renderas inuti portalen.

### rimfrost-template-micro-fe-bff

Template för BFF till micro-frontends. Proxar anrop från micro-frontendens Vue-komponent mot regelns backend-service och hanterar eventuell datatransformation.

