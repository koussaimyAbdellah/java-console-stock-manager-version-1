package com.stockapp.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.stockapp.model.Accessory;
import com.stockapp.model.Book;
import com.stockapp.model.Categorys;
import com.stockapp.model.Phone;
import com.stockapp.exception.BaseAppExcetpion;
import com.stockapp.exception.DaoException;
import com.stockapp.exception.InvalidInputException;
import com.stockapp.model.Product;
import com.stockapp.model.ValidatorInput;
import com.stockapp.util.AppLogger;
import com.stockapp.util.DBConnection;

public class ProductDAOImpl implements ProductDAO {
    private static final String INSERT_PRODUCT_SQL = "INSERT INTO products (name,price,stock_quantity,category,supplier_id,created_at,updated_at) VALUES(?,?,?,?,?,?,?)";
    private static final String INSERT_BOOK_SQL =  " INSERT INTO books (product_id,author,isbn,pages) VALUES (?,?,?,?)";
    private static final String INSERT_PHONES_SQL = "INSERT INTO phones (product_id,brand,model,storage_gb) VALUES(?,?,?,?)";
    private static final String INSERT_ACCESSORIES_SQL = "INSERT INTO accessories (product_id,accessory_type,compatibility) VALUES (?,?,?)";
    private static final String UPDATE_PRICE_SQL = "UPDATE products SET price =? WHERE id = ? AND deleted_at IS NULL";
    private static final String UPDATE_STOCK_SQL = "UPDATE products SET stock_quantity= ? WHERE id = ? AND deleted_at IS NULL";
    private static final String SOFT_DELETE_SQL = "UPDATE products SET deleted_at = ? WHERE id = ?";
    private static final String RESTOR_SOFT_DELETE_SQL = "UPDATE products SET deleted_at = null WHERE id = ?";
    private static final String HARD_DELETE_SQL = "DELETE FROM products WHERE id = ?";
    private static final String FIND_ALL_BOOK_SQL = "SELECT * FROM products p JOIN books b ON p.id = b.product_id WHERE p.deleted_at IS null ORDER BY p.created_at";
    private static final String FIND_ALL_PHONE_SQL = "SELECT * FROM products p JOIN phones ph ON p.id = ph.product_id WHERE p.deleted_at IS null ORDER BY p.created_at";
    private static final String FIND_ALL_ACCESSORY_SQL = "SELECT * FROM products p JOIN accessories a ON p.id = a.product_id WHERE p.deleted_at IS null ORDER BY p.created_at";
    private static final String FIND_CATEGORY_SQL = "SELECT category FROM products WHERE id = ? AND deleted_at IS NULL";
    private static final String FIND_CATEGORY_INCLUDING_DELETED_SQL = "SELECT category FROM products WHERE id = ?";
    private static final String FIND_PHONE_BY_ID_SQL = "SELECT p.*, ph.brand, ph.model, ph.storage_gb FROM products p JOIN phones ph ON p.id = ph.product_id WHERE p.id = ? AND p.deleted_at IS NULL";
    private static final String FIND_BOOK_BY_ID_SQL = "SELECT p.*, b.author, b.isbn, b.pages FROM products p JOIN books b ON p.id = b.product_id WHERE p.id = ? AND p.deleted_at IS NULL";
    private static final String FIND_ACCESSORY_BY_ID_SQL = "SELECT p.*, a.accessory_type, a.compatibility FROM products p JOIN accessories a ON p.id = a.product_id WHERE p.id = ? AND p.deleted_at IS NULL";
    private static final String FIND_PHONE_BY_ID_INCLUDING_DELETED_SQL = "SELECT p.*, ph.brand, ph.model, ph.storage_gb FROM products p JOIN phones ph ON p.id = ph.product_id WHERE p.id = ?";
    private static final String FIND_BOOK_BY_ID_INCLUDING_DELETED_SQL = "SELECT p.*, b.author, b.isbn, b.pages FROM products p JOIN books b ON p.id = b.product_id WHERE p.id = ?";
    private static final String FIND_ACCESSORY_BY_ID_INCLUDING_DELETED_SQL = "SELECT p.*, a.accessory_type, a.compatibility FROM products p JOIN accessories a ON p.id = a.product_id WHERE p.id = ?";
    private static final String FIND_BY_SUPPLIER_SQL = 
        "SELECT p.*, " +
           "ph.brand, ph.model, ph.storage_gb, " +
           "b.author, b.isbn, b.pages, " +
           "a.accessory_type, a.compatibility " +
        "FROM products p " +
        "LEFT JOIN phones ph ON p.id = ph.product_id AND p.category = 'PHONE' " +
        "LEFT JOIN books b ON p.id = b.product_id AND p.category = 'BOOK' " +
        "LEFT JOIN accessories a ON p.id = a.product_id AND p.category = 'ACCESSORY' " +
        "WHERE p.supplier_id = ? AND p.deleted_at IS NULL " +
        "ORDER BY p.created_at";
    private static final String FIND_BY_CATEGORY_SQL = 
        "SELECT p.*, " +
               "ph.brand, ph.model, ph.storage_gb, " +
            "b.author, b.isbn, b.pages, " +
            "a.accessory_type, a.compatibility " +
        "FROM products p " +
        "LEFT JOIN phones ph ON p.id = ph.product_id AND p.category = 'PHONE' " +
        "LEFT JOIN books b ON p.id = b.product_id AND p.category = 'BOOK' " +
        "LEFT JOIN accessories a ON p.id = a.product_id AND p.category = 'ACCESSORY' " +
        "WHERE p.category = ? AND p.deleted_at IS NULL " +
        "ORDER BY p.created_at";
    private static final String FIND_BY_LOW_STOCK_SQL = 
        "SELECT p.*, " +
           "ph.brand, ph.model, ph.storage_gb, " +
           "b.author, b.isbn, b.pages, " +
           "a.accessory_type, a.compatibility " +
        "FROM products p " +
        "LEFT JOIN phones ph ON p.id = ph.product_id AND p.category = 'PHONE' " +
        "LEFT JOIN books b ON p.id = b.product_id AND p.category = 'BOOK' " +
        "LEFT JOIN accessories a ON p.id = a.product_id AND p.category = 'ACCESSORY' " +
        "WHERE p.stock_quantity <= ? AND p.deleted_at IS NULL " +
        "ORDER BY p.stock_quantity ASC, p.created_at";
    private static final String FIND_ALL_INCLUDING_DELETED_SQL = 
        "SELECT p.*, " +
           "ph.brand, ph.model, ph.storage_gb, " +
           "b.author, b.isbn, b.pages, " +
           "a.accessory_type, a.compatibility " +
        "FROM products p " +
        "LEFT JOIN phones ph ON p.id = ph.product_id AND p.category = 'PHONE' " +
        "LEFT JOIN books b ON p.id = b.product_id AND p.category = 'BOOK' " +
        "LEFT JOIN accessories a ON p.id = a.product_id AND p.category = 'ACCESSORY' " +
        "ORDER BY p.created_at";

