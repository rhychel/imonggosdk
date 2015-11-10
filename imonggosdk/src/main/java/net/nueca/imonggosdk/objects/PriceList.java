package net.nueca.imonggosdk.objects;

import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;

/**
 * Created by rhymart on 11/10/15.
 */
public class PriceList {

    private String code;

    private Branch branch;
    private CustomerGroup customerGroup; //can be null
    private Customer customer; // can be null


}
