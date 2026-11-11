package com.stockapp.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.stockapp.exception.BaseAppExcetpion;
import com.stockapp.exception.InvalidInputException;

public class Phone extends Product {
    private String brand;
    private String model;
    private Integer storage;

    public Phone(String brand,String model,Integer storage,Integer id ,Integer stock,String name,Double price,Integer supplierId,Categorys category,
        LocalDateTime createAt,LocalDateTime delelteAt,LocalDateTime upDateAt) throws BaseAppExcetpion {
        super(id, stock, name, price, supplierId, category,delelteAt,createAt,upDateAt);

        ValidatorInput.nullValueCheekPhone(brand, model, storage);
        ValidatorInput.inputstockValidation(storage);

        this.brand = brand;
        this.model = model;
        this.storage = storage; 
           
    }
    
    public Phone(Phone other)throws BaseAppExcetpion{
        super(other);
        this.brand = other.brand;
        this.model = other.model;
        this.storage = other.storage;
    }
    public String getBrand() {
        return brand;
    }
    public String getModel() {
        return model;
    }
    public Integer getStorage() {
        return storage;
    }

    public void setModel(String model)throws InvalidInputException {
        ValidatorInput.nullCheekvaluesString(model);
        this.model = model;
    }
    public void setBrand(String brand)throws InvalidInputException {
        ValidatorInput.nullCheekvaluesString(brand);
        this.brand = brand;
    }
    public void setStorage(Integer storage)throws InvalidInputException {
        ValidatorInput.inputstockValidation(storage);
        this.storage = storage;
    }
    @Override
    public String getDetails() {
        DateTimeFormatter ftm = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String detailesFormat = "phone->{id:"+id+",name:"+name+",price:"+price+",supplier:"+getSupplierId()+",category:"+category+",brand:"+brand+",model:"+model+",storage:"+storage+",";
        detailesFormat += "createAt:"+createdAt.format(ftm)+",updateAt"+updatedAt.format(ftm);
        detailesFormat += (deletedAt == null)?"deleteAt: non deleted":"deleteAt:"+deletedAt.format(ftm);
        detailesFormat += "}";
        return detailesFormat;
    }   
}
