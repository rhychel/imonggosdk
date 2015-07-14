package net.nueca.concessioengine.lists;

import net.nueca.imonggosdk.objects.Product;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public class ProductsList extends ArrayList<Product>{
    public ProductsList(int capacity) {
        super(capacity);
    }

    public ProductsList() {
    }

    public ProductsList(Collection<? extends Product> collection) {
        super(collection);
    }
}
