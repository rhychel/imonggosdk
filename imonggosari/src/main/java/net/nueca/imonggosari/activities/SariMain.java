package net.nueca.imonggosari.activities;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SearchViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.generalscan.NotifyStyle;
import com.generalscan.OnConnectedListener;
import com.generalscan.OnDisconnectListener;
import com.generalscan.SendConstant;
import com.generalscan.bluetooth.BluetoothConnect;
import com.generalscan.bluetooth.BluetoothSettings;
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
import net.nueca.imonggosari.receivers.GenScanReadBroadcast;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.tools.NumberTools;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SariMain extends ModuleActivity {

    private Toolbar tbActionBar;

    private TextView tvTodaySales, tvNoProducts;
    private RecyclerView rvProducts, rvSelectedProducts;

    private EditText etSearchField;
    private ImageButton ibtnSearchClear;

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

    private GenScanReadBroadcast readBroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sari_main);

        tbActionBar = (Toolbar) findViewById(R.id.tbActionBar);
        setSupportActionBar(tbActionBar);
        //tbActionBar.setNavigationIcon(R.drawable.ic_store_gray);

        initCustomSearchView();

        tvTodaySales = (TextView) findViewById(R.id.tvTodaySales);
        tvNoProducts = (TextView) findViewById(R.id.tvNoProducts);

        rvProducts = (RecyclerView) findViewById(R.id.rvGridProducts);
        rvSelectedProducts = (RecyclerView) findViewById(R.id.rvListProducts);
        boolean isBigSize = getResources().getBoolean(R.bool.sari_is_big);

        tap = AnimationUtils.loadAnimation(this, R.anim.shrink);
        adapterHelper = new ProductsAdapterHelper();
        adapterHelper.setDbHelper(getHelper());

        total = BigDecimal.ZERO;
        tvTodaySales.setText("P"+NumberTools.separateInCommas(total));

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
                    tvTodaySales.setText("P"+NumberTools.separateInCommas(total));

                    adapterHelper.getSelectedProductItems().add(selectedProductItem);
                }
            });

            if(isBigSize) {
                productsAdapter.initializeGridRecyclerView(this, rvProducts, 5);
                //LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rvProducts.getLayoutParams();
                //params.weight = 0f;
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
                    tvTodaySales.setText("P"+NumberTools.separateInCommas(total));
                }
            });

            rvSelectedProducts.setAdapter(selectedProductsAdapter);

            // UI adjustments
            rvSelectedProducts.removeItemDecoration(selectedProductsAdapter.getDividerItemDecoration());
            LinearLayoutManager layoutManager = (LinearLayoutManager)rvSelectedProducts.getLayoutManager();
            layoutManager.setStackFromEnd(true);
            rvSelectedProducts.setLayoutManager(layoutManager);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sari_main, menu);

        /*mSearch = (SearchView) findViewById(R.id.mSearch);
        TextView tvSearchText = (TextView)findViewById(android.support.v7.appcompat.R.id.search_src_text);
        tvSearchText.setTextSize(12);

        mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                updateListWhenSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateListWhenSearch(newText);
                return true;
            }
        });*/

        /*initializeSearchViewEx(new SearchViewCompat.OnQueryTextListenerCompat() {
            @Override
            public boolean onQueryTextChange(String newText) {
                updateListWhenSearch(newText);
                return true;
            }
        });*/

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            BluetoothSettings.SetScaner(this);
            connectBarcodeScanner();
        }
        else if(id == R.id.mBarcodeScannerSet) {
            BluetoothSettings.SetScaner(this);
        }
        else if(id == R.id.mBarcodeScannerConnect) {
            Log.e("GenScan", "connecting");
            try {
                BluetoothConnect.Connect();
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SariMain.this, "Please set Barcode Scanner", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void initCustomSearchView() {
        etSearchField = (EditText) super.findViewById(R.id.etSearchField);
        ibtnSearchClear = (ImageButton) super.findViewById(R.id.ibtnSearchClear);

        etSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateListWhenSearch(""+s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateListWhenSearch(""+s);
                ibtnSearchClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.INVISIBLE);
            }
        });
        ibtnSearchClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearchField.setText("");
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null :
                        getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                etSearchField.clearFocus();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapterHelper.destroyProductAdapterHelper();
        stopGenScan();
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

            whereProducts.or().like("barcode_list", "%"+searchKey+"%");

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

    private void startGenScan() {
        BluetoothConnect.CurrentNotifyStyle = NotifyStyle.NotificationStyle1;
        BluetoothConnect.BindService(this);
        BluetoothConnect.SetOnConnectedListener(new OnConnectedListener() {
            @Override
            public void Connected() {
                Toast.makeText(SariMain.this, "Barcode Scanner connected", Toast.LENGTH_SHORT).show();
            }
        });
        BluetoothConnect.SetOnDisconnectListener(new OnDisconnectListener() {
            @Override
            public void Disconnected() {
                Toast.makeText(SariMain.this, "Barcode Scanner disconnected", Toast.LENGTH_SHORT).show();
            }
        });

        connectBarcodeScanner();
    }

    private void setBroadcast() {
        readBroadcast = new GenScanReadBroadcast(new GenScanReadBroadcast.ScannerListener() {
            @Override
            public void onGetBatteryData(String data) {

            }

            @Override
            public void onGetData(String data) {
                Log.e("DATA >>>", "^"+data+"^");

                Product product = null;
                try {
                    product = getHelper().getProducts().queryBuilder().where().isNull("status")
                            .and().like("barcode_list", "%" + data + "%").queryForFirst();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if(product == null) {
                    Toast.makeText(SariMain.this, "Product Not Found", Toast.LENGTH_SHORT).show();
                    return;
                }

                selectedProductsAdapter.add(product);
                rvSelectedProducts.smoothScrollToPosition(selectedProductsAdapter.getItemCount() - 1);

                SelectedProductItem selectedProductItem = adapterHelper.getSelectedProductItems().getSelectedProductItem(product);
                if (selectedProductItem == null) {
                    selectedProductItem = new SelectedProductItem();
                    selectedProductItem.setProduct(product);
                }

                Values value = new Values();
                value.setQuantity("1");
                selectedProductItem.addValues(value);

                total = total.add(new BigDecimal(value.getSubtotal()));
                tvTodaySales.setText("P"+NumberTools.separateInCommas(total));

                adapterHelper.getSelectedProductItems().add(selectedProductItem);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(SendConstant.GetDataAction);
        filter.addAction(SendConstant.GetReadDataAction);
        filter.addAction(SendConstant.GetBatteryDataAction);
        registerReceiver(readBroadcast, filter);
    }

    private void stopGenScan() {
        BluetoothConnect.UnBindService(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        setBroadcast();
        startGenScan();
    }

    @Override
    protected void onStop() {
        if (readBroadcast != null) {
            unregisterReceiver(readBroadcast);
        }
        super.onStop();
    }

    private void connectBarcodeScanner() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                /** Try to connect **/
                Log.e("GenScan", "connecting");
                try {
                    BluetoothConnect.Connect();
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SariMain.this, "Please set Barcode Scanner", Toast.LENGTH_SHORT).show();
                            //BluetoothSettings.SetScaner(SariMain.this);
                        }
                    });
                }
            }

        }.start();
    }
}
