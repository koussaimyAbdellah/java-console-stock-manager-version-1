# Stock Management JDBC Project Context

## Purpose and current state

This project is a learning-oriented Java/JDBC stock-management application. It models
suppliers and products, stores data in PostgreSQL, supports product-specific attributes
for books, phones, and accessories, and records product changes in an audit table.

The repository is currently a source-and-SQL project rather than a complete executable
application. There is no Maven/Gradle build file, application entry point, user
interface, automated test suite, or documented run command. The code is organized
around domain models, JDBC DAOs, service-layer transactions, report DTOs, exceptions,
and database scripts.

## Repository map

| Location | Role | Current contents |
|---|---|---|
| [`com/`](../com/README.md) | Java package root | `stockapp` package tree |
| [`com/stockapp/model/`](../com/stockapp/model/README.md) | Domain model and validation | Product hierarchy, supplier, audit record, enum, validator |
| [`com/stockapp/dao/`](../com/stockapp/dao/README.md) | Persistence layer | DAO contracts and PostgreSQL JDBC implementations |
| [`com/stockapp/service/`](../com/stockapp/service/README.md) | Business operations | Product, supplier, and inventory workflows |
| [`com/stockapp/service/dto/`](../com/stockapp/service/dto/README.md) | Service outputs | Report and stock-movement data carriers |
| [`com/stockapp/exception/`](../com/stockapp/exception/README.md) | Error types | Domain, service, and DAO exceptions |
| [`com/stockapp/util/`](../com/stockapp/util/README.md) | Cross-cutting utilities | Connection loading and logging |
| [`com/stockapp/ui/ConsoleApplication.java`](../com/stockapp/ui/ConsoleApplication.java) | Console UI | Interactive inventory menu |
| [`pom.xml`](../pom.xml) | Maven build | Java 17 compiler, PostgreSQL JDBC, exec goal |
| [`tableDefinition.sql`](../tableDefinition.sql) | Database schema | PostgreSQL types, tables, indexes, trigger |
| [`testSupplier.sql`](../testSupplier.sql) | Manual SQL experiment | Supplier/product soft-delete and performance checks |
| [`temp.sql`](../temp.sql) | SQL scratch query | Lists the audit enum values |
| [`db.properties`](../db.properties) | Runtime configuration | JDBC URL, username, driver, and externally supplied password |
| [`.vscode/`](../.vscode/README.md) | Editor configuration | SQLTools connection settings |
| [`lib/`](../lib/README.md) | Local dependency | `easy.jar` |
| [`thing should do.txt`](../thing%20should%20do.txt) | Learning backlog | Generics, ORM, pooling, Maven, JUnit, JavaFX, networking |

## Architecture

```text
Caller / future UI
        |
        v
ProductService, SupplierService, InventoryService
        |
        +--> ProductDAO / SupplierDAO / AuditproductLogDAO
        |          |
        |          v
        |      JDBC implementations
        |          |
        |          v
        |      PostgreSQL schema
        |
        +--> Product, Supplier, ProductAuditLog
        +--> Report and movement DTOs
        +--> AppLogger and DBConnection
```

Services own multi-step transactions. DAO methods have both connection-accepting
variants for participation in a caller transaction and convenience variants that open
their own connection. Product persistence uses a base `products` row plus one
category-specific row in `books`, `phones`, or `accessories`.

## Domain model

### Products

[`Product`](../com/stockapp/model/Product.java) is abstract and contains the common fields:
identifier, name, price, stock, category, supplier identifier, deletion timestamp, and
creation/update timestamps. It provides low-stock detection, inventory-value
calculation, equality by identifier, and an abstract details representation.

The concrete types are:

- [`Book`](../com/stockapp/model/Book.java): author, ISBN, and page count.
- [`Phone`](../com/stockapp/model/Phone.java): brand, model, and storage.
- [`Accessory`](../com/stockapp/model/Accessory.java): type and compatibility.

[`Categorys`](../com/stockapp/model/Categorys.java) maps the three supported categories:
`BOOK`, `PHONE`, and `ACCESSORY`.

### Suppliers

[`Supplier`](../com/stockapp/model/Supplier.java) contains identity, name, email, phone,
address, creation timestamp, and a soft-delete timestamp. Its constructor assigns the
creation time locally; DAO hydration replaces it with the database value.

### Audit records

[`ProductAuditLog`](../com/stockapp/model/ProductAuditLog.java) represents a product
history record. Java supports `INSERT`, `UPDATE`, `DELETE`, `RESTORE`, `RESTOCK`,
`SELL`, `DESTOCK`, and `BULKPRICEUPDATE`. The SQL schema currently defines only the
first three values, which prevents several implemented service operations from being
persisted.

## Persistence design

[`tableDefinition.sql`](../tableDefinition.sql) defines:

