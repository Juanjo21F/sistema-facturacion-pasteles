# Sistema de Facturación de Pasteles — Arquitectura en Capas

API REST para una pastelería: productos, clientes, facturación con control de inventario, anulación de facturas y reporte
mensual de productos más vendidos. La autenticación y los roles los entrega un servicio externo (`authcore-service`).

## 1. Tecnologías
Java 21 · Spring Boot 3.3 · Maven · Spring Web · Spring Data JPA / Hibernate · Bean Validation ·
H2 (desarrollo) / PostgreSQL (producción) · JUnit 5 + AssertJ + Mockito. Sin Lombok.

> Las tres arquitecturas del repositorio exponen **exactamente la misma API REST, la misma base de datos y las mismas reglas
> de negocio**; solo cambia cómo está organizado el código por dentro.

## 2. Arquitectura: N capas (estricta)

```
┌──────────────────────────────┐
│  presentation   (HTTP, DTOs) │   solo conoce a business
├──────────────────────────────┤
│  business   (reglas, tx)     │   conoce a dataaccess e integration
├───────────────┬──────────────┤
│  dataaccess   │ integration  │   no conocen capas superiores
│  (JPA, BD)    │ (authcore)   │
└───────────────┴──────────────┘
```

| Capa | Responsabilidad | Paquete |
|---|---|---|
| **Presentación** | Controladores REST, DTOs de entrada con validación, manejo de errores HTTP, chequeo de rol por endpoint | `presentation` |
| **Negocio** | **Todas** las reglas: servicios de productos, clientes, inventario, facturación, reporte, auditoría y autenticación. Define las transacciones. Entrega DTOs (nunca entidades) | `business` |
| **Acceso a datos** | Entidades JPA (solo datos, sin reglas) y repositorios Spring Data | `dataaccess` |
| **Integración** | Cliente de `authcore-service` (real y simulado) | `integration` |

Reglas del patrón:
- Cada capa solo llama a la inmediatamente inferior: la **presentación jamás importa `dataaccess` ni `integration`**; si necesita
  algo, lo pide a un servicio de negocio. Esto se verifica automáticamente en `DependenciasEntreCapasTest`.
- Las entidades son *anémicas* (getters/setters): la lógica vive en la capa de negocio (`InventarioService`, `FacturacionService`...).
- La capa de negocio convierte entidades → DTOs (`EntityMapper`) y la de presentación convierte requests → comandos (`RequestMapper`).

## 2.1 Estructura de paquetes
```
com.pasteleria.facturacion
├── presentation/  controller · request · mapper · handler (errores) · security (roles) · error
├── business/      service · dto (comandos y respuestas) · mapper · validation · security · exception
├── dataaccess/    entity (JPA, sin lógica) · repository (consultas JOIN FETCH, FOR UPDATE)
├── integration/   authcore (AuthcoreClient · Http · Mock)
└── config/        reloj e interceptor web
```

## 3. Cómo ejecutar
Requisitos: JDK 21 y Maven 3.9+.
```bash
mvn spring-boot:run            # H2 en memoria + authcore simulado (solo desarrollo)
mvn test                       # pruebas unitarias
```
La API queda en `http://localhost:8080`. Ejemplo:
```bash
curl -X POST localhost:8080/api/productos -H "Authorization: Bearer admin-token" -H "Content-Type: application/json" \
  -d '{"codigoUnico":"TOR-001","nombre":"Torta de chocolate","idCategoria":1,"precio":45000,"stock":10}'
```

## 4. Base de datos
- **Desarrollo**: H2 en memoria (modo PostgreSQL). Sin configuración.
- **PostgreSQL**: crear la base y ejecutar con el perfil `postgres`:
  ```bash
  createdb pasteleria
  export SPRING_PROFILES_ACTIVE=postgres DB_URL=jdbc:postgresql://localhost:5432/pasteleria DB_USER=... DB_PASSWORD=...
  mvn spring-boot:run
  ```
- Los scripts `src/main/resources/db/schema.sql` (tablas, PK, FK, UNIQUE, NOT NULL, CHECK, índices, secuencia de facturas)
  y `db/data.sql` (categorías) se ejecutan al iniciar; son idempotentes. Hibernate no modifica el esquema (`ddl-auto=none`).
- Tablas: `categorias`, `clientes`, `productos`, `facturas`, `detalles_factura`.
  Restricciones: `precio > 0`, `stock >= 0`, `cantidad > 0`, `codigo_unico`, `documento_identidad` y `numero_factura` UNIQUE.

