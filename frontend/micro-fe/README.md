## Relaterade repon

### rimfrost-template-micro-fe

Template för nya micro-frontends: https://github.com/Forsakringskassan/rimfrost-template-micro-fe

### rimfrost-portal-bff

Innehåller `remotes.json` där nya micro-frontends registreras.

---

# Översikt av rekommenderade tasks vid implementation av ny micro-frontend

NOTE: Se
- https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-fe
- https://github.com/Forsakringskassan/rimfrost-regel-bekraftabeslut-fe

för exempel på implementerade micro-frontends.

## Skapa nytt repo baserat på template

Template för micro-frontend: https://github.com/Forsakringskassan/rimfrost-template-micro-fe

Replace på förekomster av _template_ till regelns namn.  
TODOs i template-filerna ger tips om vad som behöver justeras.

## Konfigurera Module Federation

I `vite.config.ts`, uppdatera federationskonfigurationen:

```typescript
federation({
  name: "dinRegelApp",           // Unikt namn — används som scope i remotes.json
  exposes: {
    "./DinKomponent": "./src/components/DinKomponent.vue",
  },
  manifest: true,
  publicPath: "auto",
  shared: {
    vue: { singleton: true, requiredVersion: "^3.5.0" },
    "@fkui/vue": { singleton: true, requiredVersion: "^6.0.0" },
    pinia: { singleton: true, requiredVersion: "^3.0.0" },
  },
})
```

`manifest: true` och `publicPath: "auto"` krävs för att portalen ska kunna ladda remoten korrekt vid körning.

## Implementera huvudkomponenten

Komponenten tar emot `handlaggningId` (och `regeltyp`) som props från portalen:

```vue
<script setup lang="ts">
const props = defineProps<{
  handlaggningId: string;
  regeltyp?: string;
}>();
</script>
```

Komponenten kommunicerar med sin BFF via följande standardendpoints:

| Endpoint | Metod | Body | Motsvararar |
|----------|-------|------|-------------|
| `/api/task` | `POST` | `{ handlaggningId }` | `readData` |
| `/api/task` | `PATCH` | `{ handlaggningId, ... }` | `updateData` |
| `/api/task/done` | `POST` | `{ handlaggningId }` | `done` |

Metoderna `readData`, `updateData` och `done` syftar på motsvarande metoder i regelns backend-service — se [regler/manuell/README.md](../../regler/manuell/README.md).

## Signalera slutförd uppgift

När handläggaren slutför uppgiften dispatchar micro-frontenden ett `task-done`-event som portalen lyssnar på:

```typescript
window.dispatchEvent(
  new CustomEvent("task-done", {
    detail: {
      handlaggningId: props.handlaggningId,
      success: true,
      message: "Uppgift slutförd",
    },
  }),
);
```

## Miljövariabler

| Variabel | Dev (`.env`) | Container (`runtime-config.js`) | Beskrivning |
|----------|--------------|----------------------------------|-------------|
| BFF-URL | `VITE_BFF_URL` | `RUNTIME_BFF_URL` | Bas-URL till micro-frontendens BFF |
| Dev-ID | `VITE_DEV_HANDLAGGNING_ID` | — | Fallback `handlaggningId` för lokal testning utan portalen |

`runtime-config.js` sätter en global på `window`, namnrymd per app (t.ex. `window.__DIN_REGEL_ENV__`), **inte en delad `window._env_`**. Portalen och varje remote den laddar delar samma webbläsarfönster via Module Federation, så en gemensam global skulle göra att appar skriver över varandras runtime-konfiguration — se motsvarande avsnitt i [portal/README.md](../portal/README.md).

## Registrering i portalen

Lägg till din nya remote i `remotes.json` i `rimfrost-portal-bff`:

```json
{
  "routes": {
    "din-regel": {
      "scope": "dinRegelApp",
      "module": "DinKomponent",
      "devEntry": "http://localhost:3033/mf-manifest.json",
      "prodEntry": "https://din-regel.intern.example.com/mf-manifest.json"
    }
  }
}
```

- **scope**: matchar `name` i federationskonfigurationen i `vite.config.ts`
- **module**: namnet på den exponerade komponenten (utan `./`)
- **devEntry/prodEntry**: URL till micro-frontendens `mf-manifest.json`

Portalen laddar rätt micro-frontend baserat på uppgiftens `url`-fält, som sätts av regelns backend via `config.yaml` (nyckel: `uppgift.path`). Ingen ombyggnad av portalen krävs.

Det inbyggda `remotes.json` i `rimfrost-portal-bff` har bara platshållar-URL:er (`https://*.intern.example.com/...`) i `prodEntry` — dessa är inte riktiga URL:er. För att testa en ny remote mot en verklig `prodEntry` (t.ex. en port-forwardad tjänst i ett lokalt Kubernetes-kluster, se [portal/README.md](../portal/README.md)) utan att ändra det incheckade `remotes.json`, montera en override-fil och sätt `PORTAL_REMOTES_CONFIG_PATH` till dess sökväg — se `rimfrost-portal-bff`s egen README för detaljer.

## CORS

Micro-frontenden laddas cross-origin av portalens skal (olika ursprung/port), så webbservern som serverar de byggda filerna måste skicka `Access-Control-Allow-Origin` för att browsern ska tillåta att portalen hämtar `mf-manifest.json` och tillhörande JS-chunks. **Detta görs inte av micro-fe-templatets Apache-konfiguration idag** — den saknar CORS-headers helt. Håll koll på om templatet uppdaterats med detta innan du bygger en ny micro-frontend; annars behöver du lägga till motsvarande `Header set Access-Control-Allow-Origin`-direktiv själv.

## Testa

```bash
# Enhetstester (watch-läge)
npm test

# Med täckningsrapport
npm run test:coverage
```

Tester skrivs med Vitest och `@vue/test-utils`. Placera testfiler i `__tests__/`-kataloger bredvid koden de testar.
