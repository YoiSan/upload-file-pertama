/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */

package com.themedimension.ivoryshop.android.models;

public class CustomCard {

    //region GLOBALS

    private String cardNo;
    private String cardCsv;
    private int cardMonth;
    private int cardYear;
    private String name;
    private Address address;

    //endregion

    public CustomCard(String cardNo, String cardCsv, int cardMonth, int cardYear, String name, Address address) {
        this.cardNo = cardNo;
        this.cardCsv = cardCsv;
        this.cardMonth = cardMonth;
        this.cardYear = cardYear;
        this.name = name;
        this.address = address;
    }
}
