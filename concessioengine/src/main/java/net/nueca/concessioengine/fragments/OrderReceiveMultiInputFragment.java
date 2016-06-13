package net.nueca.concessioengine.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.SimpleOrderReceiveRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseSalesProductRecyclerAdapter;
import net.nueca.concessioengine.adapters.interfaces.OnItemClickListener;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.dialogs.SimpleOrderReceiveDialog;
import net.nueca.concessioengine.dialogs.SimpleSalesQuantityDialog;
import net.nueca.concessioengine.fragments.interfaces.ListScrollListener;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.objects.ReceivedItemValue;
import net.nueca.concessioengine.objects.ReceivedMultiItem;
import net.nueca.concessioengine.objects.ReceivedProductItem;
import net.nueca.concessioengine.objects.ReceivedProductItemLine;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.fragments.ImonggoFragment;
import net.nueca.imonggosdk.objects.BranchProduct;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.base.DBTable;
import net.nueca.imonggosdk.objects.invoice.InvoicePurpose;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 02/06/2016.
 */
public class OrderReceiveMultiInputFragment extends ImonggoFragment {

    private TextView tvNoProducts;
    private RecyclerView rvItems;
    private Toolbar tbActionBar;

    private SetupActionBar setupActionBar;

    private SimpleOrderReceiveRecyclerAdapter orderReceiveRecyclerAdapter;

    private Product product;
    private int productItemLineIndex;
    private ReceivedProductItem receivedProductItem;
    private ReceivedProductItemLine receivedProductItemLine;
    private boolean isUnitDisplay = false, isFinalize = false, displayOnly = false, canDeleteItems = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.simple_products_fragment_rv, container, false);

        receivedProductItem = ProductsAdapterHelper.getReceivedProductItems().get(product);
        receivedProductItemLine = receivedProductItem.getProductItemLineAt(productItemLineIndex);

        orderReceiveRecyclerAdapter = new SimpleOrderReceiveRecyclerAdapter(getContext(), getHelper());
        orderReceiveRecyclerAdapter.setList(
                ReceivedMultiItem.generateReceivedMultiItem(receivedProductItem, ProductsAdapterHelper.getReceivedProductItems().toList().indexOf
                        (receivedProductItem),
                        receivedProductItemLine, receivedProductItem.getProductItemLines().indexOf(receivedProductItemLine))
        );

        orderReceiveRecyclerAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                ReceivedMultiItem multiItem = orderReceiveRecyclerAdapter.getItem(position);
                showQuantityDialog(position, multiItem.getReceivedProductItemLine(), multiItem.getItemValueLineNo());
            }
        });

        rvItems = (RecyclerView) view.findViewById(R.id.rvProducts);
        tbActionBar = (Toolbar) view.findViewById(R.id.tbActionBar);
        tvNoProducts = (TextView) view.findViewById(R.id.tvNoProducts);

        view.findViewById(R.id.spCategories).setVisibility(View.GONE);

        rvItems.setAdapter(orderReceiveRecyclerAdapter);
        orderReceiveRecyclerAdapter.initializeRecyclerView(getContext(),rvItems);

        if(canDeleteItems) {
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
            itemTouchHelper.attachToRecyclerView(rvItems);
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(setupActionBar != null)
            setupActionBar.setupActionBar(tbActionBar);
    }

    public void refreshList() {
        orderReceiveRecyclerAdapter.notifyDataSetChanged();
    }

    public void forceUpdateList() {
        forceUpdateList(
                ReceivedMultiItem.generateReceivedMultiItem(receivedProductItem, ProductsAdapterHelper.getReceivedProductItems().toList().indexOf
                        (receivedProductItem),
                receivedProductItemLine, receivedProductItem.getProductItemLines().indexOf(receivedProductItemLine))
        );
    }

    public void forceUpdateList(List<ReceivedMultiItem> receivedMultiItemList) {
        orderReceiveRecyclerAdapter.updateList(receivedMultiItemList);
        //toggleNoItems("No products available.", (productRecyclerViewAdapter.getItemCount() > 0));
    }

    protected void showQuantityDialog(final int position, ReceivedProductItemLine productItemLine, final int valueIndex) {
        SimpleOrderReceiveDialog dialog = new SimpleOrderReceiveDialog(getContext(), R.style.AppCompatDialogStyle_Light_NoTitle);
        dialog.setUnitDisplay(isUnitDisplay);
        final ReceivedItemValue existingValue = productItemLine.getItemValueAt(position);

        dialog.setProductName(product.getName());
        dialog.setExpectedPrice(productItemLine.getExpected_price());
        dialog.setItemValue(existingValue);
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
                if(itemValue.getQuantity() == 0d) {
                    if(existingValue != null) {
                        ReceivedProductItemLine receivedProductItemLine = ProductsAdapterHelper.getReceivedProductItems().get(product.getId())
                                .getProductItemLineAt(productItemLineIndex);
                        receivedProductItemLine.removeItemValueAt(valueIndex);
                        orderReceiveRecyclerAdapter.remove(position);
                        orderReceiveRecyclerAdapter.notifyItemChanged(position);
                    }
                    return;
                }

                ReceivedProductItemLine receivedProductItemLine = ProductsAdapterHelper.getReceivedProductItems().get(product.getId())
                        .getProductItemLineAt(productItemLineIndex);
                receivedProductItemLine.setItemValueAt(valueIndex, itemValue);

                orderReceiveRecyclerAdapter.getItem(position).setReceivedItemValue(itemValue);
                orderReceiveRecyclerAdapter.notifyItemChanged(position);
            }
        });
        dialog.show();
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

    public void setSetupActionBar(SetupActionBar setupActionBar) {
        this.setupActionBar = setupActionBar;
    }

    public void setProductItemLineIndex(int productItemLineIndex) {
        this.productItemLineIndex = productItemLineIndex;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setUnitDisplay(boolean unitDisplay) {
        isUnitDisplay = unitDisplay;
    }

    public void setFinalize(boolean finalize) {
        isFinalize = finalize;
    }

    public void setDisplayOnly(boolean displayOnly) {
        this.displayOnly = displayOnly;
    }

    public void setCanDeleteItems(boolean canDeleteItems) {
        this.canDeleteItems = canDeleteItems;
    }

    private ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            if(target != null)
                Log.e("target", "Not null");

            if(viewHolder != null)
                Log.e("viewHolder", "Not null");
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            Log.e("onSwiped", "Index=" + viewHolder.getAdapterPosition());
            int index = viewHolder.getAdapterPosition();
            ReceivedMultiItem item = orderReceiveRecyclerAdapter.getItem(index);

            if(item.getReceivedItemValue() == null)
                return;

            ProductsAdapterHelper.getReceivedProductItems().get(item.getProduct().getId())
                    .getProductItemLineAt(item.getProductItemLineNo())
                    .removeItemValueAt(item.getItemValueLineNo());

            orderReceiveRecyclerAdapter.remove(index);
            orderReceiveRecyclerAdapter.notifyItemChanged(0);
            orderReceiveRecyclerAdapter.notifyDataSetChanged();

            //if (productsFragmentListener != null)
            //    productsFragmentListener.whenItemsSelectedUpdated();
        }

        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            Log.e("getSwipeDirs", "Yeah");
            return super.getSwipeDirs(recyclerView, viewHolder);
        }
    };
}
