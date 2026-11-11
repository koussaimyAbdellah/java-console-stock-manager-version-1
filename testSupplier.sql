-- ============================================
-- SUPPLIER SOFT DELETE COMPREHENSIVE TEST
-- ============================================

-- Test 1: Create test suppliers
-- ============================================

INSERT INTO suppliers (name, email, phone, address) VALUES
('Tech Corp', 'tech@techcorp.com', '1234567890', '123 Tech Street'),
('Gadget World', 'info@gadgetworld.com', '0987654321', '456 Gadget Ave'),
('Phone Masters', 'sales@phonemasters.com', '5551234567', '789 Mobile Lane');

-- Verify suppliers were created
SELECT id, name, email, deleted_at FROM suppliers ORDER BY id;

-- Test 2: Create products linked to these suppliers
-- ============================================
INSERT INTO products (name, price, stock_quantity, category, supplier_id) VALUES
('Smartphone X', 999.99, 50, 'PHONE', 1),
('Wireless Earbuds', 79.99, 100, 'ACCESSORY', 1),
('Programming Book', 49.99, 25, 'BOOK', 8),
('Phone Case', 19.99, 200, 'ACCESSORY',9 );

-- Verify products were created
SELECT p.id, p.name, p.category, s.name as supplier_name, p.deleted_at 
FROM products p 
JOIN suppliers s ON p.supplier_id = s.id;

-- Test 3: Test soft delete on supplier WITHOUT products (should work)
-- ============================================
-- This supplier has no products, so soft delete should work
UPDATE suppliers SET deleted_at = CURRENT_TIMESTAMP 
WHERE name = 'Gadget World';

-- Verify supplier is soft deleted
SELECT id, name, deleted_at FROM suppliers 
WHERE name = 'Gadget World';

-- Test 4: Test soft delete on supplier WITH products (should fail due to RESTRICT)
-- ============================================
-- This should fail because supplier has products and we use ON DELETE RESTRICT
BEGIN;
    UPDATE suppliers SET deleted_at = CURRENT_TIMESTAMP 
    WHERE name = 'Tech Corp';
    -- This will either fail or succeed based on your business logic
    -- Let's check what happens:
    SELECT * FROM suppliers WHERE name = 'Tech Corp';
ROLLBACK;

-- Test 5: Test querying active vs deleted suppliers
-- ============================================
-- Get all active suppliers (deleted_at IS NULL)

SELECT 'ACTIVE SUPPLIERS' as status, id, name, email 
FROM suppliers 
WHERE deleted_at IS NULL;

-- Get all deleted suppliers (deleted_at IS NOT NULL)
SELECT 'DELETED SUPPLIERS' as status, id, name, email, deleted_at 
FROM suppliers 
WHERE deleted_at IS NOT NULL;

-- Test 6: Test the index on deleted_at (performance test)
-- ============================================
-- Check if index is being used (run with EXPLAIN)
EXPLAIN ANALYZE 
SELECT * FROM suppliers 
WHERE deleted_at IS NULL;  -- Should use idx_suppliers_deleted

EXPLAIN ANALYZE 
SELECT * FROM suppliers 
WHERE deleted_at IS NOT NULL;  -- Should use idx_suppliers_deleted

-- Test 7: Test "undelete" functionality
-- ============================================
-- Restore the soft-deleted supplier

UPDATE suppliers SET deleted_at = NULL 
WHERE name = 'Gadget World';

-- Verify restoration
SELECT id, name, deleted_at FROM suppliers 
WHERE name = 'Gadget World';

-- Test 8: Test partial unique index (if you created it)
-- ============================================
-- Try to create duplicate active supplier (should fail if partial index exists)
INSERT INTO suppliers (name, email, phone) VALUES
('Tech Corp', 'duplicate@tech.com', '1112223333');

-- Create a deleted supplier with same name (should work)
INSERT INTO suppliers (name, email, phone, deleted_at) VALUES
('Tech Corp', 'deleted@tech.com', '4445556666', CURRENT_TIMESTAMP);

-- Verify both exist
SELECT name, email, deleted_at FROM suppliers WHERE name = 'Tech Corp';

-- Test 9: Test business logic - what happens to products when supplier is soft-deleted?
-- ============================================
-- Since we use RESTRICT, we need to handle products first
-- Option 1: Soft delete products first, then supplier
BEGIN;
    -- Soft delete all products from this supplier
    UPDATE products SET deleted_at = CURRENT_TIMESTAMP 
    WHERE supplier_id = (SELECT id FROM suppliers WHERE name = 'Tech Corp');
    
    -- Then soft delete the supplier
    UPDATE suppliers SET deleted_at = CURRENT_TIMESTAMP 
    WHERE name = 'Tech Corp';
    
    -- Verify both are soft deleted
    SELECT 'SUPPLIER:' as type, name, deleted_at 
    FROM suppliers WHERE name = 'Tech Corp'
    UNION ALL
    SELECT 'PRODUCT:' as type, name, deleted_at 
    FROM products WHERE supplier_id = (SELECT id FROM suppliers WHERE name = 'Tech Corp');
ROLLBACK;

-- Test 10: Test complex queries with soft delete
-- ============================================
-- Get active suppliers with their active product counts
SELECT 
    s.name as supplier_name,
    s.email,
    COUNT(p.id) as active_product_count,
    SUM(p.stock_quantity) as total_stock
FROM suppliers s
LEFT JOIN products p ON s.id = p.supplier_id AND p.deleted_at IS NULL
WHERE s.deleted_at IS NULL
GROUP BY s.id, s.name, s.email
ORDER BY active_product_count DESC;

-- Test 11: Performance comparison with/without index
-- ============================================
-- First, let's create some more test data
INSERT INTO suppliers (name, email, phone, deleted_at)
SELECT 
    'Supplier ' || i,
    'supplier' || i || '@test.com',
    '123456789' || i,
    CASE WHEN i % 3 = 0 THEN CURRENT_TIMESTAMP ELSE NULL END
FROM generate_series(1, 1000) i;

-- Test query performance with index
EXPLAIN (ANALYZE, BUFFERS)
SELECT COUNT(*) FROM suppliers WHERE deleted_at IS NULL;

-- Test 12: Clean up test data
-- ============================================
-- Delete the test suppliers we created (hard delete for cleanup)
DELETE FROM products WHERE name IN (
    'Smartphone X', 'Wireless Earbuds', 'Programming Book', 'Phone Case'
);

DELETE FROM suppliers WHERE name IN (
    'Tech Corp', 'Gadget World', 'Phone Masters'
);

-- Delete bulk test data
DELETE FROM suppliers WHERE name LIKE 'Supplier %';

-- Final verification
SELECT COUNT(*) as remaining_suppliers FROM suppliers;
SELECT COUNT(*) as remaining_products FROM products;