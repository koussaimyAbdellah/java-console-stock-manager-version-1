package com.stockapp.ui;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

import com.stockapp.dao.AuditProductLogDAOImpl;
import com.stockapp.dao.ProductDAOImpl;
import com.stockapp.dao.SupplierDAOImpl;
import com.stockapp.exception.BaseAppExcetpion;
import com.stockapp.model.Accessory;
import com.stockapp.model.Book;
import com.stockapp.model.Categorys;
import com.stockapp.model.Phone;
import com.stockapp.model.Product;
import com.stockapp.model.Supplier;
import com.stockapp.service.InventoryService;
import com.stockapp.service.ProductService;
import com.stockapp.service.SupplierService;

public final class ConsoleApplication {
    private final Scanner scanner;
    private final ProductDAOImpl productDao;
    private final SupplierDAOImpl supplierDao;
    private final ProductService productService;
    private final SupplierService supplierService;
    private final InventoryService inventoryService;

    private ConsoleApplication() {
        scanner = new Scanner(System.in);
        productDao = new ProductDAOImpl();
        supplierDao = new SupplierDAOImpl();
        productService = new ProductService(productDao, new AuditProductLogDAOImpl());
        supplierService = new SupplierService(supplierDao, productDao);
        inventoryService = new InventoryService(productDao, new AuditProductLogDAOImpl(), supplierDao);
    }

    public static void main(String[] args) {
        try {
            new ConsoleApplication().run();
        } catch (RuntimeException e) {
            System.err.println("Application stopped: " + e.getMessage());
        }
    }

    private void run() {
        boolean running = true;
        System.out.println("Stock Management Console");
        while (running) {
            printMenu();
            switch (readInt("Choose an option: ")) {
                case 1 -> execute(this::listProducts);
                case 2 -> execute(this::listSuppliers);
                case 3 -> execute(this::createSupplier);
                case 4 -> execute(this::createProduct);
                case 5 -> execute(this::restock);
                case 6 -> execute(this::sell);
                case 7 -> execute(this::lowStockReport);
                case 8 -> execute(this::healthReport);
                case 0 -> running = false;
                default -> System.out.println("Unknown option.");
            }
        }
        scanner.close();
        System.out.println("Goodbye.");
    }

    private void printMenu() {
        System.out.println("""

                1. List active products
                2. List active suppliers
                3. Create supplier
                4. Create product
                5. Restock product
                6. Sell product
                7. Low-stock report
                8. Inventory health report
                0. Exit
                """);
    }

    private void listProducts() throws Exception {
        List<Product> products = productDao.findAll();
        if (products.isEmpty()) {
            System.out.println("No active products.");
            return;
        }
        products.forEach(product -> System.out.println(product.getId() + " | "
                + product.getName() + " | " + product.getCategory().getDisplayName()
                + " | price=" + product.getPrice() + " | stock=" + product.getStock()));
    }

    private void listSuppliers() throws Exception {
        List<Supplier> suppliers = supplierDao.findAll();
        if (suppliers.isEmpty()) {
            System.out.println("No active suppliers.");
            return;
        }
        suppliers.forEach(supplier -> System.out.println(supplier.getId() + " | "
                + supplier.getName() + " | " + supplier.getEmail() + " | "
                + supplier.getPhone()));
    }

    private void createSupplier() throws Exception {
        Supplier supplier = new Supplier(null, readText("Name: "), readText("Email: "),
                readText("Phone: "), readText("Address: "));
        Integer id = supplierService.createSupplier(supplier);
        System.out.println("Supplier created with id " + id + ".");
    }

    private void createProduct() throws Exception {
        Categorys category = Categorys.getCategoryFrom(readText("Category (book/phone/accessory): "));
        Integer supplierId = readInt("Supplier id: ");
        String name = readText("Name: ");
        Double price = readDouble("Price: ");
        Integer stock = readInt("Initial stock: ");
        LocalDateTime now = LocalDateTime.now();
        Product product;
        if (category == Categorys.BOOK) {
            product = new Book(readText("Author: "), readText("ISBN: "), readInt("Pages: "),
                    null, stock, name, price, supplierId, category, now, null, now);
        } else if (category == Categorys.PHONE) {
            product = new Phone(readText("Brand: "), readText("Model: "), readInt("Storage GB: "),
                    null, stock, name, price, supplierId, category, now, null, now);
        } else {
            product = new Accessory(readText("Accessory type: "), readText("Compatibility: "),
                    null, stock, name, price, supplierId, category, now, null, now);
        }
        System.out.println("Product created with audit id " + productService.createProduct(product).getId() + ".");
    }

    private void restock() throws Exception {
        System.out.println("Restock audit id: " + productService.restockProduct(
                readInt("Product id: "), readInt("Quantity: "), "console").getId());
    }

    private void sell() throws Exception {
        System.out.println("Sale audit id: " + productService.sellProduct(
                readInt("Product id: "), readInt("Quantity: "), "console").getId());
    }

    private void lowStockReport() throws Exception {
        var report = inventoryService.generateLowStockReport(readInt("Threshold: "));
        System.out.println("Low-stock products: " + report.getTotalProducts());
        report.getCriticalStockProducts().forEach(p -> System.out.println(
                p.getId() + " | " + p.getName() + " | stock=" + p.getStock()));
    }

    private void healthReport() throws Exception {
        var report = inventoryService.performStockHealthCheck();
        System.out.println("Products: " + report.getTotalProducts()
                + ", out of stock: " + report.getOutOfStockCount()
                + ", low stock: " + report.getLowStockCount()
                + ", inventory value: " + report.getTotalInventoryValue());
    }

    private void execute(CheckedAction action) {
        try {
            action.run();
        } catch (Exception e) {
            System.out.println("Operation failed: " + e.getMessage());
        }
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        return Integer.parseInt(readText(prompt));
    }

    private double readDouble(String prompt) {
        return Double.parseDouble(readText(prompt));
    }

    @FunctionalInterface
    private interface CheckedAction {
        void run() throws Exception;
    }
}
