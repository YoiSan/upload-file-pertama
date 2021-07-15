package com.themedimension.ivoryshop.android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.activities.fragmentActivities.DrawerActivity;
import com.themedimension.ivoryshop.android.adapters.AllProductsAdapter;
import com.themedimension.ivoryshop.android.manager.ProductListsManager;
import com.themedimension.ivoryshop.android.models.Product;
import com.themedimension.ivoryshop.android.utils.Utils;

import java.util.ArrayList;

import static com.themedimension.ivoryshop.android.utils.Utils.NEWEST;
import static com.themedimension.ivoryshop.android.utils.Utils.OLDEST;
import static com.themedimension.ivoryshop.android.utils.Utils.PRICE_DOWN;
import static com.themedimension.ivoryshop.android.utils.Utils.PRICE_UP;

/**
 * Created by Andreea Braesteanu on 8/17/2017.
 */

public class BaseProductsFragment extends Fragment implements FragmentInterface, View.OnClickListener {
    public static final String ARG_PAGE = "Page";
    public static final int ALL_PRODUCTS = 0;
    public static final int CLOTHES = 1;
    public static final int SHOES = 2;
    public static final int OTHER = 3;

    private int pageNr;
    private ArrayList<Product> products;
    private String firstFilter = Utils.FILTER_ALL;
    private String secondFilter = Utils.NEWEST;
    private int oldVerticalOffset = 0;

    private AllProductsAdapter productsAdapter;
    private RelativeLayout filtersLayout;
    private AppBarLayout appBarLayout;
    private FloatingActionButton bottomFilters;
    private NestedScrollView nestedScrollView;
    private Button menBtn, womenBtn, kidsBtn, allBtn,
            priceUpBtn, priceDownBtn, newestBtn, oldestBtn;
    private Button[] firstRow, secondRow;
    private View rootView;
    private FloatingActionButton topFiltersButton;
    private RecyclerView productsRecyclerView;
    private ImageView promoImage;
    private SearchView searchView;

    public BaseProductsFragment() {
    }

