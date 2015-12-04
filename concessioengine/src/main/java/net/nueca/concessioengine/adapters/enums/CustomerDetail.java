package net.nueca.concessioengine.adapters.enums;

/**
 * Created by rhymart on 12/2/15.
 */
public enum CustomerDetail {
    NAME("Name"),
    FIRST_NAME("First name"),
    LAST_NAME("Last name"),
    CODE("Code"),
    MOBILE_NO("Mobile"),
    TEL_NO("Work"),
    COMPANY_NAME("Company"),
    ADDRESS("Address"),
    STREET("Street"),
    TOWN("Town"),
    PROVINCE("Province"),
    CUSTOMER_TYPE("Business"), // customer category
    TERMS("Terms"),
    SALES_ROUTE("Sales route"),
    DISCOUNT("Discount"),
    LAST_PURCHASE_DETAILS("Last purchase details");

    private String label;
    CustomerDetail(String label) { this.label = label; }

    @Override
    public String toString() {
        return label;
    }
}
