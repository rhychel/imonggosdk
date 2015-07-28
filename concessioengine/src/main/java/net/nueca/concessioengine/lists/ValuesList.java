package net.nueca.concessioengine.lists;

import net.nueca.concessioengine.objects.Values;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public class ValuesList extends ArrayList<Values> {
    public ValuesList(int capacity) {
        super(capacity);
    }

    public ValuesList() {
    }

    public ValuesList(Collection<? extends Values> collection) {
        super(collection);
    }

    public String getQuantity() {
        String quantity = "0";
        BigDecimal totalQuantity = new BigDecimal(0);
        for(Values values : this)
            totalQuantity.add(new BigDecimal(values.getQuantity()));

        quantity = totalQuantity.toString();
        return quantity;
    }

}