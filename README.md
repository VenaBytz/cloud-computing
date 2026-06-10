# Proyecto Final — Circuit Breaker con Resilience4J

## Integrantes del Equipo
- Benito Hernandez Ivan
- Ramirez Luna Gibran

---

## Descripción

Continuación de la Tarea 5.
Se implementaron las siguientes funcionalidades:
1. Operaciones CRUD
2. Vista para listar los productos
3. Patrón Circuit Breaker con `@CircuitBreaker` y `@TimeLimiter` mediante Resilience4J

---

## Stack tecnológico

| Servicio | Spring Boot | Spring Cloud | Puerto |
|---|---|---|---|
| eureka-server | 3.2.5 | 2023.0.1 | 8761 |
| product-service (×2) | 3.2.5 | 2023.0.1 | 8081 / 8082 |
| item-service | 3.2.5 | 2023.0.1 | 8080 |
| **zuul-server** | **2.3.12** | **Hoxton.SR12** | **8090** |

> **Nota:** Zuul fue eliminado del BOM a partir de Spring Cloud 2020.0.0. La última versión del BOM oficial que lo incluye es **Hoxton.SR12**, compatible con Spring Boot 2.3.x. La comunicación entre el cliente Eureka 2.2.x (Hoxton) y el servidor Eureka 3.2.x funciona correctamente porque la API REST de Eureka no cambió. Hystrix fue reemplazado por **Resilience4J** en `item-service`.

---

## Ejecución

```bash
cd MicroServicios
docker-compose down        # detener si ya corría
docker-compose up --build  # construir y levantar todos los servicios
```

Servicios disponibles una vez levantados:

| URL | Descripción |
|---|---|
| http://localhost:8761 | Eureka Dashboard |
| http://localhost:8090/products/** | Gateway → product-service |
| http://localhost:8090/items/** | Gateway → item-service |

---

## Estructura del proyecto

```
MicroServicios/
├── docker-compose.yml
├── eureka-server/
├── product-service/          ← instancia 1 (:8081)
│   └── src
│       ├── main
│       │   ├── java
│       │   └── resources
│       │       ├── application.properties
│       │       ├── data.sql
│       │       └── templates
│       │           └── modelos.html ← Vista para la lista de productos
│       └── test
│           └── java
│               └── com
│                   └── autos
│                       └── product
│                           └── service
│                               └── ProductServiceImplTest.java ← Pruebas con JUnit
├── item-service/
│   └── src/main/resources/
│       └── application.yml   ← configuración de Resilience4J
└── zuul-server/
```

---

# Reporte de Funcionalidades

---

## 1. Ruteo Dinámico

**Descripción:** Zuul actúa como API Gateway y enruta automáticamente las peticiones al microservicio correcto, descubriendo sus instancias en tiempo real a través de Eureka (no se necesita conocer la IP ni el puerto del servicio destino).

**Configuración** (`zuul-server/src/main/resources/application.yml`):

```yaml
zuul:
  routes:
    productos:
      path: /products/**
      serviceId: product-service   # nombre registrado en Eureka
      strip-prefix: false
    items:
      path: /items/**
      serviceId: item-service
      strip-prefix: false
```

**Ejemplo — Ruteo hacia product-service:**

```bash
curl http://localhost:8090/products/list
```

Zuul recibe la petición en `:8090`, consulta Eureka para resolver `product-service`, y reenvía transparentemente hacia `:8081/products/list`.

**Respuesta obtenida:**

```json
[
  {"id":1,"modelo":"Sedan X1","precio":20000.0,"marca":"Toyota","temporada":"Verano"},
  {"id":2,"modelo":"SUV Y2","precio":35000.0,"marca":"Honda","temporada":"Invierno"},
  {"id":3,"modelo":"Coupe Z3","precio":28000.0,"marca":"BMW","temporada":"Primavera"}
]
```

**Ejemplo — Ruteo hacia item-service:**

```bash
curl http://localhost:8090/items/1
```

**Filtros aplicados en cada ruteo:**

