package net.nueca.imonggosdk.enums;

/**
 * Created by rhymart on 8/22/15.
 * imonggosdk2 (c)2015
 */
public enum ConcessioModule {
    STOCK_REQUEST("stock_request"), // ORders
    PURCHASE_ORDERS("purchase_orders"),
    PHYSICAL_COUNT("physical_count"),
    INVOICE("invoice"), // SALES, // BOOKING
    RECEIVE_BRANCH("receive_branch"), // RECEIVE
    RECEIVE_BRANCH_PULLOUT("receive_branch_pullout"), // PULLOUT CONFIRMATION
    RELEASE_BRANCH("release_branch"), // PULLOUT_REQUEST || Pullout Document
    RECEIVE_ADJUSTMENT("receive_adjustment"), //ADJUSTMENT_OUT, // MSO
    RELEASE_ADJUSTMENT("release_adjustment"), //ADJUSTMENT_IN,
    RECEIVE_SUPPLIER("receive_supplier"), // RECEIVE(REBISCO)
    RELEASE_SUPPLIER("release_supplier"), // PULLOUT(REBISCO)

    CUSTOMERS("customers"),
    RELEASE_CUSTOMER("release_customer"), // <--- RGS
    CUSTOMER_DETAILS("customer_details"),

    APP("app"),

    HISTORY("history"),
    LAYAWAY("layaway"),
    ROUTE_PLAN("route_plan"),

    NONE("none");


    private String name;
    ConcessioModule(String name) {
        this.name = name;
    }

    public ConcessioModule[] getValidTransaction() {
        return new ConcessioModule[]{STOCK_REQUEST, PHYSICAL_COUNT, INVOICE, RECEIVE_BRANCH, RECEIVE_BRANCH_PULLOUT, RELEASE_BRANCH, RECEIVE_ADJUSTMENT, RELEASE_ADJUSTMENT, RECEIVE_SUPPLIER, RELEASE_SUPPLIER};
    }

    @Override
    public String toString() {
        return name;
    }

}
