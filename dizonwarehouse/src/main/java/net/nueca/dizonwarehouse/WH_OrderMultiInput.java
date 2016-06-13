package net.nueca.dizonwarehouse;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.dialogs.SimpleOrderReceiveDialog;
import net.nueca.concessioengine.fragments.OrderReceiveMultiInputFragment;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.objects.ReceivedItemValue;
import net.nueca.concessioengine.objects.ReceivedProductItem;
import net.nueca.concessioengine.objects.ReceivedProductItemLine;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.tools.NumberTools;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by gama on 03/06/2016.
 */
public class WH_OrderMultiInput extends ModuleActivity implements SetupActionBar {
    public static final String RECEIVED_PRODUCT_ITEM_ID = "received_product_item_id";
    public static final String RECEIVED_PRODUCT_ITEMLINE_INDEX = "received_product_itemline_index";

    private LinearLayout llFooter, llReview, llTotalAmount, llExpectedQty, llExpectedPrice;
    private TextView tvTotalAmount, tvItems, tvExpectedQty, tvExpectedPrice;
    private Toolbar tbActionBar;

    private Button btnPrimary;

    private Product product;
    private int productItemLineIndex;
    private ReceivedProductItem receivedProductItem;
    private ReceivedProductItemLine receivedProductItemLine;

    private OrderReceiveMultiInputFragment orderReceiveMultiInputFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wh_module);

        int productItemId = getIntent().getIntExtra(RECEIVED_PRODUCT_ITEM_ID, 0);
        product = ProductsAdapterHelper.getReceivedProductItems().get(productItemId).getProduct();

        if(product == null) {
            setResult(ERROR);
            finish();
        }

        productItemLineIndex = getIntent().getIntExtra(RECEIVED_PRODUCT_ITEMLINE_INDEX, 0);

        llExpectedQty = (LinearLayout) findViewById(R.id.llExpectedQty);
        llExpectedPrice = (LinearLayout) findViewById(R.id.llExpectedPrice);
        llTotalAmount = (LinearLayout) findViewById(R.id.llTotalAmount);

        tvExpectedPrice = (TextView) findViewById(R.id.tvExpectedPrice);
        tvExpectedQty = (TextView) findViewById(R.id.tvExpectedQty);
        tvItems = (TextView) findViewById(R.id.tvItems);
        tvItems.setVisibility(View.INVISIBLE);

        btnPrimary = (Button) findViewById(R.id.btn1);
        btnPrimary.setText("RECEIVE");

        receivedProductItem = ProductsAdapterHelper.getReceivedProductItems().get(product);
        receivedProductItemLine = receivedProductItem.getProductItemLineAt(productItemLineIndex);

        orderReceiveMultiInputFragment = new OrderReceiveMultiInputFragment();
        orderReceiveMultiInputFragment.setUnitDisplay(true);
        orderReceiveMultiInputFragment.setCanDeleteItems(true);
        orderReceiveMultiInputFragment.setHelper(getHelper());
        orderReceiveMultiInputFragment.setSetupActionBar(WH_OrderMultiInput.this);
        orderReceiveMultiInputFragment.setProduct(product);
        orderReceiveMultiInputFragment.setProductItemLineIndex(productItemLineIndex);

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                        R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.flContent, orderReceiveMultiInputFragment, "receiving_orders_fragment")
                .commit();

        llExpectedQty.setVisibility(View.VISIBLE);
        llExpectedPrice.setVisibility(View.VISIBLE);

        llTotalAmount.setVisibility(View.GONE);

        tvExpectedQty.setText("" + NumberTools.separateInCommas(receivedProductItemLine.getExpected_quantity()));
        tvExpectedPrice.setText("P " + NumberTools.separateInCommas(receivedProductItemLine.getExpected_price()));

        btnPrimary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleOrderReceiveDialog dialog = new SimpleOrderReceiveDialog(WH_OrderMultiInput.this, net.nueca.concessioengine.R.style
                        .AppCompatDialogStyle_Light_NoTitle);
                dialog.setUnitDisplay(true);

                dialog.setProductName(product.getName());
                dialog.setExpectedPrice(receivedProductItemLine.getExpected_price());
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                try {
                    dialog.setUnitList(getUnits(product, true));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                dialog.setReceiveDialogListener(new SimpleOrderReceiveDialog.ReceiveDialogListener() {
                    @Override
                    public void onCancel() {}

                    @Override
                    public void onSave(ReceivedItemValue itemValue) {
                        if(itemValue.getQuantity() == 0d)
                            return;

                        ReceivedProductItemLine receivedProductItemLine = ProductsAdapterHelper.getReceivedProductItems().get(product.getId())
                                .getProductItemLineAt(productItemLineIndex);
                        receivedProductItemLine.addItemValue(itemValue);
                        orderReceiveMultiInputFragment.forceUpdateList();
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wh_module_right, menu);
        menu.findItem(R.id.mDelete).setVisible(false);
        menu.findItem(R.id.mSearch).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        setResult(SUCCESS);
        super.onBackPressed();
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        setTitle(product.getName());
    }

    protected List<Unit> getUnits(Product product, boolean includeBaseUnit) throws SQLException {
        List<Unit> unitList = getHelper().fetchForeignCollection(product.getUnits().closeableIterator());

        if(includeBaseUnit) {
            Unit unit = new Unit(product);
            unit.setId(-1);
            unit.setName(product.getBase_unit_name());
            unit.setRetail_price(product.getRetail_price());

            if (unitList.size() > 0)
                unitList.add(0, unit);
            else
                unitList.add(unit);
        }

        return unitList;
    }
}
