package net.nueca.concessio;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SearchViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import net.nueca.concessioengine.activities.ModuleActivity;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.fragments.BaseProductsFragment;
import net.nueca.concessioengine.fragments.SimpleCustomersFragment;
import net.nueca.concessioengine.fragments.SimpleProductsFragment;
import net.nueca.concessioengine.fragments.interfaces.ListScrollListener;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.concessioengine.views.SearchViewEx;
import net.nueca.imonggosdk.enums.OfflineDataType;
import net.nueca.imonggosdk.interfaces.AccountListener;
import net.nueca.imonggosdk.objects.Customer;
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
public class C_Module extends ModuleActivity implements SetupActionBar {

    public SimpleProductsFragment simpleProductsFragment;
    public SimpleCustomersFragment simpleCustomersFragment;
    public SearchViewEx mSearch;
    private Button btnSample;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_fragment);

        btnSample = (Button) findViewById(R.id.btnSample);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        SwableTools.startSwable(this);

        btnSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*try {
                    User user = getHelper().getUsers().queryBuilder().where().eq("email", getSession().getEmail()).query().get(0);
                    SwableTools.sendTransaction(getHelper(), user.getHome_branch_id(), generateOrder(C_Module.this, user.getHome_branch_id()), OfflineDataType.SEND_ORDER);
                    ProductsAdapterHelper.getSelectedProductItems().clear();
                    simpleProductsFragment.refreshList();
                } catch (SQLException | JSONException e) {
                    e.printStackTrace();
                }*/
                Log.e("BUTTON", simpleCustomersFragment.getSelectedCustomers().get(0).toString());

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("BUTTON", simpleCustomersFragment.getSelectedCustomers().get(0).toString());
            }
        });

        /*simpleProductsFragment = new SimpleProductsFragment();
        simpleProductsFragment.setHelper(getHelper());
        simpleProductsFragment.setSetupActionBar(this);
        simpleProductsFragment.setProductCategories(getProductCategories(true));
        simpleProductsFragment.setListScrollListener(new ListScrollListener() {
            @Override
            public void onScrolling() {
                ViewCompat.animate(btnSample).translationY(1000.0f).setDuration(400).setInterpolator(new AccelerateDecelerateInterpolator()).start();
            }

            @Override
            public void onScrollStopped() {
                ViewCompat.animate(btnSample).translationY(0.0f).setDuration(400).setInterpolator(new AccelerateDecelerateInterpolator()).start();
            }
        });
        simpleProductsFragment.setProductsFragmentListener(new BaseProductsFragment.ProductsFragmentListener() {
            @Override
            public void whenItemsSelectedUpdated() {
                if (ProductsAdapterHelper.getSelectedProductItems().size() > 0)
                    ViewCompat.animate(btnSample).translationY(0.0f).setDuration(400).setInterpolator(new AccelerateDecelerateInterpolator()).start();
                else {
                    if (ProductsAdapterHelper.getSelectedProductItems().size() > 0)
                        ViewCompat.animate(btnSample).translationY(1000.0f).setDuration(400).setInterpolator(new AccelerateDecelerateInterpolator()).start();
                }
            }
        });*/

        try {
            if(getHelper().getCustomers().queryForAll().size() == 0) {
                String fname[] = {"John","Pepe","Sid","Mark","Jimmy","Zed","Paul","Charles","Markus"};
                String lname[] = {"Doe","Smith","Meier","Wane","Turner","Wong","Reed","Darwin","Snow"};
                for (int i = 1; i <= 100; i++) {
                    Customer customer = new Customer();
                    customer.setId(i);
                    customer.setFirst_name(fname[((int) (Math.random() * 100 % fname.length))]);
                    customer.setLast_name(lname[((int) (Math.random() * 100 % lname.length))]);
                    customer.setName(customer.getFirst_name() + " " + customer.getLast_name());
                    customer.setStreet("Unit " + (i + 400) + " DECA Corporate Center, Panganiban Drive, Barangay Tinago");
                    customer.setCity("Naga City");
                    customer.setCountry("Philippines");
                    customer.setZipcode("4400");
                    customer.setGender("M");
                    if(((int)(Math.random()*100) % 2) == 0) {
                        customer.setAlternate_code((int) (Math.random() * 100000000) + "");
                        Log.e("Customer " + i, customer.getAlternate_code());
                    }
                    customer.insertTo(getHelper());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        simpleCustomersFragment = new SimpleCustomersFragment();
        simpleCustomersFragment.setMultiSelect(true);
        simpleCustomersFragment.setHelper(getHelper());
        simpleCustomersFragment.setSetupActionBar(this);
        simpleCustomersFragment.setUseRecyclerView(true);
        simpleCustomersFragment.setListScrollListener(new ListScrollListener() {
            @Override
            public void onScrolling() {
                ViewCompat.animate(fab).translationY(1000.0f).setDuration(400)
                        .setInterpolator(new AccelerateDecelerateInterpolator()).start();
            }

            @Override
            public void onScrollStopped() {
                ViewCompat.animate(fab).translationY(0.0f).setDuration(400)
                        .setInterpolator(new AccelerateDecelerateInterpolator()).start();
            }
        });

        simpleCustomersFragment.setColor(fetchAccentColor(this));

        getSupportFragmentManager().beginTransaction()
                .add(R.id.flContent, simpleCustomersFragment)
                .commit();
    }

    private int fetchAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.simple_customers_menu, menu);
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
                    //simpleProductsFragment.updateListWhenSearch(newText);
                    simpleCustomersFragment.updateListWhenSearch(newText);
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
        if(item.getItemId() == R.id.mHistory) {
            Intent intent = new Intent(this, C_History.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.mLogout) {
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
        simpleCustomersFragment.refreshList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProductsAdapterHelper.destroyProductAdapterHelper();
        SwableTools.stopSwable(this);
    }

    @Override
    public void setupActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }
}