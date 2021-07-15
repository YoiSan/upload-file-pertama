/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.activities.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.adapters.CartAdapter;
import com.themedimension.ivoryshop.android.manager.ProductListsManager;
import com.themedimension.ivoryshop.android.utils.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.themedimension.ivoryshop.android.utils.Utils.formatPrice;

public class CartActivity extends AppCompatActivity implements View.OnClickListener {

    //region GLOBALS
    private static final String TAG = "CartActivity";
    public static ArrayList<ProductListsManager.CartItem> cartProductList = new ArrayList<ProductListsManager.CartItem>();
    private TextView cartTotalPrice;
    private Button backButton;
    private ListView cartSimpleListProduct;
    private Button cartCheckout;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        init();
        setupUI();
    }

    private void init() {
        backButton = (Button) findViewById(R.id.cart_toolbar_back_button);
        cartSimpleListProduct = (ListView) findViewById(R.id.cart_ListView);
        cartTotalPrice = (TextView) findViewById(R.id.cart_total_price);
        cartCheckout = (Button) findViewById(R.id.checkout);
    }

    private void setupUI() {
        backButton.setOnClickListener(this);

        cartProductList = ProductListsManager.getInstance().getCartItems();
        CartAdapter cartAdapter = new CartAdapter(this, R.layout.cart_item, cartProductList);
        cartSimpleListProduct.setAdapter(cartAdapter);

        cartTotalPrice.setText(getResources().getString(R.string.dollar_currency_string) + "" + formatPrice(total()));
        cartCheckout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cart_toolbar_back_button:
                onBackPressed();
                break;
            case R.id.checkout:
                if (cartProductList.size() == 0) {
                    Utils.showSingleButtonAlertWithoutTitle(CartActivity.this, getResources().getString(R.string.alert_cart_is_empty));
                } else {
                    Intent checkoutScreen = new Intent(CartActivity.this, CheckoutActivity.class);
                    startActivityForResult(checkoutScreen, 1);
                }
                break;
        }
    }

    /**
     * Calculate the total price and change it in decimal format .
     *
     * @return the string value of the total price in decimal format
     */
    public double total() {
        cartProductList = ProductListsManager.getInstance().getCartItems();
        double total = 0;
        for (int i = 0; i < cartProductList.size(); i++) {
            total = cartProductList.get(i).getPrice() * cartProductList.get(i).getQuantity() + total;
        }
        return total;
    }

    /***
     * If a product is remove from cart this function will update the total price .
     */
    public void updateTotalPrice() {
        cartTotalPrice.setText(getResources().getString(R.string.dollar_currency_string) + "" + formatPrice(total()));
    }

    /**
     * Method for handling the onClick/pressing of phone's BACK button.
     * When the button is pressed close the drawer first and if the drawer
     * is closed close the activity.
     */
    @Override
    public void onBackPressed() {
        finish();
    }

}