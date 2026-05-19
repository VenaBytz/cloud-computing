# Práctica 5 — Operaciones CRUD

## Integrantes del Equipo
- Benito Hernandez Ivan
- Ramirez Luna Gibran

---

## Descripción

Continuación de la Práctica 4.
Se implementaron las siguientes funcionalidades:
1. Operaciones CRUD
2. Vista para listar los productos
---

## Stack tecnológico

| Servicio | Spring Boot | Spring Cloud | Puerto |
|---|---|---|---|
| eureka-server | 3.2.5 | 2023.0.0 | 8761 |
| product-service (×2) | 3.2.5 | 2023.0.0 | 8081 / 8082 |
| item-service | 3.2.5 | 2023.0.0 | 8080 |
| **zuul-server** | **2.3.12** | **Hoxton.SR12** | **8090** |

> **Nota:** Zuul e Hystrix fueron eliminados del BOM a partir de Spring Cloud 2020.0.0. La última versión del BOM oficial que los incluye es **Hoxton.SR12**, compatible con Spring Boot 2.3.x. La comunicación entre el cliente Eureka 2.2.x (Hoxton) y el servidor Eureka 3.2.x funciona correctamente porque la API REST de Eureka no cambió.

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
| http://localhost:8090/api/productos/** | Gateway → product-service |
| http://localhost:8090/api/items/** | Gateway → item-service |
| http://localhost:8090/proxy/productos | Proxy con @HystrixCommand |

---

## Estructura del proyecto

```
MicroServicios/
├── docker-compose.yml
├── eureka-server/
├── product-service/          ← instancia 1 (:8081)
├── src
│   ├── main
│   │   ├── java
│   │   └── resources
│   │       ├── application.properties
│   │       ├── data.sql
│   │       └── templates
│   │           └── modelos.html ← Vista para la lista de productos
│   └── test
│       └── java
│           └── com
│               └── autos
│                   └── product
│                       └── service
│                           └── ProductServiceImplTest.java ← Pruebas con JUint
├── product-service/          ← instancia 2 (:8082) [mismo código]
├── item-service/
└── zuul-server/
```

