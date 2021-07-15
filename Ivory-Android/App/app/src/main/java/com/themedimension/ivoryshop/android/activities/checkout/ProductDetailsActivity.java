/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.activities.checkout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.activities.fragmentActivities.FavoritesActivity;
import com.themedimension.ivoryshop.android.manager.NetworkManager;
import com.themedimension.ivoryshop.android.manager.OnNetworkCallback;
import com.themedimension.ivoryshop.android.manager.ProductListsManager;
import com.themedimension.ivoryshop.android.manager.ResponseExtractor;
import com.themedimension.ivoryshop.android.models.Product;
import com.themedimension.ivoryshop.android.models.ProductSize;
import com.themedimension.ivoryshop.android.models.User;
import com.themedimension.ivoryshop.android.response.ResponseData;
import com.themedimension.ivoryshop.android.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ProductDetailsActivity extends AppCompatActivity implements View.OnClickListener, OnNetworkCallback {

    //region GLOBALS

    private static final String TAG = "ProductDetailsActivity";
    /* Product Views */
    private ImageView itemImageView;
    private TextView itemDetailsTextView;
    private TextView itemPriceTextView;
    private TextView itemNumberPicker;
    private FloatingActionButton decreaseQuantity;
    private FloatingActionButton increaseQuantity;
    /* LinearLayouts to display product size buttons and color buttons  */
    private LinearLayout itemSizesLinearLayout;
    private LinearLayout itemColorsLinearLayout;
    /*  Toolbar Views */
    private ImageView addToFavoritesButton;
    private TextView cartProductsQuantity;
    private RelativeLayout productsCartShape;
    //  Data source <object> product </object>

    private Product product;
    //  Selected size and selected quantity
    private ProductSize selectedProductSize;
    private int selectedProductQuantity;
    private NetworkManager manager;
    private int maxQuantityToChoose;
    //  List with added specificProducts to cart
    private ArrayList<Product> specificProducts;
    private String previous;

    //  OnClick on toolbar
    private View.OnClickListener toolbarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.toolbar_back_button:
                    onBackPressed();
                    break;
                case R.id.favouritesProductDetailsButton:
                    if (User.getInstance().isAnon()) {
                        Utils.showSingleButtonAlert(ProductDetailsActivity.this, getResources().getString(R.string.user_not_logged_in), getResources().getString(R.string.connect_to_see_favorites));
                    } else {
                        if (ProductListsManager.getInstance().wishlistContains(product.getProductId())) {
                            ProductListsManager.getInstance().removeWishlistItemById(product.getProductId());
                            addToFavoritesButton.setBackgroundResource(R.drawable.ic_favorite_grey);
                        } else {
                            ProductListsManager.getInstance().addItemWishlist(product.getProductId());
                            addToFavoritesButton.setBackgroundResource(R.drawable.ic_favorite_red);
                        }
//                        manager.updateCart(ProductDetailsActivity.this);
                        manager.updateWishlist(ProductDetailsActivity.this);
                    }

                    break;
                case R.id.cart_layout:
                    startActivity(new Intent(ProductDetailsActivity.this, CartActivity.class));
                    break;
            }
        }
    };

    private ArrayList<Button> sizeButtonsList;
    private ArrayList<Button> colorButtonsList;

    //  Reference list
    private ArrayList<String> sizesList = new ArrayList<String>() {{
        add("XS");
        add("S");
        add("M");
        add("L");
        add("XL");
        add("XXL");
    }};

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        int productId = getIntent().getIntExtra("item", -1);
        previous = getIntent().getStringExtra("previous") == null ? "" : getIntent().getStringExtra("previous");
        product = ProductListsManager.getInstance().getProductById(productId);
        manager = new NetworkManager(this);
        manager.setListener(ProductDetailsActivity.this);
        initToolbar();
        initUI();

        createActivity(product);
    }

    //  Initialisation of toolbar buttons
    private void initToolbar() {
        Button backButton = (Button) findViewById(R.id.toolbar_back_button);
        addToFavoritesButton = (ImageView) findViewById(R.id.favouritesProductDetailsButton);
        FrameLayout goToCartButton = (FrameLayout) findViewById(R.id.cart_layout);

        backButton.setOnClickListener(toolbarListener);
        addToFavoritesButton.setOnClickListener(toolbarListener);
        goToCartButton.setOnClickListener(toolbarListener);

        productsCartShape = (RelativeLayout) findViewById(R.id.products_quantity_shape);
        productsCartShape.setVisibility(ProductListsManager.getInstance().getCartItems().size() != 0 ? View.VISIBLE : View.GONE);
        cartProductsQuantity = (TextView) findViewById(R.id.products_inside_cart_textview);
        cartProductsQuantity.setText(String.valueOf(ProductListsManager.getInstance().getCartItems().size()));
    }

    //  Method to declare all views
    private void initUI() {
        itemImageView = (ImageView) findViewById(R.id.item_image);
        itemColorsLinearLayout = (LinearLayout) findViewById(R.id.itemColorsLinearLayoutID);
        itemDetailsTextView = (TextView) findViewById(R.id.itemDetailsTextViewID);
        itemNumberPicker = (TextView) findViewById(R.id.itemNumberPickerID);

        itemSizesLinearLayout = (LinearLayout) findViewById(R.id.item_sizes_layout);
        itemPriceTextView = (TextView) findViewById(R.id.item_price);

        Button addToCartButton = (Button) findViewById(R.id.add_to_cart);
        addToCartButton.setOnClickListener(this);


        increaseQuantity = (FloatingActionButton) findViewById(R.id.increaseNumberPickerID);
        increaseQuantity.setOnClickListener(this);
        decreaseQuantity = (FloatingActionButton) findViewById(R.id.decrease_item_quantity);
        decreaseQuantity.setOnClickListener(this);
        manageQuantityButtons();
    }

    //  Method to load a product as data source for the screen
    public void createActivity(final Product product) {
        this.product = product;
        //  Display item image
        if (NetworkManager.DEMO_DATA) {
            try {
                InputStream is = this.getAssets().open(product.getProductImage());
                Drawable d = Drawable.createFromStream(is, null);
                itemImageView.setImageDrawable(d);
                itemImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Picasso.with(getApplicationContext())
                    .load( product.getProductImage())
//                    .centerInside()
//                    .resize(380, 320)
                    .into(itemImageView);
        }

        itemDetailsTextView.setText(product.getProductName());
        itemPriceTextView.setText(getResources().getString(R.string.dollar_currency_string) + "" + product.getProductPrice());
        addItemsToSpecificProducts(product);
        createColorButtonsList(specificProducts, product.getProductColor());
        createSizeButtonsList(product.getSizes(), product);
        resetFavoriteButton(product);
    }

    //  Method to reset favorite button
    private void resetFavoriteButton(Product product) {
        if (ProductListsManager.getInstance().wishlistContains(product.getProductId())) {
            addToFavoritesButton.setBackgroundResource(R.drawable.ic_favorite_red);
        } else {
            addToFavoritesButton.setBackgroundResource(R.drawable.ic_favorite_grey);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_to_cart:
                if (selectedProductSize == null) {
                    Utils.showSingleButtonAlertWithoutTitle(ProductDetailsActivity.this, getResources().getString(R.string.select_size_msg));
                    return;
                } else {
                    if (selectedProductQuantity == 0) {
                        Utils.showSingleButtonAlertWithoutTitle(ProductDetailsActivity.this, "Please add an item first!");
                        return;
                    }
                    ProductListsManager.getInstance().addItemCart(product, selectedProductSize, selectedProductQuantity);
                    if (productsCartShape.getVisibility() == View.GONE)
                        productsCartShape.setVisibility(View.VISIBLE);
                    cartProductsQuantity.setText(String.valueOf(ProductListsManager.getInstance().getCartItems().size()));
                    Utils.showSingleButtonAlertWithoutTitle(ProductDetailsActivity.this, "Your item has been added to cart.");
                    selectedProductSize.setSizeQuantity(selectedProductSize.getSizeQuantity() - selectedProductQuantity);
                    selectedProductQuantity = 0;
                    selectedProductSize = null;
                    manageQuantityButtons();
                    itemNumberPicker.setText(String.valueOf(selectedProductQuantity));
                    for (Button button : sizeButtonsList) {
                        button.setSelected(false);
                        button.setBackgroundResource(R.drawable.roundedbutton);
                        button.setTextColor(Color.BLACK);
                    }
                    manager.updateCart(ProductDetailsActivity.this);
                }
                break;

            case R.id.increaseNumberPickerID:
                if (selectedProductSize == null) {
                    Utils.showSingleButtonAlertWithoutTitle(ProductDetailsActivity.this, getResources().getString(R.string.select_size_msg));
                    return;
                } else {
                    if (selectedProductSize.getSizeQuantity() == 0) {
                        Utils.showSingleButtonAlertWithoutTitle(ProductDetailsActivity.this, getResources().getString(R.string.stock_consumed_error_msg));
                        return;
                    }


                    if (selectedProductQuantity == selectedProductSize.getSizeQuantity()) {
                        Utils.showSingleButtonAlertWithoutTitle(ProductDetailsActivity.this, getResources().getString(R.string.max_quantity_exceeded_error_msg));
                        return;
                    }
                    selectedProductQuantity = selectedProductQuantity + 1;
                    itemNumberPicker.setText(String.valueOf(selectedProductQuantity));
                }
                break;

            case R.id.decrease_item_quantity:
                if (selectedProductQuantity > 0) {
                    selectedProductQuantity = selectedProductQuantity - 1;
                    itemNumberPicker.setText(String.valueOf(selectedProductQuantity));
                }
                break;
        }
    }


    private void selectSize(int idParam, ArrayList<ProductSize> sizes) {
        for (Button b : sizeButtonsList) {
            if (idParam == b.getId()) {
                b.setBackgroundResource(R.drawable.roundedbuttonclicked);
                b.setTextColor(Color.WHITE);

                for (ProductSize size : sizes) {
                    if (b.getText().toString().contentEquals(size.getSizeName())) {
                        selectedProductSize = product.getSizeByName(b.getText().toString());
                        if (size.getSizeQuantity() < 5) {
                            maxQuantityToChoose = size.getSizeQuantity();
                        } else {
                            maxQuantityToChoose = 5;
                        }
                    }
                }
            } else {
                b.setBackgroundResource(R.drawable.roundedbutton);
                b.setTextColor(Color.BLACK);
            }
        }
    }

    //  Method to create size buttons list
    private void createSizeButtonsList(final ArrayList<ProductSize> sizes, final Product product) {
        final int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());

        sizeButtonsList = new ArrayList<>();
        ArrayList<String> forSort = new ArrayList<>();

        int id = 0;
        for (final ProductSize s : sizes) {

            final Button sizeButton = new Button(this);

            sizeButton.setId(id);
            sizeButton.setLayoutParams(new LinearLayout.LayoutParams(width, height));
            setMargins(sizeButton, 20, 20, 20, 20);
            sizeButton.setBackgroundResource(R.drawable.roundedbutton);

            forSort.add(s.getSizeName());

            sizeButton.setTextColor(Color.BLACK);
            sizeButton.setText(s.getSizeName());

            sizeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    product.setProductSize(s);
                    selectedProductQuantity = 0;
                    itemNumberPicker.setText(String.valueOf(selectedProductQuantity));
                    selectSize(v.getId(), sizes);
                    selectedProductSize = product.getProductSize().getSizeQuantity() == 0 ? null : product.getSizeByName(sizeButton.getText().toString());
                    manageQuantityButtons();
                    if (selectedProductSize == null) {
                        Utils.showSingleButtonAlertWithoutTitle(ProductDetailsActivity.this, getResources().getString(R.string.stock_consumed_error_msg));
                        sizeButton.setBackgroundResource(R.drawable.roundedbutton);
                        sizeButton.setTextColor(Color.BLACK);
                    }
                }
            });
            sizeButtonsList.add(sizeButton);
            id++;
        }

        display(sort(forSort, product));
    }

    private void manageQuantityButtons() {
        increaseQuantity.setImageDrawable(ContextCompat.getDrawable(ProductDetailsActivity.this,
                selectedProductSize == null ? R.drawable.ic_add_green_disabled : R.drawable.ic_add_green));
        increaseQuantity.setEnabled(selectedProductSize != null);
        increaseQuantity.setClickable(selectedProductSize != null);

        decreaseQuantity.setImageDrawable(ContextCompat.getDrawable(ProductDetailsActivity.this,
                selectedProductSize == null ? R.drawable.ic_remove_green_disabled : R.drawable.ic_remove_green));
        decreaseQuantity.setEnabled(selectedProductSize != null);
        decreaseQuantity.setClickable(selectedProductSize != null);
    }

    //  Method to create buttons list for product color variations
    private void createColorButtonsList(ArrayList<Product> products, String productColor) {
        colorButtonsList = new ArrayList<>();
//        while (colorButtonsList.size() < products.size()) {
        for (final Product product : specificProducts) {
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());
            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());

            Button colorButton = new Button(this);
            colorButton.setLayoutParams(new LinearLayout.LayoutParams(width, height));
            setMargins(colorButton, 8, 8, 8, 8);

            if (product.getProductColor().contentEquals(productColor)) {
                GradientDrawable gd = new GradientDrawable();
                gd.setColor(Color.parseColor(product.getProductColor()));
                gd.setCornerRadius(100);
                gd.setStroke(4, Color.BLACK);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    colorButton.setBackground(gd);
                } else {
//                        colorButton.setBackgroundDrawable(gd);
                    colorButton.setBackground(gd);
                }
            } else {
                GradientDrawable gd = new GradientDrawable();
                gd.setColor(Color.parseColor(product.getProductColor()));
                gd.setCornerRadius(100);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    colorButton.setBackground(gd);
                } else {
                    colorButton.setBackground(gd);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                colorButton.setElevation(2);
            }

            // Clicking on color button to select item's color
            colorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clear();
                    selectedProductQuantity = 0;
                    selectedProductSize = null;
                    manageQuantityButtons();
                    itemNumberPicker.setText(String.valueOf(selectedProductQuantity));
                    createActivity(product);
                }
            });

            colorButtonsList.add(colorButton);
            itemColorsLinearLayout.addView(colorButton);
        }
