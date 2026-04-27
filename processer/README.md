# Översikt av rekommenderade steg vid implementation av ny process

NOTE: Se 
- https://github.com/Forsakringskassan/rimfrost-process-vah
- för exempel på implementerad process

## Skapa nytt repo baserat på template

Template för process: https://github.com/Forsakringskassan/rimfrost-template-process

Replace på förekomster av _Template_ till processens namn.<br>
TODOs i template-filerna ger tips om vad som behöver justeras.

## application.properties

https://github.com/Forsakringskassan/rimfrost-template-process/blob/main/src/main/resources/application.properties

Konfiguration av container.image är optionellt och används endast för att kunna bygga docker-image lokalt.

### Konfigurera de kafka-topics som processen kopplas till (incoming/outgoing).

mp.messaging.incoming.<DIN_INCOMING_KAFKA_TOPIC>.connector=smallrye-kafka
mp.messaging.incoming.<DIN_INCOMING_KAFKA_TOPIC>.auto.offset.reset=earliest
mp.messaging.incoming.<DIN_INCOMING_KAFKA_TOPIC>.value.deserializer=org.apache.kafka.common.serialization.StringDeserializer

mp.messaging.outgoing.<DIN_OUTGOING_KAFKA_TOPIC>.connector=smallrye-kafka
mp.messaging.outgoing.<DIN_OUTGOING_KAFKA_TOPIC>.value.serializer=org.apache.kafka.common.serialization.StringSerializer

## Konfigurera pom.xml med att lägga till dependency till de regler du vill använda

I pom.xml finns en TODO under <dependencies></dependencies> för att lägga till ett eller flera dependency till de regler du vill kalla på i processen.

## Konfigurera template.bpmn

https://github.com/Forsakringskassan/rimfrost-template-process/blob/main/src/main/resources/template.bpmn

Rekommenderar att man använder sig av VS code plugin Apache KIE Kogito Bundle för att hantera .bpmn filer.
Denna fil representerar hela processen.

Konfiguration som krävs:
1. Byt namn på template.bpmn till något som har med processen att göra.
2. Start Message eventets Implementation/Execution Message måste match <DIN_INCOMING_KAFKA_TOPIC> i application.properties
3. End Message eventets Implementation/Execution Message måste match <DIN_OUTGOING_KAFKA_TOPIC> i application.properties
4. Uppdatera submodulen som just nu heter "TODO: Lägg till Regler" med att ändra dess namn och Implementation/Exectutions Called Element med ett subprocess id från en importerad dependency (se en README.md under /regler för att förstå var en subprocess Id specificeras).

## Process test implementation

https://github.com/Forsakringskassan/rimfrost-template-process/blob/main/src/test/java/se/fk/github/rimfrost/process/template/TemplateContainerSmokeIT.java

