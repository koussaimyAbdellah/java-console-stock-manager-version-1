# Service Layer

Services coordinate business rules, DAOs, transactions, audit writes, and report
construction.

## `ProductService`

Provides creation, price/stock updates, soft deletion, restoration, restocking, sales,
same-category stock transfers, bulk price changes, bulk restocking/sales, and value
queries. Mutating operations generally use one connection and commit or roll back as a
unit. Product changes attempt to create `ProductAuditLog` rows.

## `SupplierService`

Checks duplicate names before creation or renaming, prevents deletion when active
products remain, soft-deletes/restores suppliers transactionally, and creates a
supplier report with category counts and inventory value.

## `InventoryService`

Builds low-stock, out-of-stock, category-value, supplier-product, most-valuable, and
stock-health reports. Its bulk stock operations return per-product
[`StockMovementDTO`](dto/StockMovementDTO.java) results so one invalid item can be
reported while other items continue within the transaction.

The services are not wired to an application entry point. Important integration risks
are documented in the root [project context](../../../src/PROJECT_CONTEXT.md), especially
the mismatch between Java audit operations and the SQL enum.
