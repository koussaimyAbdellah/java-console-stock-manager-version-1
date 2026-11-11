package com.stockapp.service.dto;

import java.util.List;
import java.util.Map;

import com.stockapp.model.Product;

public class SupplierReportDTO {
    private Integer supplierId;
    private String supplierName;
    private String email;
    private String phone;
    private Integer totalProducts;
    private Double totalInventoryValue;
    private Map<String, Integer> productCountByCategory;
    private Map<String, Double> valueByCategory;
    private List<Product> products;
    
    public SupplierReportDTO(Integer a,String b,String c,String d,
            Integer e,Double f,Map<String, Integer> j,Map<String, Double> h,
            List<Product> i
    ) {
        supplierId = a;
        supplierName = b;
        email = c;
        phone = d;
        totalProducts = e;
        totalInventoryValue = f;
        productCountByCategory = j;
        valueByCategory = h;
        products = i;
    }
    public String getEmail() {
        return email;
    }
    public String getPhone() {
        return phone;
    }
    public Map<String, Integer> getProductCountByCategory() {
        return productCountByCategory;
    }
    public List<Product> getProducts() {
        return products;
    }
    public Integer getSupplierId() {
        return supplierId;
    }
    public String getSupplierName() {
        return supplierName;
    }
    public Double getTotalInventoryValue() {
        return totalInventoryValue;
    }
    public Integer getTotalProducts() {
        return totalProducts;
    }
    public Map<String, Double> getValueByCategory() {
        return valueByCategory;
    }
}
