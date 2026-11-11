# DAO Layer

DAO interfaces define persistence contracts with two forms of most operations:

1. a method accepting a `Connection`, intended for service-managed transactions;
2. a convenience method that opens and closes its own connection.

[`ProductDAOImpl.java`](ProductDAOImpl.java) persists the common product row and the
matching subtype row, then reconstructs concrete products through category-specific
queries. It supports active-only reads, including-deleted reads, category/supplier/
low-stock queries, stock and price changes, soft/hard deletion, restoration, and value
aggregation.

[`SupplierDAOImpl.java`](SupplierDAOImpl.java) provides supplier CRUD, active and
including-deleted reads, supplier-with-products lookup, and soft-delete restoration.
its SQL uses the supplier soft-delete column defined in
[`tableDefinition.sql`](../../../tableDefinition.sql).

[`AuditProductLogDAOImpl.java`](AuditProductLogDAOImpl.java) persists audit events and
supports all-history, by-ID, by-product, and recent-hours queries.

The implementations use prepared statements and close JDBC statements/result sets with
try-with-resources. There is no connection pool and no automated DAO integration test.
