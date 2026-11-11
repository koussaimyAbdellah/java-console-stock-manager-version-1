package com.stockapp.dao;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.stockapp.exception.BaseAppExcetpion;
import com.stockapp.exception.DaoException;
import com.stockapp.model.Product;

public interface ProductDAO {
    Integer insertProduct(Product p,Connection c)throws DaoException,BaseAppExcetpion;
    Integer insertProduct(Product p)throws DaoException,BaseAppExcetpion;

    void updatePriceProduct(Integer id,Double price,Connection c)throws DaoException,BaseAppExcetpion;
    void updatePriceProduct(Integer id ,Double price)throws DaoException,BaseAppExcetpion;

    void bulkPriceUpdate(Map<Integer,Double> ProductMap,Double percentage,Connection conn) throws DaoException,BaseAppExcetpion;

    void updateStockProduct(Integer id ,Integer stock,Connection c)throws DaoException,BaseAppExcetpion;
    void updateStockProduct(Integer id,Integer Stock) throws DaoException,BaseAppExcetpion;

    void deleteSoftProduct(Integer id,Connection c)throws DaoException,BaseAppExcetpion;
    void deleteSoftProduct(Integer id)throws DaoException,BaseAppExcetpion;

    void restorProduct(Integer id,Connection c)throws DaoException,BaseAppExcetpion;
    void restorProductI(Integer id)throws DaoException,BaseAppExcetpion;

    void hardDeleteProduct(Integer id,Connection c)throws DaoException,BaseAppExcetpion;
    void hardDeleteProduct(Integer id)throws DaoException,BaseAppExcetpion;

    List<Product> findAll(Connection c)throws DaoException,BaseAppExcetpion;
    List<Product> findAll() throws DaoException,BaseAppExcetpion;

    Optional<Product> findProductById(Integer id,Connection c)throws DaoException,BaseAppExcetpion;
    Optional<Product> findProductById(Integer id)throws DaoException,BaseAppExcetpion;
    Optional<Product> findProductByIdIncludingDeleted(Integer id, Connection c)throws DaoException,BaseAppExcetpion;

    List<Product> findByCategory(String category,Connection c)throws BaseAppExcetpion,DaoException;
    List<Product> findByCategory(String categroy)throws BaseAppExcetpion,DaoException;

    List<Product> findBySupplier(Integer idSupplier,Connection c)throws BaseAppExcetpion,DaoException;
    List<Product> findBySupplier(Integer idSupplier)throws BaseAppExcetpion,DaoException;

    List<Product> findByLowStock(Integer threshold,Connection c)throws BaseAppExcetpion,DaoException;
    List<Product> findByLowStock(Integer threshold)throws BaseAppExcetpion,DaoException;

    Double getTotalValue(Connection c)throws BaseAppExcetpion,DaoException;
    Double getTotalValue()throws BaseAppExcetpion,DaoException;

    Double getTotalValueByCategory(String category,Connection c)throws BaseAppExcetpion,DaoException;
    Double getTotalvalueBycategory(String category)throws BaseAppExcetpion,DaoException;

    List<Product> findAllInlcudingDeleted(Connection c)throws BaseAppExcetpion,DaoException;
    List<Product> findAllIncludingDeleted()throws BaseAppExcetpion,DaoException;
}
