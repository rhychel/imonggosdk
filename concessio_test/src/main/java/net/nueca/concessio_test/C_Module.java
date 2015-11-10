package net.nueca.concessio_test;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SearchViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import net.nueca.concessioengine.activities.ModuleActivity;
import net.nueca.concessioengine.adapters.SimpleSalesProductRecyclerAdapter;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.dialogs.SimplePulloutRequestDialog;
import net.nueca.concessioengine.fragments.CheckoutFragment;
import net.nueca.concessioengine.fragments.MultiInputSelectedItemFragment;
import net.nueca.concessioengine.fragments.SimpleCustomersFragment;
import net.nueca.concessioengine.fragments.SimpleProductsFragment;
import net.nueca.concessioengine.fragments.SimpleReceiveFragment;
import net.nueca.concessioengine.fragments.SimpleReceiveReviewFragment;
import net.nueca.concessioengine.fragments.interfaces.MultiInputListener;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.lists.ReceivedProductItemList;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.concessioengine.views.SearchViewEx;
import net.nueca.concessioengine.views.SimplePulloutToolbarExt;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.DocumentTypeCode;
import net.nueca.imonggosdk.enums.OfflineDataType;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.DialogTools;

import org.json.JSONException;

import java.sql.SQLException;

/**
 * Created by rhymart on 8/21/15.
 * imonggosdk2 (c)2015
 */
public class C_Module extends ModuleActivity implements SetupActionBar {

    private SimpleProductsFragment simpleProductsFragment, finalizeFragment;
    private Button btnReview;

    private Toolbar toolbar;
    private boolean hasMenu = true;

    private SimpleReceiveFragment simpleReceiveFragment;
    private SimpleReceiveReviewFragment simpleReceiveReviewFragment;

    private SimplePulloutToolbarExt simplePulloutToolbarExt;
    private SimplePulloutRequestDialog simplePulloutRequestDialog;

    private CheckoutFragment checkoutFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SwableTools.startSwable(this);

        setContentView(R.layout.c_module);

        btnReview = (Button) findViewById(R.id.btnReview);

        btnReview.setOnClickListener(onClickContinue);

        /**  destroy selected items  **/
        ProductsAdapterHelper.clearSelectedProductItemList();

        simpleProductsFragment = SimpleProductsFragment.newInstance();
        finalizeFragment = SimpleProductsFragment.newInstance();

        simpleProductsFragment.setHelper(getHelper());
        simpleProductsFragment.setSetupActionBar(this);

        finalizeFragment.setHelper(getHelper());
        finalizeFragment.setSetupActionBar(this);

        simpleProductsFragment.setProductsRecyclerAdapter(null);
        finalizeFragment.setProductsRecyclerAdapter(null);

