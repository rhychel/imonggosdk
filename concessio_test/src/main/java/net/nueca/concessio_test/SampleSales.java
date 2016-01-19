package net.nueca.concessio_test;

import android.location.Criteria;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.adapters.SimpleSalesProductRecyclerAdapter;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.fragments.SimpleCheckoutFragment;
import net.nueca.concessioengine.fragments.SimpleCustomersFragment;
import net.nueca.concessioengine.fragments.SimpleProductsFragment;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.concessioengine.tools.LocationTools;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.associatives.CustomerCustomerGroupAssoc;
import net.nueca.imonggosdk.objects.base.Extras;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.DialogTools;
import net.nueca.concessioengine.tools.DiscountTools;
import net.nueca.imonggosdk.tools.NumberTools;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class SampleSales extends ModuleActivity implements SetupActionBar, View.OnClickListener {
    private static final String REVIEW_LABEL = "Review";
    private static final String SEND_LABEL = "Send";
    private static final String CHECKOUT_LABEL = "Checkout";
    private static final String CUSTOMER_LABEL = "Begin";

    private static final String CHECKOUT_TITLE = "Checkout";
    private static final String REVIEW_TITLE = "Review";
    private static final String CUSTOMER_TITLE = "Select Customer";

    private SimpleProductsFragment simpleProductsFragment, finalizeFragment;
    private SimpleCheckoutFragment simpleCheckoutFragment;
    private SimpleCustomersFragment customersFragment;

    private SimpleSalesProductRecyclerAdapter salesAdapter;

    private Toolbar toolbar;
    private Button btnReview;
    private MenuItem miSearch;

    private Customer selectedCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("SAMPLESALES", "onCreate");
        super.onCreate(savedInstanceState);


        /*try {
            Branch branch = getHelper().fetchObjects(Branch.class).queryBuilder().where().eq("id", getSession().getCurrent_branch_id()).queryForFirst();
            List<Product> products = getHelper().fetchObjects(Product.class).queryBuilder().where().like("name", "AC%").query();
            List<PriceList> priceLists = getHelper().fetchObjects(PriceList.class).queryForAll();
            while(priceLists.size() >= 1) {
                priceLists.get(0).deleteTo(getHelper());
                priceLists.remove(0);
            }

            priceLists = getHelper().fetchObjects(PriceList.class).queryForAll();
            while(priceLists.size() < 2) {
                PriceList.Builder builder = new PriceList.Builder();
                builder.branch(branch);

                PriceList priceList = builder.build();
                priceList.setId(priceLists.size() + 1);
                priceList.insertTo(getHelper());
                for(int i=0; i<2;i++) {
                    Price price = new Price();
                    price.setId(getHelper().fetchObjects(Price.class).queryForAll().size()+1);
                    price.setPriceList(priceList);
                    price.setProduct(products.get(((int)(Math.random() * 1000) )%products.size()));
                    price.setRetail_price( (((int)(Math.random() * 1000000) ) % 1000)/100 );
                    price.insertTo(getHelper());
                }
                priceList.updateTo(getHelper());
                priceLists = getHelper().fetchObjects(PriceList.class).queryForAll();
            }
            Log.e("PRICE_LIST", priceLists.size() + "");
            for(PriceList priceList : priceLists) {
                Log.e("###", priceList.getBranch() + (priceList.getCustomers() != null && priceList.getCustomers().size() > 0? " - " + new ArrayList<>
                        (priceList.getCustomers()).get(0).getName() : ""));
                for(Price price : priceList.getPrices())
                    Log.e("######", price.toString());
            }


            selectedCustomer = getHelper().fetchObjects(Customer.class).queryForAll().get(1);
            Log.e("CUSTOMER", selectedCustomer.toString());
            selectedCustomer.setPriceList(priceLists.get(0));
            selectedCustomer.updateTo(getHelper());
            priceLists.get(0).updateTo(getHelper());

        } catch (SQLException e) {
            e.printStackTrace();
        }*/

        Log.e("SAMPLE SALES", "V V V V V V V V V V V V V V V V V V V V V V V V V");
        Log.e("TEST Discount", "" + DiscountTools.applyMultipleDiscounts(new BigDecimal("100"), new BigDecimal("1"), "5%,90,20%", ","));
        try {
            //List<CustomerCustomerGroupAssoc> assocs = getHelper().fetchObjectsList(CustomerCustomerGroupAssoc.class);
            //for(CustomerCustomerGroupAssoc assoc : assocs)
            //    Log.e("#######", assoc.toString());

            //List<CustomerGroup> customerGroups = getHelper().fetchObjectsList(CustomerGroup.class);
            //for(CustomerGroup customerGroup : customerGroups)
            //    Log.e(">>>>>>>", customerGroup.toString());

            List<Price> prices = getHelper().fetchObjectsList(Price.class);
            for(Price price : prices)
                Log.e("@@@@@@@", price.toString());

            /*List<Product> t_products = getHelper().fetchObjectsList(Product.class).subList(0,2);
            for(Product product : t_products)
                Log.e("@@@@@@@ !!", product.toString());
            List<Product> products = getHelper().fetchObjects(Product.class).queryBuilder().where().eq("id", t_products.get(0).getId()).query();
            for(Product product : products)
                Log.e("@@@@@@@ **", product.toString());*/
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //new SwableTools.Transaction(getHelper()).toUpdate().queue();
        Log.e("SAMPLE SALES", "Λ Λ Λ Λ Λ Λ Λ Λ Λ Λ Λ Λ Λ Λ Λ Λ Λ Λ Λ Λ Λ Λ Λ Λ Λ");

        setContentView(R.layout.c_module);

        btnReview = (Button) findViewById(R.id.btnReview);
        btnReview.setOnClickListener(this);

        simpleProductsFragment = SimpleProductsFragment.newInstance();
        finalizeFragment = SimpleProductsFragment.newInstance();
        simpleProductsFragment.setUseRecyclerView(true);
        finalizeFragment.setUseRecyclerView(true);

        simpleProductsFragment.setHasUnits(true);
        finalizeFragment.setHasUnits(true);

        simpleProductsFragment.setHelper(getHelper());
        simpleProductsFragment.setSetupActionBar(this);

        finalizeFragment.setHelper(getHelper());
        finalizeFragment.setSetupActionBar(this);

        salesAdapter = new SimpleSalesProductRecyclerAdapter(this, getHelper());

        simpleProductsFragment.setProductsRecyclerAdapter(salesAdapter);
        simpleProductsFragment.setListingType(ListingType.ADVANCED_SALES);
        finalizeFragment.setProductsRecyclerAdapter(salesAdapter);
        finalizeFragment.setListingType(ListingType.ADVANCED_SALES);

        simpleProductsFragment.setHasUnits(true);
        simpleProductsFragment.setProductCategories(getProductCategories(true));
        simpleProductsFragment.setHasSubtotal(true);

        finalizeFragment.setHasSubtotal(true);
        finalizeFragment.setHasCategories(false);
        finalizeFragment.setHasBrand(false);
        finalizeFragment.setHasDeliveryDate(false);
        finalizeFragment.setHasUnits(true);

        customersFragment = new SimpleCustomersFragment();
        customersFragment.setHelper(getHelper());
        customersFragment.setSetupActionBar(this);

        setTitle(CUSTOMER_TITLE);
        btnReview.setText(CUSTOMER_LABEL);
        invalidateOptionsMenu();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.flContent, customersFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(btnReview != null && btnReview.getText().toString().equals(REVIEW_LABEL)) {
            getMenuInflater().inflate(R.menu.menu_sample_sales, menu);
            SearchView mSearch = (SearchView) menu.findItem(R.id.mSearch).getActionView();

            mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    simpleProductsFragment.updateListWhenSearch(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    simpleProductsFragment.updateListWhenSearch(newText);
                    return true;
                }
            });
            /*initializeSearchViewEx(new SearchViewCompat.OnQueryTextListenerCompat() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    simpleProductsFragment.updateListWhenSearch(newText);
                    return true;
                }
            });*/


            miSearch = menu.findItem(R.id.mSearch);
        }
        if(miSearch != null)
            miSearch.setVisible(btnReview.getText().toString().equals(REVIEW_LABEL));
        getSupportActionBar().setDisplayShowTitleEnabled(!btnReview.getText().toString().equals(REVIEW_LABEL));
        getSupportActionBar().setDisplayHomeAsUpEnabled(!btnReview.getText().toString().equals(CUSTOMER_LABEL));
        getSupportActionBar().setHomeButtonEnabled(!btnReview.getText().toString().equals(CUSTOMER_LABEL));

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
        else if(btnReview.getText().toString().equals(REVIEW_LABEL)) {
            btnReview.setText(CUSTOMER_LABEL);
        }

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
                DialogTools.showDialog(SampleSales.this, "Ooops!", "You have not selected any item. Kindly select first " +
                        "products.");
            else {
                setTitle(REVIEW_TITLE);
                miSearch.setVisible(false);

                btnReview.setText(CHECKOUT_LABEL);
                Log.e("SELECTED ITEMS", ""+ProductsAdapterHelper.getSelectedProductItems().getSelectedProducts().size());
                finalizeFragment.setFilterProductsBy(ProductsAdapterHelper.getSelectedProductItems().getSelectedProducts());

                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                                R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.flContent, finalizeFragment, "finalize")
                        .addToBackStack("finalize")
                        .commit();
                //getSupportActionBar().setDisplayShowTitleEnabled(true);
                //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                //getSupportActionBar().setHomeButtonEnabled(true);
            }
        } else if(btnReview.getText().toString().equals(CHECKOUT_LABEL)) {
            setTitle(CHECKOUT_TITLE);
            btnReview.setText(SEND_LABEL);

            simpleCheckoutFragment = new SimpleCheckoutFragment();
            simpleCheckoutFragment.setSetupActionBar(this);

            Invoice.Builder invoiceBuilder = new Invoice.Builder()
                    .invoice_lines(InvoiceTools.generateInvoiceLines(ProductsAdapterHelper
                            .getSelectedProductItems()));

            simpleCheckoutFragment.setInvoice(invoiceBuilder.build());
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_left, R.anim.slide_out_right)
                    .replace(R.id.flContent, simpleCheckoutFragment, "checkout")
                    .addToBackStack("checkout")
                    .commit();
        } else if(btnReview.getText().toString().equals(CUSTOMER_LABEL)) {

            if(customersFragment.getSelectedCustomers() != null && customersFragment.getSelectedCustomers().size() != 0)
                selectedCustomer = customersFragment.getSelectedCustomers().get(0);
            else
                selectedCustomer = null;
            try {
                salesAdapter.setBranch(getHelper().fetchObjects(Branch.class).queryBuilder().where()
                        .eq("id", getSession().getCurrent_branch_id()).queryForFirst());
                if(selectedCustomer != null) {
                    List<CustomerGroup> customerGroups = selectedCustomer.getCustomerGroups(getHelper());
                    Log.e("CustomerGroup", "size " + customerGroups.size());
                    if (customerGroups.size() > 0 && customerGroups.get(0) != null) {
                        Log.e("CustomerGroup", customerGroups.get(0).toString());
                        salesAdapter.setCustomerGroup(customerGroups.get(0));
                    }
                }

                salesAdapter.setCustomer(selectedCustomer);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_left, R.anim.slide_out_right)
                    .replace(R.id.flContent, simpleProductsFragment, "product_fragment")
                    .addToBackStack("product_fragment")
                    .commit();

            btnReview.setText(REVIEW_LABEL);
        } else {
            Location location = LocationTools.getLocation(this);
            if(location != null)
                Log.e("ACCURACY", location.getAccuracy() + " " + Criteria.ACCURACY_HIGH);
            Log.e(">>>", new Invoice.Builder()
                    .payments(simpleCheckoutFragment.getPayments())
                    .invoice_lines(simpleCheckoutFragment.getInvoiceLines())
                    /*.extras(new Extras.Builder()
                            .latitude("" + LocationTools.limitDecimal(location.getLatitude(), 5))
                            .longitude("" + LocationTools.limitDecimal(location.getLongitude(), 5))
                            .build())*/
                    .build()
                    .toString());
        }

        //invalidateOptionsMenu();
    }
}