    public static BaseProductsFragment newInstance(int id) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, id);
        BaseProductsFragment fragment = new BaseProductsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public int getPageNr() {
        return pageNr;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pageNr = getArguments().getInt(ARG_PAGE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_base, container, false);
        Log.e("Page Number", String.valueOf(pageNr));
        init();
        initFilterButtons(filtersLayout);
        setupUI();
        return rootView;
    }

    private void init() {
        searchView = (SearchView) getActivity().findViewById(R.id.drawer_activity_search_view);
        appBarLayout = (AppBarLayout) rootView.findViewById(R.id.appbar);
        promoImage = (ImageView) rootView.findViewById(R.id.promoImg);
        filtersLayout = (RelativeLayout) rootView.findViewById(R.id.filters_layout);
        topFiltersButton = (FloatingActionButton) rootView.findViewById(R.id.top_filter_button);
        nestedScrollView = (NestedScrollView) rootView.findViewById(R.id.nested_scroll_view);
        productsRecyclerView = (RecyclerView) rootView.findViewById(R.id.all_products_recycler_view);
        bottomFilters = (FloatingActionButton) getActivity().findViewById(R.id.bottom_filters_button);
    }

    private void setupUI() {
        searchView.onActionViewCollapsed();
        promoImage.setVisibility(pageNr == 0 ? View.VISIBLE : View.GONE);
        appBarLayout.setExpanded(pageNr == 0);
        topFiltersButton.setOnClickListener(this);


        productsRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        switch (pageNr) {
            case ALL_PRODUCTS:
                products = ProductListsManager.getInstance().getProducts();
                break;
            case CLOTHES:
                products = ProductListsManager.getInstance().getClothes();
                lockAppBarClosed();
                break;
            case SHOES:
                products = ProductListsManager.getInstance().getShoes();
                lockAppBarClosed();
                break;
            case OTHER:
                products = ProductListsManager.getInstance().getOther();
                lockAppBarClosed();
                break;
        }

        productsAdapter = new AllProductsAdapter(getActivity(), products, this);
        productsRecyclerView.setAdapter(productsAdapter);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                Log.e("scrollY", String.valueOf(scrollY));
                if (pageNr == 0)
                    ((DrawerActivity) getActivity()).setScrollY(scrollY);
            }
        });

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                    // Collapsed
                    if (searchView.isIconified())
                        bottomFilters.setVisibility(View.VISIBLE);

                } else if (verticalOffset == 0) {
                    // Expanded
                    bottomFilters.setVisibility(View.GONE);
                } else {
                    // Somewhere in between
                    if (verticalOffset < -370) {
                        bottomFilters.setVisibility(View.VISIBLE);
                        return;
                    }
                    if (oldVerticalOffset > verticalOffset) {
                        bottomFilters.setVisibility(View.GONE);
                        return;
                    }

                    oldVerticalOffset = verticalOffset;
                    Log.e("verticalOffset", String.valueOf(verticalOffset));
                }
            }
        });
        // sets the products list after the two main filters - All and NEWEST
        showAll();
        productsAdapter.sortList(NEWEST);
        resetColorRow1(allBtn);
    }

    public void initFilterButtons(View view) {

        menBtn = (Button) view.findViewById(R.id.menBtnId);
        womenBtn = (Button) view.findViewById(R.id.womenBtnId);
        kidsBtn = (Button) view.findViewById(R.id.kidsBtnId);
        allBtn = (Button) view.findViewById(R.id.allBtnId);
        newestBtn = (Button) view.findViewById(R.id.newestBtnId);
        oldestBtn = (Button) view.findViewById(R.id.oldestBtnId);
        priceUpBtn = (Button) view.findViewById(R.id.priceUpBtnId);
        priceDownBtn = (Button) view.findViewById(R.id.priceDownBtnId);

        menBtn.setOnClickListener(this);
        womenBtn.setOnClickListener(this);
        kidsBtn.setOnClickListener(this);
        allBtn.setOnClickListener(this);
        newestBtn.setOnClickListener(this);
        oldestBtn.setOnClickListener(this);
        priceUpBtn.setOnClickListener(this);
        priceDownBtn.setOnClickListener(this);

        firstRow = new Button[]{menBtn, womenBtn, kidsBtn, allBtn};
        secondRow = new Button[]{priceUpBtn, priceDownBtn, newestBtn, oldestBtn};

        allBtn.setBackgroundResource(R.color.navigation_buttons_green);
        allBtn.setTextColor(ContextCompat.getColor(getActivity(), R.color.background_white));
        newestBtn.setBackgroundResource(R.color.navigation_buttons_green);
        newestBtn.setTextColor(ContextCompat.getColor(getActivity(), R.color.background_white));
    }

    @Override
    public void fragmentIsVisible() {
    }

    @Override
    public void onFilterButtonClicked() {
        if (!appBarLayout.isActivated())
            unlockAppBarOpen();
        appBarLayout.setExpanded(true);
        filtersLayout.setVisibility(View.VISIBLE);
        nestedScrollView.smoothScrollTo(0, 0);

    }

    @Override
    public void onSearchViewOpened(String string) {
        if (productsAdapter != null)
            productsAdapter.getFilter().filter(string);
    }

    @Override
    public void onSearchViewClicked() {
        bottomFilters.setVisibility(View.GONE);
        filtersLayout.setVisibility(View.INVISIBLE);
        appBarLayout.setExpanded(false);
        lockAppBarClosed();
    }

    @Override
    public void onSearchViewClosed() {
        if (pageNr == 0)
            unlockAppBarOpen();
    }

    /*Called when fragment is replaced from favorites click from drawer*/
    @Override
    public void onRefreshFragment() {
    }

    public void isAppBarVisible(boolean isVisible) {
        appBarLayout.setExpanded(isVisible);
    }

    /*Called when SearchView is opening*/
    public void lockAppBarClosed() {
        appBarLayout.setExpanded(false, false);
        appBarLayout.setActivated(false);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        lp.height = 0;
    }

    /*Called when SearchView is closing*/
    public void unlockAppBarOpen() {
        appBarLayout.setExpanded(true, false);
        appBarLayout.setActivated(true);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        lp.height = CoordinatorLayout.LayoutParams.WRAP_CONTENT;
    }

    @Override
    public void onResume() {
        super.onResume();
        productsAdapter.notifyDataSetChanged();
        if (pageNr != 0)
            lockAppBarClosed();
        else if (((DrawerActivity) getActivity()).getScrollY() != 0) {
            appBarLayout.setExpanded(false);
        }
        bottomFilters.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.top_filter_button:
                if (pageNr == 0)
                    filtersLayout.setVisibility(filtersLayout.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                else
                    appBarLayout.setExpanded(false);
                break;

            case R.id.allBtnId:
                firstFilter = Utils.FILTER_ALL;
                resetColorRow1(allBtn);
                showAll();
                break;
            case R.id.menBtnId:
                firstFilter = Utils.FILTER_MEN;
                resetColorRow1(menBtn);
                showMenClothes();
                break;
            case R.id.womenBtnId:
                firstFilter = Utils.FILTER_WOMEN;
                resetColorRow1(womenBtn);
                showWomenClothes();
                break;
            case R.id.kidsBtnId:
                firstFilter = Utils.FILTER_KIDS;
                resetColorRow1(kidsBtn);
                showKidsClothes();
                break;

            case R.id.newestBtnId:
                secondFilter = Utils.NEWEST;
                resetColorRow2(newestBtn);
                productsAdapter.sortList(NEWEST);
                break;
            case R.id.oldestBtnId:
                secondFilter = Utils.OLDEST;
                resetColorRow2(oldestBtn);
                productsAdapter.sortList(OLDEST);
                break;
            case R.id.priceUpBtnId:
                secondFilter = Utils.PRICE_UP;
                resetColorRow2(priceUpBtn);
                productsAdapter.sortList(PRICE_UP);
                break;
            case R.id.priceDownBtnId:
                secondFilter = Utils.PRICE_DOWN;
                resetColorRow2(priceDownBtn);
                productsAdapter.sortList(PRICE_DOWN);
                break;
        }
    }


    /*
   Method to change the filter button colors from the first row when one of the buttons is clicked.
    */
    public void resetColorRow1(Button button) {
        for (Button aFirstRow : firstRow) {
            if (aFirstRow != button) {
                aFirstRow.setBackgroundResource(R.color.background_transparent);
                aFirstRow.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_grey_600));
            }
        }
        button.setBackgroundResource(R.color.navigation_buttons_green);
        button.setTextColor(ContextCompat.getColor(getActivity(), R.color.background_white));
    }

    /*
    Method to change the filter button colors from the second row when one of the buttons is clicked.
     */
    public void resetColorRow2(Button button) {
        for (Button aSecondRow : secondRow) {
            if (aSecondRow != button) {
                aSecondRow.setBackgroundResource(R.color.background_transparent);
                aSecondRow.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_grey_600));
                if (aSecondRow.getId() == R.id.priceDownBtnId) {
                    aSecondRow.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_more_grey, 0);
                }
                if (aSecondRow.getId() == R.id.priceUpBtnId) {
                    aSecondRow.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_less_grey, 0);
                }
            }
        }
        button.setBackgroundResource(R.color.navigation_buttons_green);
        button.setTextColor(ContextCompat.getColor(getActivity(), R.color.background_white));
        if (secondFilter.equals(Utils.PRICE_UP) || secondFilter.equals(Utils.PRICE_DOWN))
            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, secondFilter.equals(Utils.PRICE_DOWN) ? R.drawable.ic_expand_more_white : R.drawable.ic_expand_less_white, 0);
    }


    //region SORT

    /*
    Methods for the filters to sort product items by category.
     */
    public void showAll() {
        productsAdapter.filterListByGender(products);
        sort();

    }

    public void showWomenClothes() {
        productsAdapter.filterListByGender(Utils.getFilteredListByGender(Utils.FILTER_WOMEN, products));
        sort();

    }

    public void showMenClothes() {
        productsAdapter.filterListByGender(Utils.getFilteredListByGender(Utils.FILTER_MEN, products));
        sort();
    }

    public void showKidsClothes() {
        productsAdapter.filterListByGender(Utils.getFilteredListByGender(Utils.FILTER_KIDS, products));
        sort();
    }

    public void sort() {
        switch (secondFilter) {
            case Utils.NEWEST:
                newestBtn.performClick();
                break;
            case OLDEST:
                oldestBtn.performClick();
                break;
            case PRICE_UP:
                priceUpBtn.performClick();
                break;
            case PRICE_DOWN:
                priceDownBtn.performClick();
                break;
        }
    }
    //endregion

}
