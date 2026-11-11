package com.stockapp.model;

import java.time.LocalDateTime;

import com.stockapp.exception.InvalidInputException;
import com.stockapp.model.ProductAuditLog.OPERATIONTYPE;

// separt method to conserte class make it easy to use and maitain.
public class ValidatorInput {
    //================================regex patterns==========================================================================
    public static  final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final String STRING_PATTERN = "^[A-Za-z0-9 ]+$";
    public static final String PHONE_PATTERN = "^(\\+?[0-9]{1,3}[\\s-]?)?[0-9]{10,15}$";
    public static final String STRING_PATTERN_ALPHABIC = "^[A-Za-z ]+$";
    //====================================unitaire cheek=======================================================================
    public static void nullCheekProduct(Product p)throws InvalidInputException{
        if (p == null) {
            throw new InvalidInputException("invalid input:null product.");
        }
    }
    public static void nullCheekSupplier(Supplier s) throws InvalidInputException{
        if (s ==null) {
            throw new InvalidInputException("invalid input: null supplier exception.");
        }
    }
    public static void nullCheekvaluesString(String name)throws InvalidInputException{
        if (name == null || name.isBlank()) {
            throw new InvalidInputException("invalid input: String argument is null or missing");
        }
    }
    public static void nullCheekvaluesInteger(Integer integer)throws InvalidInputException{
        if (integer == null) {
            throw new InvalidInputException("invalid input: Integer argument is null or missing");
        }
    }
    public static void nullCheekvaluesDouble(Double doub) throws InvalidInputException{
        if(doub == null){
            throw new InvalidInputException("invalid input: double argument is null or missing");
        }
    }
    public static void nullCheekvaluesCatigory(Categorys c) throws InvalidInputException{
        if (c == null) {
            throw new InvalidInputException("invalid input : catigory argument is null or missing");
        }
    }
    public static void nullCheekvaluesLocalDateTime(LocalDateTime l) throws InvalidInputException{
        if (l == null) {
            throw new InvalidInputException("invalid input : LocalDateTime is null or missing");
        }
    }
    //====================================null values chek======================================================================
    public static void nullValuesAdutProductLog(Integer productId,OPERATIONTYPE operationstype,String changeBy,LocalDateTime changeAt )throws InvalidInputException{
        String errormessage = "invalid input: object:adutProductLog->{";
        boolean hasError = false;
        errormessage += "}";
        if (productId == null) {
            errormessage += "productId:isn null,";
            hasError = true;
        }
        if (operationstype == null) {
            errormessage += "operationType:is null,";
            hasError = true;
        }
        if(changeBy == null){
            errormessage += "changeBy:is null";
            hasError = true;
        }
        if (changeAt ==null) {
            errormessage += "changeAt:is null";
            hasError = true;
        }
        errormessage += "}";
        if (hasError) {
            throw new InvalidInputException(errormessage);
        }
    }
    public static void nullCheekvaluesSupplier(String name,String email,String phone,String address)throws InvalidInputException{
        String errormessage = "invalid input: object:supplier->{";
        int  initlen = errormessage.length() +1;
        if (name == null || name.isBlank()) {
            errormessage += "name:is null or blank,";
        }
        if (email == null || email.isBlank()) {
            errormessage += "email:is null or blank,";
        }
        if (phone == null|| phone.isBlank()) {
            errormessage += "phone:is null or blank,";
        }
        if (address == null|| address.isBlank()) {
            errormessage += "adress:is null or blank";
        }
        errormessage += "}";
        if (errormessage.length() > initlen) {
            throw new InvalidInputException(errormessage);
        }
    }
    public static void nullValueCheekaccessoir(String type,String compatibility)throws InvalidInputException{
        String errormessgase = "invalid input: object: accessoir->{";
        int initLen = errormessgase.length() +1;
        if (type == null || type.isBlank()) {
            errormessgase += "type:is null or blank,";
        }
        if (compatibility == null || compatibility.isBlank()) {
            errormessgase += "compatibility: is null or blank";
        }
        errormessgase += "}";
        if (errormessgase.length() >initLen) {
            throw new InvalidInputException(errormessgase);
        }
    }
    public static void nullCheekvaluesBook(String author,String isbn,Integer pages)throws InvalidInputException{
        String errorMessage = "invalid input: object:book->{";
        int initLen = errorMessage.length()+1;
        if (author == null || author.isBlank()) {
            errorMessage += "author:is null or blank,";
        }
        if (isbn == null || isbn.isBlank()) {
            errorMessage += "isbn: is null or blank,";
        }
        if (pages == null) {
            errorMessage += "pages: is null";
        }
        errorMessage += "}";
        if (errorMessage.length() >initLen) {
            throw new InvalidInputException(errorMessage);
        }
    }
    public static void nullValueCheekPhone(String brand,String model,Integer storage)throws InvalidInputException{
        String errormessgase = "invalid input: object:phone->{";
        int initlen = errormessgase.length()+1;
        if (brand == null || brand.isBlank()) {
            errormessgase += "brand:is null or Blank,";
        }
        if (model == null || model.isBlank()) {
            errormessgase += "model: is null or Blank";
        }
        if (storage == null) {
            errormessgase += "storage:is null";
        }
        errormessgase += "}";
        if (errormessgase.length() >initlen) {
            throw new InvalidInputException(errormessgase);
        }  
    }
    public static void nullValueCheekProduct(Integer stock,String name,Double price,Integer supplierId,Categorys category,
       LocalDateTime createAt,LocalDateTime updatedAt) throws InvalidInputException{
            String errorMessage = "invalid input: object:(product base constructer)->{";
            double initlen = errorMessage.length()+1;
            if( stock == null){
                errorMessage +="stock: is null,";
            } 
            if (name == null|| name.isBlank()) {
                errorMessage += "name: is null or blank,";
            }
            if( price ==null){
                errorMessage += "price: is null,";
            }
            if(supplierId == null){
                errorMessage += "supplierId: is null,";
            } 
            if(category == null){
                errorMessage += "category: is null,";
            }
            if(createAt == null){
                errorMessage += "createAt: is null,";
            } 
            if (updatedAt == null) {
                errorMessage += "updateAt: is null";
            }  
            errorMessage += "}";
            if (errorMessage.length() > initlen) {
                throw new InvalidInputException(errorMessage);
            }
    }
    //=============================input pattern or values unitaire validation======================================
    public static void inputNameValidation(String name)throws InvalidInputException{
        nullCheekvaluesString(name);
        if (!name.matches(STRING_PATTERN)){
            throw new InvalidInputException("invalid input: name:"+ name+",non valid:must not contain special car ex:(%$#...).");
        }
    }
    public static void inputAlphabicNameValidation(String name) throws InvalidInputException{
        nullCheekvaluesString(name);
        if (!name.matches(STRING_PATTERN_ALPHABIC)) {
                throw new InvalidInputException("invalid input {name: "+name+"},must contain alphabic.");
            }
    }
    public static void inputEmailValidation(String email)throws InvalidInputException{
        nullCheekvaluesString(email);
        if (!email.matches(EMAIL_PATTERN)) {
            throw new InvalidInputException("invalid input: email:"+email+",must contain @ and . ");
        }
    }
    public static void inputPhoneNumberVAlidation(String number) throws InvalidInputException{
        nullCheekvaluesString(number);
        if (!number.matches(PHONE_PATTERN)) {
            throw new InvalidInputException("invalid input: phone:"+number+",must have country code and the rest of number,shoul not be under 5.\nex: +123 567891 at min");
        }
    }
    public static void inputstockValidation(Integer storage)throws InvalidInputException{//can be reuse for storage and stock
        nullCheekvaluesInteger(storage);
        if (storage < 0) {
            throw new InvalidInputException("invalid input: storage:"+storage+",must be positif");
        }
    }
    public static void inputpriceValidation(Double doub) throws InvalidInputException{
        nullCheekvaluesDouble(doub);
        if (doub <=0) {
            throw new InvalidInputException("invalid input: price:"+doub+",must be strict positive.");
        }
    }
    public static void inputCheekValidionCategory(Categorys c) throws InvalidInputException{
        nullCheekvaluesCatigory(c);
        if (c.getDisplayName() == null || c.getDisplayName().isBlank()) {
            throw new InvalidInputException("invalid input: categroy"+c.getDisplayName()+"must be same as defind in category enum.");
        }
    }
    //=============================input pattern or values validation================================================
    
