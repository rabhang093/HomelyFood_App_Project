package com.example.android.HomelyFood.Model;

public class Rating {
    private String userPhone;
    private String foodId;
    private String rsteValue;
    private String comment;

    public Rating() {
    }

    public Rating(String userPhone, String foodId, String rsteValue, String comment) {
        this.userPhone = userPhone;
        this.foodId = foodId;
        this.rsteValue = rsteValue;
        this.comment = comment;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getRsteValue() {
        return rsteValue;
    }

    public void setRsteValue(String rsteValue) {
        this.rsteValue = rsteValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