    private static final String GET_TOTAL_VALUE_BY_CATEGORY_SQL = 
        "SELECT COALESCE(SUM(price * stock_quantity), 0) as total_value " +
        "FROM products " +
        "WHERE category = ? AND deleted_at IS NULL";
    private Logger logger = AppLogger.getLogger(ProductDAOImpl.class);

    private void insertIntoSpecificTable(Product p,Integer id,Connection c)throws SQLException,InvalidInputException{
        ValidatorInput.nullCheekvaluesInteger(id);
        if (p instanceof Book) {
            Book b = (Book) p;
            try (PreparedStatement pstmt = c.prepareStatement(INSERT_BOOK_SQL)) {
                pstmt.setInt(1, id);
                pstmt.setString(2, b.getAuthor());
                pstmt.setString(3, b.getIsbn());
                pstmt.setInt(4, b.getPages());
                pstmt.executeUpdate();
            }
        }else if (p instanceof Phone) {
            Phone ph = (Phone) p;
            try (PreparedStatement pstmt = c.prepareStatement(INSERT_PHONES_SQL)) {
                pstmt.setInt(1, id);
                pstmt.setString(2, ph.getBrand());
                pstmt.setString(3, ph.getModel());
                pstmt.setInt(4, ph.getStorage());
                pstmt.executeUpdate();
            }
        }else if(p instanceof Accessory){
            Accessory a = (Accessory) p;
            try (PreparedStatement pstmt = c.prepareStatement(INSERT_ACCESSORIES_SQL)) {
                pstmt.setInt(1, id);
                pstmt.setString(2, a.getType());
                pstmt.setString(3, a.getCompatibility());
                pstmt.executeUpdate();
            }
        }else{
            throw new InvalidInputException("unkowing type product:" +p.getCategory().getDisplayName());
        }
    }
    private void setComunArgument(PreparedStatement pstmt,Product p)throws SQLException{
        pstmt.setString(1, p.getName());
        pstmt.setDouble(2, p.getPrice());
        pstmt.setInt(3, p.getStock());
        pstmt.setObject(4, p.getCategory().getDisplayName().toUpperCase(), Types.OTHER);
        pstmt.setInt(5, p.getSupplierId());
        pstmt.setTimestamp(6, Timestamp.valueOf(p.getCreatedAt()));
        pstmt.setTimestamp(7, Timestamp.valueOf(p.getUpdatedAt())); 
    }
    @Override
    public Integer insertProduct(Product p, Connection c) throws DaoException,BaseAppExcetpion{
        ValidatorInput.nullCheekProduct(p);
        Integer id = null;
        try (PreparedStatement pstmt = c.prepareStatement(INSERT_PRODUCT_SQL,Statement.RETURN_GENERATED_KEYS)) {
            setComunArgument(pstmt, p);
            int row = pstmt.executeUpdate();
            if (row == 0) {
                throw new DaoException("insertion faild no row effected", null);
            }
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }else{
                    throw new DaoException("faild to get generated key.", null);
                }
            }