1. `category_enum` and `operation_enum`.
2. `suppliers`, with unique names and basic format checks.
3. `products`, with supplier foreign key, soft deletion, category, stock, and price.
4. One-to-one subtype tables for books, phones, and accessories.
5. `product_audit_log`.
6. A trigger that refreshes `products.updated_at` before updates.

[`ProductDAOImpl`](../com/stockapp/dao/ProductDAOImpl.java) uses prepared statements and
category-aware joins. It loads active products by default and offers explicit
including-deleted queries. [`SupplierDAOImpl`](../com/stockapp/dao/SupplierDAOImpl.java)
does the same for suppliers. [`AuditProductLogDAOImpl`](../com/stockapp/dao/AuditProductLogDAOImpl.java)
persists and queries history, including a PostgreSQL last-hours query.

## Service capabilities

### ProductService

[`ProductService`](../com/stockapp/service/ProductService.java) implements:

- create product plus insert audit record;
- price and stock updates plus audit records;
- soft delete and restore;
- single-product restock and sale;
- same-category stock transfer;
- bulk price update;
- bulk restock and bulk sale;
- value/category and most-expensive/most-valuable queries.

Most mutating operations explicitly begin a JDBC transaction, perform the data change,
write an audit event, commit, and roll back on failure.

### SupplierService

[`SupplierService`](../com/stockapp/service/SupplierService.java) implements supplier
creation with duplicate-name checking, name updates, guarded soft deletion, restore,
and a supplier report. Deletion refuses to soft-delete a supplier while active
products are associated with it.

### InventoryService

[`InventoryService`](../com/stockapp/service/InventoryService.java) provides low-stock,
out-of-stock, category-value, supplier-product, and most-valuable-product reports.
It also provides bulk stock movement operations, emergency-restock calculations, and
stock-health classification.

## Console UI

[`ConsoleApplication`](../com/stockapp/ui/ConsoleApplication.java) is the first
application entry point. It wires the existing DAO and service implementations and
provides menus for active product/supplier lists, supplier/product creation, restock,
sales, low-stock reporting, and inventory health reporting. It expects PostgreSQL
configuration through `db.properties` or `DB_PASSWORD`; it does not create schema
objects automatically.

## Maven build

[`pom.xml`](../pom.xml) defines the `com.stockapp` Maven project, Java 17 compilation,
the PostgreSQL JDBC driver, the `exec:java` main class, and an executable JAR manifest.
Because the existing source tree is rooted at `com/` rather than `src/main/java`, the
POM explicitly uses the project root as the source directory.

Typical commands from the project root:

```bash
mvn clean
mvn compile
mvn test
mvn package
mvn exec:java
```

`exec:java` is the Maven equivalent of running the console application. Maven does not
provide a built-in `mvn run` phase. To start the application suspended for a debugger,
use the configured profile and attach to port 5005:

```bash
mvn -Pdebug exec:java
```

The packaged application can also be started with:

```bash
java -jar target/stockapp-jdbc-1.0-SNAPSHOT.jar
```

## Validation and error handling

[`ValidatorInput`](../com/stockapp/model/ValidatorInput.java) centralizes null checks,
regular-expression checks, positive price checks, and non-negative stock/storage/page
checks. The exception hierarchy is:

```text
Exception
  +-- SQLException
  |     +-- DaoException
  +-- BaseAppExcetpion
        +-- InvalidInputException
        +-- ServiceException
        +-- DuplicatedNameException
        +-- NegativeStockValue
        +-- ProductNotFindException
        +-- SupplierNotFoundException
```

The code logs through [`AppLogger`](../com/stockapp/util/AppLogger.java), which configures
rotating files under `logs/` and warning-level console output.

## Configuration and execution assumptions

[`DBConnection`](../com/stockapp/util/DBConnection.java) looks for `db.properties` in the
working directory, then on the classpath, then falls back to hard-coded PostgreSQL
defaults. The expected keys are `db.url`, `db.username`, `db.password`, and
`db.driver`. The checked-in properties file intentionally leaves `db.password`
empty; provide it through local configuration or the `DB_PASSWORD` environment
variable.

The project also assumes:

- PostgreSQL is available locally;
- the PostgreSQL JDBC driver is available at runtime;
- `tableDefinition.sql` has been applied;
- the process working directory is the project root if file-based configuration is
  expected.

[`pom.xml`](../pom.xml) declares the Java 17 compiler settings and PostgreSQL JDBC
dependency; Maven must be installed locally to use that build.

## What is achieved

- Clear separation between model, DAO, service, DTO, exception, and utility packages.
- JDBC prepared statements rather than string-concatenated user values.
- PostgreSQL schema with foreign keys, checks, indexes, subtype tables, and an update
  timestamp trigger.
