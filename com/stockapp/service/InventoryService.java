package com.stockapp.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.stockapp.dao.*;
import com.stockapp.exception.*;
import com.stockapp.model.*;
import com.stockapp.model.ProductAuditLog.OPERATIONTYPE;
import com.stockapp.service.dto.*;
import com.stockapp.util.*;

public class InventoryService {
    private static final Logger logger = AppLogger.getLogger(InventoryService.class);

    private SupplierDAO supplierDAO;
    private ProductDAO productDAO;
    private AuditproductLogDAO auditProduct;

    public InventoryService(ProductDAO p, AuditproductLogDAO a, SupplierDAO s) {
        productDAO = p;
        auditProduct = a;
        supplierDAO = s;
    }

    public InventoryReportDTO generateLowStockReport(Integer threshold) 
            throws DaoException, BaseAppExcetpion {

        if (threshold == null || threshold < 0) {
            logger.warning("threshold must be positive");
            throw new BaseAppExcetpion("threshold must be positive");
        }

        List<Product> list = productDAO.findByLowStock(threshold);
        Integer totalProducts = list.size();

        Map<String, List<Product>> productsByCategory = list.stream()
            .sorted(Comparator.comparingInt(Product::getStock))  
            .collect(Collectors.groupingBy(
                p -> p.getCategory().getDisplayName(),
                LinkedHashMap::new,
                Collectors.toList()
            ));

        Map<String, Long> countByCategory = list.stream()
            .collect(Collectors.groupingBy(
                p -> p.getCategory().getDisplayName(),
                Collectors.counting()
            ))
            .entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))  
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));

        Map<String, Double> valueByCategory = list.stream()
            .collect(Collectors.groupingBy(
                p -> p.getCategory().getDisplayName(),
                Collectors.summingDouble(p -> p.getPrice() * p.getStock())
            ));

        return new InventoryReportDTO(
            totalProducts, 
            productsByCategory, 
            countByCategory, 
            valueByCategory, 
            list
        );
    }

    public List<Product> getOutOfStockProducts() throws BaseAppExcetpion, DaoException {
        return productDAO.findAll().stream()
            .filter(p -> p.getStock() == 0)
            .toList();
    }

    public Map<String, Double> generateCategoryValueReport() 
            throws BaseAppExcetpion, DaoException {
        Map<String, Double> map = new HashMap<>();
        
        for (Categorys category : Categorys.values()) {
            Double value = productDAO.getTotalvalueBycategory(category.getDisplayName());
            map.put(category.getDisplayName(), value);
        }

        return map.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    public List<ProductDTO> getMostValuableProducts(Integer limit) 
            throws BaseAppExcetpion, DaoException {

        if (limit == null || limit < 0) {
            throw new InvalidInputException("limit must not be null or negative");
        }
        Map<Integer, String> supplierMap = supplierDAO.findAll().stream()
            .collect(Collectors.toMap(Supplier::getId, Supplier::getName));
   
        List<Product> products = productDAO.findAll();
        
        return products.stream()
            .map(p -> new ProductDTO(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getStock(),
                p.getCategory().getDisplayName(),
                supplierMap.getOrDefault(p.getSupplierId(), "Unknown Supplier"),
                p.calculateValue()
            ))
            .sorted(Comparator.comparing(
                ProductDTO::getTotalValue,
                Comparator.reverseOrder()
            ))
            .limit(limit)
            .toList();
    }

    public Map<String, List<Product>> generateSupplierProductReport() 
            throws BaseAppExcetpion, DaoException {
      
        Map<Integer, String> supplierMap = supplierDAO.findAll().stream()
            .collect(Collectors.toMap(Supplier::getId, Supplier::getName));
        
        return productDAO.findAll().stream()
            .collect(Collectors.groupingBy(
                p -> supplierMap.getOrDefault(p.getSupplierId(), "Unknown Supplier"),
                Collectors.toList()
            ));
    }

    private Product deepCopy(Product p) throws BaseAppExcetpion {
        Product copyP = null;
        if (p instanceof Book) {
            copyP = new Book((Book) p);
        } else if (p instanceof Accessory) {
            copyP = new Accessory((Accessory) p);
        } else if (p instanceof Phone) {
            copyP = new Phone((Phone) p);
        }
        if (copyP == null) {
            throw new InvalidInputException("Unsupported product type: " + p.getClass().getName());
        }
        return copyP;
    }

    public Map<Integer, StockMovementDTO> bulkSell(
            Map<Integer, Integer> productQuantities, 
            String operator) throws BaseAppExcetpion, DaoException {
        
        Connection conn = null;
        Map<Integer, StockMovementDTO> results = new HashMap<>();
        
        try {
            if (productQuantities == null) {
                throw new InvalidInputException("Product quantities must not be null");
            }
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            for (Map.Entry<Integer, Integer> entry : productQuantities.entrySet()) {
                Integer productId = entry.getKey();
                Integer quantityToSell = entry.getValue();
                
                try {
                    Optional<Product> productOpt = productDAO.findProductById(productId, conn);
                    if (!productOpt.isPresent()) {
                        results.put(productId, new StockMovementDTO(
                            null, null, null, null, null, 
                            false, "Product not found"
                        ));
                        logger.warning("Product not found for sell: id=" + productId);
                        continue; 
                    }
                    
                    Product product = productOpt.get();
                    Integer currentStock = product.getStock();
                    
                    if (quantityToSell == null || quantityToSell <= 0) {
                        results.put(productId, new StockMovementDTO(
                            productId, product.getName(), currentStock, currentStock, 0,
                            false, "Quantity must be positive"
                        ));
                        continue;
                    }
                  
                    if (quantityToSell > currentStock) {
                        results.put(productId, new StockMovementDTO(
                            productId, product.getName(), currentStock, currentStock, 0,
                            false, "Insufficient stock. Available: " + currentStock + 
                                   ", Requested: " + quantityToSell
                        ));
                        continue;
                    }
                    
                    Integer newStock = currentStock - quantityToSell;
                    productDAO.updateStockProduct(productId, newStock, conn);
                    
                    auditProduct.insertIntoAudit(
                        new ProductAuditLog(
                            null, productId, OPERATIONTYPE.SELL,
                            product.toString(),
                            deepCopy(product).setStock(newStock).toString(),
                            operator, LocalDateTime.now()
                        ), 
                        conn
                    );
                   
                    results.put(productId, new StockMovementDTO(
                        productId, product.getName(), currentStock, newStock, quantityToSell,
                        true, null
                    ));
                    
                    logger.info("Successfully sold product " + productId + 
                               ": " + currentStock + " -> " + newStock);
                    
                } catch (DaoException | BaseAppExcetpion ex) {
                    results.put(productId, new StockMovementDTO(
                        productId, null, null, null, null,
                        false, "Failed to sell: " + ex.getMessage()
                    ));
                    logger.log(Level.WARNING, "Failed to sell product " + productId, ex);
                }
            }
            
            conn.commit();
            
            long succeeded = results.values().stream()
                .filter(StockMovementDTO::getSuccess).count();
            long failed = results.size() - succeeded;
            logger.info("Bulk sell completed. Success: " + succeeded + ", Failed: " + failed);
            
            return results;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.severe("Database error during bulk sell - rolled back all changes");
                } catch (SQLException rollbackEx) {
                    logger.log(Level.SEVERE, "Rollback failed", rollbackEx);
                }
            }
            throw new DaoException("Bulk sell failed due to database error", e);
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DBConnection.closeConnection(conn);
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Connection cleanup failed", e);
                }
            }
        }
    }

    public Map<Integer, StockMovementDTO> bulkRestock(
            Map<Integer, Integer> productQuantities, 
            String operator) throws BaseAppExcetpion, DaoException {
        
        Connection conn = null;
        Map<Integer, StockMovementDTO> results = new HashMap<>();
        
        try {
            if (productQuantities == null) {
                throw new InvalidInputException("Product quantities must not be null");
            }
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            for (Map.Entry<Integer, Integer> entry : productQuantities.entrySet()) {
                Integer productId = entry.getKey();
                Integer quantityToAdd = entry.getValue();
                
                try {
                    Optional<Product> productOpt = productDAO.findProductById(productId, conn);
                    if (!productOpt.isPresent()) {
                        results.put(productId, new StockMovementDTO(
                            null, null, null, null, null,
                            false, "Product not found"
                        ));
                        logger.warning("Product not found for restock: id=" + productId);
                        continue;
                    }
                    
                    Product product = productOpt.get();
                    Integer currentStock = product.getStock();
                    
                    if (quantityToAdd == null || quantityToAdd <= 0) {
                        results.put(productId, new StockMovementDTO(
                            productId, product.getName(), currentStock, currentStock, 0,
                            false, "Quantity must be positive"
                        ));
                        continue;
                    }
   
                    Integer newStock = currentStock + quantityToAdd;
                    productDAO.updateStockProduct(productId, newStock, conn);
                    
                    auditProduct.insertIntoAudit(
                        new ProductAuditLog(
                            null, productId, OPERATIONTYPE.RESTOCK,
                            product.toString(),
                            deepCopy(product).setStock(newStock).toString(),
                            operator, LocalDateTime.now()
                        ),
                        conn
                    );
              
                    results.put(productId, new StockMovementDTO(
                        productId, product.getName(), currentStock, newStock, quantityToAdd,
                        true, null
                    ));
                    
                    logger.info("Successfully restocked product " + productId + 
                               ": " + currentStock + " -> " + newStock);
                    
                } catch (DaoException | BaseAppExcetpion ex) {
                    results.put(productId, new StockMovementDTO(
                        productId, null, null, null, null,
                        false, "Failed to restock: " + ex.getMessage()
                    ));
                    logger.log(Level.WARNING, "Failed to restock product " + productId, ex);
                }
            }
            
            conn.commit();
            
            long succeeded = results.values().stream()
                .filter(StockMovementDTO::getSuccess).count();
            long failed = results.size() - succeeded;
            logger.info("Bulk restock completed. Success: " + succeeded + ", Failed: " + failed);
            
            return results;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.severe("Database error during bulk restock - rolled back all changes");
                } catch (SQLException rollbackEx) {
                    logger.log(Level.SEVERE, "Rollback failed", rollbackEx);
                }
            }
            throw new DaoException("Bulk restock failed due to database error", e);
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DBConnection.closeConnection(conn);
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Connection cleanup failed", e);
                }
            }
        }
    }

    public Map<Integer, Integer> calculateEmergencyRestock(Integer threshold, Integer targetStock) 
            throws BaseAppExcetpion, DaoException {
        if (threshold == null || targetStock == null || threshold < 0 || targetStock < threshold) {
            throw new InvalidInputException("Threshold and target stock are invalid");
        }
        return productDAO.findByLowStock(threshold).stream()
            .collect(Collectors.toMap(
                Product::getId,
                p -> targetStock - p.getStock()
            ));
    }

    public InventoryHealthDTO performStockHealthCheck() 
            throws BaseAppExcetpion, DaoException {
        
        List<Product> list = productDAO.findAll();
     
        Integer total = list.size();
        Integer outOfStock = Math.toIntExact(
            list.stream().filter(p -> p.getStock() == 0).count()
        );
        Integer lowStock = Math.toIntExact(
            list.stream().filter(p -> p.getStock() < 10).count()
        );
        Integer healthyStock = Math.toIntExact(
            list.stream().filter(p -> p.getStock() >= 10 && p.getStock() <= 100).count()
        );
        Integer overstocked = Math.toIntExact(
            list.stream().filter(p -> p.getStock() > 100).count()
        );
        
        Double totalInventoryValue = list.stream()
            .mapToDouble(Product::calculateValue)
            .sum();
        
        Double averageStock = list.stream()
            .mapToInt(Product::getStock)
            .average()
            .orElse(0.0);
        
        Map<String, Long> stockDistribution = list.stream()
            .collect(Collectors.groupingBy(
                p -> p.getCategory().getDisplayName(),
                Collectors.counting()
            ));
        
        return new InventoryHealthDTO(
            total, 
            outOfStock, 
            lowStock, 
            healthyStock, 
            overstocked,
            totalInventoryValue, 
            averageStock, 
            stockDistribution
        );
    }
}