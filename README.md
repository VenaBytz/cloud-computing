# Cloud Computing - Microservicios

Proyecto de ejemplo que implementa un sistema de microservicios para gestión de productos e ítems, usando **Spring Boot**, **MySQL** y **Docker Compose**.

## Integrantes del Equipo:
- Benito Hernandez Ivan 
- Ramirez Luna Gibran

## Estructura del proyecto
```bash
cloud-computing
├── MicroServicios
│   ├── docker-compose.yml
│   ├── item-service
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   └── src
│   │       └── main
│   │           ├── java
│   │           │   └── com
│   │           │       └── autos
│   │           │           └── item
│   │           │               ├── client
│   │           │               │   └── ProductClient.java
│   │           │               ├── config
│   │           │               │   └── AppConfig.java
│   │           │               ├── controller
│   │           │               │   └── ItemController.java
│   │           │               ├── dto
│   │           │               │   ├── ItemDto.java
│   │           │               │   └── ProductDto.java
│   │           │               ├── entity
│   │           │               │   └── Item.java
│   │           │               ├── ItemServiceApplication.java
│   │           │               ├── repository
│   │           │               │   └── ItemDao.java
│   │           │               └── service
│   │           │                   ├── ItemServiceImpl.java
│   │           │                   └── ItemService.java
│   │           └── resources
│   │               ├── application.properties
│   │               └── data.sql
│   ├── mysql-init
│   │   ├── itemdb.sql
│   │   └── productdb.sql
│   └── product-service
│       ├── Dockerfile
│       ├── pom.xml
│       └── src
│           └── main
│               ├── java
│               │   └── com
│               │       └── autos
│               │           └── product
│               │               ├── controller
│               │               │   └── ProductController.java
│               │               ├── dto
│               │               │   └── ProductDto.java
│               │               ├── entity
│               │               │   └── Product.java
│               │               ├── ProductServiceApplication.java
│               │               ├── repository
│               │               │   └── ProductDao.java
│               │               └── service
│               │                   ├── ProductServiceImpl.java
│               │                   └── ProductService.java
│               └── resources
│                   ├── application.properties
│                   └── data.sql
└── README.md
```
---

## Requisitos

- **Java 17+** (compatible con Spring Boot 3.x)  
- **Maven**  
- **Docker** y **Docker Compose**  

---

## Compilación de los servicios

Desde `cloud-computing/MicroServicios` ejecutar:

```bash
# Compilar item-service
cd item-service
mvn clean package

# Compilar product-service
cd ../product-service
mvn clean package

# Volver al directorio principal
cd ..
# Levantar los servicios
docker compose up --build
```
## Verificación
### **1. Pruebas con exito **
Para probar que todo funciona correctamente usar el siguiente comando
```bash
curl -s http://localhost:8081/products/ver/1
# salida esperada
{"id":1,"modelo":"Sedan X1","precio":20000.0,"marca":"Toyota","temporada":"Verano"}
```
Para crear un item llamando a product-service:
```bash
curl -X POST "http://localhost:8080/items?productId=1&cantidad=5"
```

**Salida esperada:**
```json
{"id":1,"productId":1,"cantidad":5,"iva":3200,"total":116000,"fecha":"2026-04-09T20:21:53.496852551"}
```
### **2. Prueba con fallo (Circuit Breaker activado)**

En otra terminal, detén product-service:

```bash
docker stop product-service
```

Ahora intenta crear un item:

```bash
curl -X POST "http://localhost:8080/items?productId=1&cantidad=5"
```

**Salida esperada (fallback ejecutado):**
```json
{"timestamp":"2026-04-09T20:23:38.495+00:00","status":500,"error":"Internal Server Error","path":"/items"}
```

En los logs de item-service verás:
```
Circuit breaker 'productService' is now OPEN
```

---

### **3. Prueba de recuperación**

Reinicia product-service:

```bash
docker start product-service
```
Espera 10 segundos (waitDurationInOpenState=10s) y vuelve a intentar:

```bash
curl -X POST "http://localhost:8080/items?productId=1&cantidad=5"
```

**Salida esperada:**
```json
{"id":2,"productId":1,"cantidad":5,"iva":3200,"total":116000,"fecha":"2026-04-09T20:24:57.111622572"}
```
