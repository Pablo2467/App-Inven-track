# Inven-Track · Base de datos

Modelo de datos de Inven-Track (PostgreSQL 16): scripts de creación, datos semilla y diagrama entidad-relación.

## Contenido
| Archivo | Descripción |
|---|---|
| `scripts/01_schema.sql` | Crea las 7 tablas: users, suppliers, products, lots, entries, outputs, inventory_movements |
| `scripts/02_seed.sql` | Datos de demostración: un usuario por rol (dueno, empleada), 2 proveedores, 4 productos y 5 lotes |
| `docs/er-diagram.png` | Diagrama entidad-relación |

## Cómo se usa
Este repositorio no se ejecuta solo: `it-infra` monta la carpeta `scripts/` en
`/docker-entrypoint-initdb.d` del contenedor de PostgreSQL. Los scripts corren en orden
alfabético, una sola vez, cuando el volumen de datos está vacío.

Para reiniciar la base desde cero: `docker compose down -v` (desde `it-infra`).

## Usuarios semilla
`dueno` (rol DUENO) y `empleada` (rol EMPLEADA). Las claves se guardan con BCrypt;
las claves de prueba están en el manual de implementación de `it-infra`.