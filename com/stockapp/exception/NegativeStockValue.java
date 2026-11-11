package com.stockapp.exception;

public class NegativeStockValue extends BaseAppExcetpion{
    private double availabe;
    private double requirement;
    public NegativeStockValue(double availabe,double requirement){
        super("insuffusante stock quantaty: availabale: "+availabe+",requirement:"+requirement);
        this.availabe = availabe;
        this.requirement = requirement;
    }
    public double getAvailabe() {
        return availabe;
    }
    public double getRequirement() {
        return requirement;
    }
}
