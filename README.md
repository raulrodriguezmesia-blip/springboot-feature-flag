# 🚀 Spring Boot Feature Flag Service

[![Build](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Java](https://img.shields.io/badge/java-17+-blue?logo=java)]()
[![Spring%20Boot](https://img.shields.io/badge/spring--boot-3.0-brightgreen?logo=spring)]()
[![Docker](https://img.shields.io/badge/docker-ready-blue?logo=docker)]()
[![Helm](https://img.shields.io/badge/helm-k8s-blue?logo=helm)]()
[![License](https://img.shields.io/badge/license-MIT-green)]()

[![LinkedIn](https://img.shields.io/badge/linkedin-RaulRodriguezMesia-blue?logo=linkedin)](https://linkedin.com/in/raul-rodriguez-mesia-bb8178149)
[![GitHub](https://img.shields.io/badge/github-raulrodriguezmesia--blip-black?logo=github)](https://github.com/raulrodriguezmesia-blip/springboot-feature-flag)

---

## 💼 Contexto profesional

Microservicio que implementa **feature flags dinámicos sin redeploy**, demostrando:

- ✅ **Backend avanzado**: Java 17 + Spring Boot 3 + Clean Architecture
- ✅ **Configuración dinámica**: Control de funcionalidades en tiempo real
- ✅ **CI/CD integral**: GitHub Actions + Docker + Kubernetes-ready
- ✅ **Observabilidad**: Actuator + Prometheus + Health Checks
- ✅ **Seguridad enterprise**: JWT + Role-Based Access Control

Primer proyecto de un portafolio orientado a microservicios escalables.

---

## ⚙️ Arquitectura

### Diagrama ASCII
```
[Cliente] ---> [Spring Boot Service] ---> [Feature Flag Config]
                                   |
                                   ---> [Enabled/Disabled Logic]
```

### Diagrama Mermaid
```mermaid
flowchart LR
    A[Cliente] --> B[Spring Boot Service]
    B --> C[Feature Flag Config]
    C --> D{Enabled/Disabled Logic}
    D -->|Enabled| E[Funcionalidad activa]
    D -->|Disabled| F[Funcionalidad inactiva]
```

---

## 🛠️ Instalación y ejecución

### Con Docker Compose (recomendado)
```bash
git clone https://github.com/raulrodriguezmesia-blip/springboot-feature-flag.git
cd springboot-feature-flag
make build
make up
curl http://localhost:8080/actuator/health
```

### Con Maven
```bash
mvn clean install
mvn spring-boot:run
```

### Con Kubernetes
```bash
helm install feature-flag ./helm-chart/feature-flag-service
```

---

## 🎥 Demo

### Endpoints
```bash
# Activar feature flag
curl -X POST "http://localhost:8080/api/feature-flags?key=newDashboard&enabled=true" \
  -H "Authorization: Bearer <JWT_TOKEN>"

# Verificar estado
curl http://localhost:8080/api/feature-flags/newDashboard \
  -H "Authorization: Bearer <JWT_TOKEN>"
# Resultado: true o false
```

### Video Showcase
[![Video](https://img.shields.io/badge/video-12s_Showcase-blue?logo=youtube)](https://github.com/raulrodriguezmesia-blip/springboot-feature-flag/raw/master/presentation/feature-showcase.mp4)

---

## 📅 Roadmap futuro

| Proyecto | Tecnologías | Estado |
|----------|-------------|--------|
| **Event-Driven Notification Service** | Kafka + WebSocket | 🔜 |
| **Observability Dashboard** | Prometheus + Grafana + Loki | 🔜 |
| **Microservice Template** | Spring Boot + Helm + CI/CD | 🔜 |
| **Multi-cloud Deployment Guide** | Terraform + AKS + GKE | 🔜 |

---

## 🧰 Tecnologías usadas

| ☕ Java 17 | 🍃 Spring Boot 3 | Ⓜ️ Maven | 🐳 Docker | ☁️ AWS | 🎯 Helm | 📋 GitHub Actions |
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|

---

## 💡 Valor profesional

Este proyecto demuestra experiencia en:

- ✅ Feature flags en microservicios
- ✅ Arquitectura cloud-native
- ✅ DevOps: CI/CD + Containers + Kubernetes
- ✅ Seguridad: JWT + RBAC
- ✅ Observabilidad: Métricas + Health Checks

---

## 📬 Contacto

**Raúl Rodriguez Mesia** - Backend Developer  
Especialista en Java, Spring Boot, AWS, Docker, CI/CD

[![GitHub](https://img.shields.io/badge/github-raulrodriguezmesia--blip-black?logo=github)](https://github.com/raulrodriguezmesia-blip)
[![LinkedIn](https://img.shields.io/badge/linkedin-RaulRodriguezMesia-blue?logo=linkedin)](https://linkedin.com/in/raul-rodriguez-mesia-bb8178149)

**Licencia**: MIT