            insertIntoSpecificTable(p, id, c);
            return id;
        }catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                 throw new DaoException("Cannot insert product: Supplier with ID " + p.getSupplierId() + " does not exist", e);
            }else{
                logger.log(Level.SEVERE,"insertion product faild.", e);
                throw new DaoException("inserertion product faild.", e);
            }
            
        }
    
        }
        @Override
        public Integer insertProduct(Product p)throws DaoException,BaseAppExcetpion {
            Connection conn = null;
            try{
                conn = DBConnection.getConnection();
                conn.setAutoCommit(false);
                Integer productId = insertProduct(p, conn);
                conn.commit();
                return productId;
            } catch (SQLException | BaseAppExcetpion e) {
                try {
                    if (conn != null && !conn.isClosed()) {
                        conn.rollback();
                    }
                } catch (SQLException erollback) {
                     logger.log(Level.SEVERE, "Error during rollback",erollback);
                }
                if (e instanceof SQLException) {
                    SQLException sqlEx = (SQLException) e;
                    if ("23503".equals(sqlEx.getSQLState())) {
                        throw new DaoException("Cannot insert product: Supplier with ID " + p.getSupplierId() + " does not exist", e);
                    } else {
                        throw new DaoException("Product insertion failed: " + e.getMessage(), e);
                    }
                }else{
                    throw new BaseAppExcetpion(e.getMessage());
                }
            } finally{
                try {
                    if (conn != null && !conn.isClosed()) {
                        conn.setAutoCommit(true);
                        DBConnection.closeConnection(conn);
                    }
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Error cleaning up connection", e);
                }
            }
        }

        @Override
        public void updatePriceProduct(Integer id, Double price, Connection c)throws DaoException,BaseAppExcetpion {
            ValidatorInput.nullCheekvaluesInteger(id);
            ValidatorInput.inputpriceValidation(price);
            try (PreparedStatement pstmt = c.prepareStatement(UPDATE_PRICE_SQL)) {
                pstmt.setDouble(1, price);
                pstmt.setInt(2, id);
                int row = pstmt.executeUpdate();
                if (row == 0) {
                    throw new DaoException("update price faild : no row effected.", null);
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE,"update price faild", e);
                throw new DaoException("Update faild.", e);
            }
        }
        @Override
        public void updatePriceProduct(Integer id, Double price) throws DaoException, BaseAppExcetpion {
            try (Connection conn = DBConnection.getConnection()) {
                updatePriceProduct(id, price,conn);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "update price simple faild", e);
                throw new DaoException("simple price update faild", e);
            }   catch(BaseAppExcetpion e){
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw e;
            }
        }
        @Override
        public void updateStockProduct(Integer id, Integer stock, Connection c) throws DaoException,BaseAppExcetpion{
            ValidatorInput.nullCheekvaluesInteger(id);
            ValidatorInput.inputstockValidation(stock);
            try (PreparedStatement pstmt = c.prepareStatement(UPDATE_STOCK_SQL)) {
                pstmt.setInt(1, stock);
                pstmt.setInt(2, id);
                int row = pstmt.executeUpdate();
                if (row == 0) {
                    throw new DaoException("update stock faild no row effected.", null);
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE,"update stock faild", e);
                throw new DaoException("Update faild.", e);
            }
        }
        @Override
        public void updateStockProduct(Integer id, Integer stock)throws DaoException,BaseAppExcetpion {
            try (Connection conn = DBConnection.getConnection()) {
                updateStockProduct(id, stock, conn);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "update stock simple faild", e);
                throw new DaoException("simple stock update faild", e);
            }catch (BaseAppExcetpion e){
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw e;
            }
        }
        @Override
        public void deleteSoftProduct(Integer id, Connection c)throws DaoException,BaseAppExcetpion {
            ValidatorInput.nullCheekvaluesInteger(id);
            try (PreparedStatement pstmt = c.prepareStatement(SOFT_DELETE_SQL)) {
                pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setInt(2, id);
                int row = pstmt.executeUpdate();
                if (row == 0) {
                    throw new DaoException("soft delete was faild : no row effectd", null);
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "soft delete was faild: somthing went wrong.", e);
                throw new DaoException("soft delete was faild: somthing went wrong.", e);
            }
        }
        @Override
        public void deleteSoftProduct(Integer id) throws DaoException, BaseAppExcetpion {
            try (Connection conn = DBConnection.getConnection()) {
                deleteSoftProduct(id, conn);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "simple soft delete was faild: somthing went wrong.", e);
                throw new DaoException("simple soft delete was faild: somthing went wrong.", e);
            }catch(BaseAppExcetpion e){
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw e;
            }
        }

        @Override
        public void restorProduct(Integer id, Connection c) throws DaoException, BaseAppExcetpion {
            ValidatorInput.nullCheekvaluesInteger(id);
            try (PreparedStatement pstmt = c.prepareStatement(RESTOR_SOFT_DELETE_SQL)) {
                pstmt.setInt(1, id);
                int row = pstmt.executeUpdate();
                if (row == 0) {
                    throw new DaoException("restore faild :no row effectd", null);
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "restore product faild: something went wrong.", e);
                throw new DaoException("restore product faild: something went wrong.", e);
            }
        }

        @Override
        public void restorProductI(Integer id) throws DaoException, BaseAppExcetpion {
            try (Connection conn = DBConnection.getConnection()) {
                restorProduct(id, conn);
            } catch (SQLException e){ 
                logger.log(Level.SEVERE, "simple restore faild: somthing went wrong", e);
                throw new DaoException("simple restore faild: somthing went wrong", e);
            }catch(BaseAppExcetpion e){
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw e;
            }
        }

        @Override
        public void hardDeleteProduct(Integer id, Connection c)throws DaoException,BaseAppExcetpion {
            ValidatorInput.nullCheekvaluesInteger(id);
            try (PreparedStatement pstmt = c.prepareStatement(HARD_DELETE_SQL)) {
                pstmt.setInt(1, id);
                int row = pstmt.executeUpdate();
                if (row == 0) {
                    throw new DaoException("hard delete was faild : no row effected.", null);
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "hard delete was faild: somthing went wrong", e);
                throw new DaoException("hard delete was faild: somthing went wrong", e);
            }
        }
        @Override
        public void hardDeleteProduct(Integer id) throws DaoException, BaseAppExcetpion {
            try (Connection conn = DBConnection.getConnection()) {
                hardDeleteProduct(id, conn);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "simple hard delete was faild : somthing went worng", e);
                throw new DaoException("simple hard delete failed", e);  // FIXED: Added missing throw
            } catch (BaseAppExcetpion e){
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw e;
            }
        }
        @Override
        public List<Product> findAll(Connection c) throws DaoException, BaseAppExcetpion {
            List<Product> listp = new ArrayList<>();
            try{
                try (PreparedStatement pstmt = c.prepareStatement(FIND_ALL_BOOK_SQL);ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Book book = extractBookFromResultSet(rs);
                        listp.add(book);

                    }
                }
                try (PreparedStatement pstmt = c.prepareStatement(FIND_ALL_PHONE_SQL);ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Phone phone = extractPhoneFromResultSet(rs);
                        listp.add(phone);
                    }
                }
                try (PreparedStatement pstmt = c.prepareStatement(FIND_ALL_ACCESSORY_SQL);ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Accessory accessory = extractAccessoryFromResultSet(rs);
                        listp.add(accessory);
                    }
                }
                return listp;
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "read from prducts tables is faild : somthing went wrong.", e);
                throw new DaoException("read from prducts tables is faild : somthing went wrong.", e);
            }
        }

        private LocalDateTime convertIntoLocalDateTime(Timestamp tm){
            return (tm != null)?tm.toLocalDateTime():null;
        }
        private Book extractBookFromResultSet(ResultSet rs) throws SQLException,BaseAppExcetpion{
            Integer id =rs.getInt("id");
            Integer stock = rs.getInt("stock_quantity");
            String name =rs.getString("name");
            Double price =rs.getDouble("price");
            Integer supplierId = rs.getInt("supplier_id");
            String categoryValue = rs.getString("category");
            Categorys  category = Categorys.valueOf(categoryValue.toUpperCase());
            LocalDateTime createAt = convertIntoLocalDateTime(rs.getTimestamp("created_at"));
            LocalDateTime deletedAt = convertIntoLocalDateTime(rs.getTimestamp("deleted_at"));
            LocalDateTime  upDateAt = convertIntoLocalDateTime(rs.getTimestamp("updated_at"));
            String author = rs.getString("author");
            String isbn = rs.getString("isbn");
            Integer pages = rs.getInt("pages"); 
            return new Book(author, isbn, pages, id, stock, name, price, supplierId, category, createAt, deletedAt, upDateAt);       
        }
        private Phone extractPhoneFromResultSet(ResultSet rs) throws SQLException,BaseAppExcetpion{
            Integer id =rs.getInt("id");
            Integer stock = rs.getInt("stock_quantity");
            String name =rs.getString("name");
            Double price =rs.getDouble("price");
            Integer supplierId = rs.getInt("supplier_id");
            String categoryValue = rs.getString("category");
            Categorys  category = Categorys.valueOf(categoryValue.toUpperCase());
            LocalDateTime createAt = convertIntoLocalDateTime(rs.getTimestamp("created_at"));
            LocalDateTime deletedAt = convertIntoLocalDateTime(rs.getTimestamp("deleted_at"));
            LocalDateTime  upDateAt = convertIntoLocalDateTime(rs.getTimestamp("updated_at"));
            String brand = rs.getString("brand");
            String model = rs.getString("model");
            Integer storage = rs.getInt("storage_gb");
            return new Phone(brand, model, storage, id, stock, name, price, supplierId, category, createAt, deletedAt, upDateAt);
        }
        private Accessory extractAccessoryFromResultSet(ResultSet rs)throws SQLException,BaseAppExcetpion{
            Integer id =rs.getInt("id");
            Integer stock = rs.getInt("stock_quantity");
            String name =rs.getString("name");
            Double price =rs.getDouble("price");
            Integer supplierId = rs.getInt("supplier_id");
            String categoryValue = rs.getString("category");
            Categorys  category = Categorys.valueOf(categoryValue.toUpperCase());
            LocalDateTime createAt = convertIntoLocalDateTime(rs.getTimestamp("created_at"));
            LocalDateTime deletedAt = convertIntoLocalDateTime(rs.getTimestamp("deleted_at"));
            LocalDateTime  upDateAt = convertIntoLocalDateTime(rs.getTimestamp("updated_at"));
            String type = rs.getString("accessory_type");
            String compatibility = rs.getString("compatibility");
            return new Accessory(type, compatibility, id, stock, name, price, supplierId, category, createAt, deletedAt, upDateAt);
        }
        @Override
        public List<Product> findAll() throws DaoException, BaseAppExcetpion {
            try (Connection conn = DBConnection.getConnection()) {
                return findAll(conn);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "simple read all faild:somthing went wrong.", e);
                throw new DaoException("simple read all faild:somthing went wrong.", e);
            } catch(BaseAppExcetpion b){
                logger.log(Level.SEVERE, b.getMessage(), b);
                throw b;
            }
        }
        @Override
        public Optional<Product> findProductById(Integer id, Connection c)throws DaoException,BaseAppExcetpion {
            ValidatorInput.nullCheekvaluesInteger(id);
            String category =  getCategory(id,c).toUpperCase();
            switch (category) {
                case "BOOK":
                    return Optional.ofNullable(findBookById(id, c));
                case "PHONE":
                    return Optional.ofNullable(findPhoneById(id, c));
                case "ACCESSORY":
                    return Optional.ofNullable(findAccessoryById(id, c));
                default:
                    throw new DaoException("find by id faild somthing went wrong: unknowing product category.",null);
            }
        }
        @Override
        public Optional<Product> findProductById(Integer id) throws DaoException, BaseAppExcetpion {
            try (Connection conn = DBConnection.getConnection()) {
                return findProductById(id,conn);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "simple find by id faild: somthing went wrong.", e);
                throw new DaoException("simple find by id faild: somthing went wrong.", e);
            }catch (BaseAppExcetpion e){
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw e;
            }
        }
        @Override
        public Optional<Product> findProductByIdIncludingDeleted(Integer id, Connection c)
                throws DaoException, BaseAppExcetpion {
            ValidatorInput.nullCheekvaluesInteger(id);
            String category = getCategoryIncludingDeleted(id, c).toUpperCase();
            switch (category) {
                case "BOOK":
                    return Optional.ofNullable(findBookIncludingDeleted(id, c));
                case "PHONE":
                    return Optional.ofNullable(findPhoneIncludingDeleted(id, c));
                case "ACCESSORY":
                    return Optional.ofNullable(findAccessoryIncludingDeleted(id, c));
                default:
                    throw new DaoException("Unknown product category: " + category, null);
            }
        }
        private String getCategory(Integer id, Connection c) throws DaoException {
            try (PreparedStatement pstmt = c.prepareStatement(FIND_CATEGORY_SQL)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                    throw new DaoException("Product not found with id: " + id, null);
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "category find is faild: somthing went wrong.", e);
                throw new DaoException("category find is faild: somthing went wrong.", e);
            }
        }
        private String getCategoryIncludingDeleted(Integer id, Connection c) throws DaoException {
            try (PreparedStatement pstmt = c.prepareStatement(FIND_CATEGORY_INCLUDING_DELETED_SQL)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                    throw new DaoException("Product not found with id: " + id, null);
                }
            } catch (SQLException e) {
                throw new DaoException("Failed to find product category: " + id, e);
            }
        }
        private Phone findPhoneIncludingDeleted(Integer id, Connection c) throws DaoException, BaseAppExcetpion {
            try (PreparedStatement pstmt = c.prepareStatement(FIND_PHONE_BY_ID_INCLUDING_DELETED_SQL)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() ? extractPhoneFromResultSet(rs) : null;
                }
            } catch (SQLException e) {
                throw new DaoException("Failed to find phone by id: " + id, e);
            }
        }
        private Book findBookIncludingDeleted(Integer id, Connection c) throws DaoException, BaseAppExcetpion {
            try (PreparedStatement pstmt = c.prepareStatement(FIND_BOOK_BY_ID_INCLUDING_DELETED_SQL)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() ? extractBookFromResultSet(rs) : null;
                }
            } catch (SQLException e) {
                throw new DaoException("Failed to find book by id: " + id, e);
            }
        }
        private Accessory findAccessoryIncludingDeleted(Integer id, Connection c) throws DaoException, BaseAppExcetpion {
            try (PreparedStatement pstmt = c.prepareStatement(FIND_ACCESSORY_BY_ID_INCLUDING_DELETED_SQL)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() ? extractAccessoryFromResultSet(rs) : null;
                }
            } catch (SQLException e) {
                throw new DaoException("Failed to find accessory by id: " + id, e);
            }
        }
            private Phone findPhoneById(Integer id, Connection c) throws DaoException,BaseAppExcetpion{
        try (PreparedStatement pstmt = c.prepareStatement(FIND_PHONE_BY_ID_SQL)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractPhoneFromResultSet(rs);
                } else {
                    throw new DaoException("Phone product not found with id: " + id,null);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to find phone by id: " + id, e);
            throw new DaoException("Failed to find phone by id: " + id, e);
        }
    }
     private Book findBookById(Integer id, Connection c) throws DaoException,BaseAppExcetpion {
        try (PreparedStatement pstmt = c.prepareStatement(FIND_BOOK_BY_ID_SQL)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractBookFromResultSet(rs);
                } else {
                    throw new DaoException("Book product not found with id: " + id,null);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to find book by id: " + id, e);
            throw new DaoException("Failed to find book by id: " + id, e);
        }
    }
    private Accessory findAccessoryById(Integer id, Connection c) throws DaoException,BaseAppExcetpion {
        try (PreparedStatement pstmt = c.prepareStatement(FIND_ACCESSORY_BY_ID_SQL)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractAccessoryFromResultSet(rs);
                } else {
                    throw new DaoException("Accessory product not found with id: " + id,null);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to find accessory by id: " + id, e);
            throw new DaoException("Failed to find accessory by id: " + id, e);
        }
    }
    

    @Override
    public List<Product> findByCategory(String category, Connection c) throws BaseAppExcetpion, DaoException {
    
        Categorys.getCategoryFrom(category);
    
        List<Product> products = new ArrayList<>();
    
        try (PreparedStatement pstmt = c.prepareStatement(FIND_BY_CATEGORY_SQL)) {
            pstmt.setString(1, category.toUpperCase());
        
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = createProductFromResultSet(rs);
                    if (product != null) {
                        products.add(product);
                    }
                }
            }
            return products;
        
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to find products by category: " + category, e);
            throw new DaoException("Failed to find products by category: " + category, e);
        }
    }

    private Product createProductFromResultSet(ResultSet rs) throws SQLException, BaseAppExcetpion {
        
        Integer id = rs.getInt("id");
        Integer stock = rs.getInt("stock_quantity");
        String name = rs.getString("name");
    Double price = rs.getDouble("price");
    Integer supplierId = rs.getInt("supplier_id");
    
    String categoryValue = rs.getString("category");
    Categorys category;
    try {
        category = Categorys.valueOf(categoryValue.toUpperCase());
    } catch (IllegalArgumentException e) {
        throw new BaseAppExcetpion("Invalid category value: " + categoryValue);
    }
    
    LocalDateTime createAt = convertIntoLocalDateTime(rs.getTimestamp("created_at"));
    LocalDateTime deletedAt = convertIntoLocalDateTime(rs.getTimestamp("deleted_at"));
    LocalDateTime updateAt = convertIntoLocalDateTime(rs.getTimestamp("updated_at"));

   
    switch (category) {
        case BOOK:
            String author = rs.getString("author");
            String isbn = rs.getString("isbn");
            Integer pages = rs.getInt("pages");
            return new Book(author, isbn, pages, id, stock, name, price, 
                          supplierId, category, createAt, deletedAt, updateAt);
                          
        case PHONE:
            String brand = rs.getString("brand");
            String model = rs.getString("model");
            Integer storage = rs.getInt("storage_gb");
            return new Phone(brand, model, storage, id, stock, name, price,
                           supplierId, category, createAt, deletedAt, updateAt);
                           
        case ACCESSORY:
            String type = rs.getString("accessory_type");
            String compatibility = rs.getString("compatibility");
            return new Accessory(type, compatibility, id, stock, name, price,
                               supplierId, category, createAt, deletedAt, updateAt);
                               
    default:
        throw new BaseAppExcetpion("Unknown product category: " + category);
    }
}