- Soft-delete concepts for products and suppliers in the Java DAO layer.
- Transaction-oriented product workflows with audit attempts.
- Reporting operations using Java streams and DTOs.
- Manual SQL scenarios for supplier soft-delete behavior and query inspection.
- A written learning roadmap for the next project stages.

## What is not achieved

- The console UI and Maven build are now present, but no graphical UI exists.
- JUnit tests now cover category parsing, core validation, inventory value, and
  low-stock boundaries; DAO and end-to-end database tests remain open.
- No connection pool, ORM, JavaFX layer, network layer, or Spring Boot layer.
- No migration/versioning process for the schema.
- No production-safe secret/configuration handling.
- No verified end-to-end execution against a PostgreSQL instance.

## Bug and change log

### 2026-11-05 — first repair pass

Resolved:

1. Added supplier soft-delete storage/indexes and active-only partial unique indexes
   for suppliers and products in [`tableDefinition.sql`](../tableDefinition.sql).
2. Expanded the SQL audit enum to match all Java audit operations.
3. Removed the plaintext editor password and malformed password entry. Runtime
   configuration now requires `db.password` or `DB_PASSWORD` and does not use a
   hard-coded password ([`DBConnection.java`](../com/stockapp/util/DBConnection.java)).
4. Made product restoration load deleted products and commit the transaction.
5. Corrected product deletion audit records from `UPDATE` to `DELETE`.
6. Corrected stock-transfer audit ID assignment.
7. Made numeric null validation raise `InvalidInputException` instead of allowing an
   accidental `NullPointerException`.
8. Fixed reversed not-found exception labels and nullable product IDs.
9. Fixed supplier-with-products lookup to use `products.supplier_id`.
10. Added validation for null/negative inventory report limits and thresholds.
11. Preserved SQL exception causes in supplier convenience methods, replaced an
    unsupported-product silent null with an explicit exception, and synchronized
    logger initialization.
12. Corrected bulk-price audit snapshots and generated IDs.
13. Added null/negative guards for categories, quantities, report limits, emergency
    restock targets, audit-history hours, and unsupported product copies.
14. Aligned product name/price/page validation with the database constraints and made
    low-stock checks inclusive of the threshold.
15. Added explicit null validation before product creation.
16. Prevented bulk price changes from producing the database-invalid zero price at
    exactly `-100%`, and made category validation null-safe.
17. Added a Maven build with PostgreSQL JDBC dependency and an executable
    `com.stockapp.ui.ConsoleApplication` menu for listing, creation, stock movement,
    and inventory reports.
18. Fixed the console callback boundary so checked DAO/database exceptions are shown
    as operation errors instead of preventing compilation.
19. Installed PostgreSQL 16.15 locally, created the `gradedb` database, and applied
    [`tableDefinition.sql`](../tableDefinition.sql). The schema was verified through
    password-authenticated `psql`; the password remains outside source control.
20. Added repeatable local test data to `gradedb`: three active suppliers, one
    product for each category, matching subtype rows, and three seed audit records.
21. Added JUnit 5 model and validation tests under `src/test/java`, plus Maven
    Surefire configuration. These tests run without requiring a database.
22. Added Maven clean/compile/test/package support, an executable JAR manifest, and a
    `debug` profile for JVM debugging on port 5005.
23. Fixed restock and sell audit creation: valid audit values were rejected by a
    faulty null-validation length check. Added a regression test for valid audit data.
24. Fixed PostgreSQL audit insertion by binding the `operation_enum` value as a
    database-specific enum instead of a plain VARCHAR. Restock and sell now reach
    the audit insert correctly.
25. Fixed product creation by binding the `category_enum` value as a PostgreSQL enum
    instead of a plain VARCHAR.

Still open:

| Severity | Evidence | Problem and impact |
|---|---|---|
| Medium | [`ProductService.java`](../com/stockapp/service/ProductService.java), [`InventoryService.java`](../com/stockapp/service/InventoryService.java) | Bulk operations can continue after per-item failures, but there are no automated tests proving the intended all-or-partial transaction semantics. |

Validation note: `mvn test` passes with 9 tests, 0 failures, and 0 errors. The
tests do not require a PostgreSQL connection.

## Suggested learning sequence

The existing backlog in [`thing should do.txt`](../thing%20should%20do.txt) is consistent with
the current project stage. A practical order is:

1. Add service and DAO tests using mocks/fakes and a disposable database.
2. Reconcile and version the database schema.
3. Replace direct connections with a small pool, then evaluate HikariCP.
4. Study ORM mapping with JPA/Hibernate after the JDBC behavior is covered by tests.
5. Add a JavaFX UI only after service contracts are stable.
6. Continue toward networking and Spring Boot.

## Reading boundary

This document records the repository as read and repaired on 2026-11-05. It is an
architectural and static-analysis map, not a claim that the application passes an
end-to-end database test.
