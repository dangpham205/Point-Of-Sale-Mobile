package com.example.posproject.Class;

import java.util.HashMap;
import java.util.Map;

public class Product {

    private String productName;
    private int productValue;

    public Product() {
    }

    public Product(String productName, int productValue) {
        this.productName = productName;
        this.productValue = productValue;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getProductValue() {
        return productValue;
    }

    public void setProductValue(int productValue) {
        this.productValue = productValue;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productName='" + productName + '\'' +
                ", productValue=" + productValue +
                '}';
    }

    public Map<String,Object> toMap(){
        HashMap<String,Object> result = new HashMap<>();
        result.put("productName", productName);
        result.put("productValue", productValue);

        return result;
    }
}