@Override
public List<Product> findByCategory(String category) throws BaseAppExcetpion, DaoException {
    try (Connection conn = DBConnection.getConnection()) {
        return findByCategory(category, conn);
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Failed to find products by category: " + category, e);
        throw new DaoException("Failed to find products by category: " + category, e);
    }
}


@Override
public List<Product> findBySupplier(Integer idSupplier, Connection c) throws DaoException, BaseAppExcetpion {
    ValidatorInput.nullCheekvaluesInteger(idSupplier);
    
    List<Product> products = new ArrayList<>();
    
    try (PreparedStatement pstmt = c.prepareStatement(FIND_BY_SUPPLIER_SQL)) {
        pstmt.setInt(1, idSupplier);
        
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Product product = createProductFromResultSet(rs);
                if (product != null) {
                    products.add(product);
                }
            }
        }
        return products;
        
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Failed to find products by supplier: " + idSupplier, e);
        throw new DaoException("Failed to find products by supplier: " + idSupplier, e);
    }
}

@Override
public List<Product> findBySupplier(Integer idSupplier) throws DaoException, BaseAppExcetpion {
    try (Connection conn = DBConnection.getConnection()) {
        return findBySupplier(idSupplier, conn);
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Failed to find products by supplier: " + idSupplier, e);
        throw new DaoException("Failed to find products by supplier: " + idSupplier, e);
    }
}

