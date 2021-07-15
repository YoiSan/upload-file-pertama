/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.activities.checkout;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.models.User;
import com.themedimension.ivoryshop.android.utils.EditTextValidation;
import com.themedimension.ivoryshop.android.utils.Utils;

public class DeliveryActivity extends AppCompatActivity implements View.OnClickListener {

    //region GLOBALS
    private static final String TAG = "DeliveryActivity";
    private static String PHONE_NO_LENGTH_ERROR;

    private TextView deliveryTitle;
    private Button backButton;

    private ScrollView scrollView;
    private EditText firstName, lastName, email, phone, street, streetNumber, city, ZIP, district, country;
    private ImageView plusButton;

    private LinearLayout secondRecipient;
    private RelativeLayout chooseRecipientLayout;
    private EditText recipientFirstName, recipientLastName, recipientEmail, recipientPhone,
            recipientStreet, recipientStreetNumber,
            recipientCity, recipientZIP,
            recipientDistrict, recipientCountry;

    private Button paymentButton;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        init();
        setupUI();
    }

    private void initMessages() {
        Resources res = getResources();
        PHONE_NO_LENGTH_ERROR = res.getString(R.string.phone_no_length_constraint_error_msg);
    }

    public void init() {
        initMessages();

        deliveryTitle = (TextView) findViewById(R.id.cart_toolbar_title);
        backButton = (Button) findViewById(R.id.cart_toolbar_back_button);

        scrollView = (ScrollView) findViewById(R.id.scroll_view);

        firstName = (EditText) findViewById(R.id.delivery_first_name);
        lastName = (EditText) findViewById(R.id.delivery_last_name);
        email = (EditText) findViewById(R.id.delivery_email);
        phone = (EditText) findViewById(R.id.delivery_phone_number);
        street = (EditText) findViewById(R.id.delivery_street);
        streetNumber = (EditText) findViewById(R.id.delivery_street_number);
        city = (EditText) findViewById(R.id.delivery_city);
        ZIP = (EditText) findViewById(R.id.delivery_zip);
        district = (EditText) findViewById(R.id.delivery_district);
        country = (EditText) findViewById(R.id.delivery_country);

        plusButton = (ImageView) findViewById(R.id.recipient_button);

        paymentButton = (Button) findViewById(R.id.continue_to_payment);

        secondRecipient = (LinearLayout) findViewById(R.id.second_recipient);
        chooseRecipientLayout = (RelativeLayout) findViewById(R.id.choose_recipient_layout);
        recipientFirstName = (EditText) findViewById(R.id.recipient_first_name);
        recipientLastName = (EditText) findViewById(R.id.recipient_last_name);
        recipientEmail = (EditText) findViewById(R.id.recipient_email);
        recipientPhone = (EditText) findViewById(R.id.recipient_phone);
        recipientStreet = (EditText) findViewById(R.id.recipient_street);
        recipientStreetNumber = (EditText) findViewById(R.id.recipient_number);
        recipientCity = (EditText) findViewById(R.id.recipient_city);
        recipientZIP = (EditText) findViewById(R.id.recipient_zip);
        recipientDistrict = (EditText) findViewById(R.id.recipient_district);
        recipientCountry = (EditText) findViewById(R.id.recipient_country);
        // TODO ask about the flow
    }

    private void setupUI() {
        deliveryTitle.setText(getResources().getString(R.string.delivery_title));
        backButton.setOnClickListener(this);

        phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //message when edit box is focused
                    phone.setError(PHONE_NO_LENGTH_ERROR, null);
                }
            }
        });
        //generating the first recipient
        generateFirstRecipient();

        //changing the background of the button
        plusButton.setOnClickListener(this);
        paymentButton.setOnClickListener(this);

        recipientPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //message when edit box is focused
                    recipientPhone.setError(PHONE_NO_LENGTH_ERROR, null);
                }
            }
        });
        chooseRecipientLayout.setVisibility(User.getInstance().isAnon() ? View.GONE : View.VISIBLE);
        secondRecipient.setVisibility(View.GONE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            recipientFirstName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            recipientLastName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            recipientEmail.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            recipientPhone.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            recipientStreetNumber.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            recipientStreet.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            recipientCity.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            recipientZIP.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            recipientDistrict.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            recipientCountry.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
        }
    }

    /*
        when the "plus" button is clicked the view will automatically go to the bottom of the scroll view
         */
    private void focusOnView() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(0, secondRecipient.getBottom());
            }
        });
    }

    private void editNotEnabled() {
        firstName.setEnabled(false);
        lastName.setEnabled(false);
        email.setEnabled(false);
        phone.setEnabled(false);
        street.setEnabled(false);
        streetNumber.setEnabled(false);
        city.setEnabled(false);
        ZIP.setEnabled(false);
        district.setEnabled(false);
        country.setEnabled(false);

        int disabledGrey = ContextCompat.getColor(this, R.color.hint_enabled_color_grey);

        firstName.setTextColor(disabledGrey);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            firstName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.hint_enabled_color_grey)));
        }
        lastName.setTextColor(disabledGrey);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            lastName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.hint_enabled_color_grey)));
        }
        email.setTextColor(disabledGrey);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            email.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.hint_enabled_color_grey)));
        }
        phone.setTextColor(disabledGrey);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            phone.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.hint_enabled_color_grey)));
        }
        street.setTextColor(disabledGrey);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            street.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.hint_enabled_color_grey)));
        }
        streetNumber.setTextColor(disabledGrey);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            streetNumber.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.hint_enabled_color_grey)));
        }
        city.setTextColor(disabledGrey);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            city.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.hint_enabled_color_grey)));
        }
        ZIP.setTextColor(disabledGrey);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ZIP.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.hint_enabled_color_grey)));
        }
        district.setTextColor(disabledGrey);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            district.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.hint_enabled_color_grey)));
        }
        country.setTextColor(disabledGrey);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            country.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.hint_enabled_color_grey)));
        }
    }

    /*
    when the "minus" button is pressed all edit fields from the first recipient will have the following things:
     */
    private void editEnabled() {
        firstName.setEnabled(true);
        lastName.setEnabled(true);
        email.setEnabled(true);
        phone.setEnabled(true);
        street.setEnabled(true);
        streetNumber.setEnabled(true);
        city.setEnabled(true);
        ZIP.setEnabled(true);
        district.setEnabled(true);
        country.setEnabled(true);

        firstName.setTextColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            firstName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
        }
        lastName.setTextColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            lastName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
        }
        email.setTextColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            email.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
        }
        phone.setTextColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            phone.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
        }
        street.setTextColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            street.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
        }
        streetNumber.setTextColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            streetNumber.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
        }
        city.setTextColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            city.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
        }
        ZIP.setTextColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ZIP.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
        }
        district.setTextColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            district.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
        }
        country.setTextColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            country.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
        }

        recipientFirstName.setText("");
        recipientLastName.setText("");
        recipientEmail.setText("");
        recipientPhone.setText("");
        recipientStreet.setText("");
        recipientStreetNumber.setText("");
        recipientCity.setText("");
        recipientZIP.setText("");
        recipientDistrict.setText("");
        recipientCountry.setText("");

        recipientFirstName.setError(null);
        recipientLastName.setError(null);
        recipientEmail.setError(null);
        recipientPhone.setError(null);
        recipientStreet.setError(null);
        recipientStreetNumber.setError(null);
        recipientCity.setError(null);
        recipientZIP.setError(null);
        recipientDistrict.setError(null);
        recipientCountry.setError(null);
    }

    /*
    Method to generate recipient with the user saved data
     */
    private void generateFirstRecipient() {
        if (!User.getInstance().isAnon()) {
            firstName.setText(User.getInstance().getFirstName());
            lastName.setText(User.getInstance().getLastName());
            email.setText(User.getInstance().getEmail());
            phone.setText(User.getInstance().getPhone());
            street.setText(User.getInstance().getAddress().getStreet());
            streetNumber.setText(User.getInstance().getAddress().getNumber());
            city.setText(User.getInstance().getAddress().getCity());
            ZIP.setText(User.getInstance().getAddress().getPostalCode());
            district.setText(User.getInstance().getAddress().getDistrict());
            country.setText(User.getInstance().getAddress().getCountry());
        } else {
            firstName.setText("");
            lastName.setText("");
            email.setText("");
            phone.setText("");
            street.setText("");
            streetNumber.setText("");
            city.setText("");
            ZIP.setText("");
            district.setText("");
            country.setText("");
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private boolean canContinueToPayment(EditText firstName, EditText lastName, EditText email, EditText phoneNumber,
                                         EditText street, EditText streetNr, EditText city, EditText ZIP, EditText district, EditText country) {
        if (firstName.getText().toString().trim().isEmpty() && lastName.getText().toString().trim().isEmpty() &&
                email.getText().toString().trim().isEmpty() && phoneNumber.getText().toString().trim().isEmpty() &&
                street.getText().toString().trim().isEmpty() && streetNr.getText().toString().trim().isEmpty() &&
                city.getText().toString().trim().isEmpty() && ZIP.getText().toString().trim().isEmpty() &&
                district.getText().toString().trim().isEmpty() && country.getText().toString().trim().isEmpty()) {

            Utils.showSingleButtonAlertWithoutTitle(this, getResources().getString(R.string.alert_all_fields_are_required));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                firstName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                lastName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                email.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                phoneNumber.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                street.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                streetNr.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                city.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                ZIP.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                district.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                country.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            }
            return false;
        }

        if (!EditTextValidation.isFirstNameValid(firstName.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isFirstNameValid(firstName.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                firstName.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            }
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                firstName.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            }
        }

        if (!EditTextValidation.isLastNameValid(lastName.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isLastNameValid(lastName.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                lastName.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            }
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                lastName.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            }
        }

        if (!EditTextValidation.isValidEmail(email.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isValidEmail(email.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                email.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            }
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                email.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            }
        }

        if (!EditTextValidation.isPhoneNumberValid(phoneNumber.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isPhoneNumberValid(phoneNumber.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                phoneNumber.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            }
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                phoneNumber.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            }
        }

        if (!EditTextValidation.isStreetValid(street.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isStreetValid(street.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                street.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            }
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                street.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            }
        }

        if (!EditTextValidation.isStreetNumberValid(streetNr.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isStreetNumberValid(streetNr.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                streetNr.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            }
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                streetNr.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            }
        }

        if (!EditTextValidation.isCityValid(city.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isCityValid(city.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                city.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            }
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                city.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            }
        }

        if (!EditTextValidation.isZipValid(ZIP.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isZipValid(ZIP.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ZIP.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            }
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ZIP.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            }
        }

        if (!EditTextValidation.isDistrictValid(district.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isDistrictValid(district.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                district.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            }
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                district.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            }
        }

        if (!EditTextValidation.isCountryValid(country.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isCountryValid(country.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                country.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            }
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                country.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            }
        }

        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cart_toolbar_back_button:
                onBackPressed();
                break;
            case R.id.continue_to_payment:
                onContinueClicked();
                break;
            case R.id.recipient_button:
                if (secondRecipient.getVisibility() == View.GONE) {
                    secondRecipient.setVisibility(View.VISIBLE);
                    editNotEnabled();
                    focusOnView();
                } else {
                    secondRecipient.setVisibility(View.GONE);
                    editEnabled();
                }
                break;
        }
    }

    private void onContinueClicked() {
        if (User.getInstance().isAnon()) {
            if (canContinueToPayment(firstName, lastName, email, phone,
                    street, streetNumber, city, ZIP, district, country)) {
                setUserDataFromFirstRecipient();
                startActivity(new Intent(DeliveryActivity.this, FinalPaymentActivity.class));
            }
        } else {
            if (secondRecipient.getVisibility() == View.VISIBLE) {
                if (canContinueToPayment(recipientFirstName, recipientLastName, recipientEmail, recipientPhone,
                        recipientStreet, recipientStreetNumber, recipientCity, recipientZIP, recipientDistrict, recipientCountry)) {
                    setUserDataFromSecondRecipient();
                    startActivity(new Intent(DeliveryActivity.this, FinalPaymentActivity.class));
                }
            } else {
                if (canContinueToPayment(firstName, lastName, email, phone,
                        street, streetNumber, city, ZIP, district, country)) {
                    setUserDataFromFirstRecipient();
                    startActivity(new Intent(DeliveryActivity.this, FinalPaymentActivity.class));
                }
            }
        }
    }

    private void setUserDataFromSecondRecipient() {
        User.getInstance().setData(
                User.getInstance().getId(),
                recipientFirstName.getText().toString(),
                recipientLastName.getText().toString(),
                recipientEmail.getText().toString(),
                recipientPhone.getText().toString(),
                recipientStreet.getText().toString(),
                recipientStreetNumber.getText().toString(),
                recipientCity.getText().toString(),
                recipientZIP.getText().toString(),
                recipientDistrict.getText().toString(),
                recipientCountry.getText().toString(),
                User.getInstance().getPass()
        );
    }

    private void setUserDataFromFirstRecipient() {
        User.getInstance().setData(
                User.getInstance().getId(),
                firstName.getText().toString(),
                lastName.getText().toString(),
                email.getText().toString(),
                phone.getText().toString(),
                street.getText().toString(),
                streetNumber.getText().toString(),
                city.getText().toString(),
                ZIP.getText().toString(),
                district.getText().toString(),
                country.getText().toString(),
                User.getInstance().getPass()
        );
    }

}
