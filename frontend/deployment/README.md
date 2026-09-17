## Relaterade repon

### rimfrost-kubernetes

Helm-chartet som paketerar och kör alla FE- och BFF-images tillsammans i ett Kubernetes-kluster (minikube lokalt). Det här dokumentet beskriver vad som finns i det chartet, ur ett deployment-perspektiv: https://github.com/Forsakringskassan/rimfrost-kubernetes

### rimfrost-portal-bff, rimfrost-portal-handlaggare

Skal-appen och dess BFF, se [portal/README.md](../portal/README.md).

### rimfrost-template-micro-fe, rimfrost-template-micro-fe-bff

Templates för micro-frontends och deras BFF:er, se [micro-fe/README.md](../micro-fe/README.md) och [bff/README.md](../bff/README.md).

---

# Deployment av FE och BFF till Kubernetes/OpenShift

Det här dokumentet beskriver hur de färdigbyggda FE- och BFF-images hänger ihop när de körs tillsammans, och vad som krävs för att köra dem i ett kluster — oavsett om det är vårt eget minikube-baserade Kubernetes-kluster eller ett annat teams OpenShift-miljö. Syftet är att ett team som bara har tillgång till de publicerade images (inte källkoden) ska kunna sätta upp en fungerande kopia av miljön.

Source of truth för allt nedan är Helm-chartet i `rimfrost-kubernetes` (`helm-chart/values.yaml`, `helm-chart/templates/apache.yaml`, `helm-chart/templates/quarkus.yaml`).

## 1. Imageöversikt

Alla images publiceras till `ghcr.io/forsakringskassan/*`.

**BFF:er** — Quarkus-appar, en per micro-frontend/portal. Varje BFF lyssnar på en egen port, satt via `quarkus.http.port` i appens `application.properties`. Quarkus/MicroProfile Config tillåter att detta override:as via miljövariabeln `QUARKUS_HTTP_PORT` utan kodändring — det är alltså tekniskt konfigurerbart, men ingen av apparna har idag en ConfigMap/env för det, så i praktiken är porten fixed per image tills vidare:

| BFF | Image | Tag | Port |
|-----|-------|-----|------|
| portal-bff | rimfrost-portal-bff | 2.1.2 | 9001 |
| portal-admin-bff | rimfrost-portal-admin-bff | 0.0.3 | 9091 |
| rtf-manuell-bff | rimfrost-regel-rtf-manuell-bff | 0.0.1 | 9002 |
| bekraftabeslut-bff | rimfrost-regel-bekraftabeslut-bff | 0.0.2 | 9003 |
| template-micro-fe-bff | rimfrost-template-micro-fe-bff | 0.0.2 | 9009 |

**Frontend-appar** — statiska byggen serverade via Apache httpd i imagen, alltid port 8080. Till skillnad från BFF:erna kommer porten här från httpd:s `Listen`-direktiv i baseimagen, inte från MicroProfile Config — att göra den konfigurerbar skulle kräva en egen mekanism (t.ex. en entrypoint som skriver om en monterad config-fil från en miljövariabel vid containerstart, likt CORS-workaround i avsnitt 3), inget som finns inbyggt idag:

| Frontend | Image | Tag | Roll |
|----------|-------|-----|------|
| portal-handlaggare | rimfrost-portal-handlaggare | 0.3.1 | Skal-app (host) |
| portal-admin-fe | rimfrost-portal-admin-fe | 0.0.2 | Fristående app |
| rtf-manuell-fe | rimfrost-regel-rtf-manuell-fe | 0.0.3 | Remote (Module Federation) |
| bekraftabeslut-fe | rimfrost-regel-bekraftabeslut-fe | 0.0.3 | Remote (Module Federation) |
| template-micro-fe | rimfrost-template-micro-fe | snapshot | Remote (Module Federation) |

Tabellerna speglar de tags som `rimfrost-kubernetes` pekar på just nu — kontrollera `values.yaml` för aktuella siffror innan ni sätter upp miljön, de uppdateras löpande.

## 2. Hur allt hänger ihop

