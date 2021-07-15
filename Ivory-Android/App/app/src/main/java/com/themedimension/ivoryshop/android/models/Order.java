/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */

package com.themedimension.ivoryshop.android.models;

import android.util.Log;

import com.google.gson.Gson;
import com.paypal.android.sdk.payments.PayPalItem;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalPaymentDetails;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.themedimension.ivoryshop.android.utils.Utils;
import com.themedimension.ivoryshop.android.manager.ProductListsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.FormBody;
import okhttp3.RequestBody;

import static com.themedimension.ivoryshop.android.utils.Utils.TRANSACTION_CURRENCY;

public class Order {

    //region GLOBALS

    private static final String TAG = "Order";
    private static final double PAYPAL_SHIPPING_COST = 0.00;
    private static final double PAYPAL_TAX_VALUE = 0.00;

    private static Order mInstance = new Order();

    private double discount1 = 0;
    private double discount2 = 0;
    private JSONArray mItems = new JSONArray();
    private double mTotal = -1;
    private PaymentMethodFlag mPaymentMethod;

    //endregion

    public enum PaymentMethodFlag {
        Stripe, Custom, PayPal, Default

    }

    private Order() {
        mItems = new JSONArray();
        mTotal = -1;
    }

    public static Order getInstance() {
        if (mInstance == null) {
            mInstance = new Order();
        }

        return mInstance;
    }

    public void setDiscount1(double discountValue) {
        this.discount1 = discountValue;
    }

    public void setDiscount2(double discountValue) {
        this.discount2 = discountValue;
    }

    public double getTotal() {
        return mTotal;
    }

    public JSONObject createJsonInstance(ArrayList<ProductListsManager.CartItem> items) {
        int userId = User.getInstance().getId();
        String addressStreet = User.getInstance().getAddress().getStreet();
        String addressNumber = User.getInstance().getAddress().getNumber();
        String addressCode = User.getInstance().getAddress().getPostalCode();
        String addressDistrict = User.getInstance().getAddress().getDistrict();
        String addressCity = User.getInstance().getAddress().getCity();
        String addressCountry = User.getInstance().getAddress().getCountry();

        mTotal = 0;
        JSONObject order = new JSONObject();

        JSONArray jsonItems = new JSONArray();
        for (ProductListsManager.CartItem item : items) {
            JSONObject newJson = new JSONObject();
            try {
                newJson.put("id", item.getId());
                newJson.put("size_id", item.getSize().getSizeId());
                newJson.put("quantity", item.getQuantity());
                newJson.put("price", item.getPrice());
            } catch (JSONException e) {
                Log.e(TAG, "Error creating JSON: " + e.getLocalizedMessage());
            }
            mTotal += item.getPrice() * item.getQuantity();
            jsonItems.put(newJson);
        }

        try {
            JSONObject discount = new JSONObject();
            discount.put("id", "disc_1");
            discount.put("size_id", null);
            discount.put("quantity", 1);
            discount.put("price", discount1);

            mTotal -= discount1 * mTotal / 100;
            jsonItems.put(discount);

            discount.put("id", "disc_2");
            discount.put("size_id", null);
            discount.put("quantity", 1);
            discount.put("price", discount2);

            mTotal -= discount2;
            jsonItems.put(discount);

            order.put("items", jsonItems);

            order.put("user_id", userId);
            JSONObject jsonAddress = new JSONObject();
            jsonAddress.put("street", addressStreet);
            jsonAddress.put("number", addressNumber);
            jsonAddress.put("postal_code", addressCode);
            jsonAddress.put("district", addressDistrict);
            jsonAddress.put("city", addressCity);
            jsonAddress.put("country", addressCountry);

            order.put("address", jsonAddress);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON: " + e.getLocalizedMessage());
        }

        return order;
    }

    //region PAYPAL PAYMENT

    public synchronized PayPalPayment createPaymentUsingPayPal() {
        DecimalFormat df = new DecimalFormat("0.00");

        PayPalItem[] items = new PayPalItem[ProductListsManager.getInstance().getCartItems().size() + 2];
        BigDecimal subtotal = new BigDecimal(df.format(0));
        int i = 0;
        for (ProductListsManager.CartItem p : ProductListsManager.getInstance().getCartItems()) {
            items[i] = createNewPayPalItem(p);
            BigDecimal value = new BigDecimal(df.format(items[i].getPrice().doubleValue() * items[i].getQuantity()));
            subtotal = subtotal.add(value);
            i++;
        }

        BigDecimal discount = new BigDecimal("-" + df.format(discount1 * subtotal.doubleValue() / 100));
        PayPalItem discountItem = new PayPalItem(
                "Discount",
                1,
                discount,
                TRANSACTION_CURRENCY,
                "sku-discount"
        );

        BigDecimal coupon = new BigDecimal("-" + df.format(discount2));
        PayPalItem couponItem = new PayPalItem(
                "Coupon",
                1,
                coupon,
                TRANSACTION_CURRENCY,
                "sku-coupon"
        );

        items[i] = discountItem;
        subtotal = subtotal.add(items[i].getPrice());
        items[i + 1] = couponItem;
        subtotal = subtotal.add(items[i + 1].getPrice());

        Log.e("Items", "createPaymentUsingPayPal: " + Arrays.toString(items));

        BigDecimal shippingCost = new BigDecimal(df.format(PAYPAL_SHIPPING_COST));
        BigDecimal tax = new BigDecimal(df.format(subtotal.doubleValue() * PAYPAL_TAX_VALUE));
        PayPalPaymentDetails paymentDetails = new PayPalPaymentDetails(shippingCost, subtotal, tax);

        BigDecimal totalAmount = subtotal.add(shippingCost).add(tax);
        PayPalPayment payment = new PayPalPayment(totalAmount, TRANSACTION_CURRENCY, "Total", PayPalPayment.PAYMENT_INTENT_ORDER);

        payment.items(items).paymentDetails(paymentDetails);

        return payment;
    }

    private PayPalItem createNewPayPalItem(ProductListsManager.CartItem p) {
        String itemName = p.getName();
        int itemQuantity = p.getQuantity();
        double itemPrice = p.getPrice();

        String itemCurrency = TRANSACTION_CURRENCY;
        String itemSku = "sku-" + String.valueOf(p.getId());

        BigDecimal priceInBD = new BigDecimal((new DecimalFormat("0.00")).format(itemPrice));

        return new PayPalItem(
                itemName,
                itemQuantity,
                priceInBD,
                itemCurrency,
                itemSku
        );
    }

    public RequestBody sendPayPalPaymentToServer(PaymentConfirmation confirmation) {
        return new FormBody.Builder()
                .add("method", String.valueOf(Utils.PAYMENT_PAYPAL))
                .add("amount", confirmation.getPayment().getAmountAsLocalizedString())
                .add("confirmation", new Gson().toJson(confirmation))
                .add("payment", new Gson().toJson(confirmation.getPayment()))
                .build();
    }

    //endregion

}
