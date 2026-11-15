# Inven-Track

Sistema web de gestión de inventario para una salsamentaria: productos, proveedores, usuarios, lotes, entradas, salidas con **FIFO**, alertas de vencimiento, movimientos con trazabilidad y panel ejecutivo. Monolito modular con Spring Boot + MySQL + JWT, y frontend web que consume la API REST.

> **Estado de verificación (léelo):** el código se escribió completo, pero **todavía no se ha compilado ni ejecutado**. Lo que sí se comprobó: sintaxis de `app.js` (Node), validez del `openapi.yaml` y sintaxis de 77 de los 81 `.java` con un parser (los otros 4 usan `record`/`switch` modernos que ese parser no entiende, así que quedaron sin comprobar). Las pruebas existen, pero **no se han corrido**; no se afirma que pasen hasta que tú las ejecutes (paso 7). Si algo falla, pégame el error completo y lo corrijo.

## 1. Requisitos (Windows)

| Herramienta | Versión | Comprobar |
|---|---|---|
| JDK | **21 (LTS)** | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| MySQL Server | 8.0+ | `mysql --version` |

Decisión de versión: Spring Boot 3.3.4 **no soporta Java 25** (llega hasta 23). Usa JDK 21. Si `java -version` muestra 25, instala JDK 21 y apunta `JAVA_HOME` a él.

## 2. Ubicación

Descomprime en `C:\Users\equipo\Documents\inven-track` y abre esa carpeta en VS Code (**File > Open Folder**). Instala la extensión **Extension Pack for Java**.

## 3. Base de datos

Si ya usaste una versión anterior del proyecto, borra su base (el esquema cambió):

```sql
DROP DATABASE IF EXISTS inven_track;
```

No hace falta crear la base nueva: la aplicación la crea (`createDatabaseIfNotExist=true`) y Liquibase crea las tablas.

## 4. Configuración

En **Terminal > New Terminal** (PowerShell), dentro de la carpeta del proyecto:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "TU_CLAVE_DE_MYSQL"
$env:JWT_SECRET  = "una-clave-larga-y-aleatoria-de-mas-de-32-caracteres"
```

Variables opcionales: `DB_URL`, `PORT` (3000 por defecto), `CORS_ORIGINS`, `SEED_ENABLED`, `SEED_OWNER_PASSWORD`, `SEED_EMPLOYEE_PASSWORD`.

## 4b. Ejecutar

```powershell
mvn spring-boot:run
```

Abre **http://localhost:3000**.

**Usuarios de DEMOSTRACIÓN** (se crean solo si la tabla `users` está vacía; cambia las claves con "Cambiar contraseña"):

| Usuario | Contraseña | Rol |
|---|---|---|
| `dueno` | `Dueno123*` | DUEÑO |
| `empleada` | `Empleada123*` | EMPLEADA |

También se cargan productos, proveedores, lotes y salidas **de demostración** (marcados con "DEMO"). Para arrancar sin ellos: `$env:SEED_ENABLED="false"` (entonces no se crea ningún usuario y deberás insertar uno).

## 5. Roles

| Acción | DUEÑO | EMPLEADA |
|---|---|---|
| Consultar productos, proveedores, lotes, existencias, alertas, movimientos, panel | ✔ | ✔ |
| Registrar entradas y salidas | ✔ | ✔ |
| Crear/editar/eliminar productos, proveedores, lotes | ✔ | ✘ |
| Administrar usuarios | ✔ | ✘ |

El usuario responsable de cada operación sale **del token**. Si el cliente envía `X-User-Id` y no coincide con el token, responde 403. El rol y el estado del usuario se verifican en la base de datos en cada petición (un usuario desactivado pierde acceso de inmediato).

## 6. Reglas de negocio clave

- **FIFO:** la salida descuenta primero el lote con fecha de ingreso más antigua (`ORDER BY entry_date, id`). Los lotes se bloquean con `SELECT … FOR UPDATE` dentro de una transacción: dos usuarios no pueden descontar las mismas unidades y, si algo falla, se revierte todo. Si pides más de lo disponible: 409 con el detalle.
- Las salidas normales **no usan lotes vencidos**. El motivo `EXPIRED_DISPOSAL` (baja por vencimiento) usa solo lotes vencidos.
- Vencimiento por defecto: **ingreso + 30 días**; se puede indicar otra fecha.
- Alertas: "próximo a vencer" = dentro de 7 días (configurable en pantalla o con `app.alerts.expiring-days`). Verde = sin alerta, amarillo = por vencer o stock bajo, rojo = vencido o sin existencias.
- Eliminar un producto/proveedor con historial lo **desactiva**; los movimientos nunca se pierden.
- Contraseñas con BCrypt; nunca salen en la API.

## 7. Pruebas

```powershell
mvn test                          # pruebas unitarias (no necesitan MySQL)
mvn test -Dtest=ApiFlowIT         # integración real contra MySQL (requiere la app configurada y usuarios DEMO)
```

Cubren: productos, proveedores, usuarios, BCrypt, JWT (8 h, firma, expiración), filtro de autenticación (`X-User-Id`, usuario desactivado), autorización por rol (`AuthorizationWebTest`), entradas (+30 días), salidas FIFO, rechazo por falta de stock, alertas y semáforo. `ApiFlowIT` recorre el flujo completo por HTTP contra MySQL (persistencia real).

## 8. Estructura

```
src/main/java/com/inventrack
  config/      SecurityConfig, ClockConfig, DemoDataSeeder
  security/    JwtService, JwtAuthFilter, AuthenticatedUser, JsonErrorWriter
  controller/  Auth, Product, Supplier, User, Lot, StockEntry, StockExit, Report (inventario/alertas/movimientos/dashboard)
  service/     lógica de negocio (FIFO en StockExitService)
  entity/ repository/ dto/ mapper/ exception/
src/main/resources
  db/changelog/  master.xml + 001/002/003 (Liquibase)
  static/        index.html, styles.css, app.js (frontend)
openapi.yaml     contrato OpenAPI 3.1
```

## 9. Cambios respecto al contrato/prompt original

- Rol `DUEÑO` se llama **`DUENO`** en la API y MySQL (evita problemas de codificación); en pantalla se muestra "DUEÑO".
- Java **21** en lugar de 25 (ver requisitos).
- `ddl-auto=none`: el esquema lo gestiona solo Liquibase.
- `POST /batches` crea lote + entrada + movimiento (un lote nace siempre de una entrada, así no hay stock sin trazabilidad).
- Se añadieron `/auth/login`, `/auth/me`, `/auth/change-password` y `/dashboard`.
- Los listados devuelven `{content, page, size, totalElements, totalPages}`.
- Los selectores de producto/proveedor del frontend cargan hasta 100 registros activos.

## 10. Problemas frecuentes

- `Access denied for user 'root'`: revisa `DB_USERNAME`/`DB_PASSWORD`.
- `JWT_SECRET debe tener al menos 32 caracteres`: usa una clave más larga.
- `release version 21 not supported`: tu JDK es anterior a 21.
- Puerto 3000 ocupado: `$env:PORT="3001"` (y agrega ese origen a `CORS_ORIGINS` si usas otro frontend).
- Producción: define `JWT_SECRET`, cambia las claves DEMO, usa `SEED_ENABLED=false` y HTTPS.
