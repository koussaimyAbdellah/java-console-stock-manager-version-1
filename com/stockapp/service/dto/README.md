# Service DTOs

These classes are simple mutable data carriers returned by reporting and bulk movement
operations:

- [`ProductDTO.java`](ProductDTO.java): flattened product data with supplier name and
  calculated total value.
- [`InventoryReportDTO.java`](InventoryReportDTO.java): low-stock products grouped and
  summarized by category.
- [`InventoryHealthDTO.java`](InventoryHealthDTO.java): total, out-of-stock,
  low-stock, healthy, overstocked, average-stock, and value statistics.
- [`StockMovementDTO.java`](StockMovementDTO.java): success/failure details for one
  bulk sale or restock item.
- [`SupplierReportDTO.java`](SupplierReportDTO.java): supplier details, product list,
  category counts, and category values.

DTOs do not perform validation, persistence, or business decisions.
