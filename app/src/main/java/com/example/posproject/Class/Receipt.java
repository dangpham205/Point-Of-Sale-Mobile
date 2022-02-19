package com.example.posproject.Class;

public class Receipt {

    private String currTime;
    private String customerInfo;
    private int customerTotal;
    private String customerCart;

    public Receipt() {
    }

    public Receipt(String currTime, String customerInfo, int customerTotal, String customerCart) {
        this.currTime = currTime;
        this.customerInfo = customerInfo;
        this.customerTotal = customerTotal;
        this.customerCart = customerCart;
    }

    public String getCurrTime() {
        return currTime;
    }

    public void setCurrTime(String currTime) {
        this.currTime = currTime;
    }

    public String getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(String customerInfo) {
        this.customerInfo = customerInfo;
    }

    public int getCustomerTotal() {
        return customerTotal;
    }

    public void setCustomerTotal(int customerTotal) {
        this.customerTotal = customerTotal;
    }

    public String getCustomerCart() {
        return customerCart;
    }

    public void setCustomerCart(String customerCart) {
        this.customerCart = customerCart;
    }
}