        switch (concessioModule) {
            case SALES: {
                simpleProductsFragment.setProductsRecyclerAdapter(new SimpleSalesProductRecyclerAdapter(this));
                finalizeFragment.setProductsRecyclerAdapter(new SimpleSalesProductRecyclerAdapter(this));
            }
            case ORDERS: {
                simpleProductsFragment.setHasUnits(true);
                simpleProductsFragment.setProductCategories(getProductCategories(true));

                finalizeFragment.setHasCategories(false);
                finalizeFragment.setHasBrand(false);
                finalizeFragment.setHasDeliveryDate(false);
                finalizeFragment.setHasUnits(true);

                btnReview.setVisibility(View.VISIBLE);
            } break;
            case PHYSICAL_COUNT: {
                simpleProductsFragment.setProductCategories(getProductCategories(true));
                simpleProductsFragment.setMultipleInput(true);
                simpleProductsFragment.setMultiInputListener(multiInputListener);

                finalizeFragment.setHasCategories(false);
                finalizeFragment.setMultipleInput(true);
                finalizeFragment.setMultiInputListener(multiInputListener);

                btnReview.setVisibility(View.VISIBLE);
            } break;
            case PULLOUT: {
                simplePulloutRequestDialog = new SimplePulloutRequestDialog(this, getHelper());
                simplePulloutRequestDialog.setListener(new SimplePulloutRequestDialog.PulloutRequestDialogListener() {
                    @Override
                    public void onSave(String reason, Branch source, Branch destination) {
                        TextView tvReason = (TextView)simplePulloutToolbarExt.getToolbarExtensionView()
                                .findViewById(R.id.tvReason);

                        if(reason.toLowerCase().equals("transfer to branch"))
                            tvReason.setText(reason + " " + destination.getName());
                        else
                            tvReason.setText(reason);
                    }

                    @Override
                    public void onCancel() {
                        if(simplePulloutToolbarExt == null || ((TextView)simplePulloutToolbarExt
                                .getToolbarExtensionView().findViewById(R.id.tvReason) ).getText().length() == 0)
                            onBackPressed();
                    }
                });
                simplePulloutRequestDialog.show();
                simpleProductsFragment.setHasUnits(true);
                simpleProductsFragment.setProductCategories(getProductCategories(true));

                finalizeFragment.setHasCategories(false);
                finalizeFragment.setHasBrand(false);
                finalizeFragment.setHasDeliveryDate(false);
                finalizeFragment.setHasUnits(true);

                btnReview.setVisibility(View.VISIBLE);
            } break;
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
                        simpleReceiveReviewFragment = new SimpleReceiveReviewFragment();
                        simpleReceiveReviewFragment.setParentID(parentDocumentID);
                        simpleReceiveReviewFragment.setTargetBranch(targetBranch);
                        simpleReceiveReviewFragment.setDRNo(reference);
                        simpleReceiveReviewFragment.setUseRecyclerView(true);
                        simpleReceiveReviewFragment.setHelper(getHelper());
                        simpleReceiveReviewFragment.setFragmentContainer(R.id.flContent);
                        simpleReceiveReviewFragment.setReceivedProductItemList(receivedProductItemList);
                        simpleReceiveReviewFragment.setIsManual(simpleReceiveFragment.isManual());
                        simpleReceiveReviewFragment.setFABListener(new SimpleReceiveReviewFragment.FloatingActionButtonListener() {
                            @Override
                            public void onClick(Document document) {
                                try {
                                    SwableTools.sendTransaction(getHelper(), document.getTarget_branch_id(),
                                            document, OfflineDataType.SEND_DOCUMENT);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                                        R.anim.slide_in_left, R.anim.slide_out_right)
                                .replace(R.id.flContent, simpleReceiveReviewFragment)
                                .addToBackStack("review_fragment")
                                .commit();
                    }
                });
                btnReview.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContent, simpleReceiveFragment)
                        .commit();
                return;
            }
        }

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                hasMenu = false;
                invalidateOptionsMenu();
            }
        });

        getSupportFragmentManager().beginTransaction()
                .add(R.id.flContent, simpleProductsFragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(concessioModule == ConcessioModule.PHYSICAL_COUNT) {
            simpleProductsFragment.refreshList();
            if(getSupportFragmentManager().findFragmentByTag("review") != null)
               finalizeFragment.refreshList();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hasMenu = true;
        if(!btnReview.getText().toString().equals("Review")) {
            if(concessioModule == ConcessioModule.SALES && btnReview.getText().toString().equals("Send"))
                btnReview.setText("Checkout");
            else
                btnReview.setText("Review");
        }

        if(concessioModule == ConcessioModule.PULLOUT && btnReview.getText().equals("Review")) {
            if(simplePulloutToolbarExt != null)
                simplePulloutToolbarExt.attachAfter(this, toolbar);
        } else {
            if(simplePulloutToolbarExt != null)
                simplePulloutToolbarExt.detach();
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!btnReview.getText().toString().equals("Review")) {
            setTitle("Review");
            if(concessioModule == ConcessioModule.SALES && btnReview.getText().toString().equals("Send"))
                setTitle("Checkout");
        }

        if(!btnReview.getText().toString().equals("Review")) {
            if(concessioModule == ConcessioModule.SALES && getTitle().toString().equals("Checkout"))
                getMenuInflater().inflate(R.menu.simple_checkout_menu, menu);
            else
                getMenuInflater().inflate(R.menu.simple_review_products_menu, menu);
        }
        else {
            if(hasMenu) {
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
        }

        getSupportActionBar().setDisplayShowTitleEnabled(!btnReview.getText().equals("Review"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(!btnReview.getText().equals("Review"));
        getSupportActionBar().setHomeButtonEnabled(!btnReview.getText().equals("Review"));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            onBackPressed();
        } else if(id == R.id.mClear) {
            DialogTools.showConfirmationDialog(this, "Delete All", "Are you sure you want to delete all selected items?",
                    "Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finalizeFragment.clearSelectedItems();
                }
            }, "No");
        } else if(id == R.id.mCustomer) {

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setupActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        this.toolbar = toolbar;

        if(concessioModule == ConcessioModule.PULLOUT && btnReview.getText().equals("Review")) {
            if(simplePulloutToolbarExt == null)
                simplePulloutToolbarExt = new SimplePulloutToolbarExt();
            simplePulloutToolbarExt.attachAfter(this, this.toolbar);
            simplePulloutToolbarExt.setOnClickListener(new SimplePulloutToolbarExt.OnToolbarClickedListener() {
                @Override
                public void onClick() {
                    simplePulloutRequestDialog.show();
                }
            });
        } else {
            if(simplePulloutToolbarExt != null)
                simplePulloutToolbarExt.detach();
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

    private View.OnClickListener onClickContinue = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(btnReview.getText().equals("Send")) {
                DialogTools.showSelectionDialog(C_Module.this,
                        new ArrayAdapter<>(C_Module.this, android.R.layout.simple_list_item_single_choice, getBranches()),
                        "Yes", new DialogTools.OnItemSelected<Branch>() {
                            @Override
                            public void itemChosen(final Branch branch) {
                                final Branch warehouse = getWarehouse();
                                if(warehouse == null)
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

                                                        Log.e("PCount", offlineData.getData().toString());
                                                        onBackPressed();
                                                        ProductsAdapterHelper.clearSelectedProductItemList();
                                                        simpleProductsFragment.refreshList();
                                                    } catch (SQLException | JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                break;
                                                case PULLOUT: {
                                                    try {
                                                        Document pulloutDoc = generateDocument(C_Module.this,
                                                                simplePulloutRequestDialog
                                                                        .getSelectedDestinationBranch().getId(),
                                                                DocumentTypeCode.RELEASE_BRANCH);
                                                        pulloutDoc.setDocument_purpose_name
                                                                (simplePulloutRequestDialog.getSelectedReason().getName());
                                                        OfflineData offlineData = SwableTools.sendTransaction(getHelper(),
                                                                simplePulloutRequestDialog.getSelectedSourceBranch()
                                                                        .getId(),
                                                                pulloutDoc,
                                                                OfflineDataType.SEND_DOCUMENT);

                                                        Log.e("PULLOUT", offlineData.getData().toString());
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
            } else if(btnReview.getText().toString().equals("Checkout")) {
                if(ProductsAdapterHelper.hasSelectedProductItems()) {
                    DialogTools.showDialog(C_Module.this, "Ooops!", "You haven't selected anything.");
                } else {
                    btnReview.setText("Send");

                    checkoutFragment = new CheckoutFragment();
                    checkoutFragment.setSetupActionBar(C_Module.this);
                    checkoutFragment.setInvoice(new Invoice.Builder()
                            .invoice_lines(InvoiceTools.generateInvoiceLines(ProductsAdapterHelper
                                    .getSelectedProductItems()))
                            .build());
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                                    R.anim.slide_in_left, R.anim.slide_out_right)
                            .replace(R.id.flContent, checkoutFragment, "checkout")
                            .addToBackStack("checkout")
                            .commit();
                }
            } else {
                if (ProductsAdapterHelper.getSelectedProductItems().isEmpty())
                    DialogTools.showDialog(C_Module.this, "Ooops!", "You haven't selected anything.");
                else {
                    if (concessioModule == ConcessioModule.SALES)
                        btnReview.setText("Checkout");
                    else
                        btnReview.setText("Send");
                    finalizeFragment.setFilterProductsBy(ProductsAdapterHelper.getSelectedProductItems().getSelectedProducts());
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                                    R.anim.slide_in_left, R.anim.slide_out_right)
                            .replace(R.id.flContent, finalizeFragment, "review")
                            .addToBackStack("finalizer")
                            .commit();
                }
            }
        }
    };
}