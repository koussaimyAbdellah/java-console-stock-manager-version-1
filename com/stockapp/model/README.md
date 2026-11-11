# Model Layer

The model layer represents suppliers, products, product subtypes, categories, and
audit records. Constructors validate incoming values through
[`ValidatorInput.java`](ValidatorInput.java). Products use inheritance:

```text
Product
  +-- Book
  +-- Phone
  +-- Accessory
```

[`Product.java`](Product.java) owns shared inventory behavior such as
`calculateValue()` and `isLowStock()`. Each subtype adds its database-specific fields
and implements `getDetails()`. [`Supplier.java`](Supplier.java) models supplier
identity and soft deletion. [`ProductAuditLog.java`](ProductAuditLog.java) represents
history events.

Important current behavior:

- equality for products and suppliers is identifier-based;
- product deletion is represented by a nullable timestamp;
- category parsing is case-insensitive;
- Java audit operations are broader than the SQL enum;
- validation rules are not fully aligned with the SQL checks;
- some null checks can allow a `NullPointerException` before a domain exception.
