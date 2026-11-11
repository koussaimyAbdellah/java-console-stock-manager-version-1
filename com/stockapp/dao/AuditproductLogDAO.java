package com.stockapp.dao;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import com.stockapp.exception.BaseAppExcetpion;
import com.stockapp.exception.DaoException;
import com.stockapp.model.ProductAuditLog;

public interface AuditproductLogDAO {
    Integer insertIntoAudit(ProductAuditLog p,Connection c)throws DaoException,BaseAppExcetpion;
    Integer insertIntoAudit(ProductAuditLog p)throws DaoException,BaseAppExcetpion;
    
    List<ProductAuditLog> findAll(Connection c)throws DaoException, BaseAppExcetpion;
    List<ProductAuditLog> findAll()throws DaoException, BaseAppExcetpion;

    Optional<ProductAuditLog> findById(Integer id,Connection c)throws DaoException, BaseAppExcetpion;
    Optional<ProductAuditLog> findById(Integer id)throws DaoException, BaseAppExcetpion;

    List<ProductAuditLog> findAllProductAuditById(Integer id, Connection c) throws DaoException,BaseAppExcetpion;
    List<ProductAuditLog> findAllProductAuditById(Integer id) throws DaoException,BaseAppExcetpion;

    List<ProductAuditLog> findLastHistroyByHour(Integer hours) throws BaseAppExcetpion,DaoException;
}