@Override
public List<Product> findByLowStock(Integer threshold, Connection c) throws DaoException, BaseAppExcetpion {
    ValidatorInput.inputstockValidation(threshold);

    
    List<Product> products = new ArrayList<>();
    
    try (PreparedStatement pstmt = c.prepareStatement(FIND_BY_LOW_STOCK_SQL)) {
        pstmt.setInt(1, threshold);
        
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Product product = createProductFromResultSet(rs);
                if (product != null) {
                    products.add(product);
                }
            }
        }
        return products;
        
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Failed to find low stock products with threshold: " + threshold, e);
        throw new DaoException("Failed to find low stock products with threshold: " + threshold, e);
    }
}

@Override
public List<Product> findByLowStock(Integer threshold) throws DaoException, BaseAppExcetpion {
    try (Connection conn = DBConnection.getConnection()) {
        return findByLowStock(threshold, conn);
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Failed to find low stock products with threshold: " + threshold, e);
        throw new DaoException("Failed to find low stock products with threshold: " + threshold, e);
    }
}
private static final String GET_TOTAL_VALUE_SQL = 
    "SELECT COALESCE(SUM(price * stock_quantity), 0) as total_value " +
    "FROM products " +
    "WHERE deleted_at IS NULL";

@Override
public Double getTotalValue(Connection c) throws BaseAppExcetpion, DaoException {
    try (PreparedStatement pstmt = c.prepareStatement(GET_TOTAL_VALUE_SQL);
         ResultSet rs = pstmt.executeQuery()) {
        
        if (rs.next()) {
            return rs.getDouble("total_value");
        } else {
            return 0.0;
        }
        
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Failed to calculate total inventory value", e);
        throw new DaoException("Failed to calculate total inventory value", e);
    }
}

