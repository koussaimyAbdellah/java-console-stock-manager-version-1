package com.stockapp.model;

import java.time.LocalDateTime;
import java.util.Optional;

import com.stockapp.exception.*;

public abstract class Product {
    protected Integer id;//no changable 
    protected String name;//no changable and  unique
    protected Double price;
    protected Integer stock;
    protected Categorys category;
    protected Integer SupplierId;
    protected LocalDateTime deletedAt;
    protected LocalDateTime createdAt;//non changable
    protected LocalDateTime updatedAt;

    public Product(Integer id ,Integer stock,String name,Double price,Integer supplierId,Categorys category,
        LocalDateTime deteleteAt,LocalDateTime createAt,LocalDateTime updatedAt) throws BaseAppExcetpion{

            ValidatorInput.nullValueCheekProduct(stock, name, price, supplierId, category, createAt, updatedAt);
            ValidatorInput.inputCheekValidionProduct(name, price, stock);

            this.id = id;
            this.name = name; 
            this.price = price;
            this.stock = stock;
            this.category = category;
            this.SupplierId = supplierId;
            this.deletedAt = deteleteAt;
            this.createdAt = createAt;
            this.updatedAt = updatedAt;
    }
    
    public Product(Product other) throws BaseAppExcetpion {
        ValidatorInput.nullCheekProduct(other);
        this.id = other.id;
        this.name = other.name;
        this.price = other.price;
        this.stock = other.stock;
        this.category = other.category;       
        this.SupplierId = other.SupplierId;
        this.deletedAt = other.deletedAt;   
        this.createdAt = other.createdAt;
        this.updatedAt = other.updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public Optional<LocalDateTime> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Categorys getCategory() {
        return category;
    }
    public Integer getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Double getPrice() {
        return price;
    }
    public Integer getStock() {
        return stock;
    }
    public Integer getSupplierId() {
        return SupplierId;
    }

    public void setCategory(Categorys category)throws InvalidInputException {
        ValidatorInput.nullCheekvaluesCatigory(category);
        this.category = category;
    }
    public void setId(Integer id)throws InvalidInputException {
        ValidatorInput.nullCheekvaluesInteger(id);
        this.id = id;
    }
    public void setName(String name)throws InvalidInputException {
        ValidatorInput.inputNameValidation(name);
        this.name = name;
    }
    public Product setPrice(Double price)throws InvalidInputException {
        ValidatorInput.inputpriceValidation(price);
        this.price = price;
        return this;
    }
    public Product setStock(Integer stock)throws InvalidInputException {
        ValidatorInput.inputstockValidation(stock);
        this.stock = stock;
        return this;
    }
    public void setSupplierId(Integer supplierId)throws InvalidInputException {
        ValidatorInput.nullCheekvaluesInteger(supplierId);
        this.SupplierId = supplierId;
    }
    public void setCreatedAt(LocalDateTime createdAt) throws InvalidInputException {
        ValidatorInput.nullCheekvaluesLocalDateTime(createdAt);
        this.createdAt = createdAt;
    }
    public Product setDeletedAt(LocalDateTime deletedAt){
        this.deletedAt = deletedAt;
        return this;
    }
    public void setUpdatedAt(LocalDateTime updatedAt)throws InvalidInputException {
        ValidatorInput.nullCheekvaluesLocalDateTime(updatedAt);
        this.updatedAt = updatedAt;
    }
    
    public boolean isLowStock(Integer stocks){
        if (stocks == null) {
            stocks = 10;
        }
        if (stocks < 0) {
            throw new IllegalArgumentException("Low-stock threshold must not be negative");
        }
        return this.stock <= stocks;
    }
    public Double calculateValue(){
        return (price != null && stock != null) ? price * stock : 0.0;
    }
    abstract public String getDetails();
    @Override
    public String toString() {
        return getDetails();  // Or appropriate string representation
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Product)) return false;  // Change to appropriate class
        Product other = (Product) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
