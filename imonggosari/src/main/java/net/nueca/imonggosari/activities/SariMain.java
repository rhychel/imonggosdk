package net.nueca.imonggosari.activities;

import android.os.Bundle;
import android.support.v4.widget.SearchViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.activities.ModuleActivity;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.adapters.tools.DividerItemDecoration;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.fragments.interfaces.ListScrollListener;
import net.nueca.concessioengine.lists.SelectedProductItemList;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.concessioengine.views.SearchViewEx;
import net.nueca.imonggosari.R;
import net.nueca.imonggosari.adapters.SariProductsAdapter;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.tools.NumberTools;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SariMain extends ModuleActivity {

    private Toolbar tbActionBar;

    private TextView tvTodaySales, tvNoProducts;
    private RecyclerView rvProducts, rvSelectedProducts;

    private SariProductsAdapter selectedProductsAdapter;
    private SariProductsAdapter productsAdapter;

    private ProductsAdapterHelper adapterHelper;

    private Animation tap;

    private BigDecimal total;

    private String searchKey = "", category= "no%barcode";
    //private List<Product> filterProductsBy = new ArrayList<>();
    protected static final long LIMIT = 100l;
    protected long offset = 0l;
    private int prevLast = -1;

    protected ListScrollListener listScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sari_main2);

        tbActionBar = (Toolbar) findViewById(R.id.tbActionBar);
        setSupportActionBar(tbActionBar);
        tbActionBar.setNavigationIcon(R.drawable.ic_store_gray);

        tvTodaySales = (TextView) findViewById(R.id.tvTodaySales);
        tvNoProducts = (TextView) findViewById(R.id.tvNoProducts);

        rvProducts = (RecyclerView) findViewById(R.id.rvGridProducts);
        rvSelectedProducts = (RecyclerView) findViewById(R.id.rvListProducts);
        boolean isBigSize = getResources().getBoolean(R.bool.sari_is_big);

        tap = AnimationUtils.loadAnimation(this, R.anim.shrink);
        adapterHelper = new ProductsAdapterHelper();
        adapterHelper.setDbHelper(getHelper());

        total = BigDecimal.ZERO;
        tvTodaySales.setText(NumberTools.separateInCommas(total));

        try {
            tbActionBar.setSubtitle(getUser().getName());
            Branch branch = getHelper().getBranches().queryBuilder().where().idEq(getUser().getHome_branch_id()).queryForFirst();
            if(branch != null)
                setTitle(branch.getName());

            productsAdapter = new SariProductsAdapter(this, getHelper(), getProducts());
            productsAdapter.setIsBig(isBigSize);

            productsAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClicked(View view, int position) {
                    Product product = productsAdapter.getItem(position);
                    selectedProductsAdapter.add(product);
                    rvSelectedProducts.smoothScrollToPosition(selectedProductsAdapter.getItemCount() - 1);
                    view.startAnimation(tap);

                    SelectedProductItem selectedProductItem = adapterHelper.getSelectedProductItems().getSelectedProductItem(product);
                    if (selectedProductItem == null) {
                        selectedProductItem = new SelectedProductItem();
                        selectedProductItem.setProduct(product);
                    }

                    Values value = new Values();
                    value.setQuantity("1");
                    selectedProductItem.addValues(value);

                    total = total.add(new BigDecimal(value.getSubtotal()));
                    tvTodaySales.setText(NumberTools.separateInCommas(total));

                    adapterHelper.getSelectedProductItems().add(selectedProductItem);
                }
            });

            if(isBigSize) {
                productsAdapter.initializeGridRecyclerView(this, rvProducts, 5);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rvProducts.getLayoutParams();
                params.weight = 2f;
            }
            else {
                productsAdapter.initializeGridRecyclerView(this, rvProducts, 2);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rvProducts.getLayoutParams();
                params.weight = 2f;
            }
            rvProducts.setAdapter(productsAdapter);
            rvProducts.addOnScrollListener(rvScrollListener);


            selectedProductsAdapter = new SariProductsAdapter(this, getHelper(), new ArrayList<Product>());
            selectedProductsAdapter.setMaxElement(30);
            selectedProductsAdapter.setItemLayout(R.layout.selected_product_listitem);
            selectedProductsAdapter.initializeRecyclerView(this, rvSelectedProducts);
            selectedProductsAdapter.setListener(new SariProductsAdapter.SariProductListener() {
                @Override
                public void onDelete(Product product) {
                    SelectedProductItem selectedProductItem = adapterHelper.getSelectedProductItems().getSelectedProductItem(product);
                    Values value = selectedProductItem.getValues().get(0);

                    total = total.subtract(new BigDecimal(value.getSubtotal()));
                    tvTodaySales.setText(NumberTools.separateInCommas(total));
                }
            });

            rvSelectedProducts.setAdapter(selectedProductsAdapter);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sari_main, menu);

        mSearch = (SearchViewEx) menu.findItem(R.id.mSearch).getActionView();
        initializeSearchViewEx(new SearchViewCompat.OnQueryTextListenerCompat() {
            @Override
            public boolean onQueryTextChange(String newText) {
                updateListWhenSearch(newText);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapterHelper.destroyProductAdapterHelper();
    }

    protected List<Product> getProducts() {
        List<Product> products = new ArrayList<>();

        boolean includeSearchKey = !searchKey.equals("");
        Log.e("includeSearchKey", includeSearchKey + " -- " + searchKey);
        boolean includeCategory = (!category.toLowerCase().equals("all")/* && hasCategories*/);
        Log.e("includeCategory", includeCategory + " -- " + category);
        try {
            Where<Product, Integer> whereProducts = getHelper().getProducts().queryBuilder().where();
            whereProducts.isNull("status");
            if(includeSearchKey)
                whereProducts.and().like("searchKey", "%"+searchKey+"%");
            //if(filterProductsBy.size() > 0)
            //    whereProducts.and().in("id", filterProductsBy);
            else {
                if (includeCategory) {
                    QueryBuilder<ProductTag, Integer> productWithTag = getHelper().getProductTags().queryBuilder();
                    productWithTag.selectColumns("product_id").where().like("searchKey", "%" + category.toLowerCase() + "%");

                    whereProducts.and().in("id", productWithTag);
                }
            }

            QueryBuilder<Product, Integer> resultProducts = getHelper().getProducts().queryBuilder().orderByRaw("name COLLATE NOCASE ASC")
                    .limit(LIMIT).offset(offset);
            resultProducts.setWhere(whereProducts);

            products = resultProducts.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    protected RecyclerView.OnScrollListener rvScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (listScrollListener != null)
                    listScrollListener.onScrollStopped();
            }
            else if(newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                if(listScrollListener != null)
                    listScrollListener.onScrolling();
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int visibleItemCount = rvProducts.getChildCount();
            int totalItemCount = productsAdapter.getGridLayoutManager().getItemCount();
            int firstVisibleItem = productsAdapter.getGridLayoutManager().findFirstVisibleItemPosition();

            int lastItem = firstVisibleItem + visibleItemCount;

            if(lastItem == totalItemCount) {
                if(prevLast != lastItem) {
                    offset += LIMIT;
                    whenListEndReached(getProducts());
                    prevLast = lastItem;
                }
            }
        }
    };

    protected void whenListEndReached(List<Product> productList) {
        Log.e("whenListEndReached", "called");
        productsAdapter.addAll(productList);
    }

    public void updateListWhenSearch(String searchKey) {
        this.searchKey = searchKey;
        offset = 0l;
        prevLast = 0;

        toggleNoItems("No results for \"" + searchKey + "\".", productsAdapter.updateList(getProducts()));
    }

    protected void toggleNoItems(String msg, boolean show) {
        rvProducts.setVisibility(show ? View.VISIBLE : View.GONE);
        tvNoProducts.setVisibility(show ? View.GONE : View.VISIBLE);
        tvNoProducts.setText(msg);
    }
}
