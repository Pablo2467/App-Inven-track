
# Inven-Track

**Sistema Full Stack de gestión de inventario para una salsamentaria.**

Inven-Track permite administrar productos y proveedores, registrar entradas y salidas, consultar existencias por lote y visualizar alertas de bajo stock y vencimiento. Su objetivo es mejorar la trazabilidad del inventario y apoyar decisiones operativas sobre productos perecederos.

**Stack:** Angular 20 · TypeScript · Java 21 · Spring Boot 3.3.4 · Spring Security + JWT · PostgreSQL 16 · Docker Compose · JUnit/Mockito/MockMvc · JaCoCo.

![Vista del panel principal de Inven-Track](it-docs/inven-track/assets/dashboard.jpg)

## Funcionalidades principales

- **Autenticación y autorización:** inicio de sesión con JWT y roles `DUENO` y `EMPLEADA`.
- **Catálogo:** administración de productos, categorías, unidades y stock mínimo.
- **Proveedores:** registro y mantenimiento de información de abastecimiento.
- **Lotes:** registro y consulta de existencias, fechas de ingreso y vencimiento.
- **Entradas:** una entrada crea un lote y registra el movimiento en una transacción.
- **Salidas FIFO:** se descuentan primero los lotes vigentes más antiguos por fecha de ingreso. Las salidas ordinarias excluyen lotes vencidos.
- **Alertas y panel:** avisos de vencimiento, stock bajo y resumen del inventario.
- **Trazabilidad:** registro de movimientos, cantidades y usuario responsable.

> **Nota de alcance:** la API incluye administración de usuarios para el rol `DUENO`, pero las pantallas actuales del portal se centran en inicio, productos, lotes, proveedores, entradas y salidas.

## Arquitectura del proyecto

```text
inven-track/
├── it-api/       # API REST: Spring Boot, seguridad, servicios y pruebas
├── it-db/        # Esquema PostgreSQL, datos de demostración y diagrama ER
├── it-docs/      # Framework documental y documentación del proyecto
│   └── inven-track/
├── it-infra/     # Docker Compose, configuración y contenedores
└── it-portal/    # Cliente web Angular + TypeScript
```

El portal consume endpoints de la API bajo `/api/v1`; la API gestiona las reglas de negocio y persiste datos en PostgreSQL. Docker Compose inicia los servicios `db`, `api` y `portal`.

## Inicio rápido con Docker

**Requisito:** Docker Desktop con Docker Compose v2. No es necesario instalar Maven, Java, Node.js ni PostgreSQL en Windows para este modo de ejecución.

```powershell
git clone https://github.com/Pablo2467/App-Inven-track.git
cd App-Inven-track\it-infra
Copy-Item .env.example .env
```

Antes de iniciar, **reemplaza las credenciales de ejemplo** en `it-infra/.env`, especialmente `POSTGRES_PASSWORD` y `JWT_SECRET` (secreto aleatorio de al menos 32 caracteres). Nunca subas el `.env` real a GitHub.

```powershell
docker compose up -d --build
docker compose ps
```

| Servicio | Dirección local |
|---|---|
| Portal | http://localhost:4200 |
| API | http://localhost:8080 |
| Documentación interactiva Swagger | http://localhost:8080/swagger-ui.html |
| PostgreSQL | Puerto interno del contenedor (no publicado por defecto) |

Se incluyen cuentas de **demostración** `dueno` y `empleada` en el script SQL de datos iniciales, para un entorno local. No reutilices esas cuentas ni contraseñas en producción.

Para detener los servicios preservando los datos:

```powershell
docker compose down
```

Más detalles: [instalación y operación](it-docs/inven-track/03-instalacion-y-operacion.md).

## Pruebas y calidad

En los **reportes incluidos en la copia revisada del proyecto** se registraron **126 pruebas backend, 0 fallos y 0 errores**, y **88,01 % de cobertura de líneas** con JaCoCo (455 de 517 líneas contabilizadas). Estos resultados corresponden a esos reportes; deben regenerarse después de modificar el código.

Desde `it-api/`, con Java 21 y Maven disponibles:

```powershell
mvn clean verify
```

Sin Maven local, se pueden ejecutar pruebas mediante la imagen oficial de Maven; consulta el [procedimiento de pruebas](it-docs/inven-track/05-pruebas-y-calidad.md).

## Documentación

| Documento | Contenido |
|---|---|
| [Inicio de documentación](it-docs/inven-track/README.md) | Índice y alcance |
| [Contexto y funcionalidades](it-docs/inven-track/01-contexto-y-alcance.md) | Problema, objetivos, actores y reglas de negocio |
| [Arquitectura](it-docs/inven-track/02-arquitectura.md) | Componentes, flujos, persistencia y decisiones técnicas |
| [Instalación](it-docs/inven-track/03-instalacion-y-operacion.md) | Configuración, despliegue y resolución de errores |
| [API y seguridad](it-docs/inven-track/04-api-y-seguridad.md) | Endpoints, roles, JWT, Swagger y ejemplos |
| [Pruebas](it-docs/inven-track/05-pruebas-y-calidad.md) | Ejecución, alcance, cobertura y limitaciones |
| [Manual de usuario](it-docs/inven-track/06-manual-de-usuario.md) | Uso de las pantallas y flujos operativos |
| [Próximos pasos](it-docs/inven-track/07-pendientes-y-riesgos.md) | Mejoras y condiciones para producción |

## Capturas

| Autenticación | Productos |
|---|---|
| ![Login](it-docs/inven-track/assets/login.jpg) | ![Catálogo](it-docs/inven-track/assets/productos.jpg) |

| Lotes | Entradas y salidas |
|---|---|
| ![Lotes](it-docs/inven-track/assets/lotes.jpg) | ![Salidas](it-docs/inven-track/assets/salidas.jpg) |

## Estado y alcance

Proyecto funcional en un entorno **local de demostración**. La disponibilidad pública, el monitoreo de producción, la accesibilidad integral, las pruebas de seguridad y las pruebas de extremo a extremo requieren verificación adicional. Las capturas y datos de demostración no deben interpretarse como un entorno productivo real.

## Créditos de documentación

`it-docs/` incluye un marco documental de terceros, **Monolith Governance Framework**, de Jesus Ariel Gonzalez Bonilla. Se conservaron sus archivos originales y su licencia; esta documentación de Inven-Track se incorpora en `it-docs/inven-track/` como contenido específico del proyecto.

**Proyecto:** Inven-Track · **Repositorio:** [Pablo2467/App-Inven-track](https://github.com/Pablo2467/App-Inven-track).
