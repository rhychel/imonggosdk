package net.nueca.imonggosdk.objects.customer;

import com.j256.ormlite.dao.ForeignCollection;

import net.nueca.imonggosdk.objects.PriceList;

/**
 * Created by rhymart on 11/10/15.
 */
public class CustomerGroup {

    private Customer customer;
    private String name;
    private PriceList priceList;
    private String discount_text;
    private ForeignCollection<Customer> foreignCustomer;
    private String status;

}
