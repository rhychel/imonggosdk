package net.nueca.concessio;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.fragments.BaseProductsFragment;
import net.nueca.concessioengine.fragments.SimpleProductsFragment;
import net.nueca.concessioengine.tools.DiscountTools;
import net.nueca.concessioengine.tools.InvoiceTools;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.invoice.Invoice;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.DialogTools;
import net.nueca.imonggosdk.tools.NumberTools;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Created by rhymart on 8/22/15.
 * imonggosdk2 (c)2015
 */
public class C_Finalize extends ModuleActivity {

    private Toolbar tbActionBar;
    private TabLayout tlTotal;
    private ViewPager vpReview;

    private TextView tvItems;
    private Button btn1, btn2; // CHECKOUT
    private LinearLayout llTotalAmount, llReview, llBalance;

    private TextView tvBalance, tvTotalAmount;
    private View viewStub;

    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_review_activity);

        clearTransactions = false;

        tbActionBar = (Toolbar) findViewById(R.id.tbActionBar);
        tlTotal = (TabLayout) findViewById(R.id.tlTotal);
        vpReview = (ViewPager) findViewById(R.id.vpReview);
        tvItems = (TextView) findViewById(R.id.tvItems);
        btn1 = (Button) findViewById(R.id.btn1);
        llBalance = (LinearLayout) findViewById(R.id.llBalance);
        llReview = (LinearLayout) findViewById(R.id.llReview);
        tvBalance = (TextView) findViewById(R.id.tvBalance);
        viewStub = findViewById(R.id.viewStub);

        setSupportActionBar(tbActionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        btn1.setText("CHECKOUT");

        llBalance.setVisibility(View.VISIBLE);

        if(isForHistoryDetail) {
            getSupportActionBar().setTitle(getIntent().getStringExtra(REFERENCE));
            try {
                final OfflineData offlineData = getHelper().fetchObjectsInt(OfflineData.class).queryBuilder()
                        .where().eq("reference_no", getIntent().getStringExtra(REFERENCE)).queryForFirst();
                historyDetailsListener = new HistoryDetailsListener() {
                    @Override
                    public void onVoidTransaction() {
                        // TODO for double checking..

                        new SwableTools.Transaction(getHelper())
                                .toCancel()
                                .withReason("VOID")
                                .object(offlineData)
                                .queue();

                        onBackPressed();
                    }

                    @Override
                    public void onDuplicateTransaction() {
                        // TODO for double checking..

                        ProductsAdapterHelper.isDuplicating = true;
                        Intent intent = new Intent(C_Finalize.this, C_Module.class);
                        intent.putExtra(ModuleActivity.CONCESSIO_MODULE, offlineData.getConcessioModule().ordinal());
                        startActivity(intent);
                    }
                };

                if(offlineData == null) {
                    DialogTools.showDialog(this, "Ooops!", "This data is not found in your local database.", "Go to History.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }, R.style.AppCompatDialogStyle_Light_NoTitle);
                    return;
                }
                else if(!offlineData.isSynced() && !offlineData.isSyncing()) {
                    btn2 = (Button) findViewById(R.id.btn2);
                    initializeVoidButton(btn1, getIntent().getStringExtra(REFERENCE));
                    initializeDuplicateButton(btn2, getIntent().getStringExtra(REFERENCE));
                }
                else
                    initializeDuplicateButton(btn1, getIntent().getStringExtra(REFERENCE));

                InvoiceTools.PaymentsComputation paymentsComputation = new InvoiceTools.PaymentsComputation();
                paymentsComputation.addAllInvoiceLines(offlineData.getObjectFromData(Invoice.class).getInvoiceLines());
                paymentsComputation.addAllPayments(offlineData.getObjectFromData(Invoice.class).getPayments());

                paymentsComputation.getTotalPayable(); // Total Amount
                paymentsComputation.getRemaining(); // Total Balance

                llTotalAmount = (LinearLayout) findViewById(R.id.llTotalAmount);
                tvTotalAmount = (TextView) findViewById(R.id.tvTotalAmount);

                llTotalAmount.setVisibility(View.VISIBLE);
                tvTotalAmount.setText("P"+ NumberTools.separateInCommas(paymentsComputation.getTotalPayable()));

                if(paymentsComputation.getRemaining().doubleValue() == 0)
                    llBalance.setVisibility(View.GONE);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
            getSupportActionBar().setTitle("Review");
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(C_Finalize.this, C_Checkout.class);
                    startActivityForResult(intent, SALES);
                }
            });
        }

        viewStub.setVisibility(View.VISIBLE);
        tvItems.setVisibility(View.VISIBLE);

        toggleNext(llReview, tvItems);


        reviewAdapter = new ReviewAdapter(getSupportFragmentManager());
        vpReview.setAdapter(reviewAdapter);

        tlTotal.setupWithViewPager(vpReview);

        tbActionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Double sales = DiscountTools.applyMultipleDiscounts(
                new BigDecimal(ProductsAdapterHelper.getSelectedProductItems().getSubtotal()), BigDecimal.ONE,
                ProductsAdapterHelper.getSelectedCustomer() == null? null :
                        ProductsAdapterHelper.getSelectedCustomer().getDiscount_text(),",").doubleValue();
        Double balance =
                sales + ProductsAdapterHelper.getSelectedReturnProductItems().getSubtotal();
        tvBalance.setText("P"+ NumberTools.separateInCommas(balance));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!isForHistoryDetail)
            getMenuInflater().inflate(R.menu.simple_review_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mReturn: {
                AlertDialog.Builder addAndReturnDialog = new AlertDialog.Builder(this, R.style.AppCompatDialogStyle_Light);
                addAndReturnDialog.setTitle("Add and Return Items?");
                addAndReturnDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Go to returning
                        Intent intent = new Intent(C_Finalize.this, C_Module.class);
                        intent.putExtra(RETURN_ITEMS, true);
                        intent.putExtra(ModuleActivity.FOR_CUSTOMER_DETAIL, ProductsAdapterHelper.getSelectedCustomer().getId());
                        intent.putExtra(ModuleActivity.CONCESSIO_MODULE, ConcessioModule.INVOICE.ordinal());
                        startActivityForResult(intent, RETURN_ITEMS_SALES);
                    }
                });
                addAndReturnDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                addAndReturnDialog.show();
            } break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RETURN_ITEMS_SALES) {
            Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    reviewAdapter.updateReturns();
                    vpReview.setCurrentItem(1);
                }
            };
            handler.sendEmptyMessageDelayed(0, 100);
        }
        else if(resultCode == SUCCESS) {
            setResult(SUCCESS);
            finish();
        }
    }

    public class ReviewAdapter extends FragmentPagerAdapter {

        private SimpleProductsFragment returnsProductFragment;

        public ReviewAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            SimpleProductsFragment simpleProductsFragment = SimpleProductsFragment.newInstance();
            simpleProductsFragment.setListingType(ListingType.ADVANCED_SALES);
            simpleProductsFragment.setUseSalesProductAdapter(true);
            if(position == 1)
                returnsProductFragment = simpleProductsFragment;
            simpleProductsFragment.setHelper(getHelper());
            simpleProductsFragment.setHasUnits(true);
            simpleProductsFragment.setHasToolBar(false);
            simpleProductsFragment.setHasCategories(false);
            simpleProductsFragment.setIsFinalize(true);
            simpleProductsFragment.setHasSubtotal(true);
            simpleProductsFragment.setDisplayOnly(isForHistoryDetail);
            simpleProductsFragment.setProductsFragmentListener(new BaseProductsFragment.ProductsFragmentListener() {
                @Override
                public void whenItemsSelectedUpdated() {
                    toggleNext(llReview, tvItems);
                }
            });

            simpleProductsFragment.setProductsFragmentListener(new BaseProductsFragment.ProductsFragmentListener() {
                @Override
                public void whenItemsSelectedUpdated() {
                    Double sales = DiscountTools.applyMultipleDiscounts(
                            new BigDecimal(ProductsAdapterHelper.getSelectedProductItems().getSubtotal()), BigDecimal.ONE,
                            ProductsAdapterHelper.getSelectedCustomer() == null? null :
                                    ProductsAdapterHelper.getSelectedCustomer().getDiscount_text(),",").doubleValue();
                    Double balance =
                            sales + ProductsAdapterHelper.getSelectedReturnProductItems().getSubtotal();
                    tvBalance.setText("P"+ NumberTools.separateInCommas(balance));
                }
            });
            if(position == 0)// Positive Transactions
                simpleProductsFragment.setFilterProductsBy(ProductsAdapterHelper.getSelectedProductItems().getSelectedProducts());
            else {
                simpleProductsFragment.setFilterProductsBy(ProductsAdapterHelper.getSelectedReturnProductItems().getSelectedProducts());
                simpleProductsFragment.setReturnItems(true);
            }
            return simpleProductsFragment;
        }

        public void updateReturns() {
            returnsProductFragment.setFilterProductsBy(ProductsAdapterHelper.getSelectedReturnProductItems().getSelectedProducts());
            returnsProductFragment.forceUpdateProductList();
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position == 0)
                return "TOTAL SALES";
            return "TOTAL RETURNS";
        }
    }
}
