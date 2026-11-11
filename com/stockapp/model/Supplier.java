package com.stockapp.model;

import java.time.LocalDateTime;

import com.stockapp.exception.BaseAppExcetpion;
import com.stockapp.exception.InvalidInputException;

public class Supplier {
    private Integer id;//no changable after creation.
    private String name;
    private String email;
    private String phone;
    private String adress;
    private LocalDateTime createAt;// non changable
    private LocalDateTime deleteAt;

    public Supplier(Integer id,String name,String email,String phone,String address)throws BaseAppExcetpion{

        ValidatorInput.nullCheekvaluesSupplier(name, email, phone, address);
        ValidatorInput.inputCheekValidionSupplier(name, email, phone);

        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.adress = address;
        this.createAt = LocalDateTime.now();
        this.deleteAt = null;
    }
    
    public LocalDateTime getCreateAt() {
        return createAt;
    }
    public LocalDateTime getDeleteAt() {
        return deleteAt;
    }
    public String getAddress() {
        return adress;
    }
    public String getEmail() {
        return email;
    }
    public Integer getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getPhone() {
        return phone;
    }
    public Supplier setCreateAt(LocalDateTime createAt) throws InvalidInputException {
        ValidatorInput.nullCheekvaluesLocalDateTime(createAt);
        this.createAt = createAt;
        return this;
    }
    public Supplier setDeleteAt(LocalDateTime deleteAt) {
        this.deleteAt = deleteAt;
        return this;
    }
    public void setAddress(String address)throws InvalidInputException {
        ValidatorInput.nullCheekvaluesString(address);
        this.adress = address;
    }

    public void setEmail(String email)throws InvalidInputException{
        ValidatorInput.inputEmailValidation(email);
        this.email = email;
    }
    public void setId(Integer id) throws InvalidInputException{
        ValidatorInput.nullCheekvaluesInteger(id);
        this.id = id;
    }
    public void setName(String name)throws InvalidInputException {
        ValidatorInput.inputNameValidation(name);
        this.name = name;
    }
    public void setPhone(String phone)throws InvalidInputException {
        ValidatorInput.inputPhoneNumberVAlidation(phone);
        this.phone = phone;
    }
    @Override
    public String toString() {
        return "supplier->{id:"+id+",name:"+name+",email:"+email+",phone:"+phone+",adress:"+adress+"}";  // Or appropriate string representation
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Supplier)) return false;  // Change to appropriate class
        Supplier other = (Supplier) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
