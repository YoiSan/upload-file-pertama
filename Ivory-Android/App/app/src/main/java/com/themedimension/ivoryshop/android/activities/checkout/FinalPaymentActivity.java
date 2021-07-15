/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.activities.checkout;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalProfileSharingActivity;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.stripe.Stripe;
import com.stripe.android.model.Card;
import com.stripe.android.view.CardInputWidget;
import com.themedimension.ivoryshop.android.AsyncCardCharge.AsyncCharge;
import com.themedimension.ivoryshop.android.AsyncCardCharge.ServerCommunication.OnServerCallback;
import com.themedimension.ivoryshop.android.AsyncCardCharge.ServerCommunication.ServerRequest;
import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.activities.BaseActivity;
import com.themedimension.ivoryshop.android.activities.fragmentActivities.DrawerActivity;
import com.themedimension.ivoryshop.android.manager.NetworkManager;
import com.themedimension.ivoryshop.android.manager.OnNetworkCallback;
import com.themedimension.ivoryshop.android.manager.PaymentListener;
import com.themedimension.ivoryshop.android.manager.PaymentManager;
import com.themedimension.ivoryshop.android.manager.ProductListsManager;
import com.themedimension.ivoryshop.android.manager.ResponseExtractor;
import com.themedimension.ivoryshop.android.models.CustomCard;
import com.themedimension.ivoryshop.android.models.Order;
import com.themedimension.ivoryshop.android.models.User;
import com.themedimension.ivoryshop.android.response.ResponseData;
import com.themedimension.ivoryshop.android.utils.EditTextValidation;
import com.themedimension.ivoryshop.android.utils.Utils;

