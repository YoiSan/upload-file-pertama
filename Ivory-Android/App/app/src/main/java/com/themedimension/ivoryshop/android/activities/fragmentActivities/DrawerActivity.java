/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.activities.fragmentActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.activities.LegalActivity;
import com.themedimension.ivoryshop.android.activities.SettingsActivity;
import com.themedimension.ivoryshop.android.activities.checkout.CartActivity;
import com.themedimension.ivoryshop.android.activities.registerAndLogin.LoginActivity;
import com.themedimension.ivoryshop.android.activities.registerAndLogin.RegisterActivity;
import com.themedimension.ivoryshop.android.activities.registerAndLogin.SignInActivity;
import com.themedimension.ivoryshop.android.fragments.BaseProductsFragment;
import com.themedimension.ivoryshop.android.fragments.FragmentInterface;
import com.themedimension.ivoryshop.android.manager.NetworkManager;
import com.themedimension.ivoryshop.android.manager.OnNetworkCallback;
import com.themedimension.ivoryshop.android.manager.ProductListsManager;
import com.themedimension.ivoryshop.android.manager.ResponseExtractor;
import com.themedimension.ivoryshop.android.models.User;
import com.themedimension.ivoryshop.android.response.ResponseData;
import com.themedimension.ivoryshop.android.utils.Utils;

import static com.themedimension.ivoryshop.android.manager.NetworkManager.DEMO_DATA;

