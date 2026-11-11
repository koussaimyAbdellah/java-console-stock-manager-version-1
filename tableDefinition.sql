-- ============================================
-- STEP 1: Create ENUM types
-- ============================================

CREATE TYPE category_enum AS ENUM ('PHONE', 'BOOK', 'ACCESSORY');
CREATE TYPE operation_enum AS ENUM (
    'INSERT', 'UPDATE', 'DELETE', 'RESTORE', 'RESTOCK',
    'SELL', 'DESTOCK', 'BULKPRICEUPDATE'
);

-- ============================================
-- STEP 2: Create suppliers table FIRST
-- ============================================

CREATE TABLE suppliers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL CHECK (email LIKE '%@%'),
    phone VARCHAR(20) NOT NULL CHECK (LENGTH(phone) >= 10),
    address TEXT,
    deleted_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT suppliers_name_alphabetic CHECK (name ~ '^[A-Za-z ]+$')
);

CREATE INDEX idx_suppliers_name ON suppliers(name);
CREATE INDEX idx_suppliers_deleted ON suppliers(deleted_at);
CREATE UNIQUE INDEX suppliers_active_name_unique
    ON suppliers(name) WHERE deleted_at IS NULL;

-- ============================================
-- STEP 3: Create products table
-- ============================================

CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price NUMERIC(10, 2) NOT NULL CHECK (price > 0),
    stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    category category_enum NOT NULL,
    supplier_id INTEGER NOT NULL,
    deleted_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_products_supplier FOREIGN KEY (supplier_id) 
        REFERENCES suppliers(id) ON DELETE RESTRICT,
    CONSTRAINT products_name_format CHECK (name ~ '^[A-Za-z0-9 ]+$')
);

CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_supplier ON products(supplier_id);
CREATE INDEX idx_products_deleted ON products(deleted_at);
CREATE INDEX idx_products_name ON products(name);
CREATE UNIQUE INDEX products_active_name_unique
    ON products(name) WHERE deleted_at IS NULL;

-- ============================================
-- STEP 4: Create product type tables
-- ============================================

-- Books table
CREATE TABLE books (
    product_id INTEGER PRIMARY KEY,
    author VARCHAR(100) NOT NULL,
    isbn VARCHAR(13) UNIQUE,
    pages INTEGER CHECK (pages > 0),
    
    CONSTRAINT fk_books_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT books_author_format CHECK (author ~ '^[A-Za-z ]+$')
);

-- Phones table
CREATE TABLE phones (
    product_id INTEGER PRIMARY KEY,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    storage_gb INTEGER NOT NULL CHECK (storage_gb > 0),
    
    CONSTRAINT fk_phones_product FOREIGN KEY (product_id) 
        REFERENCES products(id) ON DELETE CASCADE
);

-- Accessories table
CREATE TABLE accessories (
    product_id INTEGER PRIMARY KEY,
    accessory_type VARCHAR(50) NOT NULL,
    compatibility VARCHAR(200),
    
    CONSTRAINT fk_accessories_product FOREIGN KEY (product_id) 
        REFERENCES products(id) ON DELETE CASCADE
);

-- ============================================
-- STEP 5: Create audit log table
-- ============================================

CREATE TABLE product_audit_log (
    id SERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL,
    action operation_enum NOT NULL,
    old_value TEXT,
    new_value TEXT,
    changed_by VARCHAR(50),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_audit_product FOREIGN KEY (product_id) 
        REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_audit_product ON product_audit_log(product_id);
CREATE INDEX idx_audit_date ON product_audit_log(changed_at);

-- ============================================
-- STEP 6: Create trigger for updated_at
-- ============================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();