- `PreFilter` — registra el método HTTP, la URI y el servicio destino **antes** de enrutar. También agrega el header `X-Zuul-Gateway: zuul-server`.
- `PostFilter` — registra el código de respuesta HTTP **después** de recibir la respuesta del microservicio. Agrega el header `X-Zuul-Procesado: true`.

Log generado en zuul-server:

```
[PRE-FILTER]  Método: GET | URI: /products/list | Servicio destino: product-service
[POST-FILTER] Respuesta enviada al cliente | Código HTTP: 200
```

---

## 2. Balanceo de Carga con Ribbon

**Descripción:** Se levantaron **dos instancias** de product-service (`:8081` y `:8082`), ambas registradas en Eureka con el mismo nombre de servicio `product-service`. Zuul utiliza **Ribbon** (integrado en el proxy de Zuul) para distribuir las peticiones en modo **round-robin** entre las instancias disponibles.

**Registro en Eureka — dos instancias activas:**

```bash
curl http://localhost:8761/eureka/apps/product-service
```

```xml
<instanceId>product-service-2</instanceId>
<ipAddr>172.18.0.7</ipAddr>
<port enabled="true">8081</port>

<instanceId>product-service-1</instanceId>
<ipAddr>172.18.0.6</ipAddr>
<port enabled="true">8081</port>
```

**Ejemplo — 8 llamadas consecutivas a través de Zuul:**

```bash
for i in 1 2 3 4 5 6 7 8; do
  curl -s http://localhost:8090/products/instance-info
  echo
done
```

**Respuesta obtenida — Ribbon alterna entre instancias en round-robin:**

```
{"puerto":8081,"ip":"172.18.0.7","host":"dac68ba973a2","servicio":"product-service"}
{"puerto":8081,"ip":"172.18.0.6","host":"b1be001239f4","servicio":"product-service"}
{"puerto":8081,"ip":"172.18.0.7","host":"dac68ba973a2","servicio":"product-service"}
{"puerto":8081,"ip":"172.18.0.6","host":"b1be001239f4","servicio":"product-service"}
```

Las IPs `172.18.0.7` y `172.18.0.6` corresponden a los contenedores `product-service-2` y `product-service-1` respectivamente. Ribbon distribuye exactamente la mitad del tráfico a cada instancia.

---

## 3. Configuración de Resilience4J

**Ubicación:** `item-service/src/main/resources/application.yml`

Solo se configura en `item-service` porque es el servicio que realiza las llamadas a `product-service` y necesita protegerse ante sus fallos.

```yaml
resilience4j:
  circuitbreaker:
    configs:
      defecto:                                           # perfil de configuración reutilizable
        sliding-window-size: 6                          # evalúa las últimas 6 llamadas
        failure-rate-threshold: 50                      # abre el circuito si el 50%+ fallan
        wait-duration-in-open-state: 20s                # tiempo en estado OPEN antes de pasar a HALF-OPEN
        permitted-number-of-calls-in-half-open-state: 4 # llamadas de prueba en estado HALF-OPEN
        slow-call-rate-threshold: 50                    # abre el circuito si el 50%+ son lentas
        slow-call-duration-threshold: 2s                # una llamada se considera lenta si supera 2s
    instances:
      productService:
        base-config: defecto
  timelimiter:
    instances:
      productService:
        timeout-duration: 2s                            # cancela la llamada si supera 2s
        cancel-running-future: true                     # cancela el CompletableFuture en ejecución
```

| Parámetro | Valor | Efecto |
|---|---|---|
| `sliding-window-size` | 6 | Evalúa las últimas 6 llamadas a `product-service` |
| `failure-rate-threshold` | 50% | Si 3 de 6 llamadas fallan → circuito ABRE |
| `wait-duration-in-open-state` | 20s | El circuito espera 20s antes de intentar recuperarse |
| `permitted-number-of-calls-in-half-open-state` | 4 | En HALF-OPEN prueba con 4 llamadas para decidir si cierra |
| `timeout-duration` | 2s | El `sleep(5L)` en `getItemById` supera este límite y activa el fallback |
| `cancel-running-future` | true | Cancela el hilo del `CompletableFuture` al hacer timeout |
