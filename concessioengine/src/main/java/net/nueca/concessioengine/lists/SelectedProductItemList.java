package net.nueca.concessioengine.lists;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.objects.Product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public class SelectedProductItemList extends ArrayList<SelectedProductItem> {

    //private Double subtotal = 0d;

    private boolean isReturns = false;

    public SelectedProductItemList(int capacity) {
        super(capacity);
    }

    public SelectedProductItemList() { }

    public SelectedProductItemList(Collection<? extends SelectedProductItem> collection) {
        super(collection);
    }

    /**
     *
     * @param selectedProductItem
     * @return false when the item has been removed.
     */
    @Override
    public boolean add(SelectedProductItem selectedProductItem) {
        selectedProductItem.setReturns(isReturns);
        int index = indexOf(selectedProductItem);
        if(index > -1) {
            if(selectedProductItem.getValues().size() > 0) {
                set(index, selectedProductItem);
                //updateSubtotal();
            }
            else {
                remove(index);
                //updateSubtotal();
                return false;
            }
            return true;
        }

        if(selectedProductItem.getValues().size() > 0) {
            boolean ret = super.add(selectedProductItem);
            //updateSubtotal();
            return ret;
        }
        return true;
    }

    protected boolean addNullable(SelectedProductItem selectedProductItem) {
        return super.add(selectedProductItem);
    }

    public List<Product> getSelectedProducts() {
        List<Product> selectedProducts = new ArrayList<>();

        for(SelectedProductItem selectedProductItem : this)
            selectedProducts.add(selectedProductItem.getProduct());

        return selectedProducts;
    }

    public boolean hasSelectedProductItem(Product product) {
        return getSelectedProductItem(product) != null;
    }

    public SelectedProductItem initializeItem(Product product) {
        if(hasSelectedProductItem(product))
            return getSelectedProductItem(product);

        SelectedProductItem selectedProductItem = new SelectedProductItem(product);
        selectedProductItem.setReturns(isReturns);
        return selectedProductItem;
    }

    public SelectedProductItem getSelectedProductItem(Product product) {
        for(SelectedProductItem selectedProductItem : this)
            if (selectedProductItem.getProduct().getId() == product.getId())
                return selectedProductItem;
        return null;
    }

    public String getQuantity(Product product) {
        SelectedProductItem selectedProductItem = getSelectedProductItem(product);
        if(selectedProductItem == null)
            return "0";
        if(selectedProductItem.isMultiline())
            return selectedProductItem.getActualQuantity();
        return selectedProductItem.getQuantity();
    }

    public String getUnitName(Product product) {
        return getUnitName(product, true);
    }

    public String getUnitName(Product product, boolean withFormat) {
        SelectedProductItem selectedProductItem = getSelectedProductItem(product);
        if(selectedProductItem == null)
            return product.getBase_unit_name();
        if(selectedProductItem.isMultiline())
            return product.getBase_unit_name();
        if(selectedProductItem.getValues().get(0).getUnit() != null) {
            if (withFormat)
                return "<i>[" + selectedProductItem.getValues().get(0).getUnit().getName() + "]</i>";
            return selectedProductItem.getValues().get(0).getUnit().getName();
        }
        return product.getBase_unit_name();
    }

    public boolean isReturns() {
        return isReturns;
    }

    public void setReturns(boolean returns) {
        isReturns = returns;
    }

    public void renderToJson() {
        Gson gson = new GsonBuilder().serializeNulls().create();
        Log.e("Json", gson.toJson(this));
    }

    @Override
    public List<SelectedProductItem> subList(int start, int end) {
        return super.subList(start, end);
    }

    public Double getSubtotal() {
        double subtotal = 0d;
        for(SelectedProductItem selectedProductItem : this)
            subtotal += selectedProductItem.getValuesSubtotal();
        return subtotal;
    }

    /*public void updateSubtotal() {
        subtotal = 0d;
        for(SelectedProductItem selectedProductItem : this)
            subtotal += selectedProductItem.getValuesSubtotal();
    }*/
}
