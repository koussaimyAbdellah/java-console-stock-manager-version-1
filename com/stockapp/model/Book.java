package com.stockapp.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.stockapp.exception.BaseAppExcetpion;
import com.stockapp.exception.InvalidInputException;

public class Book extends Product {

    private String author;
    private String isbn;
    private Integer pages;
    public Book(String author,String isbn,Integer pages,Integer id ,Integer stock,String name,Double price,Integer supplierId,Categorys category,
        LocalDateTime createAt,LocalDateTime delelteAt,LocalDateTime upDateAt)throws BaseAppExcetpion{
        super(id, stock, name, price, supplierId, category,delelteAt,createAt,upDateAt);
        
        ValidatorInput.nullCheekvaluesBook(author, isbn, pages);
        ValidatorInput.inputValationBook(author, pages);

        this.author = author;
        this.isbn = isbn;
        this.pages = pages;
        
    }
    
    public Book(Book other)throws BaseAppExcetpion{
        super(other);
        this.author = other.author;
        this.isbn = other.isbn;
        this.pages = other.pages;
    }
    public String getAuthor() {
        return author;
    }
    public String getIsbn() {
        return isbn;
    }
    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) throws InvalidInputException{
        ValidatorInput.inputstockValidation(pages);
        this.pages = pages;
    }
    public void setIsbn(String isbn) throws InvalidInputException {
        ValidatorInput.nullCheekvaluesString(isbn);
        this.isbn = isbn;
    }
    public void setAuthor(String author) throws InvalidInputException {
        ValidatorInput.inputNameValidation(author);
        this.author = author;
    }
    
    @Override
    public String getDetails() {
        DateTimeFormatter ftm = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String detailesFormat = "book->{id:"+id+",name:"+name+",price:"+price+",supplier:"+getSupplierId()+",category:"+category+",author:"+author+",isbn:"+isbn+",pages:"+pages+",";
        detailesFormat += "createAt:"+createdAt.format(ftm)+",updateAt"+updatedAt.format(ftm);
        detailesFormat += (deletedAt == null)?"deleteAt: non deleted":"deleteAt:"+deletedAt.format(ftm);
        detailesFormat += "}";
        return detailesFormat;
    }
}