public class DrawerActivity extends AppCompatActivity implements View.OnClickListener, OnNetworkCallback, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    //region GLOBALS


    private static final String TAG = "DrawerActivity";

    FragmentInterface fragmentInterface;
    private NetworkManager manager;
    private Drawer result = null;
    private FrameLayout frameLayout;
    private Button categoryAllBtn,
            categoryClothesBtn,
            categoryShoesBtn,
            categoryOthersBtn;
    private Button[] tabButtons;
    private DrawerLayout drawer;
    private TextView productsQuantity;
    private FloatingActionButton bottomFiltersButton;
    private RelativeLayout productQuantityShape;
    private LinearLayout drawerListLayout;
    private LinearLayout logoutLayout;
    private ImageView expandArrow;
    private SearchView searchView;
    private Fragment fragment;
    private double scrollY;
    private RelativeLayout favoritesCounterShape;
    private TextView favoritesCounter;
    private RelativeLayout cartCounterShape;
    private TextView cartCounter;
    private boolean userIsAnon;


    public double getScrollY() {
        return scrollY;
    }

    public void setScrollY(double scrollY) {
        this.scrollY = scrollY;
    }

    //endregion
    private View.OnClickListener toolbarListener = new View.OnClickListener() {

        /**
         * Method that switches between toolbar elements
         * when one of them is clicked.
         */
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.burgerMenuButton:
                    if (!searchView.isIconified())
                        searchView.setIconified(true);
                    if (!drawer.isDrawerOpen(Gravity.LEFT)) {
                        drawer.openDrawer(Gravity.LEFT);
                    }
                    break;
                case R.id.cart_layout:
                    Intent goToCartIntent = new Intent(DrawerActivity.this, CartActivity.class);
                    startActivity(goToCartIntent);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        userIsAnon = DEMO_DATA? User.getInstance().isAnon() : User.getInstance().isAnonymous();
        initToolbar();
        initTabButtons();
        initDrawer();
        initActivityViews();
    }

    private void initToolbar() {
        Button burgerMenuButton = (Button) findViewById(R.id.burgerMenuButton);
        burgerMenuButton.setOnClickListener(toolbarListener);

        searchView = (SearchView) findViewById(R.id.drawer_activity_search_view);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseProductsFragment) fragment).onSearchViewClicked();
            }
        });
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus)
                    searchView.setIconified(true);
            }
        });
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(ContextCompat.getColor(DrawerActivity.this, R.color.black));
        searchEditText.setHintTextColor(ContextCompat.getColor(DrawerActivity.this, R.color.background_e8_grey));

        FrameLayout goToCartButton = (FrameLayout) findViewById(R.id.cart_layout);
        goToCartButton.setOnClickListener(toolbarListener);
        productQuantityShape = (RelativeLayout) findViewById(R.id.products_quantity_shape);
        productQuantityShape.setVisibility(ProductListsManager.getInstance().getCartItems().size() != 0 ? View.VISIBLE : View.GONE);
        productsQuantity = (TextView) findViewById(R.id.products_inside_cart_textview);
        productsQuantity.setText(String.valueOf(ProductListsManager.getInstance().getCartItems().size()));
    }

    private void initTabButtons() {
        categoryAllBtn = (Button) findViewById(R.id.all_pager_btnId);
        categoryClothesBtn = (Button) findViewById(R.id.clothes_pager_btnId);
        categoryShoesBtn = (Button) findViewById(R.id.shoes_pager_btnId);
        categoryOthersBtn = (Button) findViewById(R.id.other_pager_btnId);
        tabButtons = new Button[]{categoryAllBtn, categoryClothesBtn, categoryOthersBtn, categoryShoesBtn};
        categoryAllBtn.setOnClickListener(this);
        categoryAllBtn.setBackgroundResource(R.drawable.border);
        categoryAllBtn.setTextColor(ContextCompat.getColor(this, R.color.text_black));
        categoryClothesBtn.setOnClickListener(this);
        categoryShoesBtn.setOnClickListener(this);
        categoryOthersBtn.setOnClickListener(this);
    }


    /*
     * Initialization method - declaring main layout elements and setting onClickListeners.
     * */
    private void initActivityViews() {
        frameLayout = (FrameLayout) findViewById(R.id.frame_layout);
        replace(BaseProductsFragment.ALL_PRODUCTS);
        bottomFiltersButton = (FloatingActionButton) findViewById(R.id.bottom_filters_button);
        bottomFiltersButton.setOnClickListener(this);
    }

    private void initDrawer() {
        drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                Log.e("slider", String.valueOf(slideOffset));
                if (slideOffset != 0){
                    favoritesCounterShape.setVisibility(ProductListsManager.getInstance().getFavoriteProducts().size() == 0? View.GONE : View.VISIBLE);
                    favoritesCounter.setText(String.valueOf(ProductListsManager.getInstance().getFavoriteProducts().size()));
                    cartCounterShape.setVisibility(ProductListsManager.getInstance().getCartItems().size() == 0? View.GONE : View.VISIBLE);
                    cartCounter.setText(String.valueOf(ProductListsManager.getInstance().getCartItems().size()));
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (logoutLayout.getVisibility() == View.VISIBLE) {
                    expandArrow.setImageDrawable(ContextCompat.getDrawable(DrawerActivity.this, R.drawable.arrow_drop_down_white));
                    logoutLayout.setVisibility(View.GONE);
                    drawerListLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        LinearLayout favorites = (LinearLayout) findViewById(R.id.navigation_favorites);
        favorites.setOnClickListener(this);
        favoritesCounterShape = (RelativeLayout) findViewById(R.id.favorites_quantity_shape);
        favoritesCounterShape.setVisibility(ProductListsManager.getInstance().getFavoriteProducts().size() == 0? View.GONE : View.VISIBLE);
        favoritesCounter = (TextView) findViewById(R.id.favorites_drawer_text);
        favoritesCounter.setText(String.valueOf(ProductListsManager.getInstance().getFavoriteProducts().size()));
        LinearLayout cart = (LinearLayout) findViewById(R.id.navigation_cart);
        cart.setOnClickListener(this);
        cartCounterShape = (RelativeLayout) findViewById(R.id.cart_quantity_shape);
        cartCounterShape.setVisibility(ProductListsManager.getInstance().getFavoriteProducts().size() == 0? View.GONE : View.VISIBLE);
        cartCounter = (TextView) findViewById(R.id.drawer_cart_textview);
        cartCounter.setText(String.valueOf(ProductListsManager.getInstance().getCartItems().size()));
        TextView settings = (TextView) findViewById(R.id.navigation_settings);
        settings.setOnClickListener(this);
        TextView legal = (TextView) findViewById(R.id.navigation_legal);
        legal.setOnClickListener(this);
        TextView createAccount = (TextView) findViewById(R.id.navigation_create_account);
        createAccount.setOnClickListener(this);
        CardView createNewAccount = (CardView) findViewById(R.id.create_new_account_cardView);
        createNewAccount.setVisibility(userIsAnon ? View.VISIBLE : View.GONE);
        LinearLayout userLayout = (LinearLayout) findViewById(R.id.user_layout);
        userLayout.setVisibility(userIsAnon ? View.GONE : View.VISIBLE);
        TextView username = (TextView) findViewById(R.id.username);
        username.setText(userIsAnon ? "" : User.getInstance().getFirstName());
        TextView userEmail = (TextView) findViewById(R.id.user_email);
        userEmail.setText(userIsAnon ? "" : User.getInstance().getEmail());
        drawerListLayout = (LinearLayout) findViewById(R.id.drawer_list_layout);
        logoutLayout = (LinearLayout) findViewById(R.id.logout_user_layout);
        expandArrow = (ImageView) findViewById(R.id.expand_icon);
        expandArrow.setOnClickListener(this);
        TextView secondUserEmail = (TextView) findViewById(R.id.user_email_with_logo);
        secondUserEmail.setText(userIsAnon ? "" : User.getInstance().getEmail());
        TextView logout = (TextView) findViewById(R.id.logout);
        logout.setOnClickListener(this);

    }

    private void replace(int id) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = BaseProductsFragment.newInstance(id);
        fragmentInterface = BaseProductsFragment.newInstance(id);
        fragmentManager.beginTransaction().replace(R.id.frame_layout, fragment).commit();
    }

    /**
     * Method that switches between Category tabs - All,Clothes,Shoes and Other -
     * when one of them is clicked.
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.all_pager_btnId:
                changeColors(categoryAllBtn.getId());
                replace(BaseProductsFragment.ALL_PRODUCTS);
                bottomFiltersButton.setVisibility(View.GONE);

                break;
            case R.id.clothes_pager_btnId:
                changeColors(categoryClothesBtn.getId());
                replace(BaseProductsFragment.CLOTHES);
                bottomFiltersButton.setVisibility(View.VISIBLE);
                break;
            case R.id.shoes_pager_btnId:
                changeColors(categoryShoesBtn.getId());
                replace(BaseProductsFragment.SHOES);
                bottomFiltersButton.setVisibility(View.VISIBLE);
                break;
            case R.id.other_pager_btnId:
                changeColors(categoryOthersBtn.getId());
                replace(BaseProductsFragment.OTHER);
                bottomFiltersButton.setVisibility(View.VISIBLE);
                break;

            case R.id.navigation_favorites:
                if (userIsAnon) {
                    Utils.showSingleButtonAlert(DrawerActivity.this, getResources().getString(R.string.user_not_logged_in), getResources().getString(R.string.connect_to_see_favorites));
                } else {
                    drawer.closeDrawer(Gravity.LEFT);
                    Intent transferFavorites = new Intent(DrawerActivity.this, FavoritesActivity.class);
                    startActivity(transferFavorites);
                }
                break;
            case R.id.navigation_cart:
                Intent transferCart = new Intent(DrawerActivity.this, CartActivity.class);
                startActivity(transferCart);
                break;
            case R.id.navigation_settings:
                    if (userIsAnon) {
                        Utils.showSingleButtonAlert(DrawerActivity.this,getResources().getString(R.string.user_not_logged_in), getResources().getString(R.string.connect_to_see_settings));
                    } else {
                        drawer.closeDrawer(Gravity.LEFT);
                        Intent transferSettings = new Intent(DrawerActivity.this, SettingsActivity.class);
                        startActivityForResult(transferSettings, 1);
                    }
                break;
            case R.id.navigation_legal:
                Intent transferPrivacy = new Intent(DrawerActivity.this, LegalActivity.class);
                startActivityForResult(transferPrivacy, 0);
                break;
            case R.id.navigation_create_account:
                drawer.closeDrawer(Gravity.LEFT);
                Intent openRegister = new Intent(DrawerActivity.this, RegisterActivity.class);
                startActivity(openRegister);
                break;
            case R.id.bottom_filters_button:
                ((BaseProductsFragment) fragment).onFilterButtonClicked();
                break;
            case R.id.expand_icon:
                if (expandArrow.getDrawable().getConstantState().equals(ContextCompat.getDrawable(DrawerActivity.this, R.drawable.arrow_drop_down_white).getConstantState())) {
                    expandArrow.setImageDrawable(ContextCompat.getDrawable(DrawerActivity.this, R.drawable.arrow_drop_up_white));
                    drawerListLayout.setVisibility(View.GONE);
                    logoutLayout.setVisibility(View.VISIBLE);
                } else {
                    expandArrow.setImageDrawable(ContextCompat.getDrawable(DrawerActivity.this, R.drawable.arrow_drop_down_white));
                    drawerListLayout.setVisibility(View.VISIBLE);
                    logoutLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.logout:
                final AlertDialog logoutDialog = Utils.showDoubleButtonAlert(DrawerActivity.this, getResources().getString(R.string.action_logout),
                        getResources().getString(R.string.are_you_sure_you_want_to_logout),
                        getResources().getString(R.string.button_yes),
                        getResources().getString(R.string.button_no));
                logoutDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (DEMO_DATA){
                            User.getInstance().logout(DrawerActivity.this);
                            ProductListsManager.getInstance().clearManager();
                            startActivity(new Intent(DrawerActivity.this, SignInActivity.class));
                            finish();
                        } else {
                            if (manager != null) {
                                logoutDialog.dismiss();
                                manager.logoutUser();
                            }
                        }

                    }
                });
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
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        }
    }

    /**
     * When a Category tab is selected change the text color to black and underline the selected tab.
     * The idle tabs remain the same until selected - GRAY text color and no underline.
     *
     * @param buttonID
     */
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

    /**
     * When the current activity is resumed, usually after another activity is closed,
     * the onResume is called instead of the onCreate one.
     */
    @Override
    protected void onResume() {
        super.onResume();
        productQuantityShape.setVisibility(ProductListsManager.getInstance().getCartItems().size() != 0 ? View.VISIBLE : View.GONE);
        productsQuantity.setText(String.valueOf(ProductListsManager.getInstance().getCartItems().size()));
        favoritesCounterShape.setVisibility(ProductListsManager.getInstance().getFavoriteProducts().size() == 0? View.GONE : View.VISIBLE);
        favoritesCounter.setText(String.valueOf(ProductListsManager.getInstance().getFavoriteProducts().size()));
        cartCounterShape.setVisibility(ProductListsManager.getInstance().getCartItems().size() == 0? View.GONE : View.VISIBLE);
        cartCounter.setText(String.valueOf(ProductListsManager.getInstance().getCartItems().size()));
        manager = new NetworkManager(DrawerActivity.this).setListener(this);
    }


    @Override
    public void onSuccess(Object data, ResponseExtractor.ResponseType type) {
//        ResponseData responseData = (ResponseData) data;
//        int code = responseData.getErrorCode();

        User.getInstance().logout(DrawerActivity.this);
        ProductListsManager.getInstance().clearManager();
        startActivity(new Intent(DrawerActivity.this, SignInActivity.class));
        finish();

//        switch (code) {
//            case 200:
//                User.getInstance().logout(DrawerActivity.this);
//                ProductListsManager.getInstance().clearManager();
//                startActivity(new Intent(DrawerActivity.this, SignInActivity.class));
//                finish();
//                break;
//
//            default:
//                break;
//        }
    }

    @Override
    public void onFailed(Object data, ResponseExtractor.ResponseType type) {
        final ResponseData responseData = (ResponseData) data;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showSingleButtonAlertWithoutTitle(DrawerActivity.this, responseData.getDescription());
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        ((BaseProductsFragment) fragment).onSearchViewOpened(newText);
        return true;
    }

    /**
     * Method called when pressing on SearchView's CLOSE button.
     */
    @Override
    public boolean onClose() {
        ((BaseProductsFragment) fragment).onSearchViewClosed();
        return false;
    }
}