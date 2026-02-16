-- ECOMMERCE APPLICATION TEST DATA SEEDER
-- This script populates the database with realistic test data for development and testing
-- Run this AFTER executing schema.sql
-- Usage: psql -U postgres -d ecommerce -f migrations/seed.sql

-- ========== USER DATA SEEDING ==========

-- Insert roles
INSERT INTO role (id, name, is_active) VALUES
(1, 'MANAGER', TRUE),
(2, 'CLIENT', TRUE),
(3, 'WAREHOUSE', TRUE),
(4, 'SHIPPING', TRUE)
ON CONFLICT DO NOTHING;

-- Insert system users (admin, manager, warehouse staff, clients)
-- Note: These passwords are all the same = 'ravn'

INSERT INTO sys_user (username, hashed_password, role_id, is_active, created_at, last_updated_password) VALUES
-- MANAGER role
('admin_manager', '$2a$10$sFB3sa0qNz8afW/pnpfyfOCb26fZC/3SH9Ut6/marjQ8MYqVvSP5y', 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('carlos_manager', '$2a$10$sFB3sa0qNz8afW/pnpfyfOCb26fZC/3SH9Ut6/marjQ8MYqVvSP5y', 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- WAREHOUSE role
('warehouse_admin', '$2a$10$sFB3sa0qNz8afW/pnpfyfOCb26fZC/3SH9Ut6/marjQ8MYqVvSP5y', 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('juan_warehouse', '$2a$10$sFB3sa0qNz8afW/pnpfyfOCb26fZC/3SH9Ut6/marjQ8MYqVvSP5y', 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- SHIPPING role
('shipping_coordinator', '$2a$10$sFB3sa0qNz8afW/pnpfyfOCb26fZC/3SH9Ut6/marjQ8MYqVvSP5y', 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- CLIENT role
('client_maria', '$2a$10$sFB3sa0qNz8afW/pnpfyfOCb26fZC/3SH9Ut6/marjQ8MYqVvSP5y', 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('client_pedro', '$2a$10$sFB3sa0qNz8afW/pnpfyfOCb26fZC/3SH9Ut6/marjQ8MYqVvSP5y', 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('client_ana', '$2a$10$sFB3sa0qNz8afW/pnpfyfOCb26fZC/3SH9Ut6/marjQ8MYqVvSP5y', 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- Insert person records (linked to users)
INSERT INTO person (id, user_id, first_name, last_name, email, phone, document, document_type, is_active, created_at) VALUES
-- Manager
(1, 1, 'Ricardo', 'González Flores', 'ricardo.gonzalez@ecommerce.com', '+51978123456', '12345678', 'PERSON', TRUE, CURRENT_TIMESTAMP),
(2, 2, 'Carlos', 'Martínez López', 'carlos.martinez@ecommerce.com', '+51987654321', '87654321', 'PERSON', TRUE, CURRENT_TIMESTAMP),
-- Warehouse
(3, 3, 'Diego', 'Rodríguez Pérez', 'diego.rodriguez@warehouse.com', '+51912345678', '11223344', 'PERSON', TRUE, CURRENT_TIMESTAMP),
(4, 4, 'Juan', 'Sánchez Torres', 'juan.sanchez@warehouse.com', '+51923456789', '55667788', 'PERSON', TRUE, CURRENT_TIMESTAMP),
-- Shipping
(5, 5, 'Fernando', 'Vega Morales', 'fernando.vega@shipping.com', '+51934567890', '99887766', 'PERSON', TRUE, CURRENT_TIMESTAMP),
-- Clients
(6, 6, 'María', 'García Ruiz', 'maria.garcia@gmail.com', '+51998765432', '13579246', 'PERSON', TRUE, CURRENT_TIMESTAMP),
(7, 7, 'Pedro', 'López Fernández', 'pedro.lopez@hotmail.com', '+51954321098', '24681357', 'PERSON', TRUE, CURRENT_TIMESTAMP),
(8, 8, 'Ana', 'Díaz Ramírez', 'ana.diaz@yahoo.com', '+51987123654', '35792468', 'PERSON', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ========== PRODUCT DATA SEEDING ==========

-- Insert categories
INSERT INTO category (id, created_by, name, description, is_active, created_at) VALUES
(1, 1, 'Electrónica', 'Dispositivos electrónicos y accesorios', TRUE, CURRENT_TIMESTAMP),
(2, 1, 'Ropa y Accesorios', 'Prendas de vestir y accesorios de moda', TRUE, CURRENT_TIMESTAMP),
(3, 1, 'Hogar y Jardín', 'Artículos para el hogar y jardín', TRUE, CURRENT_TIMESTAMP),
(4, 1, 'Deportes y Recreación', 'Equipos deportivos y de recreación', TRUE, CURRENT_TIMESTAMP),
(5, 1, 'Libros y Educación', 'Libros, material educativo y más', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert products
INSERT INTO product (id, created_by, name, description, price, is_active, created_at, updated_at, deleted_at) VALUES
(1, 1, 'Laptop HP Pavilion 15.6"', 'Laptop HP Pavilion con procesador Intel i5, 8GB RAM, 256GB SSD. Perfecta para estudiantes y profesionales', 1299.99, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(2, 1, 'Auriculares Inalámbricos Sony', 'Auriculares Bluetooth con cancelación de ruido activo, batería 24h', 249.50, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(3, 1, 'USB-C Hub 7 en 1', 'Hub USB-C multifunción con HDMI, USB 3.0, lector de tarjetas', 45.99, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(4, 1, 'Polo de Algodón Premium', 'Polo de algodón 100% puro, disponible en varios colores', 39.99, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(5, 1, 'Zapatillas Deportivas Runner', 'Zapatillas técnicas para correr, con tecnología de amortiguación', 89.99, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(6, 1, 'Lámpara LED de Escritorio', 'Lámpara LED ajustable con 3 modos de iluminación, control táctil', 34.50, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(7, 1, 'Pelota de Fútbol Profesional', 'Balón de fútbol reglamentario, material sintético de alta calidad', 59.99, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(8, 1, 'El Quijote - Cervantes', 'Edición de bolsillo del clásico de la literatura española', 12.99, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(9, 1, 'Mochila Backpack Gris', 'Mochila resistente con compartimientos múltiples y puerto USB', 64.95, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(10, 1, 'Monitor LG 24" IPS', 'Monitor 24 pulgadas Full HD, 75Hz, ideal para diseño y gaming', 199.99, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL)
ON CONFLICT DO NOTHING;

-- Insert product-category mappings
INSERT INTO product_category (product_id, category_id, created_at) VALUES
(1, 1, CURRENT_TIMESTAMP),  -- Laptop -> Electrónica
(2, 1, CURRENT_TIMESTAMP),  -- Auriculares -> Electrónica
(3, 1, CURRENT_TIMESTAMP),  -- USB Hub -> Electrónica
(4, 2, CURRENT_TIMESTAMP),  -- Polo -> Ropa
(5, 2, CURRENT_TIMESTAMP),  -- Zapatillas -> Ropa
(6, 3, CURRENT_TIMESTAMP),  -- Lámpara -> Hogar
(7, 4, CURRENT_TIMESTAMP),  -- Pelota -> Deportes
(8, 5, CURRENT_TIMESTAMP),  -- Libro -> Libros
(9, 2, CURRENT_TIMESTAMP),  -- Mochila -> Ropa
(10, 1, CURRENT_TIMESTAMP)  -- Monitor -> Electrónica
ON CONFLICT DO NOTHING;

-- Insert tags
INSERT INTO tag (id, name, is_active, created_at) VALUES
(1, 'Nuevo', TRUE, CURRENT_TIMESTAMP),
(2, 'Bestseller', TRUE, CURRENT_TIMESTAMP),
(3, 'Oferta', TRUE, CURRENT_TIMESTAMP),
(4, 'Eco-amigable', TRUE, CURRENT_TIMESTAMP),
(5, 'Premium', TRUE, CURRENT_TIMESTAMP),
(6, 'Envío Gratis', TRUE, CURRENT_TIMESTAMP),
(7, 'En Stock', TRUE, CURRENT_TIMESTAMP),
(8, 'Tendencia', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert product-tag mappings
INSERT INTO product_tag (product_id, tag_id, created_at) VALUES
(1, 1, CURRENT_TIMESTAMP),  -- Laptop -> Nuevo
(1, 5, CURRENT_TIMESTAMP),  -- Laptop -> Premium
(2, 2, CURRENT_TIMESTAMP),  -- Auriculares -> Bestseller
(2, 6, CURRENT_TIMESTAMP),  -- Auriculares -> Envío Gratis
(3, 3, CURRENT_TIMESTAMP),  -- USB Hub -> Oferta
(4, 1, CURRENT_TIMESTAMP),  -- Polo -> Nuevo
(4, 8, CURRENT_TIMESTAMP),  -- Polo -> Tendencia
(5, 7, CURRENT_TIMESTAMP),  -- Zapatillas -> En Stock
(6, 4, CURRENT_TIMESTAMP),  -- Lámpara -> Eco-amigable
(7, 2, CURRENT_TIMESTAMP),  -- Pelota -> Bestseller
(8, 1, CURRENT_TIMESTAMP),  -- Libro -> Nuevo
(10, 5, CURRENT_TIMESTAMP)  -- Monitor -> Premium
ON CONFLICT DO NOTHING;

-- Insert product images
INSERT INTO product_image (id, product_id, created_by, image_url, is_primary_image, is_active, created_at) VALUES
(1, 1, 1, 'https://via.placeholder.com/500x500?text=Laptop+HP', TRUE, TRUE, CURRENT_TIMESTAMP),
(2, 1, 1, 'https://via.placeholder.com/500x500?text=Laptop+Lateral', FALSE, TRUE, CURRENT_TIMESTAMP),
(3, 2, 1, 'https://via.placeholder.com/500x500?text=Auriculares+Sony', TRUE, TRUE, CURRENT_TIMESTAMP),
(4, 3, 1, 'https://via.placeholder.com/500x500?text=USB+Hub', TRUE, TRUE, CURRENT_TIMESTAMP),
(5, 4, 1, 'https://via.placeholder.com/500x500?text=Polo+Azul', TRUE, TRUE, CURRENT_TIMESTAMP),
(6, 5, 1, 'https://via.placeholder.com/500x500?text=Zapatillas', TRUE, TRUE, CURRENT_TIMESTAMP),
(7, 6, 1, 'https://via.placeholder.com/500x500?text=Lampara+LED', TRUE, TRUE, CURRENT_TIMESTAMP),
(8, 7, 1, 'https://via.placeholder.com/500x500?text=Pelota+Futbol', TRUE, TRUE, CURRENT_TIMESTAMP),
(9, 8, 1, 'https://via.placeholder.com/500x500?text=Libro+Quijote', TRUE, TRUE, CURRENT_TIMESTAMP),
(10, 9, 1, 'https://via.placeholder.com/500x500?text=Mochila', TRUE, TRUE, CURRENT_TIMESTAMP),
(11, 10, 1, 'https://via.placeholder.com/500x500?text=Monitor+LG', TRUE, TRUE, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert warehouses
INSERT INTO warehouse (id, created_by, name, location, is_active, created_at, last_updated) VALUES
(1, 1, 'Almacén Principal Lima', 'Av. Prolongación Primavera 1234, Lima 15047, Perú', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'Centro de Distribución Arequipa', 'Calle Comercio 567, Arequipa 04000, Perú', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert product stock for each warehouse
INSERT INTO product_stock (product_id, warehouse_id, quantity, last_updated) VALUES
-- Warehouse 1 (Lima)
(1, 1, 15, CURRENT_TIMESTAMP),
(2, 1, 45, CURRENT_TIMESTAMP),
(3, 1, 120, CURRENT_TIMESTAMP),
(4, 1, 200, CURRENT_TIMESTAMP),
(5, 1, 85, CURRENT_TIMESTAMP),
(6, 1, 60, CURRENT_TIMESTAMP),
(7, 1, 30, CURRENT_TIMESTAMP),
(8, 1, 75, CURRENT_TIMESTAMP),
(9, 1, 50, CURRENT_TIMESTAMP),
(10, 1, 25, CURRENT_TIMESTAMP),
-- Warehouse 2 (Arequipa)
(1, 2, 8, CURRENT_TIMESTAMP),
(2, 2, 35, CURRENT_TIMESTAMP),
(3, 2, 90, CURRENT_TIMESTAMP),
(4, 2, 150, CURRENT_TIMESTAMP),
(5, 2, 60, CURRENT_TIMESTAMP),
(6, 2, 45, CURRENT_TIMESTAMP),
(7, 2, 20, CURRENT_TIMESTAMP),
(8, 2, 55, CURRENT_TIMESTAMP),
(9, 2, 40, CURRENT_TIMESTAMP),
(10, 2, 18, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ========== CLIENT DATA SEEDING ==========

-- Insert client addresses
INSERT INTO client_address (id, client_id, address_line1, address_line2, city, state, postal_code, country, created_at, updated_at) VALUES
(1, 6, 'Calle Sucre 456', 'Apartamento 302', 'Lima', 'Lima', '15001', 'Peru', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 6, 'Av. Larco 1289', 'Piso 5', 'Lima', 'Lima', '15047', 'Peru', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 7, 'Carrera 7 No. 123', 'Edificio Comercial', 'Arequipa', 'Arequipa', '04000', 'Peru', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 8, 'Jr. Junín 789', 'Casa 45', 'Lima', 'Lima', '15002', 'Peru', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert product liked by clients (for product alerts)
INSERT INTO product_liked (user_id, product_id, has_been_notified, liked_at) VALUES
(6, 1, FALSE, CURRENT_TIMESTAMP),   -- María likes Laptop
(6, 5, FALSE, CURRENT_TIMESTAMP),   -- María likes Zapatillas
(7, 2, FALSE, CURRENT_TIMESTAMP),   -- Pedro likes Auriculares
(7, 10, FALSE, CURRENT_TIMESTAMP),  -- Pedro likes Monitor
(8, 4, FALSE, CURRENT_TIMESTAMP),   -- Ana likes Polo
(8, 6, FALSE, CURRENT_TIMESTAMP)    -- Ana likes Lámpara
ON CONFLICT DO NOTHING;

-- ========== VERIFICATION QUERIES ==========
-- Uncomment and run these to verify data insertion:
-- SELECT COUNT(*) as total_roles FROM role;
-- SELECT COUNT(*) as total_users FROM sys_user;
-- SELECT COUNT(*) as total_persons FROM person;
-- SELECT COUNT(*) as total_categories FROM category;
-- SELECT COUNT(*) as total_products FROM product;
-- SELECT COUNT(*) as total_warehouses FROM warehouse;
-- SELECT COUNT(*) as total_stock_records FROM product_stock;
-- SELECT COUNT(*) as total_addresses FROM client_address;
-- SELECT u.username, p.first_name, r.name FROM sys_user u LEFT JOIN person p ON u.id = p.user_id LEFT JOIN role r ON u.role_id = r.id;
