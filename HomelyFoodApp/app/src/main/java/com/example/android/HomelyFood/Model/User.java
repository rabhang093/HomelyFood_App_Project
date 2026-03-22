package com.example.android.HomelyFood.Model;

public class User {

    private String name;
    private String pass;
    private String phone;
    private String isStaff;
    private String homeAddress;

    public User() { }

    public User(String name, String pass) {
        this.name = name;
        this.pass = pass;
        this.isStaff="false";
    }

    public String getIsStaff() {
        return isStaff;
    }

    public void setIsStaff(String isStaff) {
        this.isStaff = isStaff;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }
}
