package com.stockapp.exception;

public class SupplierNotFoundException extends BaseAppExcetpion {
    private Integer id;
    private String name;
    public SupplierNotFoundException(Integer id,String name ){
        super(messagebuilder(id, name));
        this.id = id;
        this.name = name;
    }
    private static String messagebuilder(Integer id,String name){
        if (id == null && name != null) {
            return "supplier not found name:"+name;
        }
        if (id !=null && name == null) {
            return "supplier not found id:"+id;
        }
        return"supplier not found (no argument specified).";
    }
    public Integer getId() {
        return id;
    }
    public String getName() {
        return name;
    }
}