```
                         ┌─────────────────────┐
                         │ portal-handlaggare   │  (skal-app, browsern)
                         │ (FE, Apache, :8080)  │
                         └─────────┬────────────┘
                                   │ eget API-anrop
                                   ▼
                         ┌─────────────────────┐
                         │ portal-bff (:9001)   │──▶ backend (uppgiftslager m.fl.)
                         └─────────────────────┘

     portal-handlaggare laddar även, direkt i webbläsaren via
     Module Federation, en eller flera remote-appar:

┌────────────────────┐   ┌────────────────────────┐   ┌───────────────────────┐
│ rtf-manuell-fe      │   │ bekraftabeslut-fe        │   │ template-micro-fe      │
│ (FE, Apache, :8080) │   │ (FE, Apache, :8080)      │   │ (FE, Apache, :8080)    │
└─────────┬───────────┘   └────────────┬─────────────┘   └───────────┬────────────┘
          ▼                            ▼                             ▼
┌────────────────────┐   ┌────────────────────────┐   ┌───────────────────────┐
│ rtf-manuell-bff      │   │ bekraftabeslut-bff       │   │ template-micro-fe-bff  │
│ (:9002)              │   │ (:9003)                  │   │ (:9009)                │
└──────────┬───────────┘   └────────────┬─────────────┘   └───────────┬────────────┘
           ▼                            ▼                             ▼
     respektive regels backend-service (Quarkus)

┌────────────────────┐   ┌────────────────────────┐
│ portal-admin-fe      │──▶│ portal-admin-bff         │──▶ uppgiftslager (OUL)
│ (FE, Apache, :8080) │   │ (:9091)                  │
└────────────────────┘   └────────────────────────┘
```

Viktigt att ta med sig: **varje remote-app pratar med sin egen dedikerade BFF**, inte via portal-bff. portal-bff är bara skal-appens egen BFF (uppgiftslista m.m.) plus källan för `remotes.json` (se avsnitt 4).

## 3. CORS — den viktigaste fallgropen

De publicerade FE-images (Apache) skickar **inga CORS-headers alls**, och har ingen inbyggd mekanism för att lägga till dem via config. Det är ett problem eftersom skal-appen laddar remote-apparnas `mf-manifest.json` och JS-chunks cross-origin (olika port/host).

`rimfrost-kubernetes` löser det idag med en tillfällig workaround i Helm-chartet (`cors: true` på varje FE i `values.yaml`): containerns startkommando skrivs om så att Apache startar med en extra `Include`-rad som pekar på en monterad `cors.conf` med:

```apache
Header always set Access-Control-Allow-Origin "*"
Header always set Access-Control-Allow-Methods "GET, OPTIONS"
```

Detta är uttryckligen dokumenterat som en interimslösning (se kommentar i `templates/apache.yaml`, FKPOC-1041) i väntan på att varje FE-repo får en riktig Apache-config med CORS inbyggt i imagen. **Det andra teamet kommer att behöva samma typ av workaround** (eller vänta på att den riktiga fixen är på plats) — annars blir det tomma/trasiga remote-appar i skal-appen med CORS-fel i webbläsarkonsolen.

På BFF-sidan har varje Quarkus-app en `CORS_ORIGINS`-miljövariabel som styr `quarkus.http.cors.origins`. Den måste sättas explicit till de origins som skal-appen och remote-apparna faktiskt körs på i det nya klustret — annars blir BFF-anropen blockerade av webbläsaren.

**Vanlig fallgrop:** en remote-apps egen BFF måste tillåta **skal-appens** origin, inte bara remote-appens egen port. Webbläsarens `Origin`-header speglar alltid sidan som faktiskt är öppen (skal-appen), oavsett varifrån det anropande skriptet lästes in via Module Federation. Exempel från det lokala minikube-klustret: `rtf-manuell-bff`s `CORS_ORIGINS` behövde både `http://localhost:8896` (rtf-manuell-fe:s eget, för fristående/preview-läge) **och** `http://localhost:8894` (portal-handlaggare, skal-appen) — utan den senare blockerar webbläsaren anropet trots att request:en når helt rätt BFF.

**Cookie-baserad auth:** Om det andra teamet kör sin egen cookie-/sessionsbaserade autentisering behöver webbläsaren skicka cookies cross-origin till BFF:en (FE och BFF ligger på olika origins/portar i den här arkitekturen). Det kräver att BFF:en svarar med `Access-Control-Allow-Credentials: true` — annars blockeras de credentialed anropen tyst, oavsett vad som är konfigurerat på deras sida. `portal-bff`, `portal-admin-bff` och `template-micro-fe-bff` har därför en `CORS_ALLOW_CREDENTIALS`-miljövariabel som styr `quarkus.http.cors.access-control-allow-credentials` (default `false`, opt-in per miljö). Observera att detta **inte** kan kombineras med wildcard-origin — det gäller bara BFF-CORS:en (explicit origin-lista) och påverkar inte Apache-wildcarden för de statiska FE-assetsen ovan.

