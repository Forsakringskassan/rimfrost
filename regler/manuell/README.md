# Översikt av rekommenderade tasks vid implementation av ny manuell regel

NOTE: Se 
- https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell 
- https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-openapi
- för exempel på implementerad regel och dess API

## Skapa nytt repo för regelns Rest-API

Specificera det API som portalen använder (GET/PATCH) för att hämta/uppdatera information om den operativa uppgiften.
Skapa en openapi.yaml i det nya repot för api't.

## Skapa nytt repo baserat på template

Template för maskinell regel: https://github.com/Forsakringskassan/rimfrost-template-regel-manuell

Replace på förekomster av _Template_ till regelns namn.<br>
TODOs i template-filerna ger tips om vad som behöver justeras.

## application.properties

https://github.com/Forsakringskassan/rimfrost-template-regel-manuell/blob/main/src/main/resources/application.properties

Konfiguration av container.image är optionellt och används endast för att kunna bygga docker-image lokalt.

### Konfigurera de kafka-topics som regeln kopplas till (incoming/outgoing).

De topics som manuella regler behöver konfigurera är:

| Topic | Beskrivning |
|------|------------|
| mp.messaging.incoming.regel-requests.topic | Regeln konsumerar denna topic för initiering av ny regel-exekvering |
| mp.messaging.outgoing.regel-responses.topic | Regeln producerar resultat av genomförd regel-exekvering på denna topic |
| mp.messaging.outgoing.operativt-uppgiftslager-requests.topic | Regeln skapar ny uppgift i Operativt uppgiftslager genom att skicka request på denna topic |
| mp.messaging.incoming.operativt-uppgiftslager-responses.topic | Regeln konsumerar svar från Operativt uppgiftslager på skapande av ny uppgift |
| mp.messaging.incoming.operativt-uppgiftslager-status-notification.topic | Regeln lyssnar på denna topic för uppdaterad information om uppgiftsstatus från Operativt uppgiftslager |
| mp.messaging.outgoing.operativt-uppgiftslager-status-control.topic | Regeln skickar information om uppdaterad status för uppgift på denna topic (till Operativt uppgiftslager) |


## config.yaml

https://github.com/Forsakringskassan/rimfrost-template-regel-manuell/blob/main/src/main/resources/config.yaml

YAML-fil för grundläggande konfiguration av regeln (beskrivning, roll, lagrum etc.).
Schema för YAML-filen: https://github.com/Forsakringskassan/rimfrost-framework-regel/blob/main/core/src/main/resources/schema/regel_schema.yaml

## Regel service implementation

https://github.com/Forsakringskassan/rimfrost-template-regel-manuell/blob/main/src/main/java/se/fk/github/regel/template/logic/_Template_Service.java

Implementera metoderna _readData_, _updateData_ och _done_ enligt interface _RegelManuellServiceInterface_.<br>
De här metoderna används av ramverket för att producera ett rest-API som portalen använder i ett micro-frontend.

### readData

Generar den micro-frontend som presenteras för handläggaren vid vald uppgift.

### update Data

Hanterar handläggarens uppdatering av information om uppgiften.

### done

Implementerar de åtgärder som regeln behöver utföra när handläggaren är klar med uppgiften, t.ex. ev. uppstädning av data.
Regeln förväntas även anropa ramverksmetoden _sendRegelResponse_ för att trigga skickande av regel-respons som indikation 
på att regeln är avslutad.

## Regel service test implementation

https://github.com/Forsakringskassan/rimfrost-template-regel-manuell/blob/main/src/test/java/se/fk/github/regel/template/RegelTemplateTest.java

Implementera tester av metoderna _readData_, _updateData_ och _done_.<br>

Det är också möjligt att implementera service-tester genom att extenda bastest-klassen:<br>
https://github.com/Forsakringskassan/rimfrost-framework-regel/blob/main/test-base/src/main/java/se/fk/rimfrost/framework/regel/test/RegelTest.java