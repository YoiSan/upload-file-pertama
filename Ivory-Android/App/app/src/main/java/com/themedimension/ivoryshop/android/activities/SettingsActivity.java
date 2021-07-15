/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.activities;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.activities.registerAndLogin.LoginActivity;
import com.themedimension.ivoryshop.android.manager.NetworkManager;
import com.themedimension.ivoryshop.android.manager.OnNetworkCallback;
import com.themedimension.ivoryshop.android.manager.ResponseExtractor;
import com.themedimension.ivoryshop.android.models.User;
import com.themedimension.ivoryshop.android.response.ResponseData;
import com.themedimension.ivoryshop.android.utils.EditTextValidation;
import com.themedimension.ivoryshop.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsActivity extends BaseActivity implements View.OnClickListener, OnNetworkCallback {

    //region GLOBALS

    private static final String TAG = "SettingsActivity";
    TextView toolbarTitle;
    Button saveBtn;
    Button revealButton;
    ImageView datePicker;
    Button backButton;
    private NetworkManager manager;
    private int keyDel;
    private String a;
    //    private FourDigitCardFormatWatcher fourDigitCardFormatWatcher;
    //    private Dialog d;
    private EditText firstName, lastName, email, phone, street, streetNr, city, zip, district, country, cardNr, CVS, expDate, cardHolder;
    private ScrollView scrollView;
//    private NumberPicker monthPicker, yearPicker;

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();
        setupUI();
    }

    @Override
    public void onDateSet(int year, int month, int dayOfMonth) {
        Log.e("onDateSelected", "Settings");
        expDate.setText(cardExpiryDate(year, month));
        expDate.setTextColor(ContextCompat.getColor(this, R.color.black));
        expDate.setError(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            expDate.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.black)));
        }

    }

    public void setupUI() {
        toolbarTitle.setText(getResources().getString(R.string.nav_settings));
        backButton.setOnClickListener(this);

        scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 2.0f));
        firstName.setText(User.getInstance().getFirstName());
        lastName.setText(User.getInstance().getLastName());
        email.setText(User.getInstance().getEmail());
        email.setKeyListener(null);
        email.setEnabled(false);
        phone.setText(User.getInstance().getPhone());
        street.setText(User.getInstance().getAddress().getStreet());
        streetNr.setText(User.getInstance().getAddress().getNumber());
        city.setText(User.getInstance().getAddress().getCity());
        zip.setText(User.getInstance().getAddress().getPostalCode());
        district.setText(User.getInstance().getAddress().getDistrict());
        country.setText(User.getInstance().getAddress().getCountry());

//        cardNr.setText(User.getInstance().getCardNumber());
//        CVS.setText(User.getInstance().getCVS());
//        expDate.setText(User.getInstance().getExpireDate());
//        expDate.setEnabled(false);
//        cardHolder.setText(User.getInstance().getCardHolderName());

