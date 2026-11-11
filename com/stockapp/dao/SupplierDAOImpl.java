package com.stockapp.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.sql.*;
import java.time.LocalDateTime;

import com.stockapp.exception.BaseAppExcetpion;
import com.stockapp.exception.DaoException;
import com.stockapp.model.Supplier;
import com.stockapp.model.ValidatorInput;
import com.stockapp.util.AppLogger;
import com.stockapp.util.DBConnection;

public class SupplierDAOImpl implements SupplierDAO {
    private static final String INSERT_SQL = "INSERT INTO suppliers(name,email,phone,address,created_at) VALUES (?,?,?,?,?)";
    private static final String UPDATE_EMAIL_SQL = "UPDATE suppliers SET email = ? WHERE id = ?";
    private static final String UPDATE_NAME_SQL = "UPDATE suppliers SET name = ? WHERE id = ?";
    private static final String SOFT_DELETE_SQL = "UPDATE suppliers SET deleted_at = ? WHERE id = ?";
    private static final String RESTOR_DELETED_SQL = "UPDATE suppliers SET deleted_at = NULL WHERE id = ?";
    private static final String HARD_DELETE_SQL = " DELETE FROM suppliers WHERE id = ?";
    private static final String FIND_ALL_SQL = " SELECT * FROM suppliers WHERE  deleted_at IS NULL ORDER BY created_at ";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM suppliers WHERE id = ? AND deleted_at IS NULL";
    private static final String FIND_BY_NAME_SQL = "SELECT * FROM suppliers WHERE name = ? AND deleted_at IS NULL";
    private static final String FIND_SUPPLIERS_WITH_PRODUCT_SQL = "SELECT * FROM suppliers WHERE deleted_at IS NULL AND id IN (SELECT supplier_id FROM products WHERE deleted_at IS NULL)";
    private static final String FIND_ALL_INCLUDING_DELETED_SQL = "SELECT * FROM suppliers";

