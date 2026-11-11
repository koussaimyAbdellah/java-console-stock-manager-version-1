package com.stockapp.exception;

public class InvalidInputException extends BaseAppExcetpion {
    private String m;
    public InvalidInputException(String m){
        super(m);
        this.m = m;
        //let the caller build the message here
    }
    public String getM() {
        return m;
    }
}
   
