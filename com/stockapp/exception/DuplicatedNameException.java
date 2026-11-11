package com.stockapp.exception;

public class DuplicatedNameException extends BaseAppExcetpion{
    private String duplicatedName;
    public DuplicatedNameException(String m,String c){
        super(m);
        duplicatedName = c;
    }
    public String getDuplicatedName() {
        return duplicatedName;
    }
}
