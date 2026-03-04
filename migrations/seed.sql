-- ECOMMERCE APPLICATION TEST DATA SEEDER
-- This script populates the database with realistic test data for development and testing
-- Run this AFTER executing schema.sql
-- Usage: psql -U postgres -d ecommerce -f migrations/seed.sql

-- ========== USER DATA SEEDING ==========

-- Insert roles
INSERT INTO role (name, is_active) VALUES
('MANAGER', TRUE),
('CLIENT', TRUE),
('WAREHOUSE', TRUE),
('SHIPPING', TRUE)
ON CONFLICT DO NOTHING;

-- Insert system users (admin, manager, warehouse staff, clients)
-- Note: These passwords are all the same = 'ravn'

INSERT INTO sys_user (username, hashed_password, role_id, is_active, created_at, last_updated_password) VALUES
-- MANAGER role
('admin_manager', '$2a$10$sFB3sa0qNz8afW/pnpfyfOCb26fZC/3SH9Ut6/marjQ8MYqVvSP5y', 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('charles_manager', '$2a$10$sFB3sa0qNz8afW/pnpfyfOCb26fZC/3SH9Ut6/marjQ8MYqVvSP5y', 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- WAREHOUSE role
('warehouse_admin', '$2a$10$sFB3sa0qNz8afW/pnpfyfOCb26fZC/3SH9Ut6/marjQ8MYqVvSP5y', 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('john_warehouse', '$2a$10$sFB3sa0qNz8afW/pnpfyfOCb26fZC/3SH9Ut6/marjQ8MYqVvSP5y', 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- SHIPPING role
('shipping_coordinator', '$2a$10$sFB3sa0qNz8afW/pnpfyfOCb26fZC/3SH9Ut6/marjQ8MYqVvSP5y', 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- CLIENT role
('client_mary', '$2a$10$sFB3sa0qNz8afW/pnpfyfOCb26fZC/3SH9Ut6/marjQ8MYqVvSP5y', 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('client_peter', '$2a$10$sFB3sa0qNz8afW/pnpfyfOCb26fZC/3SH9Ut6/marjQ8MYqVvSP5y', 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('client_ann', '$2a$10$sFB3sa0qNz8afW/pnpfyfOCb26fZC/3SH9Ut6/marjQ8MYqVvSP5y', 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- Insert person records (linked to users)
INSERT INTO person (user_id, first_name, last_name, email, phone, document, document_type, is_active, created_at) VALUES
-- Manager
(1, 'Richard', 'Gonzalez Flores', 'richard.gonzalez@ecommerce.com', '+51978123456', '12345678', 'PERSON', TRUE, CURRENT_TIMESTAMP),
(2, 'Charles', 'Martinez Lopez', 'charles.martinez@ecommerce.com', '+51987654321', '87654321', 'PERSON', TRUE, CURRENT_TIMESTAMP),
-- Warehouse
(3, 'James', 'Rodriguez Perez', 'james.rodriguez@warehouse.com', '+51912345678', '11223344', 'PERSON', TRUE, CURRENT_TIMESTAMP),
(4, 'John', 'Sanchez Torres', 'john.sanchez@warehouse.com', '+51923456789', '55667788', 'PERSON', TRUE, CURRENT_TIMESTAMP),
-- Shipping
(5, 'Frank', 'Vega Morales', 'frank.vega@shipping.com', '+51934567890', '99887766', 'PERSON', TRUE, CURRENT_TIMESTAMP),
-- Clients
(6, 'Mary', 'Garcia Ruiz', 'mary.garcia@gmail.com', '+51998765432', '13579246', 'PERSON', TRUE, CURRENT_TIMESTAMP),
(7, 'Peter', 'Lopez Fernandez', 'peter.lopez@hotmail.com', '+51954321098', '24681357', 'PERSON', TRUE, CURRENT_TIMESTAMP),
(8, 'Ann', 'Diaz Ramirez', 'ann.diaz@yahoo.com', '+51987123654', '35792468', 'PERSON', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ========== PRODUCT DATA SEEDING ==========

-- Insert categories
INSERT INTO category (created_by, name, description, is_active, created_at) VALUES
(1, 'POS Equipment and Terminals', 'Point of sale terminals and all-in-one solutions for checkout counters', TRUE, CURRENT_TIMESTAMP),
(1, 'Printing and Labeling', 'Thermal printers, label printers and printing supplies for retail', TRUE, CURRENT_TIMESTAMP),
(1, 'Scanners and Data Capture', 'Barcode readers and mobile devices for inventory', TRUE, CURRENT_TIMESTAMP),
(1, 'Networks and Infrastructure', 'Routers, switches and network equipment for business', TRUE, CURRENT_TIMESTAMP),
(1, 'Accessories and Supplies', 'Cash drawers, paper rolls and other point of sale accessories', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert products
INSERT INTO product (created_by, name, description, price, is_active, created_at, updated_at, deleted_at) VALUES
(1, 'POS All-in-One Terminal 15"', '15" touch POS terminal with Intel processor, 8GB RAM and 256GB SSD, ideal for retail checkout and restaurants', 1750.00, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, '80mm Thermal Receipt Printer', 'High-speed thermal printer for 80mm receipts, compatible with most POS software', 420.00, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, '2D Barcode Scanner USB', '1D/2D scanner with stand and USB connection, ideal for checkout and packing stations', 290.00, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, '4" Industrial Label Printer', 'Thermal label printer for 4" labels for warehouses and distribution centers', 1450.00, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 'Metal Cash Drawer', 'Reinforced steel cash drawer with electrical opening and removable tray', 260.00, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 'Barcode Label Rolls 57x32mm', 'Box of 50 rolls of adhesive labels for thermal and thermal transfer printers', 180.00, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 'Android Handheld POS with Scanner', 'Mobile device with Android, integrated thermal printer and 1D/2D barcode reader', 980.00, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 'Gigabit Business Router', 'High-performance router with 4 Gigabit ports and multi-VLAN support', 650.00, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, '80mm Thermal Paper Rolls', 'Box of 50 rolls of 80mm thermal paper for receipt printers', 240.00, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
(1, 'Wireless Barcode Scanner', 'Wireless 2.4GHz/Bluetooth reader with charging base, ideal for warehouses and wholesale stores', 350.00, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL)
ON CONFLICT DO NOTHING;

-- Insert product-category mappings
INSERT INTO product_category (product_id, category_id, created_at) VALUES
(1, 1, CURRENT_TIMESTAMP),  -- POS Terminal -> POS Equipment and Terminals
(2, 2, CURRENT_TIMESTAMP),  -- Thermal printer -> Printing and Labeling
(3, 3, CURRENT_TIMESTAMP),  -- 2D Scanner -> Scanners and Data Capture
(4, 2, CURRENT_TIMESTAMP),  -- Label printer -> Printing and Labeling
(5, 5, CURRENT_TIMESTAMP),  -- Cash drawer -> Accessories and Supplies
(6, 2, CURRENT_TIMESTAMP),  -- Label rolls -> Printing and Labeling
(7, 1, CURRENT_TIMESTAMP),  -- Handheld POS -> POS Equipment and Terminals
(8, 4, CURRENT_TIMESTAMP),  -- Business router -> Networks and Infrastructure
(9, 5, CURRENT_TIMESTAMP),  -- Thermal paper rolls -> Accessories and Supplies
(10, 3, CURRENT_TIMESTAMP)  -- Wireless scanner -> Scanners and Data Capture
ON CONFLICT DO NOTHING;

-- Insert tags
INSERT INTO tag (name, is_active, created_at) VALUES
('New', TRUE, CURRENT_TIMESTAMP),
('Bestseller', TRUE, CURRENT_TIMESTAMP),
('On Sale', TRUE, CURRENT_TIMESTAMP),
('Eco-friendly', TRUE, CURRENT_TIMESTAMP),
('Premium', TRUE, CURRENT_TIMESTAMP),
('Free Shipping', TRUE, CURRENT_TIMESTAMP),
('In Stock', TRUE, CURRENT_TIMESTAMP),
('Trending', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert product-tag mappings
INSERT INTO product_tag (product_id, tag_id, created_at) VALUES
(1, 1, CURRENT_TIMESTAMP),  -- POS Terminal -> New
(1, 5, CURRENT_TIMESTAMP),  -- POS Terminal -> Premium
(2, 2, CURRENT_TIMESTAMP),  -- Thermal printer -> Bestseller
(2, 6, CURRENT_TIMESTAMP),  -- Thermal printer -> Free Shipping
(3, 3, CURRENT_TIMESTAMP),  -- 2D Scanner -> On Sale
(4, 1, CURRENT_TIMESTAMP),  -- Label printer -> New
(4, 8, CURRENT_TIMESTAMP),  -- Label printer -> Trending
(5, 7, CURRENT_TIMESTAMP),  -- Cash drawer -> In Stock
(6, 4, CURRENT_TIMESTAMP),  -- Label rolls -> Eco-friendly
(7, 2, CURRENT_TIMESTAMP),  -- Handheld POS -> Bestseller
(8, 1, CURRENT_TIMESTAMP),  -- Router -> New
(10, 5, CURRENT_TIMESTAMP)  -- Wireless scanner -> Premium
ON CONFLICT DO NOTHING;

-- Insert product images
INSERT INTO product_image (product_id, created_by, image_url, is_primary_image, is_active, created_at) VALUES
(1, 1, 'https://via.placeholder.com/500x500?text=POS+Terminal', TRUE, TRUE, CURRENT_TIMESTAMP),
(1, 1, 'https://via.placeholder.com/500x500?text=POS+Terminal+Side', FALSE, TRUE, CURRENT_TIMESTAMP),
(2, 1, 'https://via.placeholder.com/500x500?text=Thermal+Receipt+Printer', TRUE, TRUE, CURRENT_TIMESTAMP),
(3, 1, 'https://via.placeholder.com/500x500?text=2D+Barcode+Scanner', TRUE, TRUE, CURRENT_TIMESTAMP),
(4, 1, 'https://via.placeholder.com/500x500?text=Industrial+Label+Printer', TRUE, TRUE, CURRENT_TIMESTAMP),
(5, 1, 'https://via.placeholder.com/500x500?text=Cash+Drawer', TRUE, TRUE, CURRENT_TIMESTAMP),
(6, 1, 'https://via.placeholder.com/500x500?text=Label+Rolls', TRUE, TRUE, CURRENT_TIMESTAMP),
(7, 1, 'https://via.placeholder.com/500x500?text=Handheld+POS', TRUE, TRUE, CURRENT_TIMESTAMP),
(8, 1, 'https://via.placeholder.com/500x500?text=Business+Router', TRUE, TRUE, CURRENT_TIMESTAMP),
(9, 1, 'https://via.placeholder.com/500x500?text=Thermal+Paper+Rolls', TRUE, TRUE, CURRENT_TIMESTAMP),
(10, 1, 'https://via.placeholder.com/500x500?text=Wireless+Scanner', TRUE, TRUE, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert warehouses
INSERT INTO warehouse (created_by, name, location, is_active, created_at, last_updated) VALUES
(1, 'Main Warehouse Lima', '1234 Spring Ave Extension, Lima 15047, Peru', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Arequipa Distribution Center', '567 Commerce St, Arequipa 04000, Peru', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
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


INSERT INTO delivery_status (name, step_order)
VALUES ('PENDING', 1),
('ACCEPTED', 2),
('IN TRANSIT', 3),
('DELIVERED', 4),
('CANCELLED', 5);

-- ========== CLIENT DATA SEEDING ==========

-- Insert client addresses
INSERT INTO client_address (client_id, address_line1, address_line2, city, state, postal_code, country, created_at, updated_at) VALUES
(6, '456 Sucre Street', 'Apt 302', 'Lima', 'Lima', '15001', 'Peru', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, '1289 Larco Ave', 'Floor 5', 'Lima', 'Lima', '15047', 'Peru', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, '123 7th Avenue', 'Commercial Building', 'Arequipa', 'Arequipa', '04000', 'Peru', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '789 Junin Street', 'House 45', 'Lima', 'Lima', '15002', 'Peru', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert product liked by clients (for product alerts)
INSERT INTO product_liked (user_id, product_id, has_been_notified, liked_at) VALUES
(6, 1, FALSE, CURRENT_TIMESTAMP),   -- Mary likes POS Terminal
(6, 5, FALSE, CURRENT_TIMESTAMP),   -- Mary likes Cash drawer
(7, 2, FALSE, CURRENT_TIMESTAMP),   -- Peter likes Thermal printer
(7, 10, FALSE, CURRENT_TIMESTAMP),  -- Peter likes Wireless scanner
(8, 4, FALSE, CURRENT_TIMESTAMP),   -- Ann likes Label printer
(8, 6, FALSE, CURRENT_TIMESTAMP)    -- Ann likes Label rolls
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
