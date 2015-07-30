package net.nueca.concessioengine.lists;

import net.nueca.concessioengine.objects.SelectedProductItem;

import java.util.ArrayList;
import java.util.Collection;

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

    public String getQuantity(int product_id) {
        int item_index = this.indexOf(product_id);
        if(item_index == -1)
            return "0";
        return this.get(item_index).getQuantity();
    }
}
