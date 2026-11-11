package com.stockapp.service.dto;

import java.util.Map;

public class InventoryHealthDTO {
    private Integer totalProducts;
    private Integer outOfStockCount;
    private Integer lowStockCount;
    private Integer healthyStockCount;
    private Integer overstockedCount;
    private Double totalInventoryValue;
    private Double averageStockPerProduct;
    private Map<String,Long> stockDistributionByCategory;

    public InventoryHealthDTO(Integer totalProducts,Integer outofStockCount,Integer lowStockCount,Integer healthStockCount,Integer a,Double totalInventoryValue ,Double averageStockPerProduct ,Map<String,Long> map){
        this.totalProducts = totalProducts;
        this.outOfStockCount = outofStockCount;
        this.lowStockCount = lowStockCount;
        this.healthyStockCount = healthStockCount;
        this.totalInventoryValue = totalInventoryValue;
        this.averageStockPerProduct = averageStockPerProduct;
        this.stockDistributionByCategory = map;
        this.overstockedCount = a;
    }

    public Double getAverageStockPerProduct() {
        return averageStockPerProduct;
    }
    public Integer getHealthyStockCount() {
        return healthyStockCount;
    }
    public Integer getLowStockCount() {
        return lowStockCount;
    }
    public Integer getOutOfStockCount() {
        return outOfStockCount;
    }
    public Integer getOverstockedCount() {
        return overstockedCount;
    }
    public Map<String, Long> getStockDistributionByCategory() {
        return stockDistributionByCategory;
    }
    public Double getTotalInventoryValue() {
        return totalInventoryValue;
    }
    public Integer getTotalProducts() {
        return totalProducts;
    }
    public void setAverageStockPerProduct(Double averageStockPerProduct) {
        this.averageStockPerProduct = averageStockPerProduct;
    }
    public void setHealthyStockCount(Integer healthyStockCount) {
        this.healthyStockCount = healthyStockCount;
    }
    public void setLowStockCount(Integer lowStockCount) {
        this.lowStockCount = lowStockCount;
    }
    public void setOutOfStockCount(Integer outOfStockCount) {
        this.outOfStockCount = outOfStockCount;
    }
    public void setOverstockedCount(Integer overstockedCount) {
        this.overstockedCount = overstockedCount;
    }
    public void setStockDistributionByCategory(Map<String, Long> stockDistributionByCategory) {
        this.stockDistributionByCategory = stockDistributionByCategory;
    }
    public void setTotalInventoryValue(Double totalInventoryValue) {
        this.totalInventoryValue = totalInventoryValue;
    }
    public void setTotalProducts(Integer totalProducts) {
        this.totalProducts = totalProducts;
    }
}
