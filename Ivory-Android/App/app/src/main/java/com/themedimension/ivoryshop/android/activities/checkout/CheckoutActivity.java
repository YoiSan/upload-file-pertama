/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.activities.checkout;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.adapters.CheckoutProductsAdapter;
import com.themedimension.ivoryshop.android.manager.ProductListsManager;
import com.themedimension.ivoryshop.android.models.Order;
import com.themedimension.ivoryshop.android.utils.Utils;
import com.themedimension.ivoryshop.android.utils.keyboardVisibilityEvent.KeyboardVisibilityEvent;
import com.themedimension.ivoryshop.android.utils.keyboardVisibilityEvent.KeyboardVisibilityEventListener;
import com.themedimension.ivoryshop.android.utils.keyboardVisibilityEvent.Unregistrar;

import java.util.ArrayList;

import static com.themedimension.ivoryshop.android.utils.Utils.formatPrice;

public class CheckoutActivity extends AppCompatActivity implements View.OnClickListener {

    //region GLOBALS

    public static final double DISCOUNT_VALUE = 0,
            COUPON_DISCOUNT_VALUE = 10;
    private static final String TAG = "CheckoutActivity";
    public static String COUPON_1;
    public static String COUPON_2;
    public int couponCode = 1;
    private boolean isKeyboardOpen = false;
    private Unregistrar mUnregistrar;
    private ArrayList<ProductListsManager.CartItem> checkoutProductList;
    private String coupon;
    private double totalPriceToCharge;
    private TextView checkOutTitle;
    private Button backToCartButton;
    private RecyclerView checkoutRecyclerView;
    private TextView checkoutCartPrice;
    private TextView checkoutSalesDiscount;
    private EditText enterCheckoutCoupon;
    private TextView checkoutCoupon;
    private TextView checkoutPrice;
    private Button gotoDelivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initConstants();
        init();
        Order.getInstance().setDiscount2(DISCOUNT_VALUE);
        checkoutProductList = ProductListsManager.getInstance().getCartItems();
        setupUI();
    }

    private void init() {
        checkOutTitle = (TextView) findViewById(R.id.cart_toolbar_title);
        backToCartButton = (Button) findViewById(R.id.cart_toolbar_back_button);

        checkoutRecyclerView = (RecyclerView) findViewById(R.id.checkout_recycler_view);

        checkoutCartPrice = (TextView) findViewById(R.id.checkoutCartPrice);
        checkoutSalesDiscount = (TextView) findViewById(R.id.sales_discount);
        enterCheckoutCoupon = (EditText) findViewById(R.id.enter_cupon_code);
        checkoutCoupon = (TextView) findViewById(R.id.checkoutCupon);
        checkoutPrice = (TextView) findViewById(R.id.checkout_price);

        gotoDelivery = (Button) findViewById(R.id.go_to_delivery);
    }

    private void setupUI() {
        checkOutTitle.setText(getResources().getString(R.string.checkout_title));
        backToCartButton.setOnClickListener(this);

        CheckoutProductsAdapter checkoutAdaptor = new CheckoutProductsAdapter(this, checkoutProductList);
        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkoutRecyclerView.setAdapter(checkoutAdaptor);

        checkoutCartPrice.setText(getResources().getString(R.string.dollar_currency_string) + "" + formatPrice(total()));
        checkoutSalesDiscount.setText(getResources().getString(R.string.sale_dollar) + "" + formatPrice(discount()));
        checkoutCoupon.setText(getResources().getString(R.string.discount_in_currency));
        totalPriceToCharge = checkoutPrice(false);
        checkoutPrice.setText(getResources().getString(R.string.dollar_currency_string) + "" + formatPrice(checkoutPrice(false)));

        gotoDelivery.setOnClickListener(this);
        mUnregistrar = KeyboardVisibilityEvent.registerEventListener(this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                Log.e("KeyboardVisibility", String.valueOf(isOpen));
                isKeyboardOpen = isOpen;
                if (!isOpen) {
                    coupon = enterCheckoutCoupon.getText().toString();
                    couponCode = couponDiscount(coupon);
                    if (couponCode == 1 || couponCode == 2) {
                        checkoutCoupon.setText(getResources().getString(R.string.sale_dollar) + "" + COUPON_DISCOUNT_VALUE);
                        Order.getInstance().setDiscount1(COUPON_DISCOUNT_VALUE);
                        totalPriceToCharge = checkoutPrice(true);
                        checkoutPrice.setText(getResources().getString(R.string.dollar_currency_string) + "" + formatPrice(totalPriceToCharge));//total
                    } else if (!enterCheckoutCoupon.getText().toString().isEmpty())  {
                        checkoutCoupon.setText(getResources().getString(R.string.discount_in_currency));
                        totalPriceToCharge = checkoutPrice(false);
                        checkoutPrice.setText(getResources().getString(R.string.dollar_currency_string) + "" + formatPrice(totalPriceToCharge));
                        Utils.showSingleButtonAlertWithoutTitle(CheckoutActivity.this, getResources().getString(R.string.alert_invalid_coupon_code));
                    }
                }
            }
        });
    }

    private void initConstants() {
        Resources res = getResources();
        COUPON_1 = res.getString(R.string.coupon_1);
        COUPON_2 = res.getString(R.string.coupon_2);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cart_toolbar_back_button:
                onBackPressed();
                break;
            case R.id.go_to_delivery:
                if (isKeyboardOpen) {
                    coupon = enterCheckoutCoupon.getText().toString();
                    couponCode = couponDiscount(coupon);
                    if (couponCode == 1 || couponCode == 2) {
                        checkoutCoupon.setText(getResources().getString(R.string.sale_dollar) + "" + COUPON_DISCOUNT_VALUE);
                        Order.getInstance().setDiscount1(COUPON_DISCOUNT_VALUE);
                        totalPriceToCharge = checkoutPrice(true);
                        checkoutPrice.setText(getResources().getString(R.string.dollar_currency_string) + "" + formatPrice(totalPriceToCharge));//total
                        Intent deliveryScreen = new Intent(CheckoutActivity.this, DeliveryActivity.class);
                        startActivityForResult(deliveryScreen, 1);
                    } else if (!enterCheckoutCoupon.getText().toString().isEmpty()) {
                        checkoutCoupon.setText(getResources().getString(R.string.discount_in_currency));
                        totalPriceToCharge = checkoutPrice(false);
                        checkoutPrice.setText(getResources().getString(R.string.dollar_currency_string) + "" + formatPrice(totalPriceToCharge));
                        Utils.showSingleButtonAlertWithoutTitle(CheckoutActivity.this, getResources().getString(R.string.alert_invalid_coupon_code));
                    } else {
                        Intent deliveryScreen = new Intent(CheckoutActivity.this, DeliveryActivity.class);
                        startActivityForResult(deliveryScreen, 1);
                    }
                } else {
                    Intent deliveryScreen = new Intent(CheckoutActivity.this, DeliveryActivity.class);
                    startActivityForResult(deliveryScreen, 1);
                }
                break;
        }
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

    /**
     * Calculate the total price .
     *
     * @return total checkout price
     */
    public double total() {
        double total = 0;
        for (ProductListsManager.CartItem p : checkoutProductList) {
            total += p.getPrice() * p.getQuantity();
        }

        return total;
    }


    /**
     * Calculate the discount price .
     *
     * @return value of discount according to total price
     */
    public double discount() {
        double salesDiscount;
        salesDiscount = (DISCOUNT_VALUE / 100) * total();
        return salesDiscount;
    }

    /**
     * Calculate the final total price.
     *
     * @param aux boolean to test if extra discount is supported
     * @return total checkout price
     */
    public double checkoutPrice(boolean aux) {
        double checkoutPrice;
        if (aux)
            checkoutPrice = total() - discount() - COUPON_DISCOUNT_VALUE;
        else {
            checkoutPrice = total() - discount();
        }
        ProductListsManager.getInstance().setTotalPrice(checkoutPrice);
        return checkoutPrice;
    }

    /**
     * Check if the user add a correct coupon .
     *
     * @param coupon the char seq the user types in the coupon field
     * @return boolean to test if entered coupon is supported by the app
     */
    public int couponDiscount(String coupon) {
        if (coupon.contentEquals(COUPON_1)) {
            return 1;
        }
        if (coupon.contentEquals(COUPON_2)) {
            return 2;
        }
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnregistrar.unregister();
    }
}



