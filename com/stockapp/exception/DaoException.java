package com.stockapp.exception;

import java.sql.SQLException;

public class DaoException extends SQLException {
    private Throwable t;
    private String m;
    
    public DaoException(String m,Throwable t){
        super(m);
        this.t = t;
        this.m = m;
    }
    public String getM() {
        return m;
    }
    public Throwable getT() {
        return t;
    }
}
