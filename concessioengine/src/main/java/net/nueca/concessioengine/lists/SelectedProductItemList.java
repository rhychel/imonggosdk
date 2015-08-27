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

    public SelectedProductItemList(int capacity) {
        super(capacity);
    }

    public SelectedProductItemList() { }

    public SelectedProductItemList(Collection<? extends SelectedProductItem> collection) {
        super(collection);
    }

    @Override
    public boolean add(SelectedProductItem selectedProductItem) {
        int index = indexOf(selectedProductItem);
        if(index > -1) {
            if(selectedProductItem.getValues().size() > 0)
                set(index, selectedProductItem);
            else
                remove(index);
            return true;
        }

        if(selectedProductItem.getValues().size() > 0)
            return super.add(selectedProductItem);
        return true;
    }

    public List<Product> getSelectedProducts() {
        List<Product> selectedProducts = new ArrayList<>();

        for(SelectedProductItem selectedProductItem : this)
            selectedProducts.add(selectedProductItem.getProduct());

        return selectedProducts;
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
        return selectedProductItem.getQuantity();
    }

    public String getUnitName(Product product) {
        SelectedProductItem selectedProductItem = getSelectedProductItem(product);
        if(selectedProductItem == null)
            return "";
        if(selectedProductItem.getValues().get(0).getUnit() != null)
            return "<i>["+selectedProductItem.getValues().get(0).getUnit().getName()+"]</i>";
        return "";
    }

    public void renderToJson() {
        Gson gson = new GsonBuilder().serializeNulls().create();
        Log.e("Json", gson.toJson(this));
    }
}
