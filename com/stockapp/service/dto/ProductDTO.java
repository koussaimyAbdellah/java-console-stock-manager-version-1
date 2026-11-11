package com.stockapp.service.dto;

public class ProductDTO {
    private Integer id;
    private String name;
    private Double price;
    private Integer stock;
    private String category;
    private String supplierName;
    private Double totalValue; 
    
        public ProductDTO(Integer id,String name,Double price,Integer stock,String category,
                        String supplierName,Double totalValue
                    ){
                        this.id = id;
                        this.totalValue = totalValue;
                        this.category = category;
                        this.name = name;
                        this.price = price;
                        this.stock = stock;
                        this.supplierName = supplierName;
                    }
        public String getCategory() {
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
        public String getSupplierName() {
            return supplierName;
        }
        public Double getTotalValue() {
            return totalValue;
        }
        public void setCategory(String category) {
            this.category = category;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void setPrice(Double price) {
            this.price = price;
        }
        public void setStock(Integer stock) {
            this.stock = stock;
        }
        public void setSupplierName(String supplierName) {
            this.supplierName = supplierName;
        }
        public void setTotalValue(Double totalValue) {
            this.totalValue = totalValue;
        }
        

}
