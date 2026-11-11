# Exception Layer

The project uses checked exceptions to distinguish validation, service, and database
failures.

- [`BaseAppExcetpion.java`](BaseAppExcetpion.java) is the base application exception.
- [`InvalidInputException.java`](InvalidInputException.java) reports invalid model or
  service input.
- [`ServiceException.java`](ServiceException.java) reports business-rule failures.
- [`DaoException.java`](DaoException.java) extends `SQLException` for persistence
  failures and stores its own message/cause fields.
- [`DuplicatedNameException.java`](DuplicatedNameException.java) identifies duplicate
  names.
- [`NegativeStockValue.java`](NegativeStockValue.java) describes insufficient stock,
  although current sale logic often uses `InvalidInputException` instead.
- [`ProductNotFindException.java`](ProductNotFindException.java) and
  [`SupplierNotFoundException.java`](SupplierNotFoundException.java) carry lookup
  context.

Static reading found reversed id/name labels in the two not-found messages, and the
product exception's primitive `getId()` can unbox null.
