package com.stockapp.service.dto;

public class StockMovementDTO {
    private Integer productId;
    private String productName;
    private Integer previousStock;
    private Integer newStock;
    private Integer quantityMoved;
    private Boolean success;
    private String errorMessage;

    public StockMovementDTO(Integer productId,String productName,Integer previu,Integer newstock,Integer quantityInteger,boolean success,String erromsg){
        this.productId = productId;
        this.productName = productName;
        this.previousStock = previu;
        this.newStock = newstock;
        this.quantityMoved = quantityInteger;
        this.success = success;
        this.errorMessage = erromsg;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    public Integer getNewStock() {
        return newStock;
    }
    public Integer getPreviousStock() {
        return previousStock;
    }
    public Integer getProductId() {
        return productId;
    }
    public String getProductName() {
        return productName;
    }
    public Integer getQuantityMoved() {
        return quantityMoved;
    }
    public Boolean getSuccess() {
        return success;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    public void setNewStock(Integer newStock) {
        this.newStock = newStock;
    }
    public void setPreviousStock(Integer previousStock) {
        this.previousStock = previousStock;
    }
    public void setProductId(Integer productId) {
        this.productId = productId;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public void setQuantityMoved(Integer quantityMoved) {
        this.quantityMoved = quantityMoved;
    }
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
}