@Override
public Double getTotalValue() throws BaseAppExcetpion, DaoException {
    try (Connection conn = DBConnection.getConnection()) {
        return getTotalValue(conn);
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Failed to calculate total inventory value", e);
        throw new DaoException("Failed to calculate total inventory value", e);
    }
}



@Override
public Double getTotalValueByCategory(String category, Connection c) throws BaseAppExcetpion, DaoException {
    // Validate the category
    Categorys.getCategoryFrom(category);
    
    try (PreparedStatement pstmt = c.prepareStatement(GET_TOTAL_VALUE_BY_CATEGORY_SQL)) {
        pstmt.setString(1, category.toUpperCase());  // FIXED: Changed from toLowerCase() to toUpperCase()
        
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total_value");
            } else {
                return 0.0;
            }
        }
        
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Failed to calculate total value for category: " + category, e);
        throw new DaoException("Failed to calculate total value for category: " + category, e);
    }
}

@Override
public Double getTotalvalueBycategory(String category) throws BaseAppExcetpion, DaoException {
    try (Connection conn = DBConnection.getConnection()) {
        return getTotalValueByCategory(category, conn);
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Failed to calculate total value for category: " + category, e);
        throw new DaoException("Failed to calculate total value for category: " + category, e);
    } catch( BaseAppExcetpion e){
        logger.log(Level.SEVERE, e.getMessage(), e);
        throw e;
    }
}


