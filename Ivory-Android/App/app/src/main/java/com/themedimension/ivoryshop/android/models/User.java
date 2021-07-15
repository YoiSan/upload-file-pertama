/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */

package com.themedimension.ivoryshop.android.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import static com.mikepenz.iconics.Iconics.TAG;

public class User {

    //region GLOBALS

    public static transient final String KEY_USER = "User";
    private static transient User mInstance = new User();
    private int mId = -1;
    private String mFirstName;
    private String mLastName;
    private String mEmail;
    private String mPass;
    private String mPhone;
    private Address mAddress;
    private String cardNumber;
    private String CVS;
    private String expireDate;
    private String cardHolderName;
    private String token;
    private boolean mAnon = false;


    //endregion

    //region CONSTRUCTORS

    public User() {
        mId = -1;
    }

    public User(JSONObject data) {
        instantiateFromJsonObject(data);
    }

    public synchronized static User getInstance() {
        if (mInstance == null) {
            mInstance = new User();
        }
        return mInstance;
    }

    public static User load(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        if (!sharedPreferences.getString(KEY_USER, "").isEmpty())
            mInstance = new Gson().fromJson(sharedPreferences.getString(KEY_USER, ""), User.class);
        return getInstance();
    }

    //endregion

    public static void save(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_USER, new Gson().toJson(getInstance())).commit();
    }

    public void setCardDetails(String cardNumber, String CVS, String expireDate, String cardHolderName) {
        this.cardNumber = cardNumber;
        this.CVS = CVS;
        this.expireDate = expireDate;
        this.cardHolderName = cardHolderName;
    }

    public boolean userCanLogin() {
        return User.getInstance().getEmail() != null && !User.getInstance().getEmail().isEmpty();
//                &&
//                User.getInstance().getPass() != null && !User.getInstance().getPass().isEmpty();
    }

    public synchronized void instantiateFromJsonObject(JSONObject data) {
        try {
            mId = data.getInt("id");
            mFirstName = data.getString("firstName");
            mLastName = data.getString("lastName");
            mEmail = data.getString("email");
            mPass = data.getString("password");
            mPhone = data.getString("phone");

            JSONObject data1 = data.getJSONObject("address");
            String name = data1.getString("street");
            String number = data1.getString("number");
            String district = data1.getString("district");
            String city = data1.getString("city");
            String country = data1.getString("country");
            String zip = data1.getString("postalCode");

            mAddress = new Address(name, number, district, zip, city, country);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Error parsing user from JSON: " + e.getLocalizedMessage());
        }
    }

    //region GETTERS

    public synchronized void instantiateUser(JSONObject data) {
        if (mInstance == null) {
            mInstance = new User();
        }

        try {
            mId = data.getInt("id");
            mFirstName = data.getString("firstName");
            mLastName = data.getString("lastName");
            mPhone = data.getString("phone");
            mEmail = data.getString("email");
            mPass = data.getString("password");

            JSONObject address = data.getJSONObject("address");
            mAddress = new Address(
                    address.getString("street"),
                    address.getString("number"),
                    address.getString("city"),
                    address.getString("postalCode"),
                    address.getString("district"),
                    address.getString("country")
            );
        } catch (JSONException e) {
            Log.e(TAG, "Error instantiating user: " + e.getLocalizedMessage());
        }
    }

    public JSONObject parseToJsonObject() {
        JSONObject object = new JSONObject();
        JSONObject address = new JSONObject();
        try {
            object.put("id", mId);
            object.put("firstName", mFirstName);
            object.put("lastName", mLastName);
            object.put("email", mEmail);
            object.put("phone", mPhone);
            object.put("password", mPass);

            address.put("street", mAddress.getStreet());
            address.put("number", mAddress.getNumber());
            address.put("city", mAddress.getCity());
            address.put("postalCode", mAddress.getPostalCode());
            address.put("district", mAddress.getDistrict());
            address.put("country", mAddress.getCountry());

            object.put("address", address);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSONObject from User: " + e.getLocalizedMessage());
        }

        return object;
    }

    public synchronized int getId() {
        return mId;
    }

    public synchronized void setId(int id) {
        mId = id;
    }

    public synchronized String getFirstName() {
        return mFirstName;
    }

    public synchronized void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public synchronized String getLastName() {
        return mLastName;
    }

    //endregion

    //region SETTERS

    public synchronized void setLastName(String lastName) {
        mLastName = lastName;
    }

    public synchronized String getEmail() {
        return mEmail;
    }

    public synchronized void setEmail(String email) {
        mEmail = email;
    }

    public synchronized String getPhone() {
        return mPhone;
    }

    public synchronized void setPhone(String phone) {
        mPhone = phone;
    }

    public synchronized Address getAddress() {
        return mAddress;
    }

    public synchronized String getPass() {
        return mPass;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    //endregion

    public synchronized void setPass(String pass) {
        mPass = pass;
    }

    @Override
    public String toString() {
        return "User{" +
                "mId=" + mId +
                ", mFirstName='" + mFirstName + '\'' +
                ", mLastName='" + mLastName + '\'' +
                ", mEmail='" + mEmail + '\'' +
                ", mPass='" + mPass + '\'' +
                ", mPhone='" + mPhone + '\'' +
                ", mAddress=" + mAddress +
                '}';
    }

    public synchronized boolean isAnon() {
        return (mEmail == null || mEmail.isEmpty()) || mPass == null;
    }


    public synchronized boolean isAnonymous() {
        return token == null || token.isEmpty();
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCVS() {
        return CVS;
    }

    public void setCVS(String CVS) {
        this.CVS = CVS;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public synchronized void setData(int id, String firstName, String lastName, String email, String phone, String street, String number, String postalCode, String district, String city, String country, String pass) {
        mId = id;
        mFirstName = firstName;
        mLastName = lastName;
        mEmail = email;
        mPhone = phone;
        mAddress = new Address(street, number, city, postalCode, district, country);
        mPass = pass;
    }


    public synchronized void setData(int id, String firstName, String lastName, String email, String phone, String street, String number, String postalCode, String district, String city, String country) {
        mId = id;
        mFirstName = firstName;
        mLastName = lastName;
        mEmail = email;
        mPhone = phone;
        mAddress = new Address(street, number, city, postalCode, district, country);
    }

    public synchronized void setData(String firstName, String lastName, String email, String phone, String street, String number, String postalCode, String district, String city, String country, String pass) {
        mFirstName = firstName;
        mLastName = lastName;
        mEmail = email;
        mPhone = phone;
        mAddress = new Address(street, number, city, postalCode, district, country);
        mPass = pass;
    }

    public synchronized void resetInstance() {
        mInstance = new User();
    }

    public synchronized void logout(Context context) {
        mInstance = new User();
        SharedPreferences sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_USER, new Gson().toJson(getInstance())).commit();
    }

}
