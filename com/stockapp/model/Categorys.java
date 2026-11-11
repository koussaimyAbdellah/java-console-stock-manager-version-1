package com.stockapp.model;

import com.stockapp.exception.InvalidInputException;

public enum Categorys {
    BOOK,PHONE,ACCESSORY;
    
    public String getDisplayName() {
        return switch(this) {
            case BOOK -> "BOOK";
            case PHONE -> "PHONE";
            case ACCESSORY -> "ACCESSORY";
        };
    }
    public static Categorys getCategoryFrom(String categroy)throws InvalidInputException{
        if (categroy == null || categroy.isBlank()) {
            throw new InvalidInputException("invalid category input: category is null or blank");
        }
        String tolowercase = categroy.toLowerCase();
        if ("book".equals(tolowercase)) {
            return Categorys.BOOK;
        }else if ("phone".equals(tolowercase)) {
            return Categorys.PHONE;
        }else if ("accessory".equals(tolowercase)) {
            return Categorys.ACCESSORY;
        }else{
            throw new InvalidInputException("invalid category input: must be book ,phone,accessory.not:" + categroy);
        }
    }
}