    private static final Logger logger = AppLogger.getLogger(SupplierDAOImpl.class);
    //================================insert method =================================================
    @Override
    public Integer insertSupplier(Supplier s, Connection c) throws DaoException,BaseAppExcetpion{
        ValidatorInput.nullCheekSupplier(s);
        try (PreparedStatement pstmt = c.prepareStatement(INSERT_SQL,Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1,s.getName());
            pstmt.setString(2, s.getEmail());
            pstmt.setString(3,s.getPhone());
            pstmt.setString(4, s.getAddress());
            LocalDateTime createdAt = s.getCreateAt();
            Timestamp tm = Timestamp.valueOf(createdAt);
            pstmt.setTimestamp(5, tm);
            int row = pstmt.executeUpdate();
            if (row == 0) {
                logger.warning("no row effected");
                throw new DaoException("inseration faild", null);
            }
            try(ResultSet rs = pstmt.getGeneratedKeys()){
                if (rs.next()) {
                    return rs.getInt(1);
                    
                }
                logger.warning("inseration successed but keys generation faild.supplier name:"+s.getName()+""
                    +",email:"+s.getEmail()+",Phone:"+s.getPhone()+",adress:"+s.getAddress()
                );
                throw new DaoException("insertion succeeded but id not returned.", null);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"insertino faild",e);
            throw new DaoException("insertion faild", e);
        }
    } 
    @Override
    public Integer insertSupplier(Supplier s) throws DaoException,BaseAppExcetpion{
        try (Connection newconn = DBConnection.getConnection()) {
            return insertSupplier(s,newconn);
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"insertion failed in simple insert operation", e);
            throw new DaoException(e.getMessage(), e);
        } catch (BaseAppExcetpion c){
            logger.log(Level.WARNING, c.getMessage(), c);
            throw c;
        }
    }  
    //===========================update methods===================================================================
    @Override
    public void updateEmailSupplier(Integer id, String email, Connection c) throws DaoException, BaseAppExcetpion {
        ValidatorInput.nullCheekvaluesInteger(id);
        ValidatorInput.inputEmailValidation(email);
        try (PreparedStatement pstmt =  c.prepareStatement(UPDATE_EMAIL_SQL)) {
            pstmt.setString(1, email);
            pstmt.setInt(2, id);
            int row = pstmt.executeUpdate();
            if(row == 0) {
                throw new DaoException("udpate email faild : no row effected", null);
            }
            logger.info("supplier id: "+id+", email:"+email);
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"update faild ", e);
            throw new DaoException("update faild error", e);
        }
    }
    @Override
    public void updateEmailSupplier(Integer id, String email) throws DaoException, BaseAppExcetpion {
        try (Connection conn = DBConnection.getConnection()) {
            updateEmailSupplier(id, email,conn);
        } catch (SQLException e) {
            throw new DaoException("updateFaild in simmple udate email", e);
        } catch(BaseAppExcetpion d ){
            logger.log(Level.SEVERE,d.getMessage(), d);
            throw d;
        }
    }

    @Override
    public void updateNameSupplier(Integer id, String name,Connection c) throws DaoException, BaseAppExcetpion {
        ValidatorInput.nullCheekvaluesInteger(id);
        ValidatorInput.inputNameValidation(name);
        try (PreparedStatement pstmt = c.prepareStatement(UPDATE_NAME_SQL)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, id);
            int row = pstmt.executeUpdate();
            if (row == 0) {
                throw new DaoException("update name faild:no row effected.", null);
            }
            logger.info("supplier id:"+id+",name:"+name);
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"update faild ", e);
            throw new DaoException("update faild error", e);
        }
    }
    @Override
    public void updateNameSupplier(Integer id, String name) throws DaoException, BaseAppExcetpion {
        try(Connection conn = DBConnection.getConnection()) {
            updateNameSupplier(id, name,conn);
        } catch (SQLException e) {
            throw new DaoException("updateFaild in simmple udate name", e);
        } catch(BaseAppExcetpion a){
            logger.log(Level.SEVERE, a.getMessage(), a);
            throw a;
        }
    }
    //=========================delete methods===============================================================
    @Override
    public LocalDateTime softDeleteSupplier(Integer id, Connection c) throws DaoException, BaseAppExcetpion {
        ValidatorInput.nullCheekvaluesInteger(id);
        try (PreparedStatement pstmt =  c.prepareStatement(SOFT_DELETE_SQL)) {
            pstmt.setInt(2,id);
            LocalDateTime ltm = LocalDateTime.now();
            Timestamp tm = Timestamp.valueOf(ltm);
            pstmt.setTimestamp(1, tm);
            int row = pstmt.executeUpdate();
            if (row == 0) {
                throw new DaoException("soft delete faild: no row effected.", null);
            }
            logger.info("supplier id:"+id+" was deleted.");
            return ltm;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"soft deleted was faild", e);
            throw new DaoException("soft delete was faild", e);
        }
        
    }
    @Override
    public LocalDateTime softDeleteSupplierI(Integer id) throws DaoException, BaseAppExcetpion {
        try (Connection conn = DBConnection.getConnection()) {
            return softDeleteSupplier(id, conn);
        } catch (SQLException e) {
            throw new DaoException("simple soft delete was faild.", e);
        } catch (BaseAppExcetpion c){
            logger.log(Level.SEVERE, c.getMessage(), c);
            throw c;
        }
    }

    @Override
    public void restorSupplier(Integer id, Connection c) throws DaoException, BaseAppExcetpion {
        ValidatorInput.nullCheekvaluesInteger(id);
        try (PreparedStatement pstmt = c.prepareStatement(RESTOR_DELETED_SQL)) {
            pstmt.setInt(1, id);
            int row = pstmt.executeUpdate();
            if (row == 0) {
                throw new DaoException("restor faild : no row effected.", null);
            }
            logger.info("supplier id : " + id +" restored .");
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"resotr was faild", e);
            throw new DaoException("restor was faild", e);
        }
    }
    @Override
    public void restorSupplier(Integer id) throws DaoException, BaseAppExcetpion {
        try (Connection conn = DBConnection.getConnection()) {
            restorSupplier(id, conn);
        } catch (SQLException e) {
            throw new DaoException("simple resotor faild", e);
        }catch(BaseAppExcetpion c){
            logger.log(Level.SEVERE, c.getMessage(), c);
            throw c;
        }
    }

    @Override
    public void hardDeleteSupplier(Integer id, Connection c) throws DaoException, BaseAppExcetpion {
        ValidatorInput.nullCheekvaluesInteger(id);
        try (PreparedStatement pstmt = c.prepareStatement(HARD_DELETE_SQL)) {
            pstmt.setInt(1, id);
            int row = pstmt.executeUpdate();
            if (row == 0) {
                throw new DaoException("hard deleted was faild : no row effectd", null);
            }
            logger.info("supplier id :"+id+" ,was removed completely from database.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"hard delete was faild", e);
            throw new DaoException("hard delete was faild", e);
        }
    }

    @Override
    public void hardDeletesupplier(Integer id) throws DaoException, BaseAppExcetpion {
        try (Connection conn = DBConnection.getConnection()) {
            hardDeleteSupplier(id, conn);
        } catch (SQLException e) {
            throw new DaoException("simple hard delete was faild",e);
        } catch (BaseAppExcetpion c){
            logger.log(Level.SEVERE,c.getMessage(), c);
            throw c;
        }
    }
    //=================================find methods ===================================  
    @Override
    public List<Supplier> findAll(Connection c) throws DaoException, BaseAppExcetpion {
        try (PreparedStatement pstmt = c.prepareStatement(FIND_ALL_SQL)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                return readResultSet(rs);
            } 
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"read from table suppliers was faild", e);
            throw new  DaoException("read from table suppliers was faild", e);
        }
    }
    @Override
    public List<Supplier> findAll() throws DaoException, BaseAppExcetpion {
        try (Connection conn = DBConnection.getConnection()) {
            return findAll(conn);
        } catch (SQLException e) {
            throw new DaoException("simple findall supplier is faild",e);
        }catch(BaseAppExcetpion c){
            logger.log(Level.SEVERE,c.getMessage(), c);
            throw c;
        }
    }
    @Override
    public Optional<Supplier> findById(Integer id, Connection c) throws DaoException, BaseAppExcetpion {
        ValidatorInput.nullCheekvaluesInteger(id);
        try (PreparedStatement pstmt = c.prepareStatement(FIND_BY_ID_SQL)) {
            pstmt.setInt(1, id);
            try(ResultSet rs = pstmt.executeQuery()){
                return readOneResultSet(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "supplier id not found : id:" +id, e);
            throw new DaoException("supplier id not found : id:"+id, e);
        }
    }
    @Override
    public Optional<Supplier> findById(Integer id) throws DaoException, BaseAppExcetpion {
        try (Connection conn = DBConnection.getConnection()) {
            return findById(id,conn);
        } catch (SQLException e) {
            throw new DaoException("simple find for supplier is faild", e);
        }catch (BaseAppExcetpion c ){
            logger.log(Level.SEVERE, c.getMessage(), c);
            throw c;
        }
    }
    @Override
    public Optional<Supplier> findByName(String name, Connection c) throws DaoException, BaseAppExcetpion {
        ValidatorInput.inputNameValidation(name);
        try (PreparedStatement pstmt = c.prepareStatement(FIND_BY_NAME_SQL)) {
            pstmt.setString(1, name);
            try(ResultSet rs = pstmt.executeQuery()){
                return readOneResultSet(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "supplier id not found : id:" +name, e);
            throw new DaoException("supplier id not found : id:"+name, e);
        }
    }

    @Override
    public Optional<Supplier> findByName(String name) throws DaoException, BaseAppExcetpion {
        try (Connection conn = DBConnection.getConnection()) {
            return findByName(name,conn);
        } catch (SQLException e) {
            throw new DaoException("simple find for supplier is faild", e);
        }catch (BaseAppExcetpion c ){
            logger.log(Level.SEVERE, c.getMessage(), c);
            throw c;
        }
    }
    @Override
    public List<Supplier> findSupplierWithProducts(Connection c) throws DaoException, BaseAppExcetpion {
        try (PreparedStatement pstmt = c.prepareStatement(FIND_SUPPLIERS_WITH_PRODUCT_SQL)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                return readResultSet(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"read from table suppliers was faild", e);
            throw new  DaoException("read from table suppliers was faild", e);
        }
    }
    @Override
    public List<Supplier> findSupplierWithProducts() throws DaoException, BaseAppExcetpion {
        try (Connection conn = DBConnection.getConnection()) {
            return findSupplierWithProducts(conn);
        }catch (SQLException e) {
            throw new DaoException("simple find for supplier is faild", e);
        }catch (BaseAppExcetpion c ){
            logger.log(Level.SEVERE, c.getMessage(), c);
           throw c;    
        }
    }

    @Override
    public List<Supplier> findAllIncludingDeleted(Connection c) throws DaoException, BaseAppExcetpion {
        try (PreparedStatement pstmt = c.prepareStatement(FIND_ALL_INCLUDING_DELETED_SQL)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                return readResultSet(rs);
            }
            } catch (SQLException e) {
                logger.log(Level.SEVERE,"read from table suppliers was faild", e);
                throw new  DaoException("read from table suppliers was faild", e);
            }
        }
        @Override
        public List<Supplier> findAllInlucdingDeleted() throws DaoException, BaseAppExcetpion {
            try (Connection conn = DBConnection.getConnection()) {
                return findAllIncludingDeleted(conn);
            }catch (SQLException e) {
            throw new DaoException("simple find for supplier is faild", e);
        }catch (BaseAppExcetpion c ){
            logger.log(Level.SEVERE, c.getMessage(), c);
           throw c;    
        }
        }
        //=========================read result set methods============================================
        private Optional<Supplier> readOneResultSet(ResultSet rs)throws SQLException,BaseAppExcetpion{
            if (rs.next()) {
                    int idsup = rs.getInt("id");
                    String  name = rs.getString("name");
                    String email = rs.getString("email");
                    String phone = rs.getString("phone");
                    String adress = rs.getString("address");
                    Timestamp tm = rs.getTimestamp("created_at");
                    LocalDateTime createAt = tm.toLocalDateTime();
                    tm = rs.getTimestamp("deleted_at");
                    LocalDateTime deleteAt = (tm == null)?null:tm.toLocalDateTime();
                    Supplier s = new Supplier(idsup, name, email, phone, adress);
                    s.setCreateAt(createAt).setDeleteAt(deleteAt);
                    logger.info("supplier was find : "+s);
                    return Optional.ofNullable(s);
                }
                return Optional.empty();
        }
        private List<Supplier> readResultSet(ResultSet rs)throws SQLException,BaseAppExcetpion{
            Supplier s = null;
            Integer id = null;
            String name = null;
            String email = null;
            String phone = null;
            String adress = null;
            LocalDateTime createdAt = null;
            LocalDateTime deletedAt = null;
            Timestamp tm = null;
            List<Supplier> tempsupplierList = new ArrayList<>() ;
            while (rs.next()) {
                    id = rs.getInt("id");
                    name = rs.getString("name");
                    email = rs.getString("email");
                    phone = rs.getString("phone");
                    adress = rs.getString("address");
                    tm = rs.getTimestamp("created_at");
                    createdAt = tm.toLocalDateTime();
                    tm = rs.getTimestamp("deleted_at");
                    deletedAt = (tm == null)?null:tm.toLocalDateTime();
                    s = new Supplier(id, name, email, phone, adress);
                    s.setCreateAt(createdAt).setDeleteAt(deletedAt);
                    tempsupplierList.add(s);
                }
                logger.info("read suppliers was succefull completed.");
                return tempsupplierList;
        }
        
    }