## 4. Runtime-konfiguration för frontend-apparna

FE-apparna bygger inte in BFF-URL:en vid build-time. Varje app läser istället ett globalt objekt på `window`, injicerat via en fil som Apache serverar:

- Filen monteras som `runtime-config.js` på `/usr/local/apache2/htdocs/runtime-config.js`, via en ConfigMap.
- Den sätter `window.__<APPNAMN>_ENV__ = { RUNTIME_BFF_URL: "..." }`.
- Namnet är **namnrymt per app** (t.ex. `__PORTAL_HANDLAGGARE_ENV__`), inte en delad `window._env_`. Skal-appen och varje remote den laddar delar samma webbläsarfönster via Module Federation — en delad global skulle göra att apparnas runtime-konfiguration skriver över varandra.

Det andra teamet behöver alltså skapa motsvarande config-injektion (ConfigMap + volume mount i OpenShift, eller motsvarande) för varje FE-app, med rätt `RUNTIME_BFF_URL` för respektive BFF i sitt eget kluster. Detta gäller oavsett app-typ (skal-app eller remote) — se dock nästa stycke för en viktig skillnad i **hur** varje app-typ faktiskt läser filen. Se även [portal/README.md](../portal/README.md) och [micro-fe/README.md](../micro-fe/README.md) för hur apparna läser detta i sin egen kod (`src/config/env.ts`).

**Skal-appar vs remote-appar — en fallgrop:** Ovanstående (`<script src="/runtime-config.js">` i `index.html`) stämmer rakt av för skal-appar som webbläsaren navigerar till direkt (`portal-handlaggare`, `portal-admin-fe`) — deras egen `index.html` laddas alltid, så scripttaggen körs garanterat innan appens kod läser `window.__APPNAMN_ENV__`.

Det stämmer **inte** för remote-appar (`rtf-manuell-fe`, `bekraftabeslut-fe`, `template-micro-fe` och framtida micro-frontends): webbläsaren laddar bara deras JS-chunk via Module Federation, aldrig deras `index.html` — scripttaggen körs alltså aldrig, och `window.__APPNAMN_ENV__` blir `undefined` oavsett vad ConfigMap:en innehåller. Koden faller då tyst tillbaka på en tom `bffUrl`, vilket gör att `fetch()`-anrop blir relativa och går mot **skal-appens** origin istället för remote-appens egen BFF (404 mot fel host, utan CORS-fel eftersom det tekniskt sett är ett giltigt anrop — bara till fel adress).

Åtgärdat i `rtf-manuell-fe`, `bekraftabeslut-fe` och `template-micro-fe`: deras `env.ts` läser numera `runtime-config.js` själv, via ett `fetch`/script-anrop mot sitt eget ursprung (`new URL(import.meta.url).origin`, som alltid pekar på appens egen deployade URL oavsett vilken shell som laddat den) — istället för att förlita sig på att någon annans `index.html` redan laddat den. ConfigMap/volume-mount-uppsättningen (ovan) är oförändrad; det är bara **hur** filen hämtas som skiljer sig. Kontrollera att ni kör en FE-version med denna fix innan ni felsöker ett liknande symptom (komponent laddas, men API-anrop 404:ar mot skal-appens egen adress).

## 5. Module Federation: `remotes.json`

portal-bff levereras med en inbyggd `remotes.json` där varje remote-app pekas ut, men med platshållar-URL:er (`cdn.example.com`) i `prodEntry` — dessa fungerar inte som de är.

`rimfrost-kubernetes` skriver över den med en egen `remotes.json`, monterad på `/deployments/config/remotes.json` och pekas ut via miljövariabeln `PORTAL_REMOTES_CONFIG_PATH`. Filen innehåller, per remote, `scope`, `module`, `devEntry` och `prodEntry` (URL till appens `mf-manifest.json`).

Det andra teamet behöver göra samma sak: montera en egen `remotes.json` i portal-bff-containern med `prodEntry`-URL:er som pekar på var respektive remote-app faktiskt nås i deras kluster (t.ex. en OpenShift Route), och sätta `PORTAL_REMOTES_CONFIG_PATH` därefter.

