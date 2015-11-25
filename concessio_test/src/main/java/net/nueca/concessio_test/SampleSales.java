package net.nueca.concessio_test;

import android.support.v4.widget.SearchViewCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.adapters.SimpleSalesProductRecyclerAdapter;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.fragments.CheckoutFragment;
import net.nueca.concessioengine.fragments.SimpleProductsFragment;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.concessioengine.views.SearchViewEx;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.tools.DialogTools;

public class SampleSales extends ModuleActivity implements SetupActionBar, View.OnClickListener {
    private static final String REVIEW_LABEL = "Review";
    private static final String SEND_LABEL = "Send";
    private static final String CHECKOUT_LABEL = "Checkout";

    private static final String CHECKOUT_TITLE = "Checkout";
    private static final String REVIEW_TITLE = "Review";

    private SimpleProductsFragment simpleProductsFragment, finalizeFragment;
    private CheckoutFragment checkoutFragment;

    private Toolbar toolbar;
    private Button btnReview;
    private MenuItem miSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_module);

        btnReview = (Button) findViewById(R.id.btnReview);
        btnReview.setOnClickListener(this);

        simpleProductsFragment = SimpleProductsFragment.newInstance();
        finalizeFragment = SimpleProductsFragment.newInstance();

        simpleProductsFragment.setHelper(getHelper());
        simpleProductsFragment.setSetupActionBar(this);

        finalizeFragment.setHelper(getHelper());
        finalizeFragment.setSetupActionBar(this);

        simpleProductsFragment.setProductsRecyclerAdapter(new SimpleSalesProductRecyclerAdapter(this, getHelper()));
        finalizeFragment.setProductsRecyclerAdapter(new SimpleSalesProductRecyclerAdapter(this, getHelper()));

        simpleProductsFragment.setHasUnits(true);
        simpleProductsFragment.setProductCategories(getProductCategories(true));

        finalizeFragment.setHasCategories(false);
        finalizeFragment.setHasBrand(false);
        finalizeFragment.setHasDeliveryDate(false);
        finalizeFragment.setHasUnits(true);

        checkoutFragment = new CheckoutFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.flContent, simpleProductsFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(btnReview != null && btnReview.getText().toString().equals(REVIEW_LABEL)) {
            getMenuInflater().inflate(R.menu.menu_sample_sales, menu);
            mSearch = (SearchViewEx) menu.findItem(R.id.mSearch).getActionView();
            initializeSearchViewEx(new SearchViewCompat.OnQueryTextListenerCompat() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    simpleProductsFragment.updateListWhenSearch(newText);
                    return true;
                }
            });


            miSearch = menu.findItem(R.id.mSearch);
        }
        miSearch.setVisible(btnReview.getText().toString().equals(REVIEW_LABEL));
        getSupportActionBar().setDisplayShowTitleEnabled(!btnReview.getText().toString().equals(REVIEW_LABEL));
        getSupportActionBar().setDisplayHomeAsUpEnabled(!btnReview.getText().toString().equals(REVIEW_LABEL));
        getSupportActionBar().setHomeButtonEnabled(!btnReview.getText().toString().equals(REVIEW_LABEL));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(btnReview.getText().toString().equals(SEND_LABEL)) {
            setTitle(REVIEW_TITLE);
            btnReview.setText(CHECKOUT_LABEL);
        }
        else if(btnReview.getText().toString().equals(CHECKOUT_LABEL))
            btnReview.setText(REVIEW_LABEL);

        btnReview.setVisibility(View.VISIBLE);

        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //getSupportActionBar().setHomeButtonEnabled(false);
        //miSearch.setVisible(true);
        invalidateOptionsMenu();
    }

    @Override
    public void setupActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        this.toolbar = toolbar;
    }

    @Override
    public void onClick(View v) {
        if(btnReview.getText().toString().equals(REVIEW_LABEL)) {
            if (ProductsAdapterHelper.getSelectedProductItems().isEmpty())
                DialogTools.showDialog(SampleSales.this, "Ooops!", "You have no selected items. Kindly select first " +
                        "products.");
            else {
                setTitle(REVIEW_TITLE);
                miSearch.setVisible(false);

                btnReview.setText(CHECKOUT_LABEL);
                finalizeFragment.setFilterProductsBy(ProductsAdapterHelper.getSelectedProductItems().getSelectedProducts());

                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                                R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.flContent, finalizeFragment, "finalize")
                        .addToBackStack("finalizer")
                        .commit();
                //getSupportActionBar().setDisplayShowTitleEnabled(true);
                //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                //getSupportActionBar().setHomeButtonEnabled(true);
            }
        } else if(btnReview.getText().toString().equals(CHECKOUT_LABEL)) {
            setTitle(CHECKOUT_TITLE);
            btnReview.setText(SEND_LABEL);

            checkoutFragment.setSetupActionBar(this);
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
        } else {
            Log.e(">>>", new Invoice.Builder().payments(checkoutFragment.getPayments()).build().toString());
        }

        invalidateOptionsMenu();
    }
}
