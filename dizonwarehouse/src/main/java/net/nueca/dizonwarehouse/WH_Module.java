package net.nueca.dizonwarehouse;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.gson.Gson;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.fragments.BaseProductsFragment;
import net.nueca.concessioengine.fragments.SimpleInventoryFragment;
import net.nueca.concessioengine.fragments.SimpleProductsFragment;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.views.SearchViewEx;
import net.nueca.dizonwarehouse.adapters.CategoryRecyclerAdapter;
import net.nueca.dizonwarehouse.dialogs.SearchDialog;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.tools.DialogTools;
import net.nueca.imonggosdk.tools.NumberTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 28/03/16.
 * dizonwarehouse (c)2016
 */
public class WH_Module extends ModuleActivity implements SearchDialog.OnSearchListener {
    public static final String BRANCH_ID = "branch_id";
    private final String CONTINUE = "CONTINUE";
    private final String SUBMIT = "SUBMIT";
    private final String RECEIVING_ORDER = "Purchase Order";
    private final String DISPATCHING_ORDER = "Sales Order";

    //private LinearLayout llFooter, llReview;
    private TextView tvTotalAmount, tvItems;
    private Button btnNext;
    private RecyclerView rvCategories;

    private SimpleInventoryFragment simpleInventoryFragment;
    private SimpleProductsFragment selectedProductFragment, finalizeFragment;
    private Branch selectedBranch;

    private Toolbar tbMain, tbRight, tbFinalize;