//        datePicker.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
    }

    public void init() {
        backButton = (Button) findViewById(R.id.cart_toolbar_back_button);
        toolbarTitle = (TextView) findViewById(R.id.cart_toolbar_title);
        scrollView = (ScrollView) findViewById(R.id.settingsScrollView);
        firstName = (EditText) findViewById(R.id.settings_first_name);
        lastName = (EditText) findViewById(R.id.settings_last_name);
        email = (EditText) findViewById(R.id.settings_email_address);
        phone = (EditText) findViewById(R.id.settings_phone_nr);
        street = (EditText) findViewById(R.id.settings_street);
        streetNr = (EditText) findViewById(R.id.number_editText);
        city = (EditText) findViewById(R.id.city_editText);
        zip = (EditText) findViewById(R.id.zip_editText);
        district = (EditText) findViewById(R.id.district_editText);
        country = (EditText) findViewById(R.id.country_editText);
//        CVS = (EditText) findViewById(R.id.settings_CVS);
        cardNr = (EditText) findViewById(R.id.settings_card_nr);
//        expDate = (EditText) findViewById(R.id.settings_exp_date);
        cardHolder = (EditText) findViewById(R.id.settings_card_holder_name);

//        datePicker = (ImageView) findViewById(R.id.settings_calendar_icon);

        saveBtn = (Button) findViewById(R.id.save_button_id);
    }

    /*
    Method that checks if the data added to the user details fields in the setting screen meets the requirements (e.g. length, input type).
     */
    public boolean changeSettings() {
        boolean ok = true;
        if (!EditTextValidation.isFirstNameValid(firstName.getText().toString().trim()).equals("Success")) {
            firstName.setError(EditTextValidation.isFirstNameValid(firstName.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                firstName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.red)));
            }
            ok = false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                firstName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.black)));
            }
        }

        if (!EditTextValidation.isLastNameValid(lastName.getText().toString().trim()).equals("Success")) {
            lastName.setError(EditTextValidation.isLastNameValid(lastName.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                lastName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.red)));
            }
            ok = false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                lastName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.black)));
            }
        }

        // Email cannot be edited
        if (!EditTextValidation.isValidEmail(email.getText().toString().trim()).equals("Success")) {
            email.setError(EditTextValidation.isValidEmail(email.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                email.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.red)));
            }
            ok = false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                email.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.black)));
            }
        }

        if (!EditTextValidation.isPhoneNumberValid(phone.getText().toString().trim()).equals("Success")) {
            phone.setError(EditTextValidation.isPhoneNumberValid(phone.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                phone.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.red)));
            }
            ok = false;

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                phone.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.black)));
            }
        }

        if (!EditTextValidation.isStreetValid(street.getText().toString().trim()).equals("Success")) {
            street.setError(EditTextValidation.isStreetValid(street.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                street.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.red)));
            }
            ok = false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                street.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.black)));
            }
        }

        if (!EditTextValidation.isStreetNumberValid(streetNr.getText().toString().trim()).equals("Success")) {
            streetNr.setError(EditTextValidation.isStreetNumberValid(streetNr.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                streetNr.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.red)));
            }
            ok = false;

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                streetNr.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.black)));
            }
        }

        if (!EditTextValidation.isCityValid(city.getText().toString().trim()).equals("Success")) {
            city.setError(EditTextValidation.isCityValid(city.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                city.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.red)));
            }
            ok = false;

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                city.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.black)));
            }
        }

        if (!EditTextValidation.isZipValid(zip.getText().toString().trim()).equals("Success")) {
            zip.setError(EditTextValidation.isZipValid(zip.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                zip.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.red)));
            }
            ok = false;

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                zip.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.black)));
            }
        }

        if (!EditTextValidation.isDistrictValid(district.getText().toString().trim()).equals("Success")) {
            district.setError(EditTextValidation.isDistrictValid(district.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                district.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.red)));
            }
            ok = false;

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                district.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.black)));
            }
        }

        if (!EditTextValidation.isCountryValid(country.getText().toString().trim()).equals("Success")) {
            country.setError(EditTextValidation.isCountryValid(country.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                country.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.red)));
            }
            ok = false;

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                country.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.black)));
            }
        }
        return ok;
    }


    private void saveCardInfo() {
        if (cardNr.getText().length() != 0 && CVS.getText().length() != 0 &&
                !expDate.getText().toString().equals(getResources().getString(R.string.mm_yy)) && cardHolder.getText().length() != 0) {
            User.getInstance().setCardDetails(cardNr.getText().toString(), CVS.getText().toString(),
                    expDate.getText().toString(), cardHolder.getText().toString());
            User.save(this);
        }
    }

    private boolean canSaveCardDetails() {
        if (CVS.getText().length() != 0 ||
                expDate.getText().length() != 0 ||
                cardHolder.getText().length() != 0 ||
                cardNr.getText().length() != 0) {


            if (!EditTextValidation.isCardNumberValid(cardNr.getText().toString().trim()).equals("Success")) {
                cardNr.setError(EditTextValidation.isCardNumberValid(cardNr.getText().toString().trim()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cardNr.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.red)));
                }
                return false;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cardNr.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.black)));
                }
            }
            if (!EditTextValidation.isCVSValid(CVS.getText().toString().trim()).equals("Success")) {
                CVS.setError(EditTextValidation.isCVSValid(CVS.getText().toString().trim()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    CVS.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.red)));
                }
                return false;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    CVS.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.black)));
                }
            }

            if (expDate.getText().toString().isEmpty()) {
                expDate.setError("Expire date cannot be empty.");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    expDate.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.red)));
                }
                return false;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    expDate.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.black)));
                }
            }

            if (!EditTextValidation.isCardHolderNameValid(cardHolder.getText().toString().trim()).equals("Success")) {
                Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isCardHolderNameValid(cardHolder.getText().toString().trim()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cardHolder.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                }
                return false;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cardHolder.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SettingsActivity.this, R.color.black)));
                }
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_button_id:
                if (changeSettings()) {
                    JSONObject user = new JSONObject();
                    try {

                        user.put("id", User.getInstance().getId());
                        user.put("firstName", firstName.getText().toString().trim());
                        user.put("lastName", lastName.getText().toString().trim());
                        user.put("email", email.getText().toString().trim());
                        user.put("phone", phone.getText().toString().trim());

                        JSONObject address = new JSONObject();
                        address.put("street", street.getText().toString().trim());
                        address.put("number", Integer.parseInt(streetNr.getText().toString().trim()));
                        address.put("postalCode", zip.getText().toString().trim());
                        address.put("city", city.getText().toString().trim());
                        address.put("country", country.getText().toString().trim());
                        address.put("district", district.getText().toString().trim());

                        user.put("address", address);
                        new NetworkManager(this).setListener(this).updateUserData(user);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error parsing data to JSON: " + e.getLocalizedMessage());
                    }
                }
                break;
