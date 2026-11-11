package com.stockapp.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.stockapp.exception.BaseAppExcetpion;
import com.stockapp.exception.InvalidInputException;


public class Accessory extends Product {
    private String type;
    private String compatibility;
    
    public Accessory(String type,String compatibility,Integer id,Integer stock,String name,Double price,Integer supplierId,Categorys category,
        LocalDateTime createAt,LocalDateTime delelteAt,LocalDateTime upDateAt)throws BaseAppExcetpion{
            super(id, stock, name, price, supplierId, category,delelteAt,createAt,upDateAt);
            ValidatorInput.nullValueCheekaccessoir(type, compatibility);
            this.type = type;
            this.compatibility = compatibility;
    }

    public Accessory(Accessory other) throws BaseAppExcetpion{
        super(other);
        this.type = other.type;
        this.compatibility = other.compatibility;
    }
    public String getType() {
        return type;
    }
    public String getCompatibility() {
        return compatibility;
    }

    public void setType(String type)throws InvalidInputException {
        ValidatorInput.nullCheekvaluesString(type);
        this.type = type;
    }
    public void setCompatibility(String compatibility)throws InvalidInputException {
        ValidatorInput.nullCheekvaluesString(compatibility);
        this.compatibility = compatibility;
    }

    @Override
    public String getDetails() {
        DateTimeFormatter ftm = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String detailesFormat = "Accessory->{id:"+id+",name:"+name+",price:"+price+",supplier:"+getSupplierId()+",category:"+category+",type:"+type+",compatibility:"+compatibility+",";
        detailesFormat += "createAt:"+createdAt.format(ftm)+",updateAt"+updatedAt.format(ftm);
        detailesFormat += (deletedAt == null)?"deleteAt: non deleted":"deleteAt:"+deletedAt.format(ftm);
        detailesFormat += "}";
        return detailesFormat;
    }
}
