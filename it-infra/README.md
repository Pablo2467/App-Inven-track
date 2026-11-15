# Inven-Track · Manual de implementación

Sistema de inventario para una salsamentaria. Este repositorio levanta toda la aplicación
(base de datos, API y portal) con un solo comando.

## 1. Requisitos
| Herramienta | Versión |
|---|---|
| Docker Desktop | 4.x o superior (con Docker Compose v2) |
| Git | 2.x |

No necesitas instalar Java, Maven, Node ni PostgreSQL: todo corre en contenedores.

## 2. Clonar los cuatro repositorios
Los cuatro deben quedar **uno al lado del otro**, porque el `docker-compose.yml`
busca `../it-api`, `../it-db` y `../it-portal`.

```powershell
mkdir inven-track
cd inven-track
git clone --branch v1.0.0 https://github.com/code-corhuila/it-api.git
git clone --branch v1.0.0 https://github.com/code-corhuila/it-db.git
git clone --branch v1.0.0 https://github.com/code-corhuila/it-portal.git
git clone --branch v1.0.0 https://github.com/code-corhuila/it-infra.git
```

Resultado:
```
inven-track/
├── it-api/
├── it-db/
├── it-portal/
└── it-infra/
```

## 3. Crear el archivo .env
```powershell
cd it-infra
Copy-Item .env.example .env
```
Los valores del ejemplo sirven tal cual para probar. En un entorno real, cambia
`POSTGRES_PASSWORD` y `JWT_SECRET` (mínimo 32 caracteres).

## 4. Arrancar
```powershell
docker compose up --build
```
La primera vez tarda **[mide tu tiempo real: X minutos]** (descarga imágenes y dependencias).
Está listo cuando en la terminal aparece que la API arrancó (`Started InvenTrackApplication`).

## 5. URL y puertos
| Servicio | URL |
|---|---|
| Portal | http://localhost:4200 |
| API | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |
| PostgreSQL | interno (no se expone al equipo) |

## 6. Usuarios de prueba
| Usuario | Clave | Rol | Puede |
|---|---|---|---|
| `dueno` | `*` | DUENO | Todo, incluida la gestión de usuarios |
| `empleada` | `*` | EMPLEADA | Operar inventario; no gestiona usuarios |

## 7. Correr las pruebas
**API** (desde la carpeta `it-api`):
```powershell
docker run --rm -v "${PWD}:/app" -v "${HOME}\.m2:/root/.m2" -w /app maven:3.9-eclipse-temurin-21 mvn clean verify
```
Verifica pruebas de servicio y controlador y la cobertura mínima del 85 %.
El reporte queda en `target/site/jacoco/index.html`.

**Colección de Postman:** importa `it-api/postman/<archivo>.json` y ejecútala con Collection Runner
(con la aplicación levantada).

**Portal:** [pendiente: comando de las pruebas del portal].

## 8. Detener y borrar los datos
```powershell
docker compose down        # detiene, conserva los datos
docker compose down -v     # detiene y BORRA la base de datos
```

## 9. Problemas frecuentes
**El puerto 8080 está ocupado.** Verás un error de "port is already allocated".
Averigua quién lo usa con `netstat -ano | findstr :8080` y ciérralo, o cambia `API_PORT` en `.env`.

**Compose dice "falta JWT_SECRET" (o POSTGRES_...).** No creaste el `.env`.
Ejecuta `Copy-Item .env.example .env` dentro de `it-infra`.

**Cambié los scripts de `it-db` y no se reflejan.** PostgreSQL ejecuta los scripts solo si el
volumen está vacío. Corre `docker compose down -v` y vuelve a levantar.

**El portal muestra un error de CORS.** El origen del portal no coincide con `CORS_ORIGINS`
del `.env`. Debe ser exactamente `http://localhost:4200`; corrige y reinicia con `docker compose up`.