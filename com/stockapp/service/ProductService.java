package com.stockapp.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.stockapp.dao.AuditproductLogDAO;
import com.stockapp.dao.ProductDAO;
import com.stockapp.exception.BaseAppExcetpion;
import com.stockapp.exception.DaoException;
import com.stockapp.exception.ServiceException;
import com.stockapp.exception.InvalidInputException;
import com.stockapp.model.Accessory;
import com.stockapp.model.Book;
import com.stockapp.model.Categorys;
import com.stockapp.model.Phone;
import com.stockapp.model.Product;
import com.stockapp.model.ProductAuditLog;
import com.stockapp.model.ValidatorInput;
import com.stockapp.model.ProductAuditLog.OPERATIONTYPE;
import com.stockapp.util.AppLogger;
import com.stockapp.util.DBConnection;

public class ProductService {
    Logger logger = AppLogger.getLogger(ProductService.class);
    private ProductDAO productDAO;
    private AuditproductLogDAO auditProduct;
    public ProductService(ProductDAO p,AuditproductLogDAO ap){
        productDAO = p;
        auditProduct = ap;
    }

    public ProductAuditLog createProduct(Product p) throws BaseAppExcetpion,DaoException{
        ValidatorInput.nullCheekProduct(p);
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            if (p.getId() != null) {
                if (productDAO.findProductById(p.getId(),conn).isPresent()) {
                    throw new DaoException("product id: "+p.getId()+" already exist.", null);
                }
            }
            Integer i = productDAO.insertProduct(p, conn);
            p.setId(i);
            ProductAuditLog pal = new ProductAuditLog(null,p.getId(), OPERATIONTYPE.INSERT, null, p.toString(),"admin", LocalDateTime.now());
            Integer auditId = auditProduct.insertIntoAudit(pal, conn);
            pal.setId(auditId);
            conn.commit();
            return pal;
            } catch (SQLException | BaseAppExcetpion e) {
        if (conn != null) {
            try {
                conn.rollback();
                logger.warning("Transaction rolled back for product creation");
            } catch (SQLException rollbackEx) {
                logger.log(Level.SEVERE, "Rollback failed", rollbackEx);
            }
        }
        
        if (e instanceof ServiceException) {
            throw (ServiceException) e;
        } else if (e instanceof DaoException) {
            throw (DaoException) e;
        } else if (e instanceof BaseAppExcetpion) {
            throw (BaseAppExcetpion) e;
        } else {
            throw new DaoException("Create product failed", e);
        }
        
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

    private Product deepCopy(Product p)throws BaseAppExcetpion{
        ValidatorInput.nullCheekProduct(p);
        Product copyP = null;
            if (p instanceof Book) {
                copyP = new Book((Book)p);
            }else if (p instanceof Accessory) {
                copyP = new Accessory((Accessory)p);
            }else if( p instanceof Phone){
                copyP = new Phone((Phone) p);
            }
            if (copyP == null) {
                throw new InvalidInputException("Unsupported product type: " + p.getClass().getName());
            }
            return copyP;
    }
    public ProductAuditLog updateProductPrice(Integer productId,Double newPrice,String changedBy) throws BaseAppExcetpion,DaoException{
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            Product p = productDAO.findProductById(productId,conn).orElseThrow(()-> new DaoException("product was not found.", null));
            productDAO.updatePriceProduct(productId, newPrice,conn);
            Product copyP = deepCopy(p);
            copyP.setUpdatedAt(LocalDateTime.now());
            ProductAuditLog pal = new ProductAuditLog(null, productId, OPERATIONTYPE.UPDATE, p.toString(), copyP.setPrice(newPrice).toString(), changedBy,LocalDateTime.now());
            Integer i = auditProduct.insertIntoAudit(pal, conn);
            pal.setId(i);
            conn.commit();
            return pal;
        } catch (SQLException|BaseAppExcetpion e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException c) {
                    logger.log(Level.SEVERE,"faild to rollback", c);
                    throw new DaoException("faild to rollback", c);
                }
            }

            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            } else if (e instanceof BaseAppExcetpion) {
                throw (BaseAppExcetpion) e;
            } else if (e instanceof DaoException) {
                throw (DaoException) e;
            } else {
                throw new DaoException("update product price was failed", e);
            }
        }finally{
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DBConnection.closeConnection(conn);
                } catch (SQLException e) {
                    logger.log(Level.SEVERE,"connection clean up faild.",e);
                }
            }
        }
    }

    public ProductAuditLog updateProductStock(Integer productId, Integer newStock, String changedBy) throws BaseAppExcetpion,DaoException{
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            Product p = productDAO.findProductById(productId,conn).orElseThrow(()-> new DaoException("product was not found.", null));
            productDAO.updateStockProduct(productId,newStock ,conn);
            Product copyP = deepCopy(p);
            copyP.setUpdatedAt(LocalDateTime.now());
            ProductAuditLog pal = new ProductAuditLog(null, productId, OPERATIONTYPE.UPDATE, p.toString(), copyP.setStock(newStock).toString(), changedBy,LocalDateTime.now());
            Integer i = auditProduct.insertIntoAudit(pal, conn);
            pal.setId(i);
            conn.commit();
            return pal;
        } catch (SQLException|BaseAppExcetpion e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException c) {
                    logger.log(Level.SEVERE,"faild to rollback", c);
                    throw new DaoException("faild to rollback", c);
                }
            }

            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            } else if (e instanceof BaseAppExcetpion) {
                throw (BaseAppExcetpion) e;
            } else if (e instanceof DaoException) {
                throw (DaoException) e;
            } else {
                throw new DaoException("update product stock was failed", e);
            }
        }finally{
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DBConnection.closeConnection(conn);
                } catch (SQLException e) {
                    logger.log(Level.SEVERE,"connection clean up faild.",e);
                }
            }
        }
    }

    public ProductAuditLog deleteProduct(Integer productId, String deletedBy) throws BaseAppExcetpion,DaoException{
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            Product p = productDAO.findProductById(productId,conn).orElseThrow(()->new DaoException("product not found: id"+productId, null));
            productDAO.deleteSoftProduct(productId,conn);
            Product copyP = deepCopy(p);
            ProductAuditLog pal = new ProductAuditLog(null, productId, OPERATIONTYPE.DELETE, p.toString(), copyP.setDeletedAt(LocalDateTime.now()).toString(),deletedBy,LocalDateTime.now());
            Integer i = auditProduct.insertIntoAudit(pal,conn);
            pal.setId(i);
            conn.commit();
            return pal;
        } catch (SQLException|BaseAppExcetpion e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException c) {
                    logger.log(Level.SEVERE,"faild to rollback", c);
                    throw new DaoException("faild to rollback", c);
                }
            }

            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            } else if (e instanceof BaseAppExcetpion) {
                throw (BaseAppExcetpion) e;
            } else if (e instanceof DaoException) {
                throw (DaoException) e;
            } else {
                throw new DaoException("delete product  was failed", e);
            }
        } finally{
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DBConnection.closeConnection(conn);
                } catch (SQLException e) {
                    logger.log(Level.SEVERE,"connection clean up faild.",e);
                }
            }
        }
    }
    
    public ProductAuditLog restoreProduct(Integer productId, String restoredBy)throws DaoException,BaseAppExcetpion{
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            Product p = productDAO.findProductByIdIncludingDeleted(productId, conn)
                .orElseThrow(()->new DaoException("product not found", null));
            productDAO.restorProduct(productId,conn);
            Product copyP = deepCopy(p);
            copyP.setDeletedAt(null);
            ProductAuditLog pal = new ProductAuditLog(null, productId, OPERATIONTYPE.RESTORE,p.toString(), copyP.toString(), restoredBy,LocalDateTime.now());
            Integer i = auditProduct.insertIntoAudit(pal,conn);
            pal.setId(i);
            conn.commit();
            return pal;
        } catch (SQLException|BaseAppExcetpion e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException c) {
                    logger.log(Level.SEVERE,"faild to rollback", c);
                    throw new DaoException("faild to rollback", c);
                }
            }

            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            } else if (e instanceof BaseAppExcetpion) {
                throw (BaseAppExcetpion) e;
            } else if (e instanceof DaoException) {
                throw (DaoException) e;
            } else {
                throw new DaoException("restore product  was failed", e);
            }
        }finally{
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DBConnection.closeConnection(conn);
                } catch (SQLException e) {
                    logger.log(Level.SEVERE,"connection clean up faild.",e);
                }
            }
        }
    }
    
    public ProductAuditLog restockProduct(Integer productId, Integer quantity, String changedBy) throws BaseAppExcetpion,DaoException{
        Connection conn = null;
        try {
            ValidatorInput.inputstockValidation(quantity);
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            Product p = productDAO.findProductById(productId, conn).orElseThrow(() ->  new DaoException("product with id "+productId+" was not found", null));
            Integer newStock = p.getStock() + quantity;
            ProductAuditLog pal = null;
            Product copyP = deepCopy(p);
            copyP.setStock(newStock);//validation imbaded in setter for neggative stock values.
            productDAO.updateStockProduct(productId,newStock, conn);
            pal = new ProductAuditLog(null,productId,OPERATIONTYPE.RESTOCK,p.toString(),copyP.toString(),changedBy,LocalDateTime.now());
            Integer palId = auditProduct.insertIntoAudit(pal, conn);
            pal.setId(palId);
            conn.commit();
            return pal;
        } catch (SQLException|BaseAppExcetpion e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException c) {
                    logger.log(Level.SEVERE,"faild to rollback", c);
                    throw new DaoException("faild to rollback", c);
                }
            }

            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            } else if (e instanceof BaseAppExcetpion) {
                throw (BaseAppExcetpion) e;
            } else if (e instanceof DaoException) {
                throw (DaoException) e;
            } else {
                throw new DaoException("restock product  was failed", e);
            }
        }finally{
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DBConnection.closeConnection(conn);
                } catch (SQLException e) {
                    logger.log(Level.SEVERE,"connection clean up faild.",e);
                }
            }
        }
    }

    public ProductAuditLog sellProduct(Integer productId, Integer quantity, String changedBy) throws BaseAppExcetpion,DaoException{
        Connection conn = null;
        try {
            ValidatorInput.inputstockValidation(quantity);
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            Product p = productDAO.findProductById(productId, conn).orElseThrow(() ->  new DaoException("product with id "+productId+" was not found", null));
            Integer newStock = p.getStock() - quantity;
            ValidatorInput.inputstockValidation(newStock);
            ProductAuditLog pal = null;
            Product copyP = deepCopy(p);
            copyP.setStock(newStock);//validation imbaded in setter for neggative stock values.
            productDAO.updateStockProduct(productId,newStock, conn);
            pal = new ProductAuditLog(null,productId,OPERATIONTYPE.SELL,p.toString(),copyP.toString(),changedBy,LocalDateTime.now());
            Integer palId = auditProduct.insertIntoAudit(pal, conn);
            pal.setId(palId);
            conn.commit();
            return pal;
        } catch (SQLException|BaseAppExcetpion e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException c) {
                    logger.log(Level.SEVERE,"faild to rollback", c);
                    throw new DaoException("faild to rollback", c);
                }
            }

            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            } else if (e instanceof BaseAppExcetpion) {
                throw (BaseAppExcetpion) e;
            } else if (e instanceof DaoException) {
                throw (DaoException) e;
            } else {
                throw new DaoException("sell product  was failed", e);
            }
        }finally{
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DBConnection.closeConnection(conn);
                } catch (SQLException e) {
                    logger.log(Level.SEVERE,"connection clean up faild.",e);
                }
            }
        }
    }
    public List<ProductAuditLog> transferStock(Integer fromProductId, Integer toProductId, Integer quantity, String operator) throws BaseAppExcetpion,DaoException {
        Connection conn = null;
        try {
            ValidatorInput.inputstockValidation(quantity);
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            Product pfrom = productDAO.findProductById(fromProductId, conn).orElseThrow(()->new DaoException("product form not found : id "+fromProductId, null));
            Product pto = productDAO.findProductById(toProductId, conn).orElseThrow(()-> new DaoException("product to not found : id "+toProductId, null));
            if (!pfrom.getCategory().getDisplayName().equals(pto.getCategory().getDisplayName())) {
                throw new ServiceException("cannot trensfert between difrents categorys.id from: "+pfrom.getId()+" category: "+pfrom.getCategory().getDisplayName()
                                            +"; id to: "+pto.getId()+" category: "+pto.getCategory().getDisplayName());
            }
            Integer destockFrom = pfrom.getStock() - quantity;
            if (destockFrom<0) {
                throw new ServiceException("cannot destock from product id: "+pfrom.getId());   
            }
            productDAO.updateStockProduct(fromProductId, destockFrom, conn);
            productDAO.updateStockProduct(toProductId, quantity+pto.getStock(),conn);
            Product copyPfrom = deepCopy(pfrom);
            copyPfrom.setStock(destockFrom);
            Product copyPto = deepCopy(pto);
            copyPto.setStock(pto.getStock()+quantity);
            ProductAuditLog pal = new ProductAuditLog(null, fromProductId, OPERATIONTYPE.DESTOCK,pfrom.toString(),copyPfrom.toString(),operator,LocalDateTime.now());
            ProductAuditLog pal2 = new ProductAuditLog(null, toProductId,OPERATIONTYPE.RESTOCK, pto.toString(), copyPto.toString(), operator,LocalDateTime.now());
            pal.setId(auditProduct.insertIntoAudit(pal,conn));
            pal2.setId(auditProduct.insertIntoAudit(pal2, conn));
            conn.commit();
            List<ProductAuditLog> list = new ArrayList<>();
            list.add(pal);
            list.add(pal2);
            return list;
        } catch (SQLException|BaseAppExcetpion e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException c) {
                    logger.log(Level.SEVERE,"faild to rollback", c);
                    throw new DaoException("faild to rollback", c);
                }
            }

            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            } else if (e instanceof BaseAppExcetpion) {
                throw (BaseAppExcetpion) e;
            } else if (e instanceof DaoException) {
                throw (DaoException) e;
            } else {
                throw new DaoException("transfer product  was failed.", e);
            }
        }finally{
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DBConnection.closeConnection(conn);
                } catch (SQLException e) {
                    logger.log(Level.SEVERE,"connection clean up faild.",e);
                }
            }
        }
    }

    public List<ProductAuditLog> bulkPrice(String category,Double percentageChange,String operator)throws BaseAppExcetpion,DaoException{
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            Categorys selectedCategory = Categorys.getCategoryFrom(category);
            if(percentageChange == null || percentageChange > 1000 || percentageChange <= -100){
                throw new ServiceException("percentage exceeded level: must be greater than -100 and less than 1000");
            }
            List<Product> list = productDAO.findByCategory(selectedCategory.getDisplayName(),conn);
            if (list.isEmpty()) {
                throw new ServiceException("category "+category+" is empthy.");
            }
            Map<Integer,Double> newMap = new HashMap<>();
            for(Product p : list){
                newMap.put(p.getId(),p.getPrice());
            }
            productDAO.bulkPriceUpdate(newMap, percentageChange, conn);
            List<Product> listCopy = new ArrayList<>();
            Double percantage = (100+percentageChange)/100.0;
            for (Product product : list) {
                Product newP = deepCopy(product);
                newP.setPrice(product.getPrice()*percantage);
                listCopy.add(newP);
            };
            List<ProductAuditLog> listpal = new ArrayList<>();
            for(int i = 0; i<list.size();i++){
                ProductAuditLog pal = new ProductAuditLog(null,list.get(i).getId(),OPERATIONTYPE.BULKPRICEUPDATE
                                                        , list.get(i).toString(), listCopy.get(i).toString()
                                                        , operator,LocalDateTime.now());
                pal.setId(auditProduct.insertIntoAudit(pal, conn));
                listpal.add(pal);
            }
            conn.commit();
            return listpal;
        } catch (SQLException|BaseAppExcetpion e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException c) {
                    logger.log(Level.SEVERE,"faild to rollback", c);
                    throw new DaoException("faild to rollback", c);
                }
            }

            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            } else if (e instanceof BaseAppExcetpion) {
                throw (BaseAppExcetpion) e;
            } else if (e instanceof DaoException) {
                throw (DaoException) e;
            } else {
                throw new DaoException("bulk price update was failed", e);
            }
        }finally{
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DBConnection.closeConnection(conn);
                } catch (SQLException e) {
                    logger.log(Level.SEVERE,"connection clean up faild.",e);
                }
            }
        }
    }
    public Map<Integer, Boolean> bulkRestock(
        Map<Integer, Integer> productQuantities, 
        String operator) throws BaseAppExcetpion, DaoException {
    
        Connection conn = null;
        Map<Integer, Boolean> results = new HashMap<>();
    
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
                        results.put(productId, false);
                        logger.warning("Product not found for restock: id=" + productId);
                        continue;
                    }
                
                    Product product = productOpt.get();
                
                    if (quantityToAdd == null || quantityToAdd <= 0) {
                        results.put(productId, false);
                        logger.warning("Invalid quantity for product " + productId + ": " + quantityToAdd);
                        continue;
                    }
                
                    Integer currentStock = product.getStock();
                    Integer newStock = currentStock + quantityToAdd;
                
                    productDAO.updateStockProduct(productId, newStock, conn);
                
                    ProductAuditLog audit = new ProductAuditLog(
                        null,
                        productId,
                        OPERATIONTYPE.RESTOCK,
                        product.toString(),
                        deepCopy(product).setStock(newStock).toString(),
                        operator,
                        LocalDateTime.now()
                    );
                    auditProduct.insertIntoAudit(audit, conn);
                
                    results.put(productId, true);
                    logger.info("Successfully restocked product " + productId + 
                           ": " + currentStock + " -> " + newStock);
                
                } catch (DaoException | BaseAppExcetpion e) {
                    results.put(productId, false);
                    logger.log(Level.WARNING, 
                            "Failed to restock product " + productId, e);
                }
            }
        
            conn.commit();
            logger.info("Bulk restock completed: " + 
                    results.values().stream().filter(b -> b).count() + 
                    " succeeded, " + 
                    results.values().stream().filter(b -> !b).count() + 
                    " failed");
        
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
        
        }   finally {
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
    public Map<Integer, Boolean> bulkSell(
        Map<Integer, Integer> productQuantities, 
        String operator) throws BaseAppExcetpion, DaoException {
    
        Connection conn = null;
        Map<Integer, Boolean> results = new HashMap<>();
    
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
                        results.put(productId, false);
                        logger.warning("Product not found for sell: id=" + productId);
                        continue;
                    }
                
                    Product product = productOpt.get();
                
                    if (quantityToSell == null || quantityToSell <= 0 ||  product.getStock()<quantityToSell) {
                        results.put(productId, false);
                        logger.warning("Invalid quantity for product " + productId + ": " + quantityToSell);
                        continue;
                    }
                
                    Integer currentStock = product.getStock();
                    Integer newStock = currentStock - quantityToSell;
                
                    productDAO.updateStockProduct(productId, newStock, conn);
                
                    ProductAuditLog audit = new ProductAuditLog(
                        null,
                        productId,
                        OPERATIONTYPE.SELL,
                        product.toString(),
                        deepCopy(product).setStock(newStock).toString(),
                        operator,
                        LocalDateTime.now()
                    );
                    auditProduct.insertIntoAudit(audit, conn);
                
                    results.put(productId, true);
                    logger.info("Successfully sold product " + productId + 
                           ": " + currentStock + " -> " + newStock);
                
                } catch (DaoException | BaseAppExcetpion e) {
                    results.put(productId, false);
                    logger.log(Level.WARNING, 
                            "Failed to sell product " + productId, e);
                }
            }
        
            conn.commit();
            logger.info("Bulk sell completed: " + 
                    results.values().stream().filter(b -> b).count() + 
                    " succeeded, " + 
                    results.values().stream().filter(b -> !b).count() + 
                    " failed");
        
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
            throw new DaoException("Bulk selles failed due to database error", e);
        
        }   finally {
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

    public Map<String, Double> getValueByCategory()throws BaseAppExcetpion,DaoException{
        List<Product> list = productDAO.findAll();
        return list.stream().collect(Collectors.groupingBy(
                                    p->p.getCategory().getDisplayName(),
                                    Collectors.summingDouble(
                                        product -> product.getStock()*product.getPrice()
                                    )));
    }
    public List<Product> getMostExpensiveProducts(Integer limit) throws BaseAppExcetpion,DaoException{
        if (limit == null || limit < 0) {
            throw new InvalidInputException("limit must not be null or negative");
        }
        List<Product> list = productDAO.findAll();
        list.sort(Comparator.comparing(Product::getPrice).reversed());
        return list.stream().limit(limit).toList();
    }
    
    public List<Product> getMostValuableProducts(Integer limit) throws BaseAppExcetpion, DaoException {
    List<Product> allProducts = productDAO.findAll();
    
    return allProducts.stream()
            .sorted(Comparator.comparingDouble(
                    (Product p) -> p.getPrice() * p.getStock()
            ).reversed())
            .limit(limit)
            .toList();  
}
}
