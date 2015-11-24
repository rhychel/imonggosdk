package net.nueca.concessio;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SearchViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.dialogs.SimplePulloutRequestDialog;
import net.nueca.concessioengine.fragments.MultiInputSelectedItemFragment;
import net.nueca.concessioengine.fragments.SimpleInventoryFragment;
import net.nueca.concessioengine.fragments.SimpleProductsFragment;
import net.nueca.concessioengine.fragments.SimplePulloutFragment;
import net.nueca.concessioengine.fragments.SimpleReceiveFragment;
import net.nueca.concessioengine.fragments.SimpleReceiveReviewFragment;
import net.nueca.concessioengine.fragments.interfaces.MultiInputListener;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.lists.ReceivedProductItemList;
import net.nueca.concessioengine.views.SearchViewEx;
import net.nueca.concessioengine.views.SimplePulloutToolbarExt;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.DocumentTypeCode;
import net.nueca.imonggosdk.enums.OfflineDataType;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Inventory;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.DialogTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by rhymart on 8/21/15.
 * imonggosdk2 (c)2015
 */
public class C_Module extends ModuleActivity implements SetupActionBar {

    private SimpleProductsFragment simpleProductsFragment, finalizeFragment;
    private Button btnSummary;

    private Toolbar toolbar;
    private boolean hasMenu = true;

    private SimpleReceiveFragment simpleReceiveFragment;
    private SimpleReceiveReviewFragment simpleReceiveReviewFragment;

    private SimpleInventoryFragment simpleInventoryFragment;

