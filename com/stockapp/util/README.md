# Utility Layer

[`DBConnection.java`](DBConnection.java) loads JDBC properties from the working
directory, then classpath, then hard-coded defaults. It loads the PostgreSQL driver,
opens `DriverManager` connections, and exposes a close helper.

[`AppLogger.java`](AppLogger.java) configures the root logger with rotating files under
`logs/` and warning-level console output. It removes existing root handlers during
first initialization.

Current operational limitations:

- there is no connection pooling;
- credentials must be supplied locally through `db.password` or `DB_PASSWORD`;
- configuration is sensitive to the process working directory;
- the checked-in password value is intentionally empty;
- logger initialization is not synchronized for concurrent first use.
