---
## PARAMETER MANAGER CONFIG
param-efluid:

    managed-datasource:
        driver-class-name: oracle.jdbc.OracleDriver
        url: jdbc:oracle:thin:@localhost:49161:xe
        username: reference
        password: reference
        meta:
            filter-schema: REFERENCE

    security:
        salt: 12345678901234567890123456789012

    details:
        instance-name: SOURCE-ORACLE-MGMT
    
    model-identifier:
        enabled: true
        class-name: fr.uem.efluid.tools.DemoDatabaseIdentifier
        show-sql: true

## TECH FEATURES CUSTOM
spring:
    profiles:
        active: prod

    datasource:
        url: jdbc:oracle:thin:@small-server:49161:xe
        username: mgmt_src
        password: mgmt_src
        driver-class-name: oracle.jdbc.OracleDriver

    jpa:
        show-sql: true
        properties:
            hibernate.dialect: org.hibernate.dialect.Oracle12cDialect

## WEB SERVER CONFIG
server:
    port: 8080