    // For the pullout module
    private SimplePulloutFragment simplePulloutFragment;
    private SimplePulloutToolbarExt simplePulloutToolbarExt;
    private SimplePulloutRequestDialog simplePulloutRequestDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.c_module);

        btnSummary = (Button) findViewById(R.id.btnSummary);

        btnSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Gson gson = new GsonBuilder().serializeNulls().create();
                Document document = generateDocument(C_Module.this, 341, DocumentTypeCode.RELEASE_SUPPLIER);
                Extras extras = new Extras();
                extras.setCustomer_id(182076);
                document.setExtras(extras);
                try {
                    JSONObject jsonObject = new JSONObject(gson.toJson(document));
                    Log.e("jsonObject", jsonObject.toString());

                    updateInventoryFromSelectedItemList(false);
                    List<Inventory> inventoryList = getHelper().fetchObjectsList(Inventory.class);
                    for(Inventory inventory : inventoryList) {
                        Log.e("Inventory", inventory.getProduct().getName()+" = "+inventory.getQuantity());
                    }

//                    new SwableTools.Transaction(getHelper())
//                            .toSend()
//                            .object(document)
//                            .forBranch(341)
//                            .queue();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }); //onClickSummary

        simpleProductsFragment = SimpleProductsFragment.newInstance();
        finalizeFragment = SimpleProductsFragment.newInstance();

        simpleProductsFragment.setHelper(getHelper());
        simpleProductsFragment.setSetupActionBar(this);

        finalizeFragment.setHelper(getHelper());
        finalizeFragment.setSetupActionBar(this);
        switch (concessioModule) {
            case ORDERS: {
                simpleProductsFragment.setHasUnits(true);
                simpleProductsFragment.setProductCategories(getProductCategories(!getModuleSetting().getProductListing().isLock_category()));
                simpleProductsFragment.setShowCategoryOnStart(getModuleSetting().getProductListing().isShow_categories_on_start());

                finalizeFragment.setHasCategories(false);
                finalizeFragment.setHasBrand(false);
                finalizeFragment.setHasDeliveryDate(false);
                finalizeFragment.setHasUnits(true);

                btnSummary.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.flContent, simpleProductsFragment)
                        .commit();
            }
            break;
            case PHYSICAL_COUNT: {
                simpleProductsFragment.setProductCategories(getProductCategories(true));
                simpleProductsFragment.setMultipleInput(true);
                simpleProductsFragment.setMultiInputListener(multiInputListener);

                finalizeFragment.setHasCategories(false);
                finalizeFragment.setMultipleInput(true);
                finalizeFragment.setMultiInputListener(multiInputListener);

                btnSummary.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction()
                    .add(R.id.flContent, simpleProductsFragment)
                    .commit();
            }
            break;
            case RECEIVE: {
                simpleReceiveFragment = new SimpleReceiveFragment();
                simpleReceiveFragment.setHelper(getHelper());
                simpleReceiveFragment.setSetupActionBar(this);
                simpleReceiveFragment.setUseRecyclerView(false);
                simpleReceiveFragment.setFragmentContainer(R.id.flContent);
                simpleReceiveFragment.setProductCategories(getProductCategories(true));
                simpleReceiveFragment.setFABListener(new SimpleReceiveFragment.FloatingActionButtonListener() {
                    @Override
                    public void onClick(ReceivedProductItemList receivedProductItemList, Branch targetBranch,
                                        String reference, Integer parentDocumentID) {
                        SimpleReceiveReviewFragment simpleReceiveReviewFragment = new SimpleReceiveReviewFragment();
                        simpleReceiveReviewFragment.setParentID(parentDocumentID);
                        simpleReceiveReviewFragment.setTargetBranch(targetBranch);
                        simpleReceiveReviewFragment.setDRNo(reference);
                        simpleReceiveReviewFragment.setUseRecyclerView(true);
                        simpleReceiveReviewFragment.setHelper(getHelper());
                        simpleReceiveReviewFragment.setFragmentContainer(R.id.flContent);
                        simpleReceiveReviewFragment.setReceivedProductItemList(receivedProductItemList);
                        simpleReceiveReviewFragment.setIsManual(simpleReceiveFragment.isManual());

                        getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                                        R.anim.slide_in_left, R.anim.slide_out_right)
                                .replace(R.id.flContent, simpleReceiveReviewFragment)
                                .addToBackStack("review_fragment")
                                .commit();
                    }
                });
                btnSummary.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContent, simpleReceiveFragment)
                        .commit();
            }
            break;
            case INVENTORY: {
                if(getModuleSetting() != null)
                    Log.e("moduleSetting", "Yeah");
                simpleInventoryFragment = new SimpleInventoryFragment();
                simpleInventoryFragment.setHelper(getHelper());
                simpleInventoryFragment.setSetupActionBar(this);
                simpleInventoryFragment.setHasUnits(true);
                simpleInventoryFragment.setProductCategories(getProductCategories(!getModuleSetting().getProductListing().isLock_category()));
                simpleInventoryFragment.setShowCategoryOnStart(getModuleSetting().getProductListing().isShow_categories_on_start());

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContent, simpleInventoryFragment)
                        .commit();
            } break;
            case PULLOUT_REQUEST: {
                simplePulloutRequestDialog = new SimplePulloutRequestDialog(this, getHelper());
                simplePulloutRequestDialog.setTitle("Choose a reason");
                simplePulloutRequestDialog.setListener(new SimplePulloutRequestDialog.PulloutRequestDialogListener() {
                    @Override
                    public void onSave(String reason, Branch source, Branch destination) {
                        Log.e("Reason", reason);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                if(getModuleSetting().isRequire_document_reason())
                    simplePulloutRequestDialog.show();

                simplePulloutFragment = new SimplePulloutFragment();
                simplePulloutFragment.setHelper(getHelper());
                simplePulloutFragment.setSetupActionBar(this);
                simplePulloutFragment.setHasUnits(true);
                simplePulloutFragment.setProductCategories(getProductCategories(!getModuleSetting().getProductListing().isLock_category()));
                simplePulloutFragment.setShowCategoryOnStart(getModuleSetting().getProductListing().isShow_categories_on_start());

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContent, simplePulloutFragment)
                        .commit();
            } break;
        }

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                hasMenu = false;
                btnSummary.setText("Send");
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        btnSummary.setText("Summary");
                        hasMenu = true;
                        onBackPressed();
                        getSupportActionBar().invalidateOptionsMenu();
                    }
                });
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                getSupportActionBar().setTitle("Finalize");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().invalidateOptionsMenu();
            }
        });

