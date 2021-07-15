/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.activities.registerAndLogin;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.activities.LegalActivity;
import com.themedimension.ivoryshop.android.activities.fragmentActivities.DrawerActivity;
import com.themedimension.ivoryshop.android.manager.NetworkManager;
import com.themedimension.ivoryshop.android.manager.OnNetworkCallback;
import com.themedimension.ivoryshop.android.manager.ProductListsManager;
import com.themedimension.ivoryshop.android.manager.ResponseExtractor;
import com.themedimension.ivoryshop.android.models.User;
import com.themedimension.ivoryshop.android.response.ResponseData;
import com.themedimension.ivoryshop.android.utils.EditTextValidation;
import com.themedimension.ivoryshop.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * In RegisterActivity a new user is created and his information is stored into a sharedpref.
 */
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, OnNetworkCallback {

    //region GLOBALS

    private static final String TAG = "RegisterActivity";

    private static String PASSWORD_CONSTRAINT_MSG;
    private static String PHONE_NO_CONSTRAINT_MSG;

    private NetworkManager mNetworkManager;

    private Button registerButton;
    private Button backButton;
    private TextView termsAndConditions;
    private CheckBox checkBox;
    private EditText firstName,
            lastName,
            email,
            phone,
            street,
            number,
            city,
            ZIP,
            district,
            country,
            password,
            confirmPassword;
    private TextView registerTitle;

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();
        setUpViews();
    }

    public void setUpViews() {
        registerTitle.setText(getResources().getString(R.string.register_title));
        backButton.setOnClickListener(this);

        //setting phone number message to inform the user that it must be 10 digits long
        phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    phone.setError(PHONE_NO_CONSTRAINT_MSG, null);
                }
            }
        });

        //setting password message to inform the user

        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    password.setError(PASSWORD_CONSTRAINT_MSG, null);
                }
            }
        });

        SpannableString content = new SpannableString(getResources().getString(R.string.accept_terms_and_conditions_string));
        content.setSpan(new UnderlineSpan(), 13, content.length(), 0);
        termsAndConditions.setText(content);
        termsAndConditions.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    termsAndConditions.setTextColor(ContextCompat.getColor(RegisterActivity.this, R.color.colorPink));
                    startActivity(new Intent(RegisterActivity.this, LegalActivity.class));
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    //finger was lifted
                    termsAndConditions.setTextColor(ContextCompat.getColor(RegisterActivity.this, R.color.text_medium_grey));
                }
                return true;
            }
        });

        checkBox.setOnClickListener(this);
        registerButton.setOnClickListener(this);

    }

    //Method to initialise EditViews, TextViews, Buttons and CheckBox
    public void init() {
        registerTitle = (TextView) findViewById(R.id.simple_toolbar_title);
        backButton = (Button) findViewById(R.id.simple_toolbar_back_button);

        firstName = (EditText) findViewById(R.id.register_first_name);
        lastName = (EditText) findViewById(R.id.register_last_name);
        email = (EditText) findViewById(R.id.register_email);
        phone = (EditText) findViewById(R.id.register_phone_number);
        street = (EditText) findViewById(R.id.register_street);
        number = (EditText) findViewById(R.id.register_street_number);
        city = (EditText) findViewById(R.id.register_city);
        ZIP = (EditText) findViewById(R.id.zipID);
        district = (EditText) findViewById(R.id.register_district);
        country = (EditText) findViewById(R.id.register_country);

        password = (EditText) findViewById(R.id.register_password);
        confirmPassword = (EditText) findViewById(R.id.register_confirm_password);

        checkBox = (CheckBox) findViewById(R.id.register_terms_checkBox);
        termsAndConditions = (TextView) findViewById(R.id.terms_and_conditions_text);

        registerButton = (Button) findViewById(R.id.register_button);

        PHONE_NO_CONSTRAINT_MSG = getResources().getString(R.string.phone_no_length_constraint_error_msg);
        PASSWORD_CONSTRAINT_MSG = getResources().getString(R.string.password_constraint_msg);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_button:
                if (canRegister()) {
                    //all the fields are correct. A new user will be generated and saved
//                    String md5Password = Utils.md5(password.getText().toString());

                    JSONObject newUser = new JSONObject();
                    JSONObject newAddress = new JSONObject();
                    try {
                        newUser.put("firstName", firstName.getText().toString());
                        newUser.put("lastName", lastName.getText().toString());
                        newUser.put("email", email.getText().toString());
                        newUser.put("phone", phone.getText().toString());
//                        newUser.put("password", md5Password);

                        newAddress.put("street", street.getText().toString());
                        newAddress.put("district", district.getText().toString());
                        newAddress.put("number", number.getText().toString());
                        newAddress.put("city", city.getText().toString());
                        newAddress.put("postalCode", ZIP.getText().toString());
                        newAddress.put("country", country.getText().toString());

                        newUser.put("address", newAddress);
                        newUser.put("password", password.getText().toString());
                        mNetworkManager.createAccount(this, newUser);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error creating user JSON object: " + e.getLocalizedMessage());
                    }
                }
                break;
            case R.id.register_terms_checkBox:
                termsAndConditions.setTextColor(checkBox.isChecked() ? Color.BLACK : ContextCompat.getColor(RegisterActivity.this, R.color.colorPink));
                checkBox.setButtonDrawable(ContextCompat.getDrawable(RegisterActivity.this, R.drawable.register_checkbox_selector));
                break;
            case R.id.simple_toolbar_back_button:
                onBackPressed();
                break;
        }
    }

    /**
     * canRegister method is checking if the fields are valid.
     * If not then alert messages will be displayed and the invalid fields will be underlined with red.
     */
    private boolean canRegister() {
        if (firstName.getText().toString().isEmpty() && lastName.getText().toString().isEmpty() &&
                email.getText().toString().isEmpty() && phone.getText().toString().isEmpty() &&
                street.getText().toString().isEmpty() && number.getText().toString().isEmpty() &&
                city.getText().toString().isEmpty() && ZIP.getText().toString().isEmpty() &&
                district.getText().toString().isEmpty() && country.getText().toString().isEmpty() &&
                password.getText().toString().isEmpty() && confirmPassword.getText().toString().isEmpty()) {
            Utils.showSingleButtonAlertWithoutTitle(this, getResources().getString(R.string.alert_all_fields_are_required));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                firstName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                lastName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                email.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                phone.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                street.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                number.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                city.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                ZIP.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                district.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                country.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                password.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                confirmPassword.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            }
            checkBox.setButtonDrawable(ContextCompat.getDrawable(this, R.drawable.checkbox_not_checked_selector));
            return false;
        }

        if (!EditTextValidation.isFirstNameValid(firstName.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isFirstNameValid(firstName.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                firstName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            }
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                firstName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.navigation_buttons_violet)));
        }

        if (!EditTextValidation.isLastNameValid(lastName.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isLastNameValid(lastName.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                lastName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                lastName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.navigation_buttons_violet)));
        }

        if (!EditTextValidation.isValidEmail(email.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isValidEmail(email.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                email.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                email.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.navigation_buttons_violet)));
        }

        if (!EditTextValidation.isPhoneNumberValid(phone.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isPhoneNumberValid(phone.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                phone.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                phone.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.navigation_buttons_violet)));
        }

        if (!EditTextValidation.isStreetValid(street.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isStreetValid(street.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                street.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                street.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.navigation_buttons_violet)));
        }

        if (!EditTextValidation.isStreetNumberValid(number.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isStreetNumberValid(number.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                number.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                number.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.navigation_buttons_violet)));
        }

        if (!EditTextValidation.isCityValid(city.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isCityValid(city.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                city.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                city.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.navigation_buttons_violet)));
        }

        if (!EditTextValidation.isZipValid(ZIP.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isZipValid(ZIP.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                ZIP.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            return false;
        } else {
            ZIP.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.navigation_buttons_violet)));
        }

        if (!EditTextValidation.isDistrictValid(district.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isDistrictValid(district.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                district.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                district.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.navigation_buttons_violet)));
        }

        if (!EditTextValidation.isCountryValid(country.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isCountryValid(country.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                country.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                country.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.navigation_buttons_violet)));
        }

        if (!EditTextValidation.isPasswordValid(password.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isPasswordValid(password.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                password.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                password.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.navigation_buttons_violet)));
        }


        if (!EditTextValidation.isConfirmPasswordValid(confirmPassword.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isConfirmPasswordValid(confirmPassword.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                confirmPassword.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                confirmPassword.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.navigation_buttons_violet)));
        }

        if (!confirmPassword.getText().toString().equals(password.getText().toString())) {
            Utils.showSingleButtonAlertWithoutTitle(this, getResources().getString(R.string.alert_passwords_don_t_match));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                password.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                confirmPassword.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            }
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                password.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
                confirmPassword.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            }
        }

        if (!checkBox.isChecked()) {
            checkBox.setButtonDrawable(ContextCompat.getDrawable(this, R.drawable.checkbox_not_checked_selector));
            Utils.showSingleButtonAlertWithoutTitle(RegisterActivity.this, getResources().getString(R.string.alert_terms_and_conditions));
            return false;
        } else {
            checkBox.setButtonDrawable(ContextCompat.getDrawable(this, R.drawable.register_checkbox_selector));
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mNetworkManager = new NetworkManager(this);
        mNetworkManager.setListener(this);
    }

    //finishing activity if the back button is pressed
    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onSuccess(Object data, ResponseExtractor.ResponseType type) {
//        Intent goToSignInScreenIntent = new Intent(this, LoginActivity.class);
//        goToSignInScreenIntent.putExtra("email", email.getText().toString());
//        goToSignInScreenIntent.putExtra("password", password.getText().toString());
//        startActivity(goToSignInScreenIntent);
        if (type.name().equals("NEW_USER")) {
            mNetworkManager.loginAccount(RegisterActivity.this, email.getText().toString().trim(), password.getText().toString().trim());
        } else {
            ProductListsManager.getInstance().resetInstance();
            User.save(RegisterActivity.this);
            startActivity(new Intent(RegisterActivity.this, DrawerActivity.class));
            finish();
        }

//
    }

    @Override
    public void onFailed(final Object data, ResponseExtractor.ResponseType type) {
        final String TAG = "ServerResponse";
        final ResponseData responseData = (ResponseData) data;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showSingleButtonAlertWithoutTitle(RegisterActivity.this, responseData.getDescription());
            }
        });
    }
}