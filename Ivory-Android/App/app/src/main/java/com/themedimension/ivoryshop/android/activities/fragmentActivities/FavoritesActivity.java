/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.activities.fragmentActivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.activities.checkout.CartActivity;
import com.themedimension.ivoryshop.android.fragments.BaseFavoritesFragment;
import com.themedimension.ivoryshop.android.manager.ProductListsManager;

public class FavoritesActivity extends AppCompatActivity implements View.OnClickListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    //region GLOBALS
    public static final int FAVORITE_ALL_PRODUCTS = 0;
    public static final int FAVORITE_CLOTHES = 1;
    public static final int FAVORITE_SHOES = 2;
    public static final int FAVORITE_OTHER = 3;
    private Fragment fragment;


    private Button backButton;
    private RelativeLayout cartQuantityShape;
    private TextView productsQuantity;
    private FrameLayout goToCartButton;
    private SearchView searchView;
    private EditText searchEditText;

    private Button favoritesCategoryAllBtn, favoritesCategoryClothesBtn, favoritesCategoryShoesBtn, favoritesCategoryOthersBtn;
    private Button[] tabButtons;
    private FloatingActionButton bottomFiltersButton;

    private int favoriteProductsNestedScrollViewY;
    private View.OnClickListener toolbarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.favorites_back_button:
                    onBackPressed();
                    break;
                case R.id.cart_layout:
                    Intent goToCartIntent = new Intent(FavoritesActivity.this, CartActivity.class);
                    startActivity(goToCartIntent);
                    break;
            }
        }
    };

    public int getFavoriteProductsNestedScrollViewY() {
        return favoriteProductsNestedScrollViewY;
    }

    public void setFavoriteProductsNestedScrollViewY(int favoriteProductsNestedScrollViewY) {
        this.favoriteProductsNestedScrollViewY = favoriteProductsNestedScrollViewY;
    }

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        init();
        setupUI();
    }

    private void init() {
        backButton = (Button) findViewById(R.id.favorites_back_button);
        cartQuantityShape = (RelativeLayout) findViewById(R.id.products_quantity_shape);
        productsQuantity = (TextView) findViewById(R.id.products_inside_cart_textview);
        goToCartButton = (FrameLayout) findViewById(R.id.cart_layout);
        searchView = (SearchView) findViewById(R.id.favorites_search_view);
        searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);

        favoritesCategoryAllBtn = (Button) findViewById(R.id.favourites_all_products);
        favoritesCategoryClothesBtn = (Button) findViewById(R.id.favourites_clothes);
        favoritesCategoryShoesBtn = (Button) findViewById(R.id.favourites_shoes);
        favoritesCategoryOthersBtn = (Button) findViewById(R.id.favourites_other);
        tabButtons = new Button[]{favoritesCategoryAllBtn, favoritesCategoryClothesBtn, favoritesCategoryShoesBtn, favoritesCategoryOthersBtn};

        bottomFiltersButton = (FloatingActionButton) findViewById(R.id.favorites_filter_button);

    }

    private void setupUI() {
        backButton.setOnClickListener(toolbarListener);
        cartQuantityShape.setVisibility(ProductListsManager.getInstance().getCartItems().size() != 0 ? View.VISIBLE : View.GONE);
        productsQuantity.setText(String.valueOf(ProductListsManager.getInstance().getCartItems().size()));
        goToCartButton.setOnClickListener(toolbarListener);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseFavoritesFragment) fragment).onSearchViewClicked();
            }
        });
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        searchEditText.setTextColor(ContextCompat.getColor(FavoritesActivity.this, R.color.black));
        searchEditText.setHintTextColor(ContextCompat.getColor(FavoritesActivity.this, R.color.background_e8_grey));

        favoritesCategoryAllBtn.setOnClickListener(this);
        favoritesCategoryAllBtn.setBackgroundResource(R.drawable.border);
        favoritesCategoryAllBtn.setTextColor(ContextCompat.getColor(this, R.color.text_black));
        favoritesCategoryClothesBtn.setOnClickListener(this);
        favoritesCategoryShoesBtn.setOnClickListener(this);
        favoritesCategoryOthersBtn.setOnClickListener(this);

        bottomFiltersButton.setOnClickListener(this);
        replace(FAVORITE_ALL_PRODUCTS);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.favourites_all_products:
                changeColors(favoritesCategoryAllBtn.getId());
                replace(FAVORITE_ALL_PRODUCTS);
                break;
            case R.id.favourites_clothes:
                changeColors(favoritesCategoryClothesBtn.getId());
                replace(FAVORITE_CLOTHES);
                break;
            case R.id.favourites_shoes:
                changeColors(favoritesCategoryShoesBtn.getId());
                replace(FAVORITE_SHOES);
                break;
            case R.id.favourites_other:
                changeColors(favoritesCategoryOthersBtn.getId());
                replace(FAVORITE_OTHER);
                break;

            case R.id.favorites_filter_button:
                ((BaseFavoritesFragment) fragment).onFilterButtonClicked();
                break;
        }
    }

    // Method to change the color of the tab currently selected by the user.
    public void changeColors(int buttonID) {
        for (Button tabButton : tabButtons) {
            if (tabButton.getId() == buttonID) {
                tabButton.setBackgroundResource(R.drawable.border);
                tabButton.setTextColor(ResourcesCompat.getColor(getResources(), R.color.text_black, null));
            } else {
                tabButton.setBackgroundResource(R.color.background_white);
                tabButton.setTextColor(ResourcesCompat.getColor(getResources(), R.color.text_medium_grey, null));
            }
        }
    }

    private void replace(int id) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = BaseFavoritesFragment.newInstance(id);
        fragmentManager.beginTransaction().replace(R.id.favorites_frame_layout, fragment).commit();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cartQuantityShape.setVisibility(ProductListsManager.getInstance().getCartItems().size() != 0 ? View.VISIBLE : View.GONE);
        productsQuantity.setText(String.valueOf(ProductListsManager.getInstance().getCartItems().size()));
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        ((BaseFavoritesFragment) fragment).onSearchViewOpened(newText);
        return true;
    }

    @Override
    public boolean onClose() {
        ((BaseFavoritesFragment) fragment).onSearchViewClosed();
        return false;
    }
}

