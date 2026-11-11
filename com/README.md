# Java Package Root

This folder contains the Java source tree. The application package is
[`stockapp/`](stockapp/README.md), under the namespace `com.stockapp`.

The package tree follows a layered JDBC design:

- `model`: domain entities and validation;
- `dao`: persistence interfaces and implementations;
- `service`: business workflows;
- `exception`: checked application errors;
- `util`: database connection and logging helpers.

See the root [project context](../src/PROJECT_CONTEXT.md) for the complete architecture,
database map, achieved functionality, missing pieces, and static issue report.