## 6. Miljövariabler per BFF

| BFF | Miljövariabler | Beskrivning |
|-----|-----------------|-------------|
| portal-bff | `BE_OUL_URL`, `CORS_ORIGINS`, `CORS_ALLOW_CREDENTIALS`, `PORTAL_REMOTES_CONFIG_PATH` | URL till uppgiftslager (OUL), tillåtna CORS-origins, om cookies ska tillåtas cross-origin, sökväg till egen `remotes.json` |
| portal-admin-bff | `BE_OUL_URL`, `BE_OUL_MANAGEMENT_URL`, `CORS_ORIGINS`, `CORS_ALLOW_CREDENTIALS` | URL:er till uppgiftslagrets vanliga och admin-API |
| rtf-manuell-bff | `BE_RTF_MANUELL_URL`, `CORS_ORIGINS` | URL till rtf-manuell-regelns backend |
| bekraftabeslut-bff | `BE_BEKRAFTABESLUT_URL`, `CORS_ORIGINS` | URL till bekräfta-beslut-regelns backend |
| template-micro-fe-bff | `BACKEND_URL`, `CORS_ORIGINS`, `CORS_ALLOW_CREDENTIALS` | URL till regelns backend (generisk template-variant) |

`CORS_ALLOW_CREDENTIALS` defaultar till `false` och behöver bara sättas till `true` om det andra teamets frontend faktiskt skickar cookies i anropen till BFF:en. `rtf-manuell-bff` och `bekraftabeslut-bff` har inte fått denna miljövariabel ännu — samma ändring behöver göras där om de ska stödja cookie-baserad auth.

Se även [bff/README.md](../bff/README.md) för de generella miljövariabler (`PORT`, `BE_URL`, `BE_RULE_PATH`) som gäller för BFF:er byggda på `rimfrost-template-micro-fe-bff`.

## 7. Vad som är lokal-dev-specifikt — kopiera inte rakt av

Chartet i `rimfrost-kubernetes` är byggt för lokal utveckling mot minikube, och innehåller därför en del värden som **inte** ska återanvändas rakt av i en annan miljö:

- `CORS_ORIGINS` är satta till `http://localhost:3000`, `:3030` och portarna som `port-forward.sh` exponerar (t.ex. `:8894`–`:8898`). Dessa är minikube-teamets egna lokala tunnlar, inte riktiga hostnamn.
- `RUNTIME_BFF_URL` och `remotes.json`-URL:erna pekar likaså på `localhost:<port-forward-port>`.
- Ingress-annoteringarna i `values.yaml` (`nginx.ingress.kubernetes.io/...`) gäller ingress-nginx och har ingen direkt motsvarighet i OpenShift — där används Routes istället för Ingress.

Det andra teamet ska **ersätta dessa lokala värden med sina egna Route/Service-hostnamn**, inte kopiera in `localhost`-adresserna i sin OpenShift-miljö.

## 8. Checklista för att sätta upp miljön på nytt

1. Säkerställ åtkomst till `ghcr.io/forsakringskassan/*` (eller sätt upp ett image pull secret).
2. Deploya varje backend-service (uppgiftslager, rtf-manuell, bekraftabeslut, …) och notera deras interna URL:er.
3. Deploya varje BFF med rätt `BE_*_URL`/`BACKEND_URL` mot steg 2, och en preliminär `CORS_ORIGINS` (kan uppdateras i steg 6).
4. Deploya varje FE-app (Apache-image) med CORS-workaround (avsnitt 3) tills en riktig fix finns i respektive repo.
5. Sätt upp `runtime-config.js`-injektion per FE-app med rätt `RUNTIME_BFF_URL` mot steg 3 (avsnitt 4).
6. Uppdatera varje BFF:s `CORS_ORIGINS` till de faktiska URL:er FE-apparna nås på i klustret (Routes).
7. Sätt upp portal-bffs `remotes.json` med riktiga `prodEntry`-URL:er för varje remote-app, och peka `PORTAL_REMOTES_CONFIG_PATH` på den (avsnitt 5).
8. Testa hela kedjan: öppna portal-handlaggare, verifiera att uppgiftslistan laddas (portal-bff) och att minst en remote-app laddas via Module Federation utan CORS-fel i webbläsarkonsolen.
