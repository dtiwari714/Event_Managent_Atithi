package com.example.userseller.models;

public class ModelShop {
    private String uid,Email,Name,ShopName,Phone,DeliveryFee,Country,State,City
            ,Address,Latitude,Longitude,Timestamp,AccountType,
            Online,ShopOpen,ProfileImg;

    public ModelShop() {

    }

    public ModelShop(String uid, String email, String name, String shopName,
                     String phone, String deliveryFee, String country,
                     String state, String city, String address,
                     String latitude, String longitude, String timestamp,
                     String accountType, String online, String shopOpen,
                     String profileImg) {
        this.uid = uid;
        this.Email = email;
        this.Name = name;
        this.ShopName = shopName;
        this.Phone = phone;
        this.DeliveryFee = deliveryFee;
        this.Country = country;
        this.State = state;
        this.City = city;
        this.Address = address;
        this.Latitude = latitude;
        this.Longitude = longitude;
        this.Timestamp = timestamp;
        this.AccountType = accountType;
        this.Online = online;
        this.ShopOpen = shopOpen;
        this.ProfileImg = profileImg;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getShopName() {
        return ShopName;
    }

    public void setShopName(String shopName) {
        ShopName = shopName;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getDeliveryFee() {
        return DeliveryFee;
    }

    public void setDeliveryFee(String deliveryFee) {
        DeliveryFee = deliveryFee;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(String timestamp) {
        Timestamp = timestamp;
    }

    public String getAccountType() {
        return AccountType;
    }

    public void setAccountType(String accountType) {
        AccountType = accountType;
    }

    public String getOnline() {
        return Online;
    }

    public void setOnline(String online) {
        Online = online;
    }

    public String getShopOpen() {
        return ShopOpen;
    }

    public void setShopOpen(String shopOpen) {
        ShopOpen = shopOpen;
    }

    public String getProfileImg() {
        return ProfileImg;
    }

    public void setProfileImg(String profileImg) {
        ProfileImg = profileImg;
    }
}
