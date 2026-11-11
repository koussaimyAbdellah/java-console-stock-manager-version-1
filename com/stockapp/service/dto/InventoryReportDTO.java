package com.stockapp.service.dto;

import java.util.List;
import java.util.Map;

import com.stockapp.model.Product;

public class InventoryReportDTO{
    private Integer totalProducts;
    private Map<String, List<Product>> productsByCategory;
    private Map<String, Long> countByCategory;
    private Map<String, Double> valueByCategory;
    private List<Product> criticalStockProducts;
    
    public InventoryReportDTO(Integer a,Map<String, List<Product>> b,Map<String,Long> c,Map<String,Double> d 
                                ,List<Product> e
                            ){
                                totalProducts =a;
                                productsByCategory = b;
                                countByCategory = c;
                                valueByCategory = d;
                                criticalStockProducts = e;
                            }

    public Map<String, Long> getCountByCategory() {
        return countByCategory;
    }
    public List<Product> getCriticalStockProducts() {
        return criticalStockProducts;
    }
    public Map<String, List<Product>> getProductsByCategory() {
        return productsByCategory;
    }
    public Integer getTotalProducts() {
        return totalProducts;
    }
    public Map<String, Double> getValueByCategory() {
        return valueByCategory;
    }
    public void setCountByCategory(Map<String, Long> countByCategory) {
        this.countByCategory = countByCategory;
    }
    public void setCriticalStockProducts(List<Product> criticalStockProducts) {
        this.criticalStockProducts = criticalStockProducts;
    }
    public void setProductsByCategory(Map<String, List<Product>> productsByCategory) {
        this.productsByCategory = productsByCategory;
    }
    public void setTotalProducts(Integer totalProducts) {
        this.totalProducts = totalProducts;
    }
    public void setValueByCategory(Map<String, Double> valueByCategory) {
        this.valueByCategory = valueByCategory;
    }
}

