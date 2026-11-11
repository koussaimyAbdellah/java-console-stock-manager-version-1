package com.stockapp.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.*;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.stockapp.dao.ProductDAO;
import com.stockapp.dao.SupplierDAO;
import com.stockapp.exception.BaseAppExcetpion;
import com.stockapp.exception.DaoException;
import com.stockapp.exception.DuplicatedNameException;
import com.stockapp.exception.ServiceException;
import com.stockapp.util.AppLogger;
import com.stockapp.util.DBConnection;
import com.stockapp.model.*;
import com.stockapp.service.dto.SupplierReportDTO;

public class SupplierService {
    private static final Logger logger = AppLogger.getLogger(SupplierService.class); 
    private SupplierDAO supplierDao;
    private ProductDAO productDAO;
    public SupplierService(SupplierDAO sd,ProductDAO pd){
        supplierDao = sd;
        productDAO = pd;
    }

    private void checkSupplierDuplicatdName(String name) throws BaseAppExcetpion,DaoException{
        String news = supplierDao.findByName(name).map(Supplier::getName).orElse("non found");
        if (name.equals(news)) {
            BaseAppExcetpion b = new DuplicatedNameException(name,news);
            logger.log(Level.SEVERE, b.getMessage(), b);
            throw b;
        }
    }
    public Integer createSupplier(Supplier s) throws BaseAppExcetpion,DaoException{
        checkSupplierDuplicatdName(s.getName());
        return supplierDao.insertSupplier(s);
    }

    public void updateSupplierName(Integer supplierId,String newName)throws BaseAppExcetpion,DaoException{
        checkSupplierDuplicatdName(newName);
        supplierDao.updateNameSupplier(supplierId, newName);
    }// i just implemetn does who need duplicated name  check because i dont  check the name if exist or not in dao.
    // for the rest of the crud i just call them dircetly because i do all does validation input.
    /*-----------------advanced operatoin------------------------------------------- */
    public void deleteSupplier(Integer supplierId)throws BaseAppExcetpion,DaoException,ServiceException{
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            Optional<Supplier> s = supplierDao.findById(supplierId,conn);
            if (!s.isPresent()) {
                throw new ServiceException("supplier not found with id:"+supplierId);
            }
            Supplier sp = s.get();
            List<Product> list = productDAO.findBySupplier(supplierId,conn);
            if (!list.isEmpty()) {
                    throw new ServiceException("cannot delete supplier with active products ."+ list.size()+" product(s).");
            }
            supplierDao.softDeleteSupplier(supplierId,conn);
            conn.commit();
            logger.info("supplier exist with id:" + sp.getId());
        } catch (SQLException | BaseAppExcetpion e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warning("Transaction rolled back for supplier id: " + supplierId);
                } catch (SQLException rollbackEx) {
                    logger.log(Level.SEVERE, "Rollback failed", rollbackEx);
                }
            }
            
            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            } else if (e instanceof BaseAppExcetpion) {
                throw (BaseAppExcetpion) e;
            } else if (e instanceof DaoException) {
                throw (DaoException) e;
            } else {
                throw new DaoException("Delete supplier failed", e);
            }
            
        }finally{
            try {
                if (conn !=null) {
                    conn.setAutoCommit(true);
                    DBConnection.closeConnection(conn);
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE,"connection clean up faild." , e);
            }
            
        }
    }
    
    public void restoresupplier(Integer suppLierId) throws BaseAppExcetpion,DaoException{
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            supplierDao.restorSupplier(suppLierId, conn);
            conn.commit();
            logger.info("supplier with id : "+suppLierId+" was restored.");
        } catch (SQLException|BaseAppExcetpion e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warning("Transaction rolled back for supplier id: " + suppLierId);
                } catch (SQLException i) {
                    logger.log(Level.SEVERE, "Rollback failed", i);
                }
            }
            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            } else if (e instanceof BaseAppExcetpion) {
                throw (BaseAppExcetpion) e;
            } else if (e instanceof DaoException) {
                throw (DaoException) e;
            } else {
                throw new DaoException("restor supplier failed", e);
            }
        }finally{
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DBConnection.closeConnection(conn);
                } catch (SQLException e) {
                    logger.log(Level.SEVERE,"connection clean up faild." , e);
                    throw new DaoException("connection clean up faild.", e);
                }
            }
        }
    }

    public SupplierReportDTO getsSupplierReportDTO(Integer supplierId) throws BaseAppExcetpion,DaoException{
        Supplier supplier = supplierDao.findById(supplierId)
            .orElseThrow(() -> new ServiceException("supplier with id."+supplierId));
        List<Product> listProduct = productDAO.findBySupplier(supplierId);
        Integer totalProduct = 0;
        Double totalValueProduct = 0.0;
        Map<String,Integer> productCountByValue = new HashMap<>();
        Map<String,Double> valueByCategory = new HashMap<>();

        if (!listProduct.isEmpty()) {
            totalProduct = listProduct.size();
            totalValueProduct = listProduct.stream().mapToDouble(l->l.getStock()*l.getPrice()).sum();
            productCountByValue = listProduct.stream().collect(Collectors.groupingBy(p ->p.getCategory().getDisplayName(),Collectors.collectingAndThen(Collectors.toList(), List::size)));
            valueByCategory = listProduct.stream().collect(Collectors.groupingBy(p->p.getCategory().getDisplayName(),Collectors.summingDouble(Product::calculateValue)));
        }

        return new SupplierReportDTO(supplier.getId(),supplier.getName(),supplier.getEmail(), supplier.getPhone(), 
                        totalProduct, totalValueProduct, productCountByValue, valueByCategory, listProduct);
    }
}
