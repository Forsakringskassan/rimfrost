# Koppla in en regel

En kort guide för att bygga en ny micro-frontend (MFE) och dess BFF för en regel, och koppla ihop dem med resten av Rimfrost. Inga OpenShift- eller image-detaljer här — bara vad som behöver göras, och vilka värden som skiljer sig mellan miljöer.

1. [Kör Rimfrost lokalt](#1-kör-hela-rimfrost-lokalt)
2. [Ny regel](#2-skapa-en-ny-regel)
3. [Bygg MFE:n](#3-bygg-micro-frontenden-mfe)
4. [Bygg BFF:n](#4-bygg-bffn)
5. [Koppla ihop allt](#5-koppla-ihop-allt)
6. [Karta över värden](#6-karta-över-vad-som-ändras-per-miljö)

---

## 1. Kör hela Rimfrost lokalt

Ni får en fungerande kopia av hela kedjan — portal, alla BFF:er och backend-tjänster — utan att behöva förstå varje del i förväg. Allt styrs från repot `rimfrost-kubernetes`.

1. **Starta klustret** — `./deploy.sh` startar ett lokalt kluster och deployar hela applikationen: portal, samtliga BFF:er, backend-tjänster och micro-frontends.
2. **Öppna allt lokalt** — `./port-forward.sh` exponerar varje tjänst på en fast `localhost`-port. Körs automatiskt efter `deploy.sh`, men gå hit också om ni behöver återstarta enskilda forwards.
3. **Skörda testdata** — `./populate_oul.sh` skapar några riktiga ärenden i uppgiftslagret, så det finns något att öppna i portalen direkt.
4. **Logga in** — öppna portalen på sin port-forwardade adress, logga in som en handläggare och verifiera att en uppgift går att öppna.

## 2. Skapa en ny regel

Det här steget förutsätter vi att ni redan kan — regelmotorn, kontraktet mot handläggningstjänsten och själva regellogiken täcks inte här. Nästa avsnitt börjar där regeln redan finns och har ett `uppgift.path` som pekar ut den nya MFE:n.

## 3. Bygg micro-frontend:en (MFE)

Utgå alltid från templatet — det innehåller redan rätt uppsättning för Module Federation och runtime-konfiguration.

1. **Klona templatet** — utgå från `rimfrost-template-micro-fe`. Byt ut alla förekomster av `template` mot regelns namn — TODO-kommentarerna i templatet pekar ut var.
2. **Ställ in Module Federation** — i `vite.config.ts`: ge appen ett unikt `name` (blir `scope` i registreringen) och exponera huvudkomponenten under `exposes`.
3. **Bygg komponenten** — den tar emot `handlaggningId` (och ev. `regeltyp`) som props från portalen. Inget mer behövs för att bli inladdad.
4. **Prata med sin egen BFF** — tre standardendpoints täcker det mesta:

   ```
   GET   /api/task       → läs data
   PATCH /api/task       → uppdatera data
   POST  /api/task/done  → markera klar
   ```

5. **Signalera när uppgiften är klar:**

   ```js
   window.dispatchEvent(new CustomEvent("task-done", {
     detail: { handlaggningId, success: true, message: "Uppgift slutförd" },
   }));
   ```

> ❄️ **Vanlig fallgrop: index.html laddas aldrig**
>
> Portalen laddar er MFE som en Module Federation-remote — då hämtar webbläsaren *bara* er JS-chunk, aldrig er egen `index.html`. En `<script>`-tagg där som ska sätta konfiguration på `window` körs alltså aldrig i det läget.
>
> Templatets `env.ts` löser det redan åt er: appen hämtar sin egen `runtime-config.js` själv, från sitt eget ursprung. Behåll det mönstret — skriv inte om det till att förlita sig på att någon annan sida redan laddat filen.

## 4. Bygg BFF:n

Utgå från `rimfrost-template-micro-fe-bff`. Den generiska varianten täcker det mesta — den vidarebefordrar bara anrop till regelns egen backend-tjänst.

| Miljövariabel | Styr |
|---|---|
| `PORT` | Vilken port BFF:en lyssnar på |
| `BE_URL` | Bas-URL till regelns egen backend-tjänst |
| `BE_RULE_PATH` | Sökvägen till regeln på den tjänsten |
| `CORS_ORIGINS` | Vilka origins som får anropa BFF:en — se fallgropen nedan |

## 5. Koppla ihop allt

De sista stegen gör att portalen faktiskt hittar och kan ladda den nya MFE:n.

1. **Registrera i `remotes.json`** — lägg till en post i `remotes.json` i `rimfrost-portal-bff`:

   ```json
   {
     "routes": {
       "din-regel": {
         "scope": "dinRegelApp",
         "module": "DinKomponent",
         "devEntry": "http://localhost:PORT/mf-manifest.json",
         "prodEntry": "https://din-regel.er-egen-miljo/mf-manifest.json"
       }
     }
   }
   ```

   Portalen väljer rätt MFE baserat på uppgiftens `url`-fält, som sätts av regelns backend. Ingen ombyggnad av portalen krävs.

2. **Ge MFE:n sin `RUNTIME_BFF_URL`** — precis som övriga appar läser MFE:n sin BFF-adress från en monterad `runtime-config.js`. Sätt den till er BFF:s faktiska adress i er miljö.
3. **Sätt `CORS_ORIGINS` på BFF:en** — måste tillåta skal-appens origin, se fallgropen nedan.
4. **Testa hela kedjan** — öppna portalen, verifiera att uppgiftslistan laddas och att den nya MFE:n renderas utan CORS-fel i webbläsarkonsolen.

> ❄️ **Vanlig fallgrop: rätt origin i CORS_ORIGINS**
>
> Webbläsarens `Origin`-header på ett anrop från er MFE speglar alltid **sidan som är öppen** (portalens origin) — aldrig var koden som gjorde anropet råkade laddas ifrån. Er BFF:s `CORS_ORIGINS` måste alltså innehålla portalens origin, inte bara MFE:ns egen adress. Missas det blockeras anropen tyst av webbläsaren även om de når helt rätt BFF.

## 6. Karta över vad som ändras per miljö

Samma värden som i vårt lokala kluster, fast pekande på er egen miljös adresser.

| Var | Värde | Vårt lokala kluster | Er miljö |
|---|---|---|---|
| MFE | `RUNTIME_BFF_URL` | `http://localhost:9002` | **URL till er BFF** |
| Regel-BFF | `CORS_ORIGINS` | `…,localhost:8894` | **Portalens origin + ev. MFE:ns eget** |
| Regel-BFF | `BE_URL` / `BE_RULE_PATH` | Intern URL till regelns backend | **Motsvarande URL i er miljö** |
| portal-bff | `remotes.json` → `prodEntry` | Port-forwardad URL per MFE | **Er MFE:s riktiga `mf-manifest.json`-URL** |
| portal-bff | `PORTAL_REMOTES_CONFIG_PATH` | Sökväg till monterad `remotes.json` | **Motsvarande sökväg i er miljö** |

---

Mer detaljer finns i [deployment/README.md](README.md) och [micro-fe/README.md](../micro-fe/README.md) — den här guiden är den förenklade versionen.