//            case R.id.settings_calendar_icon:
//                showCalendar(SettingsActivity.this);
//                break;
            case R.id.cart_toolbar_back_button:
                onBackPressed();
                break;
        }
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onSuccess(Object data, ResponseExtractor.ResponseType type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                User.save(SettingsActivity.this);
                Toast.makeText(SettingsActivity.this, "Update successful", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }


    @Override
    public void onFailed(Object data, ResponseExtractor.ResponseType type) {
        final ResponseData responseData = (ResponseData) data;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showSingleButtonAlertWithoutTitle(SettingsActivity.this, responseData.getDescription());
            }
        });

    }

//    private class FourDigitCardFormatWatcher implements TextWatcher {
//        // Change this to what you want... ' ', '-' etc..
//        private static final char space = '-';
//        private int start;
//        private int before;
//
//        @Override
//        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            this.start = start;
//            this.before = before;
//        }
//
//        @Override
//        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//        }
//
//        /*
//      Method to add the character '-' after every four digits when adding the credit card number.
//       */
//        @Override
//        public void afterTextChanged(Editable s) {
////             Remove spacing char
//            if (s.length() > 0 && (s.length() % 5) == 0) {
//                final char c = s.charAt(s.length() - 1);
//                if (space == c) {
//                    s.delete(s.length() - 1, s.length());
//                }
//            }
//            // Insert char where needed.
//            if (s.length() > 0 && (s.length() % 5) == 0) {
//                char c = s.charAt(s.length() - 1);
//                // Only if its a digit where there should be a space we insert a space
//                if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(space)).length <= 3) {
//                    s.insert(s.length() - 1, String.valueOf(space));
//                }
//            }
//
//            formatCardNr(s);
//        }
//
//        private void formatCardNr(Editable string) {
//            String string1 = String.valueOf(string).replace("-", "");
//            for (int i = 0; i < string1.length(); i++) {
//                if (i == 4 || i == 9 || i == 14)
//                    string1 = new StringBuilder(string1).insert(i, "-").toString();
//            }
//
//            Log.e("string1 length", String.valueOf(string1.length()));
//            cardNr.removeTextChangedListener(fourDigitCardFormatWatcher);
//            cardNr.setText(string1);
//            cardNr.setSelection(before == 1 ? start : cardNr.getText().toString().length());
//            cardNr.addTextChangedListener(fourDigitCardFormatWatcher);
//        }
//    }
}