-- =====================================================================
-- Inven-Track · datos semilla (DEMOSTRACIÓN)
-- Un usuario por rol. Las claves se guardan con BCrypt (nunca en texto plano).
-- Las fechas son relativas a hoy para que las alertas de vencimiento se vean.
-- =====================================================================
SET TIME ZONE 'America/Bogota';

INSERT INTO users (full_name, email, username, password_hash, role, status, created_at, updated_at) VALUES
  ('Dueño de la Salsamentaria (DEMO)', 'dueno@inventrack.local',    'dueno',    '$2a$10$1vi26UxygM6wZqQsF.ofOOPEPoyUXF//rcu6IN4w7kCJmOHlyCP5q', 'DUENO',    'ACTIVE', LOCALTIMESTAMP, LOCALTIMESTAMP),
  ('Empleada de Mostrador (DEMO)',     'empleada@inventrack.local', 'empleada', '$2a$10$8UzrDX0ovPoyvf.Md/b57.d4Qaistf/7mm8cJw4rE0y3yktr.Qi5K', 'EMPLEADA', 'ACTIVE', LOCALTIMESTAMP, LOCALTIMESTAMP);

INSERT INTO suppliers (name, tax_id, phone, email, address, status, created_at, updated_at) VALUES
  ('Distribuciones del Huila (DEMO)', '900123456-1', '3001234567', 'ventas@distribuciones.test', 'Neiva, Huila', 'ACTIVE', LOCALTIMESTAMP, LOCALTIMESTAMP),
  ('Lácteos La Sabana (DEMO)',        '900765432-5', '3109876543', 'pedidos@lacteos.test',       'Neiva, Huila', 'ACTIVE', LOCALTIMESTAMP, LOCALTIMESTAMP);

INSERT INTO products (code, name, description, category, unit, min_stock, status, created_at, updated_at) VALUES
  ('JAM-001', 'Jamón de cerdo',       'Jamón en bloque (DEMO)',  'Embutidos', 'kg',      5, 'ACTIVE', LOCALTIMESTAMP, LOCALTIMESTAMP),
  ('QUE-001', 'Queso mozzarella',     'Queso para pizza (DEMO)', 'Lácteos',   'kg',      4, 'ACTIVE', LOCALTIMESTAMP, LOCALTIMESTAMP),
  ('SAL-001', 'Salchicha ranchera',   'Paquete x 10 (DEMO)',     'Embutidos', 'paquete', 8, 'ACTIVE', LOCALTIMESTAMP, LOCALTIMESTAMP),
  ('CHO-001', 'Chorizo santarrosano', 'Chorizo fresco (DEMO)',   'Embutidos', 'kg',      3, 'ACTIVE', LOCALTIMESTAMP, LOCALTIMESTAMP);

-- Lotes. JAM-A y JAM-B permiten ver FIFO; QUE-A vence pronto; CHO-A ya está vencido.
-- (JAM-A y QUE-A ya tienen salidas, por eso su disponible es menor que el inicial.)
INSERT INTO lots (lot_number, product_id, supplier_id, initial_quantity, available_quantity, entry_date, expiration_date, status, created_at, updated_at)
SELECT v.lot, p.id, s.id, v.initial, v.available, CURRENT_DATE - v.days_ago, CURRENT_DATE + v.days_to_expire, 'AVAILABLE', LOCALTIMESTAMP, LOCALTIMESTAMP
FROM (VALUES
  ('JAM-A', 'JAM-001', '900123456-1', 20, 12, 10,  20),
  ('JAM-B', 'JAM-001', '900123456-1', 30, 30,  3,  27),
  ('QUE-A', 'QUE-001', '900765432-5', 12,  3,  8,   4),
  ('SAL-A', 'SAL-001', '900123456-1', 40, 40,  2,  28),
  ('CHO-A', 'CHO-001', '900123456-1',  6,  6, 25,  -2)
) AS v(lot, product_code, supplier_tax, initial, available, days_ago, days_to_expire)
JOIN products p  ON p.code   = v.product_code
JOIN suppliers s ON s.tax_id = v.supplier_tax;

-- Una entrada por lote (la cantidad de la entrada es la inicial del lote).
INSERT INTO entries (product_id, supplier_id, lot_id, quantity, entry_date, user_id, notes, created_at, updated_at)
SELECT l.product_id, l.supplier_id, l.id, l.initial_quantity, l.entry_date, u.id, 'DATO DE DEMOSTRACIÓN', LOCALTIMESTAMP, LOCALTIMESTAMP
FROM lots l CROSS JOIN users u
WHERE u.username = 'dueno';

INSERT INTO inventory_movements (movement_type, product_id, lot_id, entry_id, quantity, user_id, created_at, updated_at)
SELECT 'ENTRY', e.product_id, e.lot_id, e.id, e.quantity, e.user_id, LOCALTIMESTAMP, LOCALTIMESTAMP
FROM entries e;

-- Dos ventas de mostrador, descontadas de los lotes más antiguos (FIFO).
INSERT INTO outputs (product_id, quantity, reason, notes, exit_date, user_id, created_at, updated_at)
SELECT p.id, v.qty, 'SALE', 'Venta mostrador (DEMO)', CURRENT_DATE, u.id, LOCALTIMESTAMP, LOCALTIMESTAMP
FROM (VALUES ('JAM-001', 8), ('QUE-001', 9)) AS v(product_code, qty)
JOIN products p ON p.code = v.product_code
CROSS JOIN users u
WHERE u.username = 'dueno';

INSERT INTO inventory_movements (movement_type, product_id, lot_id, output_id, quantity, user_id, created_at, updated_at)
SELECT 'EXIT', o.product_id, l.id, o.id, o.quantity, o.user_id, LOCALTIMESTAMP, LOCALTIMESTAMP
FROM outputs o
JOIN lots l ON l.product_id = o.product_id AND l.lot_number IN ('JAM-A', 'QUE-A');