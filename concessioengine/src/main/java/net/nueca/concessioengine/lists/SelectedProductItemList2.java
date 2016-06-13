package net.nueca.concessioengine.lists;

import android.util.Log;

import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.tools.NumberTools;

import java.util.Collections;
import java.util.Comparator;

/**
 * Created by gama on 9/16/15.
 */
public class SelectedProductItemList2 extends SelectedProductItemList {
    private boolean isSorted = false;

    @Override
    public boolean add(SelectedProductItem selectedProductItem) {
        isSorted = false;
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

    public void sort() {
        sort(true);
    }

    public void sort(final boolean isAscending) {
        if(isSorted || size() <= 0)
            return;

        Collections.sort(this, new Comparator<SelectedProductItem>() {
            public int compare(SelectedProductItem item1, SelectedProductItem item2) {
                if(isAscending)
                    return item1.getProduct().getName().compareToIgnoreCase(item2.getProduct().getName());
                else
                    return item2.getProduct().getName().compareToIgnoreCase(item1.getProduct().getName());
            }
        });

        int lineNo = 1;
        for(SelectedProductItem item : this) {
            for(Values itemValue : item.getValues()) {
                itemValue.setLine_no(lineNo);
                lineNo++;
            }
        }
        isSorted = true;
    }

    public void removeZeroValue() {
        for(SelectedProductItem item : this) {
            for(Values itemValue : item.getValues()) {
                double received = NumberTools.toDouble(itemValue.getQuantity());
                double returned = itemValue.getExtendedAttributes() == null? 0d :
                        NumberTools.toDouble(itemValue.getExtendedAttributes().getOutright_return());
                if(received == 0d && returned == 0d) {
                    item.remove(item.getValues().indexOf(itemValue));
                }
            }
            if(item.getValues().isEmpty())
                this.remove(item);
        }
    }
}
