package net.nueca.imonggosdk.enums;

import android.util.Log;

import net.nueca.imonggosdk.R;

/**
 * Created by rhymart on 8/22/15.
 * imonggosdk2 (c)2015
 */
public enum ConcessioModule {
    APPLICATION("APPLICATION"),
    USER("USER"),
    PRODUCTS("PRODUCTS"),
    PRINTER("PRINTER"),

    STOCK_REQUEST("stock_request", R.drawable.ic_stock_request), // Orders
    PURCHASE_ORDERS("purchase_orders", R.drawable.ic_stock_request),
    PHYSICAL_COUNT("physical_count", R.drawable.ic_physical_count),
    INVOICE("invoice", R.drawable.ic_booking), // SALES, // BOOKING
    INVOICE_PARTIAL("invoice_partial"),
    RECEIVE_BRANCH("receive_branch", R.drawable.ic_receiving), // RECEIVE
    RECEIVE_BRANCH_PULLOUT("receive_branch_pullout", R.drawable.ic_pullout_confirmation), // PULLOUT CONFIRMATION
    RELEASE_BRANCH("release_branch", R.drawable.ic_pullout_request), // PULLOUT_REQUEST || Pullout Document
    RECEIVE_ADJUSTMENT("receive_adjustment", R.drawable.ic_adjustment_in), //ADJUSTMENT_OUT, // MSO
    RELEASE_ADJUSTMENT("release_adjustment", R.drawable.ic_mso), //ADJUSTMENT_IN,
    RECEIVE_SUPPLIER("receive_supplier", R.drawable.ic_receiving), // RECEIVE(REBISCO)
    RELEASE_SUPPLIER("release_supplier", R.drawable.ic_pullout), // PULLOUT(REBISCO)

    CUSTOMERS("customers", R.drawable.ic_customers),
    RELEASE_CUSTOMER("release_customer"), // <--- RGS
    CUSTOMER_DETAILS("customer_details"),

    APP("app"),

    HISTORY("history", R.drawable.ic_history),
    LAYAWAY("layaway", R.drawable.ic_layaway),
    ROUTE_PLAN("route_plan", R.drawable.ic_booking),

    NONE("none"),
    ALL("all", "All Transactions");


    private String name;
    private String label;
    private int logo = -1;
    ConcessioModule(String name) {
        this.name = name;
    }

    ConcessioModule(String name, String label) {
        this.name = name;
        this.label = label;
    }

    ConcessioModule(String name, int logo) {
        this.logo = logo;
        this.name = name;
    }

    public ConcessioModule[] getValidTransaction() {
        return new ConcessioModule[]{STOCK_REQUEST, PHYSICAL_COUNT, INVOICE, RECEIVE_BRANCH, RECEIVE_BRANCH_PULLOUT, RELEASE_BRANCH, RECEIVE_ADJUSTMENT, RELEASE_ADJUSTMENT, RECEIVE_SUPPLIER, RELEASE_SUPPLIER};
    }

    public static ConcessioModule[] convertToConcessioModules(int[] ordinals) {
        if(ordinals == null)
            return null;
        ConcessioModule[] concessioModules = new ConcessioModule[ordinals.length];

        int i = 0;
        for(int ordinal : ordinals) {
            Log.e("ConcessioModule", ordinal+"--");
            Log.e("ConcessioMOdule", values()[ordinal].name);
            concessioModules[i++] = values()[ordinal];
        }
        Log.e("ConcessioModule", concessioModules.length+"");

        return concessioModules;
    }

    public int getLogo() {
        return logo;
    }

    public void setLogo(int logo) {
        this.logo = logo;
    }

    public String getLabel() {
        return label;
    }

    public ConcessioModule setLabel(String label) {
        this.label = label;
        return this;
    }

    @Override
    public String toString() {
        return name;
    }

}
