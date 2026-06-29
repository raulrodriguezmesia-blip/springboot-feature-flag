![Feature Flag Service](cover.png)

# 🚀 Feature Flag Service

**Servicio de gestión de features dinámicas con Spring Boot 3.x | Java 21 | DevOps Pro**

*Para ver la portada completa, visita el repositorio en [github.com/raulrodriguezmesia-blip/springboot-feature-flag](https://github.com/raulrodriguezmesia-blip/springboot-feature-flag).*

---

## 📢 Descripción General

El **Feature Flag Service** es un microservicio Spring Boot 3.x que permite activar/desactivar funciones en tiempo real sin necessidade de redploy. Integra:
- **Seguridad**: JWT (con refresh tokens) y control de roles (ADMIN/USER).
- **Caché**: Redis (con fallback en memoria).
- **Observabilidad**: Actuator + Prometheus + JSON estructurado.
- **Actualizaciones en tiempo real**: WebSocket/SSE.

---

## 🔮 Arquitectura Técnica

```mermaid
graph TD
    A[Web Client] -->|HTTPS| B[API Gateway]
    B -->|JWT Auth| C[Feature Flag Service]
    C -->|Cache| D[Redis]
    C -->|Persistence| E[PostgreSQL / H2]
    C -->|Audit| F[Audit Service]
    C -->|Notifications| G[Webhook / WebSocket / SSE]
    C -->|Metrics| H[Prometheus via Actuator]
    style A fill:#f9f,stroke:#333,stroke-width:1px
    style B fill:#bbf,stroke:#333,stroke-width:1px
    style C fill:#cfc,stroke:#333,stroke-width:2px
    style D fill:#ff9,stroke:#333,stroke-width:1px
    style E fill:#9cf,stroke:#333,stroke-width:1px
    style F fill:#f96,stroke:#333,stroke-width:1px
    style G fill:#bbf,stroke:#333,stroke-width:1px
    style H fill:#cfc,stroke:#333,stroke-width:1px
```

---

## 📊 Componentes de Despliegue

### 1. `Dockerfile` (Multi-stage)
```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY ./pom.xml .\nRUN mvn dependency:go-offline
COPY ./src/main ./src/main
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
```

---

## ⏳ Beneficios Técnicos

| Característica      | Valor            |
|----------------------|------------------|
| **Flexibilidad**     | Desactivar/activar features en runtime. |  