//        }
    }

    //  Method to set the button margin
    private void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    //  Method to reset the source data in order to load another <object> product </object>
    // as source data for the screen
    private void clear() {
        colorButtonsList = new ArrayList<>();
        itemColorsLinearLayout.removeAllViewsInLayout();
        itemSizesLinearLayout.removeAllViewsInLayout();
        this.selectedProductSize = null;
    }

    /**
     * Method to search product color variations
     * <p>
     * Note:1. Product x { x.color == Color.RED }
     * and
     * 2. Product x { x.color == Color.BLUE }
     * represent 2 different products, both being color variations of the
     * same product. So, we could say that
     * 1. x.getID() != 2. x.getID() &&
     * 1. x.getSizesAndQuantity() != 2. x.getSizesAndQuantity()
     * but
     * 1. x.getName() == 2. x.getName()
     * <p>
     * <code> return </code> <variable> newList </variable> that will represent the
     * list of products considered to be variations of <param> product </param>
     */
    private void addItemsToSpecificProducts(Product product) {
        specificProducts = new ArrayList<>();
        for (Product product1 : ProductListsManager.getInstance().getProducts()) {
            if (product1.getProductName().contentEquals(product.getProductName())) {
                specificProducts.add(product1);
            }
        }
    }


    //  Method to sort a list
    private ArrayList<String> sort(ArrayList<String> listToSort, Product product) {
        ArrayList<String> sortedSizesList = new ArrayList<>();
        if (product.getCategory().contentEquals("Clothes") || product.getCategory().contentEquals("Other")) {
            for (int i = 0; i < sizesList.size(); i++) {
                for (int j = 0; j < listToSort.size(); j++) {
                    if (sizesList.get(i).equalsIgnoreCase(listToSort.get(j))) {
                        sortedSizesList.add(listToSort.get(j));
                    }
                }
            }
        } else {
            while (listToSort.size() > 0) {
                int position = 0;
                String aux = listToSort.get(position);
                for (int iterator = 0; iterator < listToSort.size(); iterator++) {
                    if (Double.valueOf(listToSort.get(iterator)) < Double.valueOf(aux)) {
                        aux = listToSort.get(iterator);
                        position = iterator;
                    }
                }
                sortedSizesList.add(aux);
                listToSort.remove(position);
            }
        }
        return sortedSizesList;
    }

    //  Method to display size buttons
    private void display(ArrayList<String> sortedList) {
        for (String size : sortedList) {
            for (Button sizeButton : sizeButtonsList) {
                if (size.contentEquals(sizeButton.getText())) {
                    itemSizesLinearLayout.addView(sizeButton);
                }
            }
        }
    }

    private void check(Product product) {
        for (ProductSize size : product.getSizes()) {
            if (size.getSizeQuantity() == 0) {
                product.removeSize(size);
                check(product);
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        this.clear();
        if (product.getProductSize() != null) {
            for (Button sizeButton : sizeButtonsList) {
                if (sizeButton.getText().toString().equals(product.getProductSize().getSizeName())) {
                    sizeButton.setSelected(true);
                }
            }
        }

        cartProductsQuantity.setText(String.valueOf(ProductListsManager.getInstance().getCartItems().size()));
        productsCartShape.setVisibility(ProductListsManager.getInstance().getCartItems().size() == 0 ? View.GONE : View.VISIBLE);
//        this.createActivity(product);
    }

    @Override
    public void onBackPressed() {
        if (previous.equals("favorites")) {
            startActivity(new Intent(ProductDetailsActivity.this, FavoritesActivity.class));
            finish();
        } else
            finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {
            finish();
        }
    }

    @Override
    public void onSuccess(Object data, ResponseExtractor.ResponseType type) {
        ResponseData responseData = (ResponseData) data;
        int code = responseData.getErrorCode();

        switch (code) {
            case 200:
                Log.d(TAG, "onSuccess: Successful request");
                break;

            default:
                Utils.showSingleButtonAlertWithoutTitle(ProductDetailsActivity.this, responseData.getDescription());
//                Log.e(TAG, responseData.getDescription());
        }
    }

    @Override
    public void onFailed(Object data, ResponseExtractor.ResponseType type) {
        ResponseData responseData = (ResponseData) data;
        Log.e(TAG, "onFailed: " + responseData.getDescription());
    }
}
