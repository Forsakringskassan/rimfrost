# Rimfrost release rimfrost-1_2: bill of materials

This lists every `Forsakringskassan/rimfrost-*` repository that has the git tag `rimfrost-1_2`, with the artifacts it publishes and their versions.

- **Repository** links to the repository at the `rimfrost-1_2` tag.
- **Version** links to the release tag. The poms in the repositories declare `*-SNAPSHOT`, so the version is the release tag that `rimfrost-1_2` points at. Between that release tag and `rimfrost-1_2` the only change is a changelog update.
- `rimfrost-framework-regel-oul-asyncapi` has no `0.0.2` git tag, so its version comes from `gradle.properties`.

72 repositories publish Maven artifacts, and 7 more are tagged but publish none.

## Using the BOM

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>se.fk.rimfrost</groupId>
      <artifactId>rimfrost-bom</artifactId>
      <version>1.2</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

## Framework

| Repository | Artifacts (groupId:artifactId) | Version | Build |
|---|---|---|---|
| [rimfrost-framework-bff](https://github.com/Forsakringskassan/rimfrost-framework-bff/tree/rimfrost-1_2) | `se.fk.rimfrost.framework.bff:rimfrost-framework-bff`<br>`se.fk.rimfrost.framework.bff:rimfrost-framework-bff` (classifier `tests`) | [0.0.1](https://github.com/Forsakringskassan/rimfrost-framework-bff/tree/0.0.1) | Maven |
| [rimfrost-framework-erbjudande-topic-adapter](https://github.com/Forsakringskassan/rimfrost-framework-erbjudande-topic-adapter/tree/rimfrost-1_2) | `se.fk.rimfrost.framework.erbjudande.topic:rimfrost-framework-erbjudande-topic-adapter` | [0.0.2](https://github.com/Forsakringskassan/rimfrost-framework-erbjudande-topic-adapter/tree/0.0.2) | Maven |
| [rimfrost-framework-handlaggning-adapter](https://github.com/Forsakringskassan/rimfrost-framework-handlaggning-adapter/tree/rimfrost-1_2) | `se.fk.rimfrost.framework.handlaggning:rimfrost-framework-handlaggning-adapter` | [1.2.4](https://github.com/Forsakringskassan/rimfrost-framework-handlaggning-adapter/tree/1.2.4) | Maven |
| [rimfrost-framework-oul-adapter](https://github.com/Forsakringskassan/rimfrost-framework-oul-adapter/tree/rimfrost-1_2) | `se.fk.rimfrost.framework.oul:rimfrost-framework-oul-adapter` | [1.1.5](https://github.com/Forsakringskassan/rimfrost-framework-oul-adapter/tree/1.1.5) | Maven |
| [rimfrost-framework-oul](https://github.com/Forsakringskassan/rimfrost-framework-oul/tree/rimfrost-1_2) | `se.fk.rimfrost.framework.oul:rimfrost-framework-oul` | [1.1.3](https://github.com/Forsakringskassan/rimfrost-framework-oul/tree/1.1.3) | Maven |
| [rimfrost-framework-process](https://github.com/Forsakringskassan/rimfrost-framework-process/tree/rimfrost-1_2) | `se.fk.rimfrost.framework.process:rimfrost-framework-process` | [1.6.4](https://github.com/Forsakringskassan/rimfrost-framework-process/tree/1.6.4) | Maven |
| [rimfrost-framework-referensdata-interface](https://github.com/Forsakringskassan/rimfrost-framework-referensdata-interface/tree/rimfrost-1_2) | `se.fk.rimfrost.framework.referensdata:rimfrost-framework-referensdata-interface` | [0.0.1](https://github.com/Forsakringskassan/rimfrost-framework-referensdata-interface/tree/0.0.1) | Maven |
| [rimfrost-framework-regel-asyncapi](https://github.com/Forsakringskassan/rimfrost-framework-regel-asyncapi/tree/rimfrost-1_2) | `se.fk.rimfrost.framework.regel:rimfrost-framework-regel-asyncapi` | [1.1.4](https://github.com/Forsakringskassan/rimfrost-framework-regel-asyncapi/tree/1.1.4) | Maven |
| [rimfrost-framework-regel-error-codes](https://github.com/Forsakringskassan/rimfrost-framework-regel-error-codes/tree/rimfrost-1_2) | `se.fk.rimfrost.framework.regel.error:rimfrost-framework-regel-error-codes` | [1.1.1](https://github.com/Forsakringskassan/rimfrost-framework-regel-error-codes/tree/1.1.1) | Maven |
| [rimfrost-framework-regel-komplettering](https://github.com/Forsakringskassan/rimfrost-framework-regel-komplettering/tree/rimfrost-1_2) | `se.fk.rimfrost.framework.regel.komplettering:rimfrost-framework-regel-komplettering`<br>`se.fk.rimfrost.framework.regel.komplettering:rimfrost-framework-regel-komplettering` (classifier `tests`) | [0.1.1](https://github.com/Forsakringskassan/rimfrost-framework-regel-komplettering/tree/0.1.1) | Maven |
| [rimfrost-framework-regel-manuell-openapi](https://github.com/Forsakringskassan/rimfrost-framework-regel-manuell-openapi/tree/rimfrost-1_2) | `se.fk.rimfrost.framework.regel.manuell:rimfrost-framework-regel-manuell-openapi-jaxrs-spec`<br>`se.fk.rimfrost.framework.regel.manuell:rimfrost-framework-regel-manuell-openapi-spec` | [1.0.1](https://github.com/Forsakringskassan/rimfrost-framework-regel-manuell-openapi/tree/1.0.1) | Gradle |
| [rimfrost-framework-regel-manuell](https://github.com/Forsakringskassan/rimfrost-framework-regel-manuell/tree/rimfrost-1_2) | `se.fk.rimfrost.framework.regel.manuell:rimfrost-framework-regel-manuell`<br>`se.fk.rimfrost.framework.regel.manuell:rimfrost-framework-regel-manuell` (classifier `tests`) | [1.4.5](https://github.com/Forsakringskassan/rimfrost-framework-regel-manuell/tree/1.4.5) | Maven |
| [rimfrost-framework-regel-maskinell](https://github.com/Forsakringskassan/rimfrost-framework-regel-maskinell/tree/rimfrost-1_2) | `se.fk.rimfrost.framework.regel.maskinell:rimfrost-framework-regel-maskinell`<br>`se.fk.rimfrost.framework.regel.maskinell:rimfrost-framework-regel-maskinell` (classifier `tests`) | [1.1.8](https://github.com/Forsakringskassan/rimfrost-framework-regel-maskinell/tree/1.1.8) | Maven |
| [rimfrost-framework-regel-oul-asyncapi](https://github.com/Forsakringskassan/rimfrost-framework-regel-oul-asyncapi/tree/rimfrost-1_2) | `se.fk.rimfrost.framework.regel.oul:rimfrost-framework-regel-oul-asyncapi` | 0.0.2 | Gradle |
| [rimfrost-framework-regel-oul-openapi](https://github.com/Forsakringskassan/rimfrost-framework-regel-oul-openapi/tree/rimfrost-1_2) | `se.fk.rimfrost.framework.regel.oul:rimfrost-framework-regel-oul-openapi-jaxrs-spec`<br>`se.fk.rimfrost.framework.regel.oul:rimfrost-framework-regel-oul-openapi-spec` | [0.0.3](https://github.com/Forsakringskassan/rimfrost-framework-regel-oul-openapi/tree/0.0.3) | Gradle |
| [rimfrost-framework-regel-oul](https://github.com/Forsakringskassan/rimfrost-framework-regel-oul/tree/rimfrost-1_2) | `se.fk.rimfrost.framework.regel.oul:rimfrost-framework-regel-oul`<br>`se.fk.rimfrost.framework.regel.oul:rimfrost-framework-regel-oul` (classifier `tests`) | [0.1.2](https://github.com/Forsakringskassan/rimfrost-framework-regel-oul/tree/0.1.2) | Maven |
| [rimfrost-framework-regel](https://github.com/Forsakringskassan/rimfrost-framework-regel/tree/rimfrost-1_2) | `se.fk.rimfrost.framework.regel:rimfrost-framework-regel`<br>`se.fk.rimfrost.framework.regel:rimfrost-framework-regel` (classifier `tests`) | [1.4.4](https://github.com/Forsakringskassan/rimfrost-framework-regel/tree/1.4.4) | Maven |
| [rimfrost-framework-sid-adapter](https://github.com/Forsakringskassan/rimfrost-framework-sid-adapter/tree/rimfrost-1_2) | `se.fk.rimfrost.framework.sid:rimfrost-framework-sid-adapter` | [0.0.1](https://github.com/Forsakringskassan/rimfrost-framework-sid-adapter/tree/0.0.1) | Maven |

## Adapters

| Repository | Artifacts (groupId:artifactId) | Version | Build |
|---|---|---|---|
| [rimfrost-adapter-arbetsgivare](https://github.com/Forsakringskassan/rimfrost-adapter-arbetsgivare/tree/rimfrost-1_2) | `se.fk.rimfrost.adapter.arbetsgivare:rimfrost-adapter-arbetsgivare` | [1.1.4](https://github.com/Forsakringskassan/rimfrost-adapter-arbetsgivare/tree/1.1.4) | Maven |
| [rimfrost-adapter-folkbokford](https://github.com/Forsakringskassan/rimfrost-adapter-folkbokford/tree/rimfrost-1_2) | `se.fk.rimfrost.adapter.folkbokford:rimfrost-adapter-folkbokford` | [1.1.5](https://github.com/Forsakringskassan/rimfrost-adapter-folkbokford/tree/1.1.5) | Maven |
| [rimfrost-adapter-referensdata](https://github.com/Forsakringskassan/rimfrost-adapter-referensdata/tree/rimfrost-1_2) | `se.fk.rimfrost.adapter.referensdata:rimfrost-adapter-referensdata` | [1.1.2](https://github.com/Forsakringskassan/rimfrost-adapter-referensdata/tree/1.1.2) | Maven |
| [rimfrost-adapter-team](https://github.com/Forsakringskassan/rimfrost-adapter-team/tree/rimfrost-1_2) | `se.fk.rimfrost.adapter.team:rimfrost-adapter-team` | [0.0.1](https://github.com/Forsakringskassan/rimfrost-adapter-team/tree/0.0.1) | Maven |

## Service APIs

| Repository | Artifacts (groupId:artifactId) | Version | Build |
|---|---|---|---|
| [rimfrost-service-arbetsgivare-openapi](https://github.com/Forsakringskassan/rimfrost-service-arbetsgivare-openapi/tree/rimfrost-1_2) | `se.fk.rimfrost.api.arbetsgivare:rimfrost-arbetsgivare-api-jaxrs-spec`<br>`se.fk.rimfrost.api.arbetsgivare:rimfrost-arbetsgivare-api-spec` | [2.0.1](https://github.com/Forsakringskassan/rimfrost-service-arbetsgivare-openapi/tree/2.0.1) | Gradle |
| [rimfrost-service-erbjudande-topic-openapi](https://github.com/Forsakringskassan/rimfrost-service-erbjudande-topic-openapi/tree/rimfrost-1_2) | `se.fk.rimfrost.erbjudande.kafka.topic:rimfrost-service-erbjudande-topic-api-jaxrs-spec`<br>`se.fk.rimfrost.erbjudande.kafka.topic:rimfrost-service-erbjudande-topic-api-spec` | [0.0.1](https://github.com/Forsakringskassan/rimfrost-service-erbjudande-topic-openapi/tree/0.0.1) | Gradle |
| [rimfrost-service-folkbokforing-openapi](https://github.com/Forsakringskassan/rimfrost-service-folkbokforing-openapi/tree/rimfrost-1_2) | `se.fk.rimfrost.api.folkbokforing:rimfrost-folkbokforing-api-jaxrs-spec`<br>`se.fk.rimfrost.api.folkbokforing:rimfrost-folkbokforing-api-spec` | [2.0.2](https://github.com/Forsakringskassan/rimfrost-service-folkbokforing-openapi/tree/2.0.2) | Gradle |
| [rimfrost-service-handlaggning-asyncapi](https://github.com/Forsakringskassan/rimfrost-service-handlaggning-asyncapi/tree/rimfrost-1_2) | `se.fk.rimfrost:rimfrost-service-handlaggning-asyncapi` | [1.0.0](https://github.com/Forsakringskassan/rimfrost-service-handlaggning-asyncapi/tree/1.0.0) | Gradle |
| [rimfrost-service-handlaggning-openapi](https://github.com/Forsakringskassan/rimfrost-service-handlaggning-openapi/tree/rimfrost-1_2) | `se.fk.rimfrost:rimfrost-service-handlaggning-openapi-jaxrs-spec`<br>`se.fk.rimfrost:rimfrost-service-handlaggning-openapi-spec` | [2.0.7](https://github.com/Forsakringskassan/rimfrost-service-handlaggning-openapi/tree/2.0.7) | Gradle |
| [rimfrost-service-oul-asyncapi](https://github.com/Forsakringskassan/rimfrost-service-oul-asyncapi/tree/rimfrost-1_2) | `se.fk.rimfrost:rimfrost-service-oul-asyncapi` | [1.1.2](https://github.com/Forsakringskassan/rimfrost-service-oul-asyncapi/tree/1.1.2) | Gradle |
| [rimfrost-service-oul-management-openapi](https://github.com/Forsakringskassan/rimfrost-service-oul-management-openapi/tree/rimfrost-1_2) | `se.fk.rimfrost.oul.management:rimfrost-service-oul-management-api-jaxrs-spec`<br>`se.fk.rimfrost.oul.management:rimfrost-service-oul-management-api-spec` | [1.4.1](https://github.com/Forsakringskassan/rimfrost-service-oul-management-openapi/tree/1.4.1) | Gradle |
| [rimfrost-service-oul-management-regler-openapi](https://github.com/Forsakringskassan/rimfrost-service-oul-management-regler-openapi/tree/rimfrost-1_2) | `se.fk.rimfrost.oul.management.regler:rimfrost-service-oul-management-regler-api-jaxrs-spec`<br>`se.fk.rimfrost.oul.management.regler:rimfrost-service-oul-management-regler-api-spec` | [0.0.7](https://github.com/Forsakringskassan/rimfrost-service-oul-management-regler-openapi/tree/0.0.7) | Gradle |
| [rimfrost-service-oul-openapi](https://github.com/Forsakringskassan/rimfrost-service-oul-openapi/tree/rimfrost-1_2) | `se.fk.rimfrost.oul.handlaggning:rimfrost-service-oul-openapi-jaxrs-spec`<br>`se.fk.rimfrost.oul.handlaggning:rimfrost-service-oul-openapi-spec` | [2.3.1](https://github.com/Forsakringskassan/rimfrost-service-oul-openapi/tree/2.3.1) | Gradle |
| [rimfrost-service-referensdata-openapi](https://github.com/Forsakringskassan/rimfrost-service-referensdata-openapi/tree/rimfrost-1_2) | `se.fk.rimfrost:rimfrost-service-referensdata-openapi-jaxrs-spec`<br>`se.fk.rimfrost:rimfrost-service-referensdata-openapi-spec` | [1.1.1](https://github.com/Forsakringskassan/rimfrost-service-referensdata-openapi/tree/1.1.1) | Gradle |
| [rimfrost-service-sid-openapi](https://github.com/Forsakringskassan/rimfrost-service-sid-openapi/tree/rimfrost-1_2) | `se.fk.rimfrost.sid:rimfrost-service-sid-api-jaxrs-spec`<br>`se.fk.rimfrost.sid:rimfrost-service-sid-api-spec` | [0.0.2](https://github.com/Forsakringskassan/rimfrost-service-sid-openapi/tree/0.0.2) | Gradle |
| [rimfrost-service-team-openapi](https://github.com/Forsakringskassan/rimfrost-service-team-openapi/tree/rimfrost-1_2) | `se.fk.rimfrost.team:rimfrost-service-team-openapi-jaxrs-spec`<br>`se.fk.rimfrost.team:rimfrost-service-team-openapi-spec` | [0.1.1](https://github.com/Forsakringskassan/rimfrost-service-team-openapi/tree/0.1.1) | Gradle |
| [rimfrost-service-workflow-openapi](https://github.com/Forsakringskassan/rimfrost-service-workflow-openapi/tree/rimfrost-1_2) | `se.fk.rimfrost.workflow:rimfrost-service-workflow-openapi-jaxrs-spec`<br>`se.fk.rimfrost.workflow:rimfrost-service-workflow-openapi-spec` | [0.1.2](https://github.com/Forsakringskassan/rimfrost-service-workflow-openapi/tree/0.1.2) | Gradle |

## Services

| Repository | Artifacts (groupId:artifactId) | Version | Build |
|---|---|---|---|
| [rimfrost-service-arbetsgivare](https://github.com/Forsakringskassan/rimfrost-service-arbetsgivare/tree/rimfrost-1_2) | `fk.rimfrost:arbetsgivare` | [1.0.1](https://github.com/Forsakringskassan/rimfrost-service-arbetsgivare/tree/1.0.1) | Maven |
| [rimfrost-service-erbjudande-topic](https://github.com/Forsakringskassan/rimfrost-service-erbjudande-topic/tree/rimfrost-1_2) | `se.fk.github.rimfrost:rimfrost-service-erbjudande-topic` | [0.1.2](https://github.com/Forsakringskassan/rimfrost-service-erbjudande-topic/tree/0.1.2) | Maven |
| [rimfrost-service-folkbokforing](https://github.com/Forsakringskassan/rimfrost-service-folkbokforing/tree/rimfrost-1_2) | `fk.rimfrost:folkbokford` | [1.1.2](https://github.com/Forsakringskassan/rimfrost-service-folkbokforing/tree/1.1.2) | Maven |
| [rimfrost-service-handlaggning](https://github.com/Forsakringskassan/rimfrost-service-handlaggning/tree/rimfrost-1_2) | `se.fk.github.rimfrost:rimfrost-service-handlaggning` | [1.3.3](https://github.com/Forsakringskassan/rimfrost-service-handlaggning/tree/1.3.3) | Maven |
| [rimfrost-service-oul](https://github.com/Forsakringskassan/rimfrost-service-oul/tree/rimfrost-1_2) | `se.fk.github.rimfrost:rimfrost-operativt-uppgiftslager` | [1.7.1](https://github.com/Forsakringskassan/rimfrost-service-oul/tree/1.7.1) | Maven |
| [rimfrost-service-referensdata](https://github.com/Forsakringskassan/rimfrost-service-referensdata/tree/rimfrost-1_2) | `se.fk.github.rimfrost:rimfrost-service-referensdata` | [1.1.2](https://github.com/Forsakringskassan/rimfrost-service-referensdata/tree/1.1.2) | Maven |
| [rimfrost-service-sid](https://github.com/Forsakringskassan/rimfrost-service-sid/tree/rimfrost-1_2) | `se.fk.github.rimfrost:rimfrost-service-sid` | [0.1.1](https://github.com/Forsakringskassan/rimfrost-service-sid/tree/0.1.1) | Maven |
| [rimfrost-service-team](https://github.com/Forsakringskassan/rimfrost-service-team/tree/rimfrost-1_2) | `se.fk.github.rimfrost:rimfrost-service-team` | [0.1.0](https://github.com/Forsakringskassan/rimfrost-service-team/tree/0.1.0) | Maven |
| [rimfrost-service-workflow](https://github.com/Forsakringskassan/rimfrost-service-workflow/tree/rimfrost-1_2) | `se.fk.github.rimfrost:rimfrost-service-workflow` | [0.2.4](https://github.com/Forsakringskassan/rimfrost-service-workflow/tree/0.2.4) | Maven |

## Process APIs and processes

| Repository | Artifacts (groupId:artifactId) | Version | Build |
|---|---|---|---|
| [rimfrost-process-asyncapi](https://github.com/Forsakringskassan/rimfrost-process-asyncapi/tree/rimfrost-1_2) | `se.fk.rimfrost:rimfrost-process-asyncapi` | [1.2.3](https://github.com/Forsakringskassan/rimfrost-process-asyncapi/tree/1.2.3) | Maven |
| [rimfrost-process-vab](https://github.com/Forsakringskassan/rimfrost-process-vab/tree/rimfrost-1_2) | `se.fk.github.rimfrost:rimfrost-vard-av-boskap` | [0.0.5](https://github.com/Forsakringskassan/rimfrost-process-vab/tree/0.0.5) | Maven |
| [rimfrost-process-vah](https://github.com/Forsakringskassan/rimfrost-process-vah/tree/rimfrost-1_2) | `se.fk.github.rimfrost:rimfrost-vard-av-husdjur` | [1.1.7](https://github.com/Forsakringskassan/rimfrost-process-vah/tree/1.1.7) | Maven |

## Rules (regler)

| Repository | Artifacts (groupId:artifactId) | Version | Build |
|---|---|---|---|
| [rimfrost-regel-bekraftabeslut-bff](https://github.com/Forsakringskassan/rimfrost-regel-bekraftabeslut-bff/tree/rimfrost-1_2) | `se.fk.github:rimfrost-regel-bekraftabeslut-bff` | [0.0.2](https://github.com/Forsakringskassan/rimfrost-regel-bekraftabeslut-bff/tree/0.0.2) | Maven |
| [rimfrost-regel-bekraftabeslut-openapi](https://github.com/Forsakringskassan/rimfrost-regel-bekraftabeslut-openapi/tree/rimfrost-1_2) | `se.fk.rimfrost.regel.bekraftabeslut.openapi:rimfrost-regel-bekraftabeslut-openapi-jaxrs-spec`<br>`se.fk.rimfrost.regel.bekraftabeslut.openapi:rimfrost-regel-bekraftabeslut-openapi-spec` | [1.1.2](https://github.com/Forsakringskassan/rimfrost-regel-bekraftabeslut-openapi/tree/1.1.2) | Gradle |
| [rimfrost-regel-bekraftabeslut-subprocess](https://github.com/Forsakringskassan/rimfrost-regel-bekraftabeslut-subprocess/tree/rimfrost-1_2) | `se.fk.github.rimfrost.regel.subprocess:rimfrost-regel-bekraftabeslut-subprocess` | [1.1.7](https://github.com/Forsakringskassan/rimfrost-regel-bekraftabeslut-subprocess/tree/1.1.7) | Maven |
| [rimfrost-regel-bekraftabeslut](https://github.com/Forsakringskassan/rimfrost-regel-bekraftabeslut/tree/rimfrost-1_2) | `se.fk.rimfrost:rimfrost-regel-bekraftabeslut` | [1.1.9](https://github.com/Forsakringskassan/rimfrost-regel-bekraftabeslut/tree/1.1.9) | Maven |
| [rimfrost-regel-rtf-manuell-bff](https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-bff/tree/rimfrost-1_2) | `se.fk.github:rimfrost-regel-rtf-manuell-bff` | [0.0.2](https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-bff/tree/0.0.2) | Maven |
| [rimfrost-regel-rtf-manuell-komplettering-bff](https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-komplettering-bff/tree/rimfrost-1_2) | `se.fk.github:rimfrost-regel-rtf-manuell-komplettering-bff` | [0.0.2](https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-komplettering-bff/tree/0.0.2) | Maven |
| [rimfrost-regel-rtf-manuell-komplettering](https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-komplettering/tree/rimfrost-1_2) | `se.fk.rimfrost:rimfrost-regel-rtf-manuell-komplettering` | [0.0.7](https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-komplettering/tree/0.0.7) | Maven |
| [rimfrost-regel-rtf-manuell-openapi](https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-openapi/tree/rimfrost-1_2) | `se.fk.rimfrost.regel.rtf.manuell:rimfrost-regel-rtf-manuell-openapi-jaxrs-spec`<br>`se.fk.rimfrost.regel.rtf.manuell:rimfrost-regel-rtf-manuell-openapi-spec` | [1.2.1](https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-openapi/tree/1.2.1) | Gradle |
| [rimfrost-regel-rtf-manuell-subprocess](https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-subprocess/tree/rimfrost-1_2) | `se.fk.github.rimfrost.regel.subprocess:rimfrost-regel-rtf-manuell-subprocess` | [1.1.8](https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-subprocess/tree/1.1.8) | Maven |
| [rimfrost-regel-rtf-manuell](https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell/tree/rimfrost-1_2) | `se.fk.rimfrost:rimfrost-regel-rtf-manuell` | [1.3.6](https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell/tree/1.3.6) | Maven |
| [rimfrost-regel-rtf-maskinell-subprocess](https://github.com/Forsakringskassan/rimfrost-regel-rtf-maskinell-subprocess/tree/rimfrost-1_2) | `se.fk.github.rimfrost.regel.subprocess:rimfrost-regel-rtf-maskinell-subprocess` | [1.1.7](https://github.com/Forsakringskassan/rimfrost-regel-rtf-maskinell-subprocess/tree/1.1.7) | Maven |
| [rimfrost-regel-rtf-maskinell](https://github.com/Forsakringskassan/rimfrost-regel-rtf-maskinell/tree/rimfrost-1_2) | `com.example:rimfrost-regel-rtf-maskinell` | [1.1.7](https://github.com/Forsakringskassan/rimfrost-regel-rtf-maskinell/tree/1.1.7) | Maven |

## Portal

| Repository | Artifacts (groupId:artifactId) | Version | Build |
|---|---|---|---|
| [rimfrost-portal-admin-bff](https://github.com/Forsakringskassan/rimfrost-portal-admin-bff/tree/rimfrost-1_2) | `se.fk.github:rimfrost-portal-admin-bff` | [0.0.4](https://github.com/Forsakringskassan/rimfrost-portal-admin-bff/tree/0.0.4) | Maven |
| [rimfrost-portal-bff](https://github.com/Forsakringskassan/rimfrost-portal-bff/tree/rimfrost-1_2) | `se.fk.github:rimfrost-portal-bff` | [2.1.3](https://github.com/Forsakringskassan/rimfrost-portal-bff/tree/2.1.3) | Maven |

## Templates

| Repository | Artifacts (groupId:artifactId) | Version | Build |
|---|---|---|---|
| [rimfrost-template-micro-fe-bff](https://github.com/Forsakringskassan/rimfrost-template-micro-fe-bff/tree/rimfrost-1_2) | `se.fk.github:rimfrost-template-micro-fe-bff` | [0.1.0](https://github.com/Forsakringskassan/rimfrost-template-micro-fe-bff/tree/0.1.0) | Maven |
| [rimfrost-template-process](https://github.com/Forsakringskassan/rimfrost-template-process/tree/rimfrost-1_2) | `se.fk.github.rimfrost:rimfrost-template-process` | [1.1.2](https://github.com/Forsakringskassan/rimfrost-template-process/tree/1.1.2) | Maven |
| [rimfrost-template-referensdata-erbjudande](https://github.com/Forsakringskassan/rimfrost-template-referensdata-erbjudande/tree/rimfrost-1_2) | `se.fk.rimfrost.referensdata:rimfrost-template-referensdata-erbjudande` | [0.0.1](https://github.com/Forsakringskassan/rimfrost-template-referensdata-erbjudande/tree/0.0.1) | Maven |
| [rimfrost-template-regel-komplettering-openapi](https://github.com/Forsakringskassan/rimfrost-template-regel-komplettering-openapi/tree/rimfrost-1_2) | `se.fk.rimfrost.template.regel.komplettering.openapi:rimfrost-template-regel-komplettering-openapi-jaxrs-spec`<br>`se.fk.rimfrost.template.regel.komplettering.openapi:rimfrost-template-regel-komplettering-openapi-spec` | [0.0.2](https://github.com/Forsakringskassan/rimfrost-template-regel-komplettering-openapi/tree/0.0.2) | Gradle |
| [rimfrost-template-regel-komplettering](https://github.com/Forsakringskassan/rimfrost-template-regel-komplettering/tree/rimfrost-1_2) | `se.fk.rimfrost:rimfrost-template-regel-komplettering` | [0.0.1](https://github.com/Forsakringskassan/rimfrost-template-regel-komplettering/tree/0.0.1) | Maven |
| [rimfrost-template-regel-manuell-openapi](https://github.com/Forsakringskassan/rimfrost-template-regel-manuell-openapi/tree/rimfrost-1_2) | `se.fk.rimfrost.template.regel.manuell.openapi:rimfrost-template-regel-manuell-openapi-jaxrs-spec`<br>`se.fk.rimfrost.template.regel.manuell.openapi:rimfrost-template-regel-manuell-openapi-spec` | [1.1.1](https://github.com/Forsakringskassan/rimfrost-template-regel-manuell-openapi/tree/1.1.1) | Gradle |
| [rimfrost-template-regel-manuell](https://github.com/Forsakringskassan/rimfrost-template-regel-manuell/tree/rimfrost-1_2) | `se.fk.rimfrost:rimfrost-template-regel-manuell` | [1.1.2](https://github.com/Forsakringskassan/rimfrost-template-regel-manuell/tree/1.1.2) | Maven |
| [rimfrost-template-regel-maskinell](https://github.com/Forsakringskassan/rimfrost-template-regel-maskinell/tree/rimfrost-1_2) | `com.example:rimfrost-template-regel-maskinell` | [1.1.2](https://github.com/Forsakringskassan/rimfrost-template-regel-maskinell/tree/1.1.2) | Maven |
| [rimfrost-template-regel-subprocess](https://github.com/Forsakringskassan/rimfrost-template-regel-subprocess/tree/rimfrost-1_2) | `se.fk.github.rimfrost.regel.subprocess:rimfrost-template-regel-subprocess` | [1.1.3](https://github.com/Forsakringskassan/rimfrost-template-regel-subprocess/tree/1.1.3) | Maven |

## Other

| Repository | Artifacts (groupId:artifactId) | Version | Build |
|---|---|---|---|
| [rimfrost-ersattning-data](https://github.com/Forsakringskassan/rimfrost-ersattning-data/tree/rimfrost-1_2) | `se.fk.rimfrost.ersattningdata:rimfrost-ersattning-data` | [1.0.0](https://github.com/Forsakringskassan/rimfrost-ersattning-data/tree/1.0.0) | Maven |
| [rimfrost-referensdata-erbjudande](https://github.com/Forsakringskassan/rimfrost-referensdata-erbjudande/tree/rimfrost-1_2) | `se.fk.rimfrost.referensdata:rimfrost-referensdata-erbjudande` | [1.1.1](https://github.com/Forsakringskassan/rimfrost-referensdata-erbjudande/tree/1.1.1) | Maven |

## Tagged, but not in the Maven BOM

These repositories have the `rimfrost-1_2` tag but publish no Maven artifact.

| Repository | Version | Type |
|---|---|---|
| [rimfrost-kubernetes](https://github.com/Forsakringskassan/rimfrost-kubernetes/tree/rimfrost-1_2) | - | Kubernetes manifests and smoketest, no release tag |
| [rimfrost-portal-admin-fe](https://github.com/Forsakringskassan/rimfrost-portal-admin-fe/tree/rimfrost-1_2) | [0.0.2](https://github.com/Forsakringskassan/rimfrost-portal-admin-fe/tree/0.0.2) | npm frontend |
| [rimfrost-portal-handlaggare](https://github.com/Forsakringskassan/rimfrost-portal-handlaggare/tree/rimfrost-1_2) | [0.4.0](https://github.com/Forsakringskassan/rimfrost-portal-handlaggare/tree/0.4.0) | npm frontend |
| [rimfrost-regel-bekraftabeslut-fe](https://github.com/Forsakringskassan/rimfrost-regel-bekraftabeslut-fe/tree/rimfrost-1_2) | [0.0.4](https://github.com/Forsakringskassan/rimfrost-regel-bekraftabeslut-fe/tree/0.0.4) | npm frontend |
| [rimfrost-regel-rtf-manuell-fe](https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-fe/tree/rimfrost-1_2) | [0.0.4](https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-fe/tree/0.0.4) | npm frontend |
| [rimfrost-regel-rtf-manuell-komplettering-fe](https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-komplettering-fe/tree/rimfrost-1_2) | [0.0.1](https://github.com/Forsakringskassan/rimfrost-regel-rtf-manuell-komplettering-fe/tree/0.0.1) | npm frontend |
| [rimfrost-template-micro-fe](https://github.com/Forsakringskassan/rimfrost-template-micro-fe/tree/rimfrost-1_2) | [2.0.1](https://github.com/Forsakringskassan/rimfrost-template-micro-fe/tree/2.0.1) | npm frontend |