    public static void inputCheekValidionSupplier(String name,String email,String phone)throws InvalidInputException{
        String errorMessage = "invalid input: object:supplier->{";
        double initlen = errorMessage.length()+1;
        if (!name.matches(STRING_PATTERN)){
            errorMessage += "name:"+name+" non valid:must not contain special car ex:(%$#...),";
        }
        
        if (!email.matches(EMAIL_PATTERN)) {
           errorMessage += "email:"+email+" non valid:must contain @ and,";
        }
        if (!phone.matches(PHONE_PATTERN)) {
            errorMessage +="phone:"+phone+" non valid:must have country code and the rest of number,shoul not be under 5.\nex: +123 567891 at min,";
        }
        errorMessage += "}";
        if (errorMessage.length() > initlen) {
            throw new InvalidInputException(errorMessage);
        }
    }
    public static void inputCheekValidionProduct(String name,Double price,Integer stock)throws InvalidInputException{
        String errorMessage = "invalid input: object:(product constructer)->{";
        double initlen = errorMessage.length()+1;
        if (!name.matches(STRING_PATTERN)) {
                errorMessage += "name: "+name+" non valid:must contain alphabic,";
            }
        if (price <= 0) {
                errorMessage += "price :"+price+" non valid:must be strict possitive,";
            }
            if (stock < 0) {
                errorMessage +="stock:"+stock+" non valid:must be positive,";
            }
        errorMessage += "}";
        if (errorMessage.length() > initlen) {
            throw new InvalidInputException(errorMessage);
        }
    }
    
    public static void inputValationBook(String author,Integer pages)throws InvalidInputException{
        String errorMessage = "invalid input: object:(product constructer)->{";
        double initlen = errorMessage.length()+1;

        if (!author.matches(STRING_PATTERN_ALPHABIC)) {
            errorMessage +="name:"+author+" non valid: msut have alphabic,";
        }
        if (pages <= 0) {
            errorMessage += "pages:"+pages+" non valid:must be positif";
        }
        errorMessage += "}";
        if (errorMessage.length() > initlen) {
            throw new InvalidInputException(errorMessage);
        }
    }
}