---

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
      path: /api/productos/**
      serviceId: product-service   # nombre registrado en Eureka
      stripPrefix: true
    items:
      path: /api/items/**
      serviceId: item-service
      stripPrefix: true
    productos-directo:
      path: /api/directo/**
      url: http://localhost:8081    # ruteo estático por URL directa
      stripPrefix: true
```

**Ejemplo — Ruteo hacia product-service:**

```bash
curl http://localhost:8090/api/productos/products/list
```

Zuul recibe la petición en `:8090`, consulta Eureka para resolver `product-service`, y reenvía transparentemente hacia `:8081/products/list`.

**Respuesta obtenida:**

```json
[
  {"id":1,"modelo":"Sedan X1","precio":20000.0,"marca":"Toyota","temporada":"Verano"},
  {"id":2,"modelo":"SUV Y2","precio":35000.0,"marca":"Honda","temporada":"Invierno"},
  {"id":3,"modelo":"Coupe Z3","precio":28000.0,"marca":"BMW","temporada":"Primavera"},
  ...
]
```

**Ejemplo — Ruteo hacia item-service:**

```bash
curl http://localhost:8090/api/items/items/1
```

**Respuesta obtenida:**

```json
{"id":1,"productId":1,"cantidad":2,"iva":6400,"total":46400,"fecha":"2026-05-01T00:00:00"}
```

**Filtros aplicados en cada ruteo:**

- `PreFilter` — registra el método HTTP, la URI y el servicio destino **antes** de enrutar. También agrega el header `X-Zuul-Gateway: zuul-server`.
- `PostFilter` — registra el código de respuesta HTTP **después** de recibir la respuesta del microservicio. Agrega el header `X-Zuul-Procesado: true`.

Log generado en zuul-server:

```
[PRE-FILTER]  Método: GET | URI: /api/productos/products/list | Servicio destino: product-service
[POST-FILTER] Respuesta enviada al cliente | Código HTTP: 200
```

---

## 2. Balanceo de Carga con Hystrix / Ribbon

**Descripción:** Se levantaron **dos instancias** de product-service (`:8081` y `:8082`), ambas registradas en Eureka con el mismo nombre de servicio `product-service`. Zuul utiliza **Ribbon** (integrado en el proxy de Zuul) para distribuir las peticiones en modo **round-robin** entre las instancias disponibles. Hystrix envuelve cada llamada con un Circuit Breaker, de modo que si una instancia falla, el tráfico se redirige a la otra sin que el cliente lo note.

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
  curl -s http://localhost:8090/api/productos/products/instance-info
  echo
done
```

**Respuesta obtenida — Ribbon alterna entre instancias en round-robin:**

```
{"puerto":8081,"ip":"172.18.0.7","host":"dac68ba973a2","servicio":"product-service"}
{"puerto":8081,"ip":"172.18.0.6","host":"b1be001239f4","servicio":"product-service"}
{"puerto":8081,"ip":"172.18.0.7","host":"dac68ba973a2","servicio":"product-service"}
{"puerto":8081,"ip":"172.18.0.6","host":"b1be001239f4","servicio":"product-service"}
{"puerto":8081,"ip":"172.18.0.7","host":"dac68ba973a2","servicio":"product-service"}
{"puerto":8081,"ip":"172.18.0.6","host":"b1be001239f4","servicio":"product-service"}
{"puerto":8081,"ip":"172.18.0.7","host":"dac68ba973a2","servicio":"product-service"}
{"puerto":8081,"ip":"172.18.0.6","host":"b1be001239f4","servicio":"product-service"}
```

Las IPs `172.18.0.7` y `172.18.0.6` corresponden a los contenedores `product-service-2` y `product-service-1` respectivamente. Ribbon distribuye exactamente la mitad del tráfico a cada instancia.

---

## 3. Recuperación de Errores con `@HystrixCommand`

**Descripción:** Cuando se realiza una llamada a un microservicio y éste falla (conexión rechazada, timeout, excepción), Hystrix intercepta el error y ejecuta automáticamente el **método alternativo (fallback)** indicado en la anotación `@HystrixCommand`. El cliente recibe una respuesta alternativa coherente en lugar de un error HTTP 500.

**Implementación** (`ProductProxyService.java`):

```java
@HystrixCommand(fallbackMethod = "obtenerProductoFallback")
public Object obtenerProducto(Long id) {
    return restTemplate.getForObject(
        "http://product-service/products/ver/" + id, Object.class);
}

public Object obtenerProductoFallback(Long id, Throwable e) {
    Map<String, Object> resp = new HashMap<>();
    resp.put("fallback", true);
    resp.put("id", id);
    resp.put("modelo", "PRODUCTO NO DISPONIBLE");
    resp.put("marca", "N/A");
    resp.put("precio", 0.0);
    resp.put("mensaje", "product-service falló. Datos alternativos retornados por @HystrixCommand.");
    return resp;
}
```

**Pasos para reproducir:**

```bash
# 1. Verificar funcionamiento normal
curl http://localhost:8090/proxy/productos/1
```

```json
{"id":1,"modelo":"Sedan X1","precio":20000.0,"marca":"Toyota","temporada":"Verano"}
```

```bash
# 2. Detener el servicio para simular fallo
docker stop product-service product-service-2

# 3. Llamar al mismo endpoint
curl http://localhost:8090/proxy/productos/1
```

**Respuesta con fallback activado:**

```json
{
  "fallback": true,
  "id": 1,
  "modelo": "PRODUCTO NO DISPONIBLE",
  "marca": "N/A",
  "precio": 0.0,
  "mensaje": "product-service falló. Datos alternativos retornados por @HystrixCommand.",
  "origen": "@HystrixCommand fallbackMethod"
}
```

```bash
# Verificar fallback en lista también
curl http://localhost:8090/proxy/productos
```

```json
{
  "fallback": true,
  "productos": [],
  "mensaje": "product-service no disponible. Lista vacía retornada por @HystrixCommand.",
  "origen": "@HystrixCommand fallbackMethod"
}
```

```bash
# 4. Restaurar el servicio
docker start product-service product-service-2
```

---

## 4. Recuperación por Latencia (`timeoutInMilliseconds`)

**Descripción:** Si un microservicio responde más lento de lo permitido (en este caso 1 segundo), Hystrix cancela la llamada y ejecuta el método alternativo **sin esperar a que el servicio termine**. Esto evita que peticiones lentas bloqueen hilos y degraden todo el sistema.

**Implementación** (`ProductProxyService.java`):

```java
@HystrixCommand(
    fallbackMethod = "listarLentoFallback",
    commandProperties = {
        @HystrixProperty(
            name  = "execution.isolation.thread.timeoutInMilliseconds",
            value = "1000"   // 1 segundo máximo
        )
    }
)
public Object listarProductosLento() {
    // /products/slow duerme 2 segundos → dispara el timeout de 1 s
    return restTemplate.getForObject(
        "http://product-service/products/slow", Object.class);
}

public Object listarLentoFallback(Throwable e) {
    Map<String, Object> resp = new HashMap<>();
    resp.put("fallback", true);
    resp.put("productos", Collections.emptyList());
    resp.put("mensaje", "El servicio tardó más de 1 segundo. Método alternativo activado por LATENCIA.");
    resp.put("timeout_ms", 1000);
    return resp;
}
```

**Endpoint lento simulado** (`ProductController.java`):

```java
@GetMapping("/products/slow")
public ResponseEntity<List<ProductDto>> listarSlow() throws InterruptedException {
    Thread.sleep(2000); // demora 2 segundos
    return ResponseEntity.ok(service.getProducts());
}
```

**Ejemplo:**

```bash
time curl http://localhost:8090/proxy/productos-lento
```

**Respuesta obtenida — fallback ejecutado en ~1 segundo:**

```json
{
  "fallback": true,
  "productos": [],
  "mensaje": "El servicio tardó más de 1 segundo. Método alternativo activado por LATENCIA.",
  "timeout_ms": 1000,
  "origen": "@HystrixCommand timeoutInMilliseconds=1000"
}
```

```
curl -s http://localhost:8090/proxy/productos-lento  0.01s user 0.01s system 2% cpu 1.062 total
```

El endpoint `/products/slow` tarda 2 segundos en responder. Hystrix cancela la llamada al cumplirse 1 segundo y retorna el fallback. El tiempo total de respuesta fue de **1.062 segundos**, confirmando que Hystrix no esperó los 2 segundos completos.
