## Relaterade repon

### rimfrost-portal-bff

Portalen kommunicerar med Portal BFF för uppgiftslistan och remote-registret. Se dess repo för konfiguration av `remotes.json` och mock-data.

### rimfrost-service-handlaggning

Backend-tjänsten som hanterar handläggares uppgifter. Portal BFF kommunicerar med denna för att hämta uppgiftslistan.

---

# Kom igång med portalen (rimfrost-portal-handlaggare)

NOTE: Se
- https://github.com/Forsakringskassan/rimfrost-portal-handlaggare

för det implementerade portalprojektet.

## Starta lokalt

```bash
npm install
npm run dev
```

Portalen körs på http://localhost:3030.

## Miljövariabler

| Variabel | Dev (`.env.development`) | Container (`runtime-config.js`) | Beskrivning |
|----------|--------------------------|----------------------------------|-------------|
| BFF-URL | `VITE_BFF_URL` | `RUNTIME_BFF_URL` | Bas-URL till Portal BFF |

Konfigurationen läses via `src/config/env.ts` — använd aldrig `import.meta.env` direkt i källkoden.

`runtime-config.js` sätter en global på `window`, t.ex. `window.__PORTAL_HANDLAGGARE_ENV__ = { RUNTIME_BFF_URL: "..." }`. **Denna global är namnrymd per app, inte en delad `window._env_`** — portalen och varje micro-frontend den laddar via Module Federation delar samma webbläsarfönster, så en gemensam global skulle göra att portalens och en remotes runtime-konfiguration skriver över varandra. Se motsvarande avsnitt i [micro-fe/README.md](../micro-fe/README.md).

## Köra mot ett Kubernetes-kluster

Portalen och Portal BFF kan köras i ett lokalt Kubernetes-kluster (Helm-chartet i [`rimfrost-kubernetes`](https://github.com/Forsakringskassan/rimfrost-kubernetes)) för att testa hela kedjan — portal, BFF:er och backend-tjänster — tillsammans. `port-forward.sh` i det repot exponerar varje tjänst på en fast `localhost`-port; portalen når då sin BFF via samma `RUNTIME_BFF_URL`-mekanism som beskrivs ovan, satt till BFF:ens port-forwardade port.

## Testa

```bash
# Enhetstester
npm test

# E2E-tester (headless)
npm run test:e2e

# E2E med UI
npm run test:e2e:ui
```

E2E-testerna kräver att portalens dev-server (port 3030) och en micro-frontend preview-server (port 3039) är startade. Playwright startar dessa automatiskt.

## Registrera en ny micro-frontend

Inga ändringar i det här repot krävs för att lägga till en ny micro-frontend. Allt hanteras via `remotes.json` i `rimfrost-portal-bff`.

Se [micro-fe/README.md](../micro-fe/README.md) för hur du skapar och registrerar en ny micro-frontend.
