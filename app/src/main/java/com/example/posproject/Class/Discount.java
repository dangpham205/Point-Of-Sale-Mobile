package com.example.posproject.Class;

import java.util.HashMap;
import java.util.Map;

public class Discount {

    private String discountName;
    private int discountValue;

    public Discount() {
    }

    public Discount(String nameDiscount, int valueDiscount) {
        this.discountName = nameDiscount;
        this.discountValue = valueDiscount;
    }

    public String getDiscountName() {
        return discountName;
    }

    public void setDiscountName(String discountName) {
        this.discountName = discountName;
    }

    public int getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(int discountValue) {
        this.discountValue = discountValue;
    }

    @Override
    public String toString() {
        return "Discount{" +
                "nameDiscount='" + discountName + '\'' +
                ", valueDiscount=" + discountValue +
                '}';
    }

    public Map<String,Object> toMap(){
        HashMap<String,Object> result = new HashMap<>();
        result.put("discountName", discountName);
        result.put("discountValue", discountValue);

        return result;
    }
}
