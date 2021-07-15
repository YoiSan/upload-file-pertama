/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.models;

/**
 * The <class> Address </class> represents one of the fields of any user.
 * <p>
 * You should also check this:
 *
 * @see User
 */

public class Address {

    //region GLOBALS

    private String street;
    private String number;
    private String city;
    private String postalCode;
    private String district;
    private String country;

    //endregion

    /*  Constructors used for creating new instances of this class.
    */
    public Address() {
    }

    public Address(String street, String number, String city, String postalCode, String district, String country) {
        this.street = street;
        this.number = number;
        this.city = city;
        this.postalCode = postalCode;
        this.district = district;
        this.country = country;
    }

    /*  These setters and getters are mainly used in screens that enable the user to
    *   modify the delivery details or the actual address.
    */
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return this.street +
                " " + this.number +
                " " + this.city +
                " " + this.postalCode +
                " " + this.district +
                " " + this.country;
    }
}
