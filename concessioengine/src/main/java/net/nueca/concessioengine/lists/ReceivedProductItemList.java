package net.nueca.concessioengine.lists;

import android.util.Log;

import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.objects.Product;

/**
 * Created by gama on 9/16/15.
 */
public class ReceivedProductItemList extends SelectedProductItemList {
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

        return super.addNullable(selectedProductItem);
    }

    public String getDiscrepancy(Product product) {
        SelectedProductItem selectedProductItem = getSelectedProductItem(product);
        if(selectedProductItem == null)
            return "0";
        return selectedProductItem.getDiscrepancy();
    }

    public String getReturn(Product product) {
        SelectedProductItem selectedProductItem = getSelectedProductItem(product);
        if(selectedProductItem == null)
            return "0";
        return selectedProductItem.getReturn();
    }
}
