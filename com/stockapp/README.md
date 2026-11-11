# `com.stockapp`

`com.stockapp` is the main application package. It contains the stock-management
domain, JDBC persistence, transaction-oriented services, report DTOs, checked
exceptions, and shared utilities.

There is currently no `main` class or UI package. Consumers must construct DAO
implementations and services themselves. Package-specific behavior is documented in
the READMEs below:

- [DAO layer](dao/README.md)
- [Model layer](model/README.md)
- [Service layer](service/README.md)
- [DTOs](service/dto/README.md)
- [Exceptions](exception/README.md)
- [Utilities](util/README.md)
- [`ConsoleApplication`](ui/ConsoleApplication.java): first console entry point

The complete project map and maintained bug/change log is in the root
[project context](../../../src/PROJECT_CONTEXT.md).
