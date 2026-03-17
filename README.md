# Cloud Computing - Microservicios

Proyecto de ejemplo que implementa un sistema de microservicios para gestión de productos e ítems, usando **Spring Boot**, **MySQL** y **Docker Compose**.

## Integrantes del Equipo:
Benito Hernandez Ivan 
Ramirez Luna Gibran

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
Para probar que todo funciona correctamente usar el siguiente comando
```bash
curl -s http://localhost:8081/products/ver/1
# salida esperada
{"id":1,"modelo":"Sedan X1","precio":20000.0,"marca":"Toyota","temporada":"Verano"}
```
