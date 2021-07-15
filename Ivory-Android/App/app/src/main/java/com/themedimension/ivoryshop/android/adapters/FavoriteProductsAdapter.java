package com.themedimension.ivoryshop.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.activities.checkout.ProductDetailsActivity;
import com.themedimension.ivoryshop.android.activities.fragmentActivities.FavoritesActivity;
import com.themedimension.ivoryshop.android.manager.NetworkManager;
import com.themedimension.ivoryshop.android.manager.OnNetworkCallback;
import com.themedimension.ivoryshop.android.manager.ProductListsManager;
import com.themedimension.ivoryshop.android.manager.ResponseExtractor;
import com.themedimension.ivoryshop.android.models.Product;
import com.themedimension.ivoryshop.android.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import static com.themedimension.ivoryshop.android.utils.Utils.NEWEST;
import static com.themedimension.ivoryshop.android.utils.Utils.OLDEST;
import static com.themedimension.ivoryshop.android.utils.Utils.PRICE_DOWN;
import static com.themedimension.ivoryshop.android.utils.Utils.PRICE_UP;

/**
 * Created by Andreea Braesteanu on 8/21/2017.
 */

public class FavoriteProductsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private Context context;
    private ArrayList<Product> products;
    private ArrayList<Product> filteredProducts;
    private NetworkManager networkManager;

    public FavoriteProductsAdapter(Context context, ArrayList<Product> allProducts) {
        this.context = context;
        this.products = allProducts;
        this.filteredProducts = products;
        networkManager = new NetworkManager((FavoritesActivity) context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.widget_layout_products_gridview, parent, false);
        return new FavoriteProductsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final FavoriteProductsViewHolder productsHolder = ((FavoriteProductsViewHolder) holder);
        final Product product = filteredProducts.get(position);
        if (product != null) {
            productsHolder.favorite.setImageResource(ProductListsManager.getInstance().wishlistContains(product.getProductId()) ?
                    R.drawable.ic_favorite_red : R.drawable.ic_favorite_grey);
            productsHolder.cart.setImageResource(ProductListsManager.getInstance().cartContains(product.getProductId()) ?
                    R.drawable.ic_shopping_cart_green : R.drawable.ic_shopping_cart_grey);
            productsHolder.mainProductName.setText(product.getProductName());
            productsHolder.mainProducePrice.setText(context.getResources().getString(R.string.dollar_currency_string) + "" + product.getProductPrice().toString());


            Log.e("TAG", Utils.BASE_IMAGE_RES_URL + product.getProductImage());
            if (NetworkManager.DEMO_DATA) {
                try {
                    InputStream is = context.getAssets().open(product.getProductImage());
                    Drawable d = Drawable.createFromStream(is, null);
                    productsHolder.mainProductImage.setImageDrawable(d);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Picasso.with(context)
                        .load(Utils.BASE_IMAGE_RES_URL + product.getProductImage())
                        .centerInside()
                        .resize(156, 132)
                        .into(productsHolder.mainProductImage);
            }


            productsHolder.favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (ProductListsManager.getInstance().wishlistContains(product.getProductId())) {
                        productsHolder.favorite.setImageResource(R.drawable.ic_favorite_grey);
                        ProductListsManager.getInstance().removeWishlistItemById(product.getProductId());

                        networkManager.setListener(new OnNetworkCallback() {
                            @Override
                            public void onSuccess(Object data, ResponseExtractor.ResponseType type) {
                                products.remove(product);
                                filteredProducts.remove(product);
                                notifyDataSetChanged();
                            }

                            @Override
                            public void onFailed(Object data, ResponseExtractor.ResponseType type) {

                            }
                        });
                        networkManager.updateWishlist(context);
//                        Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT).show();
                    }
                }

            });

            productsHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent openProductDetailScreen = new Intent(context, ProductDetailsActivity.class);
                    openProductDetailScreen.putExtra("item", product.getProductId());
                    openProductDetailScreen.putExtra("previous", "favorites");
                    context.startActivity(openProductDetailScreen);
                    ((FavoritesActivity) context).finish();
//                        itemListener.onClick(product.getProductId());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return filteredProducts.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();

                if (charString.isEmpty()) {

                    filteredProducts = products;
                } else {

                    ArrayList<Product> filteredList = new ArrayList<>();

                    for (Product product : products) {

                        if (product.getProductName().toLowerCase().contains(charString)) {

                            filteredList.add(product);
                        }
                    }

                    filteredProducts = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredProducts;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredProducts = (ArrayList<Product>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void filterListByGender(ArrayList<Product> filteredProducts) {
        this.filteredProducts = new ArrayList<>();
        this.filteredProducts = filteredProducts;
        notifyDataSetChanged();
    }

    public void sortList(String type) {
        switch (type) {
            case NEWEST:
                Collections.sort(filteredProducts, new Comparator<Product>() {
                    public int compare(Product p1, Product p2) {
                        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
                        Date product1Date = null;
                        Date product2Date = null;
                        try {
                            product1Date = sdf.parse(p1.getProductAddedDate());
                            product2Date = sdf.parse(p2.getProductAddedDate());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return product2Date.compareTo(product1Date);
                    }
                });
                notifyDataSetChanged();
                break;
            case OLDEST:
                Collections.sort(filteredProducts, new Comparator<Product>() {
                    public int compare(Product p1, Product p2) {
                        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
                        Date product1Date = null;
                        Date product2Date = null;
                        try {
                            product1Date = sdf.parse(p1.getProductAddedDate());
                            product2Date = sdf.parse(p2.getProductAddedDate());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return product1Date.compareTo(product2Date);
                    }
                });
                notifyDataSetChanged();
                break;
            case PRICE_UP:
                Collections.sort(filteredProducts, new Comparator<Product>() {
                    public int compare(Product p1, Product p2) {
                        return p2.getProductPrice().compareTo(p1.getProductPrice());
                    }
                });
                notifyDataSetChanged();
                break;
            case PRICE_DOWN:
                Collections.sort(filteredProducts, new Comparator<Product>() {
                    public int compare(Product p1, Product p2) {
                        return p1.getProductPrice().compareTo(p2.getProductPrice());
                    }
                });
                notifyDataSetChanged();
                break;
        }
    }

    private class FavoriteProductsViewHolder extends RecyclerView.ViewHolder {
        LinearLayout parentLayout;
        TextView mainProductName;
        ImageView mainProductImage;
        TextView mainProducePrice;
        ImageView cart;
        ImageView favorite;

        FavoriteProductsViewHolder(View convertView) {
            super(convertView);
            this.parentLayout = (LinearLayout) convertView.findViewById(R.id.grid_view_items);
            this.mainProductName = (TextView) convertView.findViewById(R.id.productName);
            this.mainProductImage = (ImageView) convertView.findViewById(R.id.productImage);
            this.mainProducePrice = (TextView) convertView.findViewById(R.id.productPrice);
            this.cart = (ImageView) convertView.findViewById(R.id.cart);
            this.favorite = (ImageView) convertView.findViewById(R.id.favorite);
        }
    }
}

