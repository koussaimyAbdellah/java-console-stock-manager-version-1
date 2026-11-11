package com.stockapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.stockapp.exception.BaseAppExcetpion;
import com.stockapp.exception.DaoException;
import com.stockapp.model.ProductAuditLog;
import com.stockapp.model.ValidatorInput;
import com.stockapp.model.ProductAuditLog.OPERATIONTYPE;
import com.stockapp.util.AppLogger;
import com.stockapp.util.DBConnection;

public class AuditProductLogDAOImpl implements AuditproductLogDAO {
    private static final Logger logger = AppLogger.getLogger(SupplierDAOImpl.class);
    private static final String INSERT_AUDIT_SQL = "INSERT INTO product_audit_log (product_id,action,old_value,new_value,changed_by,changed_at) VALUES (?,?,?,?,?,?)";
    private static final String FIND_ALL_SQL = " SELECT * FROM product_audit_log ";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM product_audit_log WHERE id = ?";
    private static final String FIND_PRODUCT_AUDIT_BY_ID_SQL = "SELECT * FROM product_audit_log WHERE product_id = ?";
    private static final String FIND_AUDIT_LAST_HOURS_HISTROY = "SELECT * FROM product_audit_log WHERE CURRENT_TIMESTAMP-(? * INTERVAL '1 hour') <= changed_at";

    @Override
    public Integer insertIntoAudit(ProductAuditLog p, Connection c)throws DaoException,BaseAppExcetpion {
        try (PreparedStatement pstmt = c.prepareStatement(INSERT_AUDIT_SQL,Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, p.getProductId());
            pstmt.setObject(2, p.getOperationsType().getString(), java.sql.Types.OTHER);
            pstmt.setString(3, p.getOldValue());
            pstmt.setString(4, p.getNewValue());
            pstmt.setString(5, p.getChangedBy());
            pstmt.setTimestamp(6,Timestamp.valueOf(p.getCahngedAt()));
            int row = pstmt.executeUpdate();
            if (row == 0) {
                throw new DaoException("insertion faild no row effected.", null);
            }
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }else{
                    throw new DaoException("inseretion succesed but getkeys id faild.", null);
                }
            } 
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                 throw new DaoException("Cannot insert Audit: product with ID " + p.getProductId()+ " does not exist", e);
            }else{
                logger.log(Level.SEVERE,"insertion product faild.", e);
                throw new DaoException("insertion faild.", e);
            }
             
        }
    }
    @Override
    public Integer insertIntoAudit(ProductAuditLog p) throws DaoException, BaseAppExcetpion {
        try (Connection conn = DBConnection.getConnection()) {
            return insertIntoAudit(p, conn);
        } catch (SQLException e) {
            throw new DaoException("simple insertion audut faild:something went wrong.",e);
        }catch (BaseAppExcetpion e){
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        }
    }

    private List<ProductAuditLog> extractResult(ResultSet rs)throws SQLException,BaseAppExcetpion{
        List<ProductAuditLog> listt = new ArrayList<>();
        while (rs.next()) {
            Integer id = rs.getInt("id");
            Integer productId = rs.getInt("product_id");
            String operationsType = rs.getString("action");
            OPERATIONTYPE oper ;
            oper = OPERATIONTYPE.valueOf(operationsType);
            String oldValue = rs.getString("old_value");
            String newValue = rs.getString("new_value");
            String changeBy = rs.getString("changed_by");
            LocalDateTime changeAt = rs.getTimestamp("changed_at").toLocalDateTime();
            ProductAuditLog pp = new ProductAuditLog(id, productId, oper, oldValue, newValue, changeBy, changeAt);
            listt.add(pp);
        }
        return listt;
    };
    @Override
    public List<ProductAuditLog> findAll(Connection c)throws DaoException, BaseAppExcetpion {
        try (PreparedStatement pstmt = c.prepareStatement(FIND_ALL_SQL);ResultSet rs = pstmt.executeQuery()) {
            return extractResult(rs);
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"findAll faild:something went wrong." , e);
            throw new DaoException("findAll faild:something went wrong.", e);
        }
    }
    @Override
    public List<ProductAuditLog> findAll() throws DaoException, BaseAppExcetpion {
        try (Connection conn = DBConnection.getConnection()) {
            return findAll(conn);
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"simple insert faild: somthing went wrong." , e);
            throw new DaoException("simple insert faild: somthing went wrong.",e);
        } catch(BaseAppExcetpion e){
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        }
    }