@Override
public List<Product> findAllInlcudingDeleted(Connection c)throws BaseAppExcetpion,DaoException{
    List<Product> products = new ArrayList<>();
    
    try (PreparedStatement pstmt = c.prepareStatement(FIND_ALL_INCLUDING_DELETED_SQL);
         ResultSet rs = pstmt.executeQuery()) {
        
        while (rs.next()) {
            Product product = createProductFromResultSet(rs);
            if (product != null) {
                products.add(product);
            }
        }
        return products;
        
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Failed to retrieve all products including deleted", e);
        throw new DaoException("Failed to retrieve all products including deleted", e);
    }
}

@Override
public List<Product> findAllIncludingDeleted() throws BaseAppExcetpion, DaoException {
    try (Connection conn = DBConnection.getConnection()) {
        return findAllInlcudingDeleted(conn);
    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Failed to retrieve all products including deleted", e);
        throw new DaoException("Failed to retrieve all products including deleted", e);
    }catch(BaseAppExcetpion e){
        logger.log(Level.SEVERE, e.getMessage(), e);
        throw e;
    }
}
   @Override
   public void bulkPriceUpdate(Map<Integer, Double> productUpdates, Double percentage, Connection conn)
           throws DaoException, BaseAppExcetpion {
        try (PreparedStatement pstm = conn.prepareStatement(UPDATE_PRICE_SQL)) {
            Double percentageVals = (100 + percentage)/100;
            int count = 0;
            for(Map.Entry<Integer, Double> vals:productUpdates.entrySet()){
                pstm.setDouble(1, vals.getValue()*percentageVals);
                pstm.setInt(2, vals.getKey());
                pstm.addBatch();

                count++;

                if (count%1000 == 0) {
                    pstm.executeBatch();
                }
            }
            pstm.executeBatch();
        } catch (SQLException e) {
            logger.log(Level.SEVERE,"bulk pirce operation was faild", e);
            throw new DaoException("bulk pirce operation was faild", e) ; 
        }
       
   }
}