## 5. Configurar authcore-service
Variables de entorno (ver `application.properties`):

| Variable | Significado | Por defecto |
|---|---|---|
| `AUTHCORE_MOCK_ENABLED` | `true` = authcore simulado (tokens `admin-token`, `empleado-token`) | `true` |
| `AUTHCORE_BASE_URL` | URL de authcore-service | `http://localhost:9000` |
| `AUTHCORE_VALIDATE_PATH` | Endpoint de validación | `/api/auth/validate` |
| `AUTHCORE_TIMEOUT_MS` | Timeout de conexión/lectura | `2000` |

**En producción poner `AUTHCORE_MOCK_ENABLED=false`.** Contrato asumido (editable en `AuthcoreHttpAdapter`, es el único sitio):
`GET {base}{path}` con `Authorization: Bearer <token>` → `200 {"userId","username","role":"ADMINISTRADOR|EMPLEADO"}`; `401/403` si el token no sirve.
Cada petición a `/api/**` debe traer el encabezado `Authorization: Bearer <token>`.
Para cifrar datos en tránsito active HTTPS (líneas `server.ssl.*` comentadas) o termine TLS en un proxy.

## 6. Endpoints
| Método | Ruta | Rol |
|---|---|---|
| GET | `/api/productos?soloDisponibles=true` · `/api/productos/{id}` | ADMIN, EMPLEADO |
| POST / PUT / DELETE | `/api/productos` · `/api/productos/{id}` (DELETE = desactivar) | ADMIN |
| GET, POST, PUT, DELETE | `/api/clientes` · `/api/clientes/{id}` (DELETE = desactivar) | ADMIN, EMPLEADO |
| POST | `/api/facturas` | ADMIN, EMPLEADO |
| GET | `/api/facturas/{id}` | ADMIN, EMPLEADO |
| PUT | `/api/facturas/{id}/anular` | ADMIN |
| GET | `/api/reportes/ventas?mes=10&anio=2026` | ADMIN |

Crear factura (el total NO se envía; lo calcula el backend):
```json
{ "idCliente": 1, "detalles": [ { "idProducto": 1, "cantidad": 2 }, { "idProducto": 2, "cantidad": 3 } ] }
```
Errores: JSON `{timestamp,status,error,mensaje,detalles}`; 400 validación · 401 sin/ con token inválido · 403 rol insuficiente ·
404 no existe · 409 duplicado · 422 regla de negocio (p. ej. stock insuficiente) · 503 authcore caído. Nunca se devuelven stack traces.

## 7. Roles
- **ADMINISTRADOR**: CRUD de productos, clientes, registro y anulación de ventas, inventario (stock en productos), reportes.
- **EMPLEADO**: clientes, registrar/consultar facturas y consultar productos (necesita verlos para vender). No puede crear/editar/desactivar productos, anular facturas ni ver reportes.
- *Decisión a confirmar*: el enunciado no dice quién anula facturas; se dejó solo al ADMINISTRADOR (control financiero). Para permitirlo al EMPLEADO basta cambiar `@RolesPermitidos` en `anular` del controlador de facturas.

## 8. Reglas de negocio clave
- Código de producto, documento de cliente y número de factura únicos. Precio > 0, stock ≥ 0, cantidad > 0.
- Productos o clientes inactivos no se pueden usar en nuevas ventas.
- Venta: valida cliente y productos, **bloquea las filas de producto** (`SELECT … FOR UPDATE`, ordenadas por id), verifica stock de *todas* las líneas antes de descontar, calcula subtotales y total, descuenta stock y guarda factura + detalles **en una sola transacción**; cualquier fallo hace rollback.
- No se permite repetir el mismo producto en una factura (se rechaza con un mensaje claro).
- Anulación: bloquea la factura, cambia a ANULADA y restaura stock **una sola vez** (segunda anulación → `FacturaYaAnuladaException`). Las anuladas no entran al reporte.
- Operaciones críticas (venta, anulación, desactivaciones) se registran en el log `AUDITORIA` con usuario y rol.
- El número de factura es `FAC-<año>-<secuencia de 6 dígitos>`, tomada de una secuencia de base de datos.

## 9. Pruebas
`mvn test` — 15 pruebas:
- `ProductoYClienteServiceTest` (pruebas 1–6), `FacturacionServiceTest` (7–14) y `ReporteServiceTest` (15–16): la capa de negocio
  aislada, con la capa de datos simulada con Mockito.
- `DependenciasEntreCapasTest`: falla si una capa importa a otra que no debe.
- `ApiIntegrationTest`: recorre toda la API con MockMvc + H2.