private Optional<ProductAuditLog> extractResultforOne(ResultSet rs)throws SQLException,BaseAppExcetpion{
    if (rs.next()) {
            Integer id = rs.getInt("id");
            Integer productId = rs.getInt("product_id");
            String operationsType = rs.getString("action");
            OPERATIONTYPE oper ;
            oper = OPERATIONTYPE.valueOf(operationsType);
            String oldValue = rs.getString("old_value");
            String newValue = rs.getString("new_value");
            String changeBy = rs.getString("changed_by");
            LocalDateTime changeAt = rs.getTimestamp("changed_at").toLocalDateTime();
            ProductAuditLog pp = new ProductAuditLog(id, productId, oper, oldValue, newValue, changeBy, changeAt);
            return Optional.ofNullable(pp);
    }
    return Optional.empty();
}
@Override
public Optional<ProductAuditLog> findById(Integer id, Connection c) throws DaoException, BaseAppExcetpion {
    ValidatorInput.nullCheekvaluesInteger(id);

    try (PreparedStatement pstmt = c.prepareStatement(FIND_BY_ID_SQL)) {
        pstmt.setInt(1, id);
        try (ResultSet rs = pstmt.executeQuery()) {
            return extractResultforOne(rs);
        } 
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "find by id faild :something went wrong for id:"+id+" or no found.", e);
        throw new DaoException("find by id faild :something went wrong for id:"+id+" or no found.", e);
    }
}

@Override
public Optional<ProductAuditLog> findById(Integer id) throws DaoException, BaseAppExcetpion {
    try (Connection conn = DBConnection.getConnection()) {
        return findById(id, conn);
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "simple find by id faild:something went worng", e);
        throw new DaoException("simple find by id faild:something went worng", e);
    } catch (BaseAppExcetpion e){
        logger.log(Level.SEVERE, e.getMessage(), e);
        throw e;
    }
}

@Override
public List<ProductAuditLog> findAllProductAuditById(Integer id, Connection c)
        throws DaoException, BaseAppExcetpion {
    ValidatorInput.nullCheekvaluesInteger(id);
    try (PreparedStatement pstm = c.prepareStatement(FIND_PRODUCT_AUDIT_BY_ID_SQL)) {
        pstm.setInt(1, id);
        try (ResultSet rs = pstm.executeQuery()) {
            return extractResult(rs);
        }
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "find by id faild :something went wrong for product_id:"+id+" or no found.", e);
        throw new DaoException("find by id faild :something went wrong for product_id:"+id+" or no found.", e);
        
    }
}

@Override
public List<ProductAuditLog> findAllProductAuditById(Integer id) throws DaoException, BaseAppExcetpion {
    try (Connection conn = DBConnection.getConnection()) {
        return findAllProductAuditById(id, conn);
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "simple find by product_id faild:something went worng", e);
        throw new DaoException("simple find by product_id faild:something went worng", e);
    } catch(BaseAppExcetpion e){
        logger.log(Level.SEVERE, e.getMessage(), e);
        throw e;
    }
}
@Override
public List<ProductAuditLog> findLastHistroyByHour(Integer hours) throws BaseAppExcetpion, DaoException {
    ValidatorInput.nullCheekvaluesInteger(hours);
    if (hours < 0) {
        throw new BaseAppExcetpion("hours must not be negative");
    }
    try (Connection conn = DBConnection.getConnection();PreparedStatement pstm =conn.prepareStatement(FIND_AUDIT_LAST_HOURS_HISTROY)) {
        pstm.setInt(1, hours);
        try (ResultSet rs = pstm.executeQuery()) {
            return extractResult(rs);
        }
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "find last history by hours faild:something went wrong for hours:"+hours, e);
        throw new DaoException("find last history by hours faild:something went wrong for hours:"+hours, e);
    }
}
}