    private BaseProductsFragment.ProductsFragmentListener selectionListener = new BaseProductsFragment.ProductsFragmentListener() {
        @Override
        public void whenItemsSelectedUpdated() {

            if(selectedProductFragment.getProductsRecyclerAdapter() != null) {
                selectedProductFragment.forceUpdateProductList(ProductsAdapterHelper.getSelectedProductItems().getSelectedProducts());
                if(selectedProductFragment.getProductsRecyclerAdapter().getItemCount() > 0)
                ( (RecyclerView)selectedProductFragment.getView().findViewById(R.id.rvProducts) ).smoothScrollToPosition(selectedProductFragment
                        .getProductsRecyclerAdapter().getItemCount()-1);
                updateFooter();
            }
        }
    };
    private BaseProductsFragment.ProductsFragmentListener selectedListener = new BaseProductsFragment.ProductsFragmentListener() {
        @Override
        public void whenItemsSelectedUpdated() {
            if(simpleInventoryFragment.getProductsRecyclerAdapter() != null)
                simpleInventoryFragment.getProductsRecyclerAdapter().notifyDataSetChanged();
            if(btnNext.getText().toString().equals(SUBMIT) && finalizeFragment.getProductsRecyclerAdapter() != null) {
                finalizeFragment.setFilterProductsBy(ProductsAdapterHelper.getSelectedProductItems().getSelectedProducts());
                finalizeFragment.getProductsRecyclerAdapter().notifyDataSetChanged();
                if(ProductsAdapterHelper.getSelectedProductItems().size() == 0)
                    onBackPressed();
            }
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
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        //llReview = (LinearLayout) findViewById(R.id.llReview);
        //llFooter = (LinearLayout) findViewById(R.id.llFooter);
        btnNext = (Button) findViewById(R.id.btn1);
        btnNext.setText(CONTINUE);
        tvTotalAmount = (TextView) findViewById(R.id.tvTotalAmount);
        tvItems = (TextView) findViewById(R.id.tvItems);
        rvCategories = (RecyclerView) findViewById(R.id.rvCategories);

        List<String> categories = new ArrayList<>();

        showDialog();

        ProductsAdapterHelper.clearSelectedProductItemList(true);
        //changeToReview = true;
        simpleInventoryFragment = new SimpleInventoryFragment();
        simpleInventoryFragment.setHelper(getHelper());
        simpleInventoryFragment.setListingType(ListingType.SALES_GRID);
        simpleInventoryFragment.setSetupActionBar(sabSelection);
        simpleInventoryFragment.setHasUnits(true);
        simpleInventoryFragment.setProductCategories(getProductCategories(!getModuleSetting(concessioModule).getProductListing()
                .isLock_category()));
        simpleInventoryFragment.setShowCategoryOnStart(getModuleSetting(concessioModule).getProductListing().isShow_categories_on_start());
        simpleInventoryFragment.setHasSubtotal(true);
        simpleInventoryFragment.setProductsFragmentListener(selectionListener);
        simpleInventoryFragment.setHasCategories(true);
        // if there's branch product
        simpleInventoryFragment.setBranch(selectedBranch);

        initFinalizeFragment();

        initSelectedProductFragment();
        selectedProductFragment.setListingType(ListingType.ADVANCED_SALES);
        selectedProductFragment.setHasSubtotal(true);
        selectedProductFragment.setHasCategories(false);
        selectedProductFragment.setHasBrand(false);
        selectedProductFragment.setHasDeliveryDate(false);
        selectedProductFragment.setHasUnits(true);
        // if there's branch product
        selectedProductFragment.setBranch(selectedBranch);

        btnNext.setOnClickListener(nextClickedListener);

        switch (concessioModule) {
            case RECEIVE_SUPPLIER:  // Receiving
                /** Setup Bottom Category **/
                categories = getProductCategories(!getModuleSetting(concessioModule).getProductListing().isLock_category());
                categories.set(0, RECEIVING_ORDER.toUpperCase());
                /** ---------------------- **/
            break;
            case RELEASE_BRANCH:    // Dispatch
                /** Setup Bottom Category **/
                categories = getProductCategories(!getModuleSetting(concessioModule).getProductListing().isLock_category());
                categories.set(0, DISPATCHING_ORDER.toUpperCase());
                /** ---------------------- **/
            break;
        }

        CategoryRecyclerAdapter categoryAdapter = new CategoryRecyclerAdapter(this, categories);
        rvCategories.setAdapter(categoryAdapter);
        categoryAdapter.initializeRecyclerView(this, rvCategories, true);
        LinearLayoutManager layoutManager = categoryAdapter.getLinearLayoutManager();
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvCategories.setLayoutManager(layoutManager);
        categoryAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                simpleInventoryFragment.setSelectedCategory(position);
            }
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flSelectionContent, simpleInventoryFragment)
                .commit();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flReviewContent, selectedProductFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wh_module_left, menu);
        refreshTitle();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private SetupActionBar sabSelection = new SetupActionBar() {
        @Override
        public void setupActionBar(Toolbar toolbar) {
            toolbar.inflateMenu(R.menu.wh_module_right);
            toolbar.getMenu().findItem(R.id.mDelete).setVisible(concessioModule == ConcessioModule.RELEASE_BRANCH);
            SearchViewEx searchViewEx = (SearchViewEx) toolbar.getMenu().findItem(R.id.mSearch).getActionView();
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

            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return onOptionsItemSelected(item);
                }
            });
            tbRight = toolbar;
            refreshTitle();
        }
    };
    private SetupActionBar sabFinalize = new SetupActionBar() {
        @Override
        public void setupActionBar(Toolbar toolbar) {
            toolbar.inflateMenu(R.menu.wh_module_right);
            toolbar.getMenu().findItem(R.id.mDelete).setVisible(concessioModule == ConcessioModule.RELEASE_BRANCH);
            SearchViewEx searchViewEx = (SearchViewEx) toolbar.getMenu().findItem(R.id.mSearch).getActionView();
            searchViewEx.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    finalizeFragment.updateListWhenSearch(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    finalizeFragment.updateListWhenSearch(newText);
                    return false;
                }
            });

            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return onOptionsItemSelected(item);
                }
            });
            tbFinalize = toolbar;
            refreshTitle();
        }
    };

    private SetupActionBar sabReview = new SetupActionBar() {
        @Override
        public void setupActionBar(Toolbar toolbar) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });

            tbMain = toolbar;
        }
    };

    private void refreshTitle() {
        switch (concessioModule) {
            case RECEIVE_SUPPLIER:
                tbRight.setTitle("PO Number: " + reference);
                if(tbFinalize != null)
                    tbFinalize.setTitle("PO Number: " + reference);
                break;
            case RELEASE_BRANCH:
                tbRight.setTitle("SO Number: " + reference);
                if(tbFinalize != null)
                    tbFinalize.setTitle("SO Number: " + reference);
                break;
            default:
                tbRight.setTitle(reference);
                if(tbFinalize != null)
                    tbFinalize.setTitle(reference);
                break;
        }

        if(tbMain == null)
            return;

        TextView tvBranch = (TextView) findViewById(R.id.tvBranch);
        tvBranch.setText(selectedBranch != null? "| " + selectedBranch.getName() : "");

        if(btnNext.getText().toString().equals(SUBMIT)) {
            tvBranch.setVisibility(View.GONE);
            tbMain.setTitle("Review");
            return;
        }

        switch (concessioModule) {
            case RECEIVE_SUPPLIER:
                tvBranch.setVisibility(View.GONE);
                tbMain.setTitle("Receiving");
                break;
            case RELEASE_BRANCH:
                tvBranch.setVisibility(View.VISIBLE);
                tbMain.setTitle("Dispatching");
                break;
            default:
                tvBranch.setVisibility(View.GONE);
                tbMain.setTitle("");
                break;
        }
    }

    private void showDialog() {
        String title = "Enter Order Number";
        if(concessioModule == ConcessioModule.RECEIVE_SUPPLIER)
            title = "Enter " + RECEIVING_ORDER + " Number";
        else if(concessioModule == ConcessioModule.RELEASE_BRANCH)
            title = "Enter " + DISPATCHING_ORDER + " Number";

        new SearchDialog(this,R.style.WarehouseTheme_InputDialog).setTitle(title).setOnSearchListener(this).show();
    }

    @Override
    public boolean onSearch(String text) {
        reference = text;
        refreshTitle();
        try {
            Log.e("ORDERS", new Gson().toJson(Order.fetchAll(getHelper(),Order.class)));
            return getHelper().fetchObjects(Order.class).queryBuilder().where().eq("reference",text).queryForFirst() != null;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onCancel() {
        onBackPressed();
    }

    private void initSelectedProductFragment() {
        selectedProductFragment = SimpleProductsFragment.newInstance();
        selectedProductFragment.setHelper(getHelper());
        selectedProductFragment.setSetupActionBar(sabReview);
        selectedProductFragment.setIsFinalize(true);
        selectedProductFragment.setProductsFragmentListener(selectedListener);
        selectedProductFragment.setReturnItems(isReturnItems);
        selectedProductFragment.setUseSalesProductAdapter(true);
    }

    private void initFinalizeFragment() {
        finalizeFragment = SimpleProductsFragment.newInstance();
        finalizeFragment.setHelper(getHelper());
        finalizeFragment.setListingType(ListingType.SALES_GRID);
        finalizeFragment.setSetupActionBar(sabFinalize);
        finalizeFragment.setHasUnits(true);
        finalizeFragment.setHasSubtotal(true);
        finalizeFragment.setProductsFragmentListener(selectionListener);
        // if there's branch product
        finalizeFragment.setBranch(getBranches().get(0));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(btnNext.getText().toString().equals(SUBMIT)) {
            btnNext.setText(CONTINUE);
            refreshTitle();
            rvCategories.setVisibility(View.VISIBLE);
        }
    }

    private void updateFooter() {
        tvTotalAmount.setText("P"+NumberTools.separateInCommas(ProductsAdapterHelper.getSelectedProductItems().getSubtotal()));
        int count = ProductsAdapterHelper.getSelectedProductItems().size();
        tvItems.setText(count + " Item" + (count != 1? "s" : ""));
    }

    private View.OnClickListener nextClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(btnNext.getText().toString().equals(CONTINUE)) {
                if (ProductsAdapterHelper.getSelectedProductItems().isEmpty())
                    DialogTools.showDialog(WH_Module.this, "Ooops!", "You have no selected items. Kindly select first products.");
                else {
                    btnNext.setText(SUBMIT);
                    finalizeFragment.setFilterProductsBy(ProductsAdapterHelper.getSelectedProductItems().getSelectedProducts());
                    rvCategories.setVisibility(View.GONE);
                    getSupportFragmentManager().beginTransaction()
//                            .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right,
//                                    R.anim.slide_in_right, R.anim.slide_out_left)
                            .replace(R.id.flSelectionContent, finalizeFragment, "finalize")
                            .addToBackStack("finalizer")
                            .commit();
                    refreshTitle();
                }
            }
            else if(btnNext.getText().toString().equals(SUBMIT)) {

            }
        }
    };
}
