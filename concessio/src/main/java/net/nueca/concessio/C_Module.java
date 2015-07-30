package net.nueca.concessio;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SearchViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;

import net.nueca.concessioengine.activities.ModuleActivity;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.fragments.BaseProductsFragment;
import net.nueca.concessioengine.fragments.SimpleProductsFragment;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.concessioengine.views.SearchViewEx;
import net.nueca.imonggosdk.enums.OfflineDataType;
import net.nueca.imonggosdk.interfaces.AccountListener;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.objects.order.OrderLine;
import net.nueca.imonggosdk.swable.ImonggoSwable;
import net.nueca.imonggosdk.swable.ImonggoSwableServiceConnection;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.AccountTools;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 6/4/15.
 * imonggosdk (c)2015
 */
public class C_Module extends ModuleActivity implements BaseProductsFragment.SetupActionBar {

    public SimpleProductsFragment simpleProductsFragment;
    public SearchViewEx mSearch;
    private Button btnSample;

    private ImonggoSwableServiceConnection swableConnection = new ImonggoSwableServiceConnection(new ImonggoSwable.SwableStateListener() {
        @Override
        public void onSwableStarted() {
            Log.e("C_Module", "onSwableStarted");
        }

        @Override
        public void onQueued(OfflineData offlineData) {
            Log.e("C_Module", "onQueued -- "+offlineData.getData());
        }

        @Override
        public void onSyncing(OfflineData offlineData) {
            Log.e("C_Module", "onSyncing -- "+offlineData.getData());
        }

        @Override
        public void onSynced(OfflineData offlineData) {
            Log.e("C_Module", "onSynced -- "+offlineData.getData());
        }

        @Override
        public void onSyncProblem(OfflineData offlineData, boolean hasInternet, Object response, int responseCode) {
            Log.e("C_Module", "onQueued -- "+offlineData.getData()+" | "+hasInternet+" | "+responseCode);
        }

        @Override
        public void onUnauthorizedAccess(Object response, int responseCode) {
            Log.e("C_Module", "onUnauthorizedAccess -- "+responseCode);
        }

        @Override
        public void onAlreadyCancelled(OfflineData offlineData) {
            Log.e("C_Module", "onAlreadyCancelled -- "+offlineData.getData());
        }

        @Override
        public void onSwableStopping() {
            Log.e("C_Module", "onSwableStopping");
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_fragment);

        btnSample = (Button) findViewById(R.id.btnSample);
        SwableTools.startAndBindSwable(this, swableConnection);
        btnSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Order.Builder order = new Order.Builder();
                List<OrderLine> orderLines = new ArrayList<>();
                for(int i = 0;i < ProductsAdapterHelper.getSelectedProductItems().size();i++) {
                    SelectedProductItem selectedProductItem = ProductsAdapterHelper.getSelectedProductItems().get(i);
                    Values value = selectedProductItem.getValues().get(0);

                    OrderLine orderLine = new OrderLine.Builder()
                            .line_no(value.getLine_no())
                            .product_id(selectedProductItem.getProduct().getId())
                            .quantity(Double.valueOf(value.getQuantity()))
                            .build();
                    orderLines.add(orderLine);
                }
                order.order_lines(orderLines);
                order.order_type_code("stock_request");
                try {
                    order.serving_branch_id(getSession().getUser().getHome_branch_id());
                    order.generateReference(C_Module.this, getSession().getDevice_id());
                    SwableTools.sendTransaction(getHelper(),
                            getSession().getUser().getId(),
                            order.build(),
                            OfflineDataType.SEND_ORDER);
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        simpleProductsFragment = new SimpleProductsFragment();
        simpleProductsFragment.setHelper(getHelper());
        simpleProductsFragment.setSetupActionBar(this);
        simpleProductsFragment.setProductCategories(getProductCategories(true));
        simpleProductsFragment.setListScrollListener(new BaseProductsFragment.ListScrollListener() {
            @Override
            public void onScrolling() {
                ViewCompat.animate(btnSample).translationY(1000.0f).setDuration(400).setInterpolator(new AccelerateDecelerateInterpolator()).start();
            }

            @Override
            public void onScrollStopped() {
                if (ProductsAdapterHelper.getSelectedProductItems().size() > 0)
                    ViewCompat.animate(btnSample).translationY(0.0f).setDuration(400).setInterpolator(new AccelerateDecelerateInterpolator()).start();
            }
        });
        simpleProductsFragment.setProductsFragmentListener(new BaseProductsFragment.ProductsFragmentListener() {
            @Override
            public void whenItemsSelectedUpdated() {
                if (ProductsAdapterHelper.getSelectedProductItems().size() > 0)
                    ViewCompat.animate(btnSample).translationY(0.0f).setDuration(400).setInterpolator(new AccelerateDecelerateInterpolator()).start();
                else
                    ViewCompat.animate(btnSample).translationY(1000.0f).setDuration(400).setInterpolator(new AccelerateDecelerateInterpolator()).start();
            }
        });
        getSupportFragmentManager().beginTransaction()
                .add(R.id.flContent, simpleProductsFragment)
            .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.simple_products_menu, menu);
        mSearch = (SearchViewEx) menu.findItem(R.id.mSearch).getActionView();

        if(mSearch != null) {
            mSearch.setSearchViewExListener(new SearchViewEx.SearchViewExListener() {
                @Override
                public void whenBackPressed() {
                    if(!mSearch.isIconified())
                        mSearch.setIconified(true);
                }
            });
            mSearch.setIconifiedByDefault(true);
            SearchViewCompat.setOnQueryTextListener(mSearch, new SearchViewCompat.OnQueryTextListenerCompat() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    simpleProductsFragment.updateListWhenSearch(newText);
                    return true;
                }

            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if(mSearch != null) {
            if(!SearchViewCompat.isIconified(mSearch))
                closeSearchField(mSearch);
            else
                super.onBackPressed();
        }
        else
            super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.mLogout) {
            AlertDialog.Builder logout = new AlertDialog.Builder(this);
            logout.setMessage("Logout account?");
            logout.setTitle("Logout");
            logout.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        AccountTools.unlinkAccount(C_Module.this, getHelper(), new AccountListener() {
                            @Override
                            public void onLogoutAccount() {

                            }

                            @Override
                            public void onUnlinkAccount() {
                                Intent intent = new Intent(C_Module.this, C_SampleLogin.class);
                                finish();
                                startActivity(intent);
                            }
                        });
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
            logout.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            logout.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        simpleProductsFragment.refreshList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProductsAdapterHelper.destroyProductAdapterHelper();
        SwableTools.stopAndUnbindSwable(this, swableConnection);
    }

    @Override
    public void setupActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }
}
