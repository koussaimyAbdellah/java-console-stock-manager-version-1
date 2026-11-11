package com.stockapp.model;

import java.time.LocalDateTime;

import com.stockapp.exception.BaseAppExcetpion;

// this class is gonna use to fortamed this to 
// reporte file .so this should match the definition
//of table
public class ProductAuditLog {

    public enum OPERATIONTYPE{
    UPDATE,DELETE,INSERT,RESTORE,RESTOCK,
    SELL,DESTOCK,BULKPRICEUPDATE;

    public String getString(){
        return switch(this){
            case UPDATE -> "UPDATE";
            case DELETE -> "DELETE";
            case INSERT -> "INSERT";
            case RESTORE ->"RESTORE";
            case RESTOCK -> "RESTOCK";
            case SELL -> "SELL";
            case DESTOCK -> "DESTOCK";
            case BULKPRICEUPDATE ->"BULKPRICEUPDATE";
        };
    }
    }
    private Integer id;
    private Integer productId;
    private OPERATIONTYPE operationsType;
    private String oldValue;
    private String newValue;
    private String changeBy;
    private LocalDateTime changeAt;

    public ProductAuditLog(Integer id,Integer productId,
        OPERATIONTYPE type,String oldValue,String newValue,
        String changedBy,LocalDateTime changedAt)throws BaseAppExcetpion{
            ValidatorInput.nullValuesAdutProductLog(productId, type, changedBy, changedAt);
            this.id = id;
            this.productId = productId;
            this.operationsType = type;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.changeBy = changedBy;
            this.changeAt = changedAt;
    }
    public void setId(Integer id)throws BaseAppExcetpion {
        ValidatorInput.nullCheekvaluesInteger(id);
        this.id = id;
    }
  public LocalDateTime getCahngedAt() {
      return changeAt;
  }
  public String getChangedBy() {
      return changeBy;
  }
  public Integer getId() {
      return id;
  }
  public String getNewValue() {
      return newValue;
  }
  public String getOldValue() {
      return oldValue;
  }
  public OPERATIONTYPE getOperationsType() {
      return operationsType;
  }
  public Integer getProductId() {
      return productId;
  }
  @Override
    public String toString() {
        return String.format(
            "AuditLog[id=%d, productId=%d, type=%s, changedBy=%s, changedAt=%s]",
            id, productId, operationsType, changeBy, changeAt);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ProductAuditLog)) return false;  // Change to appropriate class
        ProductAuditLog other = (ProductAuditLog) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}