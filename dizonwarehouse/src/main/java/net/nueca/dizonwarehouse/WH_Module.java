package net.nueca.dizonwarehouse;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.fragments.BaseProductsFragment;
import net.nueca.concessioengine.fragments.SimpleInventoryFragment;
import net.nueca.concessioengine.fragments.interfaces.MultiInputListener;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.tools.OrderTools;
import net.nueca.concessioengine.views.SearchViewEx;
import net.nueca.dizonwarehouse.dialogs.SearchDialog;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.tools.NumberTools;
import net.nueca.imonggosdk.tools.SettingTools;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by gama on 28/03/16.
 * dizonwarehouse (c)2016
 */
public class WH_Module extends ModuleActivity implements SearchDialog.OnSearchListener, SetupActionBar {
    public static final String BRANCH_ID = "branch_id";
    private final String CONTINUE = "CONTINUE";
    private final String RECEIVING_ORDER = "Purchase Order";
    private final String DISPATCHING_ORDER = "Sales Order";

    private LinearLayout llFooter, llReview;
    private TextView tvTotalAmount, tvItems, tvLabel, tvValue;
    private Button btnNext;
    private Toolbar tbActionBar;

    private SimpleInventoryFragment simpleInventoryFragment;
    private Branch selectedBranch;

    private BaseProductsFragment.ProductsFragmentListener selectionListener = new BaseProductsFragment.ProductsFragmentListener() {
        @Override
        public void whenItemsSelectedUpdated() {
            updateFooter();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wh_module);

        if(getIntent().hasExtra(BRANCH_ID)) {
            try {
                selectedBranch = getHelper().fetchObjectsInt(Branch.class).queryBuilder().where()
                        .eq("id", getIntent().getIntExtra(BRANCH_ID, 0)).queryForFirst();

                if(selectedBranch == null)
                    selectedBranch = getHelper().fetchObjectsInt(Branch.class).queryBuilder().where()
                            .eq("id",SettingTools.currentBranchId(this)).queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        llReview = (LinearLayout) findViewById(R.id.llReview);
        llFooter = (LinearLayout) findViewById(R.id.llFooter);
        btnNext = (Button) findViewById(R.id.btn1);
        btnNext.setText(CONTINUE);
        tvTotalAmount = (TextView) findViewById(R.id.tvTotalAmount);
        tvItems = (TextView) findViewById(R.id.tvItems);

        showDialog();

        ProductsAdapterHelper.clearSelectedProductItemList(true);
        simpleInventoryFragment = new SimpleInventoryFragment();
        simpleInventoryFragment.setHelper(getHelper());
        simpleInventoryFragment.setListingType(ListingType.ADVANCED_SALES);
        simpleInventoryFragment.setSetupActionBar(this);
        simpleInventoryFragment.setHasUnits(true);
        simpleInventoryFragment.setProductCategories(getProductCategories(!getModuleSetting(concessioModule).getProductListing()
                .isLock_category()));
        simpleInventoryFragment.setShowCategoryOnStart(getModuleSetting(concessioModule).getProductListing().isShow_categories_on_start());
        simpleInventoryFragment.setHasSubtotal(true);
        simpleInventoryFragment.setProductsFragmentListener(selectionListener);
        simpleInventoryFragment.setHasCategories(true);
        simpleInventoryFragment.setFilterProductsBy(new ArrayList<Product>());

        simpleInventoryFragment.setMultipleInput(true);
        simpleInventoryFragment.setMultiInputListener(new MultiInputListener() {
            @Override
            public void showInputScreen(Product product) {

            }
        });
        // if there's branch product
        simpleInventoryFragment.setBranch(selectedBranch);

        btnNext.setOnClickListener(nextClickedListener);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flContent, simpleInventoryFragment)
                .commit();
    }

    private void showDialog() {
        String title = "Enter Order Number";
        if(concessioModule == ConcessioModule.RECEIVE_SUPPLIER)
            title = "Enter " + RECEIVING_ORDER + " Number";
        else if(concessioModule == ConcessioModule.RELEASE_BRANCH)
            title = "Enter " + DISPATCHING_ORDER + " Number";

        SearchDialog dialog = new SearchDialog(this,R.style.WarehouseTheme_InputDialog).setTitle(title).setOnSearchListener(this);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public boolean onSearch(String text) {
        reference = text;
        refreshTitle();
        try {
            Order order = getHelper().fetchObjects(Order.class).queryBuilder().where().eq("reference",text).queryForFirst();
            Log.e("WH_Module", order == null? "not found" : "found Order ~ " + order.toJSONString());

            if(order != null) {
                simpleInventoryFragment.setFilterProductsBy(OrderTools.generateSelectedItemList(getHelper(), order));
                simpleInventoryFragment.forceUpdateProductList();
                simpleInventoryFragment.refreshList();
            }

            return order != null;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onCancel() {
        onBackPressed();
    }

    private void updateFooter() {
        tvTotalAmount.setText("P"+NumberTools.separateInCommas(ProductsAdapterHelper.getSelectedProductItems().getSubtotal()));
        int count = ProductsAdapterHelper.getSelectedProductItems().size();
        tvItems.setText(count + " Item" + (count != 1? "s" : ""));
    }

    private View.OnClickListener nextClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(WH_Module.this, WH_Finalize.class);
            intent.putExtra(BRANCH_ID, selectedBranch.getId());
            intent.putExtra(WH_Finalize.IS_RECEIVING, concessioModule == ConcessioModule.RECEIVE_SUPPLIER);
            intent.putExtra(WH_Finalize.IS_DISPATCHING, concessioModule == ConcessioModule.RELEASE_BRANCH);
            if(concessioModule == ConcessioModule.RECEIVE_SUPPLIER)
                startActivityForResult(intent, WH_RECEIVING);
            else if(concessioModule == ConcessioModule.RELEASE_BRANCH)
                startActivityForResult(intent, WH_DISPATCHING);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wh_module_right, menu);
        menu.findItem(R.id.mDelete).setVisible(concessioModule == ConcessioModule.RELEASE_BRANCH);
        SearchViewEx searchViewEx = (SearchViewEx) menu.findItem(R.id.mSearch).getActionView();
        searchViewEx.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                simpleInventoryFragment.updateListWhenSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                simpleInventoryFragment.updateListWhenSearch(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void refreshTitle() {
        switch (concessioModule) {
            case RECEIVE_SUPPLIER:
                tvLabel.setText("PO Number: ");
                tvValue.setText(reference == null? "" : reference);
                break;
            case RELEASE_BRANCH:
                tvLabel.setText("SO Number: ");
                tvValue.setText(reference == null? "" : reference);
                break;
            default:
                tvLabel.setText("Ref No: ");
                tvValue.setText(reference == null? "" : reference);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setupActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        tbActionBar = toolbar;
        tbActionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        tvLabel = (TextView) findViewById(R.id.tvLabel);
        tvValue = (TextView) findViewById(R.id.tvValue);
        findViewById(R.id.toolbar_extension).setVisibility(View.VISIBLE);

        refreshTitle();

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WH_RECEIVING || requestCode == WH_DISPATCHING) {
            if(resultCode == SUCCESS)
                finish();
        }
    }
}