import org.json.JSONException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.StringTokenizer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FinalPaymentActivity extends BaseActivity implements View.OnClickListener, OnServerCallback, ServerRequest, PaymentListener, CompoundButton.OnCheckedChangeListener, OnNetworkCallback {

    //region GLOBALS

    private static final String TAG = "FinalPaymentActivity";

    private static final int RESULT_SUCCEEDED = 1;

    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;
    private static final int REQUEST_CODE_PROFILE_SHARING = 3;
    //  Change this in order to use a custom card payment method.
    //  Supported methods:
    //      - Stripe
    //      - PayPal
    //  In order to activate one of the methods, just change its value to <value>true</value>.
    private static final boolean STRIPE_FLAG = false;
    private static final boolean PAYPAL_FLAG = true;
    private static String CARD_HOLDER_RULE_MSG;
    private NetworkManager manager;
    private double totalPriceToCharge;

    private TextView activityTitle;
    private Button backButton;
    //  Card Input Fields
    private LinearLayout cardDetails;
    private EditText cardNumber;
    private EditText CVS;
    private EditText cardHolderName;
    private EditText expDate;
    private CheckBox checkBox;
    private ImageButton dateButton;
    private View cardLayoutBlockingView;

    // Stripe card widget
    private CardInputWidget stripeCardInputWidget;

    private View payWithPaypalBlockingView;
    private Button payWithPayPal;
    private Button placeOrderButton;

    private PayPalConfiguration config = new PayPalConfiguration()
            .environment(Utils.CONFIG_ENVIRONMENT)
            .clientId(Utils.CONFIG_CLIENT_ID);

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        totalPriceToCharge = getIntent().getDoubleExtra("amount", 0);

        init();
        setupUI();

        PaymentManager.getInstance().setListener(FinalPaymentActivity.this);

        Intent newIntent = new Intent(this, PayPalService.class);
        newIntent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(newIntent);

        manager = new NetworkManager(this);
    }

    private void init() {
        activityTitle = (TextView) findViewById(R.id.cart_toolbar_title);
        backButton = (Button) findViewById(R.id.cart_toolbar_back_button);

        cardDetails = (LinearLayout) findViewById(R.id.card_details);
        cardNumber = (EditText) findViewById(R.id.payment_card_number);
        CVS = (EditText) findViewById(R.id.payment_csv);
        expDate = (EditText) findViewById(R.id.date);
        dateButton = (ImageButton) findViewById(R.id.payment_calendar_icon);
        cardHolderName = (EditText) findViewById(R.id.payment_card_holder_name);
        cardLayoutBlockingView = findViewById(R.id.card_layout_blocking_view);

        checkBox = (CheckBox) findViewById(R.id.payment_checkBox);

        stripeCardInputWidget = (CardInputWidget) findViewById(R.id.cardInputWidget);

        payWithPaypalBlockingView = findViewById(R.id.pay_with_paypal_blocking_view);
        payWithPayPal = (Button) findViewById(R.id.pay_with_payPal);
        placeOrderButton = (Button) findViewById(R.id.place_order);

    }

    private void setupUI() {
        CARD_HOLDER_RULE_MSG = getResources().getString(R.string.card_holder_completion_hint);

        activityTitle.setText(getResources().getString(R.string.payment_title));
        backButton.setOnClickListener(this);

        cardDetails.setVisibility(STRIPE_FLAG ? View.GONE : View.VISIBLE);
        if (!User.getInstance().isAnon()) {
            cardNumber.setText(User.getInstance().getCardNumber());
            CVS.setText(User.getInstance().getCVS());
            expDate.setText(User.getInstance().getExpireDate());
            cardHolderName.setText(User.getInstance().getCardHolderName());
        }

        dateButton.setOnClickListener(this);
        expDate.setEnabled(false);
        cardHolderName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    cardHolderName.setError(CARD_HOLDER_RULE_MSG, null);
                }
            }
        });

        checkBox.setOnCheckedChangeListener(this);

        /* Setting up the views by the flags state*/
        stripeCardInputWidget.setVisibility(STRIPE_FLAG ? View.VISIBLE : View.GONE);


        if (PAYPAL_FLAG) {
            payWithPayPal.setVisibility(View.VISIBLE);
            payWithPayPal.setOnClickListener(this);
        } else {
            payWithPayPal.setVisibility(View.GONE);
            payWithPayPal.setOnClickListener(null);
        }
        payWithPayPal.setOnClickListener(this);
        placeOrderButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.place_order:
                if (checkBox.isChecked()) {
                    RequestBody requestBody = new FormBody.Builder()
                            .add("method", String.valueOf(Utils.PAYMENT_NO_PAYMENT))
                            .add("order", Order.getInstance().createJsonInstance(ProductListsManager.getInstance().getCartItems()).toString())
                            .add("amount", String.valueOf(Order.getInstance().getTotal()))
                            .add("currency", Utils.TRANSACTION_CURRENCY)
                            .build();
                    PaymentManager.getInstance().makePayment(requestBody, Order.PaymentMethodFlag.Default);
                    return;
                }

                if (STRIPE_FLAG) {
                    createNewOrder();
                    return;
                }

                if (fieldsAreValid()) {
                    createNewOrder();
                }
                break;
            case R.id.payment_calendar_icon:
                showCalendar(FinalPaymentActivity.this);
                break;
            case R.id.pay_with_payPal:
                onPayPalPressed();
                break;
            case R.id.cart_toolbar_back_button:
                onBackPressed();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PaymentManager.getInstance().setListener(FinalPaymentActivity.this);
    }

    public void onPayPalPressed(){
         /*
         * PAYMENT_INTENT_SALE will cause the payment to complete immediately.
         * Change PAYMENT_INTENT_SALE to
         *   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
         *   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
         *     later via calls from your server.
         *
         * Also, to include additional payment details and an item list, see getStuffToBuy() below.
         */
        PayPalPayment thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE);

        /*
         * See getStuffToBuy(..) for examples of some available payment options.
         */

        Intent intent = new Intent(FinalPaymentActivity.this, PaymentActivity.class);

        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }

    private PayPalPayment getThingToBuy(String paymentIntent) {
        return new PayPalPayment(new BigDecimal(ProductListsManager.getInstance().getTotalPrice()), "USD", "Total",
                paymentIntent);
    }


    @Override
    public void onDateSet(int year, int month, int dayOfMonth) {
        expDate.setText(cardExpiryDate(year, month));
    }

    /**
     * Create new Order and try to use it in a transaction between the
     * user's bank account and the app owner's bank account.
     * <p>
     * For further details about the Stripe integration, please visit
     *
     * @see Stripe or <a href="https://stripe.com/docs">Stripe Documentation</a>
     */
    private void createNewOrder() {
        final ArrayList<ProductListsManager.CartItem> orderedProducts = ProductListsManager.getInstance().getCartItems();

        if (STRIPE_FLAG && !checkBox.isChecked()) {
            Stripe.apiKey = Utils.STRIPE_SECRET_KEY;

            //  Create Card object.
            Card card = null;
            card = stripeCardInputWidget.getCard();

            //  Create stripe source and send it to server to finish payment asynchronous.
            if (card != null) {
                if (card.validateCard()) {
//                    PaymentManager.getInstance().setListener(this);
                    AsyncCharge chargeAsynchronous = new AsyncCharge(this, card, this);
                    chargeAsynchronous.execute();
                } else {
                    Log.e("Payment registration", "Validation error: " + "\n" +
                            card.validateCard() + "\n" +
                            card.validateCVC() + "\n" +
                            card.validateExpiryDate());
                    Utils.showSingleButtonAlertWithoutTitle(this, getResources().getString(R.string.alert_error_validating_card));
                }
            } else {
                Utils.showSingleButtonAlertWithoutTitle(this, getResources().getString(R.string.alert_please_enter_valid_card));
            }
        } else {
            String cardNo = cardNumber.getText().toString();
            String cardCSV = CVS.getText().toString();
            String cardDate = expDate.getText().toString();

            String reformattedCardNo = "";
            StringTokenizer tokenizer = new StringTokenizer(cardNo);
            while (tokenizer.hasMoreTokens()) {
                reformattedCardNo += tokenizer.nextToken() + " ";
            }

            int tokenIndex = cardDate.indexOf('/');
            int cardMonth = Integer.parseInt(cardDate.substring(0, tokenIndex));
            int cardYear = Integer.parseInt(cardDate.substring(tokenIndex + 1));

            CustomCard card = new CustomCard(
                    reformattedCardNo,
                    cardCSV,
                    cardMonth,
                    cardYear,
                    User.getInstance().getFirstName() + " " + User.getInstance().getLastName(),
                    User.getInstance().getAddress()
            );

            RequestBody requestBody = new FormBody.Builder()
                    .add("method", String.valueOf(Utils.PAYMENT_CUSTOM))
                    .add("order", Order.getInstance().createJsonInstance(orderedProducts).toString())
                    .add("amount", String.valueOf(Order.getInstance().getTotal()))
                    .add("currency", Utils.TRANSACTION_CURRENCY)
                    .add("source", new Gson().toJson(card))
                    .build();

//            PaymentManager.getInstance().setListener(this);
            PaymentManager.getInstance().makePayment(requestBody, Order.PaymentMethodFlag.Custom);
        }
    }

    private boolean fieldsAreValid() {

        if (cardNumber.getText().toString().isEmpty() && CVS.getText().toString().isEmpty() && expDate.getHint().toString().equals(getResources().getString(R.string.mm_yy)) && cardHolderName.getText().toString().isEmpty()) {
            Utils.showSingleButtonAlertWithoutTitle(this, getResources().getString(R.string.alert_all_fields_are_required));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cardNumber.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                CVS.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                expDate.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
                cardHolderName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            }
            return false;
        }

        if (!EditTextValidation.isCardNumberValid(cardNumber.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isCardNumberValid(cardNumber.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cardNumber.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            }
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cardNumber.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            }
        }

        if (!EditTextValidation.isCVSValid(CVS.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isCVSValid(CVS.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CVS.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            }
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CVS.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            }
        }

        if (expDate.getText().toString().trim().equals(getResources().getString(R.string.mm_yy))) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), getResources().getString(R.string.alert_expire_date));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                expDate.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            }
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                expDate.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            }
        }

        if (!EditTextValidation.isCardHolderNameValid(cardHolderName.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isCardHolderNameValid(cardHolderName.getText().toString().trim()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cardHolderName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            }
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cardHolderName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            }
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /*
    * Send payment request to server
    */
    @Override
    public void sendServerRequest(RequestBody content) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(Utils.BASE_URL)
                .post(content)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                Log.e("OnServerCallback", "onFailure: " + e.getLocalizedMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FinalPaymentActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //  Process the response data if needed.
                String responseData = response.body().string();

                onServerCallback(RESULT_OK, totalPriceToCharge);
            }
        });
    }

    /**
     * Method called once the server has sent a response back to the app as a result
     * of a transaction processing.
     *
     * @param resultCode code to check if transaction has been made successfully
     * @param value      the amount of money that have been sent from the user's account to the app owner
     */
    @Override
    public void onServerCallback(int resultCode, final double value) {
        if (resultCode == RESULT_OK) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(FinalPaymentActivity.this, String.format("Payment registered successfully: %s", value), Toast.LENGTH_LONG).show();
                }
            });

            ProductListsManager.getInstance().resetCart();

            CartActivity.cartProductList = ProductListsManager.getInstance().getCartItems();
            Intent result = new Intent();
            setResult(RESULT_SUCCEEDED, result);
            finish();
        } else {
            Log.e("Payment error", "onServerCallback: " + resultCode);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(FinalPaymentActivity.this, "Unable to communicate with server.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    /**
     * This method will be called once the PayPalActivity has finished (the user completed
     * or cancelled the payment).
     *
     * @param requestCode the code with which the PayPalActivity has been called
     * @param resultCode  code representing the user's action of cancelling / finishing a payment
     * @param data        bundle of information that will contain (if the payment has not been cancelled)
     *                    a payment confirmation that must be sent to the server for further processing
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm =
                        data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4));
                        Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));
                        /**
                         *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
                         * or consent completion.
                         * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                         * for more details.
                         *
                         * For sample mobile backend interactions, see
                         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
                         */
                        PaymentManager.getInstance().onConfirmationReceived(confirm);
                        Toast.makeText(FinalPaymentActivity.this,"PaymentConfirmation info received from PayPal",Toast.LENGTH_LONG).show();


                    } catch (JSONException e) {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        TAG,
                        "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        } else if (requestCode == REQUEST_CODE_FUTURE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PayPalAuthorization auth =
                        data.getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
                if (auth != null) {
                    try {
                        Log.i("FuturePaymentExample", auth.toJSONObject().toString(4));

                        String authorization_code = auth.getAuthorizationCode();
                        Log.i("FuturePaymentExample", authorization_code);

                        sendAuthorizationToServer(auth);
                        Toast.makeText(FinalPaymentActivity.this,"Future Payment code received from PayPal",Toast.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        Log.e("FuturePaymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("FuturePaymentExample", "The user canceled.");
            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        "FuturePaymentExample",
                        "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
            }
        } else if (requestCode == REQUEST_CODE_PROFILE_SHARING) {
            if (resultCode == Activity.RESULT_OK) {
                PayPalAuthorization auth =
                        data.getParcelableExtra(PayPalProfileSharingActivity.EXTRA_RESULT_AUTHORIZATION);
                if (auth != null) {
                    try {
                        Log.i("ProfileSharingExample", auth.toJSONObject().toString(4));

                        String authorization_code = auth.getAuthorizationCode();
                        Log.i("ProfileSharingExample", authorization_code);

                        sendAuthorizationToServer(auth);
                        Toast.makeText(FinalPaymentActivity.this,"Profile Sharing code received from PayPal",Toast.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        Log.e("ProfileSharingExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("ProfileSharingExample", "The user canceled.");
            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        "ProfileSharingExample",
                        "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
            }
        }
    }

    private void sendAuthorizationToServer(PayPalAuthorization authorization) {

        /**
         * TODO: Send the authorization response to your server, where it can
         * exchange the authorization code for OAuth access and refresh tokens.
         *
         * Your server must then store these tokens, so that your server code
         * can execute payments for this user in the future.
         *
         * A more complete example that includes the required app-server to
         * PayPal-server integration is available from
         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
         */

    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        cardLayoutBlockingView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        payWithPaypalBlockingView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        payWithPayPal.setEnabled(!isChecked);
        if (isChecked) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cardNumber.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
                CVS.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
                expDate.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
                cardHolderName.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            }
            disableEditTexts();
            if (STRIPE_FLAG) {
                stripeCardInputWidget.clearFocus();
                cardLayoutBlockingView.setEnabled(false);
                stripeCardInputWidget.setCardNumber("");
                stripeCardInputWidget.setCvcCode("");
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(stripeCardInputWidget.getWindowToken(), 0);
            }
        } else {
            enableEditTexts();
        }

    }

    private void disableEditTexts() {
        cardNumber.setEnabled(false);
        CVS.setEnabled(false);
        cardHolderName.setEnabled(false);
        dateButton.setEnabled(false);

//        cardNumber.setText("");
//        CVS.setText("");
//        cardHolderName.setText("");
//        expDate.setText("");

        cardNumber.setError(null);
        CVS.setError(null);
        cardHolderName.setError(null);
        expDate.setError(null);
    }

    private void enableEditTexts() {
        cardNumber.setEnabled(true);
        CVS.setEnabled(true);
        cardHolderName.setEnabled(true);
        dateButton.setEnabled(true);
    }


    //region PAYMENT CALLBACKS
    @Override
    public void onSuccess(Object data) {
        ProductListsManager.getInstance().resetCart();
        manager.setListener(FinalPaymentActivity.this);
        manager.updateCart(FinalPaymentActivity.this);
    }

    @Override
    public void onFailed(Object data) {
        ResponseData<Error> responseData = (ResponseData<Error>) data;
        final Error e = responseData.getResponse();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FinalPaymentActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    //region NETWORK CALLBACKS
    @Override
    public void onSuccess(Object data, ResponseExtractor.ResponseType type) {
        Log.e("Network callback", "Success");
        AlertDialog dialog = Utils.getSingleButtonAlertWithoutTitle(FinalPaymentActivity.this, getResources().getString(R.string.order_placed_successfully), getResources().getString(R.string.button_ok));
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FinalPaymentActivity.this, DrawerActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onFailed(Object data, ResponseExtractor.ResponseType type) {
        Log.e("Network callback", "Failed");
        Utils.showSingleButtonAlertWithoutTitle(FinalPaymentActivity.this, "Failed to update cart.");
    }
}
