package com.stockapp.dao;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

import java.util.Optional;

import com.stockapp.exception.BaseAppExcetpion;
import com.stockapp.exception.DaoException;
import com.stockapp.model.Supplier;

public interface SupplierDAO {
    Integer insertSupplier(Supplier s)throws DaoException,BaseAppExcetpion;
    Integer insertSupplier(Supplier s,Connection c)throws DaoException,BaseAppExcetpion;

    void updateEmailSupplier(Integer id,String email) throws DaoException,BaseAppExcetpion; 
    void updateEmailSupplier(Integer id,String email,Connection c)throws DaoException,BaseAppExcetpion;

    void updateNameSupplier(Integer id, String name) throws DaoException,BaseAppExcetpion;
    void updateNameSupplier(Integer id,String name,Connection c)throws DaoException,BaseAppExcetpion;
    //update the whole supplier like mutlit update.

    LocalDateTime softDeleteSupplierI(Integer id) throws DaoException,BaseAppExcetpion;
    LocalDateTime softDeleteSupplier(Integer id ,Connection c)throws DaoException,BaseAppExcetpion;

    void restorSupplier(Integer id)throws DaoException,BaseAppExcetpion;
    void restorSupplier(Integer id ,Connection c)throws DaoException,BaseAppExcetpion;

    void hardDeletesupplier(Integer id)throws DaoException,BaseAppExcetpion;
    void hardDeleteSupplier(Integer id ,Connection c)throws DaoException,BaseAppExcetpion;

    List<Supplier> findAll(Connection c)throws DaoException,BaseAppExcetpion;
    List<Supplier> findAll() throws DaoException,BaseAppExcetpion;

    Optional<Supplier> findById(Integer id,Connection c)throws DaoException,BaseAppExcetpion;
    Optional<Supplier> findById(Integer id) throws DaoException,BaseAppExcetpion;

    Optional<Supplier> findByName(String name,Connection c)throws DaoException,BaseAppExcetpion;
    Optional<Supplier> findByName(String name) throws DaoException,BaseAppExcetpion;

    List<Supplier> findSupplierWithProducts(Connection c)throws DaoException,BaseAppExcetpion;
    List<Supplier> findSupplierWithProducts() throws DaoException,BaseAppExcetpion;

    List<Supplier> findAllInlucdingDeleted() throws DaoException,BaseAppExcetpion;
    List<Supplier> findAllIncludingDeleted(Connection c) throws DaoException,BaseAppExcetpion;
}
