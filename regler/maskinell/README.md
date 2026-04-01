# Översikt av rekommenderade tasks vid implementation av ny maskinell regel

NOTE: Se https://github.com/Forsakringskassan/rimfrost-regel-rtf-maskinell för ett exempel på implementerad regel.

## Skapa nytt repo baserat på template

Template för maskinell regel: https://github.com/Forsakringskassan/rimfrost-template-regel-maskinell

Replace på förekomster av _Template_ till regelns namn.<br>
TODOs i template-filerna ger tips om vad som behöver justeras.

## application.properties

https://github.com/Forsakringskassan/rimfrost-template-regel-maskinell/blob/main/src/main/resources/application.properties

Konfigurera de kafka-topics som regeln kopplas till (incoming/outgoing).
Konfiguration av container.image är optionellt och används endast för att kunna bygga docker-image lokalt.

## config.yaml

https://github.com/Forsakringskassan/rimfrost-template-regel-maskinell/blob/main/src/main/resources/config.yaml

YAML-fil för grundläggande konfiguration av regeln (beskrivning, roll, lagrum etc.).
Schema för YAML-filen: https://github.com/Forsakringskassan/rimfrost-framework-regel/blob/main/core/src/main/resources/schema/regel_schema.yaml

## Regel service implementation

https://github.com/Forsakringskassan/rimfrost-template-regel-maskinell/blob/main/src/main/java/se/fk/github/regelmaskinell/logic/RegelService.java

Implementera den abstrakta metoden processRegel som baserat på en uppläst handläggning skapar en uppdaterad handläggning
inklusive t.ex. producerade resultat samt ett utfall.

## Regel service test implementation

https://github.com/Forsakringskassan/rimfrost-template-regel-maskinell/blob/main/src/test/java/se/fk/github/regelmaskinell/RegelMaskinellTest.java

Implementera tester av metoden processRegel.<br>
Det är också möjligt att implementera service-tester genom att extenda bastest-klassen:<br>
https://github.com/Forsakringskassan/rimfrost-framework-regel-maskinell/blob/main/src/test/java/se/fk/rimfrost/framework/regel/maskinell/AbstractRegelMaskinellTest.java