//        getSupportFragmentManager().beginTransaction()
//                .add(R.id.flContent, simpleProductsFragment)
//                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (concessioModule == ConcessioModule.PHYSICAL_COUNT) {
            simpleProductsFragment.refreshList();
            if (getSupportFragmentManager().findFragmentByTag("finalize") != null)
                finalizeFragment.refreshList();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        btnSummary.setText("Summary");

        if(getModuleSetting().isRequire_document_reason()) {
            if (concessioModule == ConcessioModule.PULLOUT_REQUEST) {
                if (simplePulloutToolbarExt != null)
                    simplePulloutToolbarExt.attachAfter(this, toolbar);
            } else {
                if (simplePulloutToolbarExt != null)
                    simplePulloutToolbarExt.detach();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (hasMenu) {
            getMenuInflater().inflate(R.menu.simple_products_menu, menu);
            menu.findItem(R.id.mHistory).setVisible(false);
            menu.findItem(R.id.mLogout).setVisible(false);

            mSearch = (SearchViewEx) menu.findItem(R.id.mSearch).getActionView();
            initializeSearchViewEx(new SearchViewCompat.OnQueryTextListenerCompat() {
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
    public void setupActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        this.toolbar = toolbar;
        if(getModuleSetting().isRequire_document_reason()) {
            if (concessioModule == ConcessioModule.PULLOUT_REQUEST) {
                if (simplePulloutToolbarExt == null)
                    simplePulloutToolbarExt = new SimplePulloutToolbarExt();
                simplePulloutToolbarExt.attachAfter(this, this.toolbar);
                simplePulloutToolbarExt.setOnClickListener(new SimplePulloutToolbarExt.OnToolbarClickedListener() {
                    @Override
                    public void onClick() {
                        simplePulloutRequestDialog.show();
                    }
                });
            } else {
                if (simplePulloutToolbarExt != null)
                    simplePulloutToolbarExt.detach();
            }
        }
    }

    private MultiInputListener multiInputListener = new MultiInputListener() {
        @Override
        public void showInputScreen(Product product) {
            Intent intent = new Intent(C_Module.this, C_MultiInput.class);
            intent.putExtra(MultiInputSelectedItemFragment.PRODUCT_ID, product.getId());
            startActivity(intent);
        }
    };

    private View.OnClickListener onClickSummary = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (btnSummary.getText().equals("Send")) {
                DialogTools.showSelectionDialog(C_Module.this,
                        new ArrayAdapter<>(C_Module.this, android.R.layout.simple_list_item_single_choice, getBranches()),
                        "Yes", new DialogTools.OnItemSelected<Branch>() {
                            @Override
                            public void itemChosen(final Branch branch) {
                                final Branch warehouse = getWarehouse();
                                if (warehouse == null)
                                    DialogTools.showDialog(C_Module.this, "Ooops!", "You have no warehouse. Kindly contact your admin.");
                                else {
                                    DialogTools.showConfirmationDialog(C_Module.this, "Send", "Are you sure?", "Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            switch (concessioModule) {
                                                case ORDERS: {
                                                    try {
                                                        SwableTools.sendTransaction(getHelper(), branch.getId(),
                                                                generateOrder(C_Module.this, warehouse.getId()), OfflineDataType.SEND_ORDER);
                                                        onBackPressed();
                                                        ProductsAdapterHelper.clearSelectedProductItemList();
                                                        simpleProductsFragment.refreshList();
                                                    } catch (SQLException | JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                break;
                                                case PHYSICAL_COUNT: {
                                                    try {
                                                        OfflineData offlineData = SwableTools.sendTransaction(getHelper(), branch.getId(),
                                                                generateDocument(C_Module.this), OfflineDataType.SEND_DOCUMENT);

                                                        Log.e("PCount", offlineData.getObjectFromData().toString());
                                                        onBackPressed();
                                                        ProductsAdapterHelper.clearSelectedProductItemList();
                                                        simpleProductsFragment.refreshList();
                                                    } catch (SQLException | JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    }, "No");
                                }
                            }
                        }, "No");
            } else {
                if (ProductsAdapterHelper.getSelectedProductItems().isEmpty())
                    DialogTools.showDialog(C_Module.this, "Ooops!", "You have no selected items. Kindly select first products.");
                else {
                    btnSummary.setText("Send");
                    finalizeFragment.setFilterProductsBy(ProductsAdapterHelper.getSelectedProductItems().getSelectedProducts());
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                            .add(R.id.flContent, finalizeFragment, "finalize")
                            .addToBackStack("finalizer")
                            .commit();
                }
            }
        }
    };
}
