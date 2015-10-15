package net.nueca.imonggosdk.objects.invoice;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.tools.NumberTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

/**
 * Created by rhymart on 10/14/15.
 * imonggosdk2 (c)2015
 */
@DatabaseTable
public class Discount {

    private static final transient BigDecimal ONE_HUNDRED = new BigDecimal(100);

    @DatabaseField(generatedId=true)
    private int id = 0;
    @DatabaseField
    private String discount_text = "", discount_description = "";

    @DatabaseField(foreign=true,foreignAutoRefresh=true,columnName="product_id")
    private transient Product product;
    @DatabaseField(foreign=true,foreignAutoRefresh=true,columnName="sales_promotion_id")
    private transient SalesPromotion salesPromotion;
    private transient String quantity = "0";


    public Discount() { }

    public Discount(String discount_text) {
        this.discount_text = discount_text;
    }

    public Discount(Product product) {
        this.product = product;
    }

    /**
     * 10%|10|@10 -- When no Q, check value if it is a valid discount value;
     * Q{qty:<q>,discount:<10%|10|@10>}
     */
    public boolean isValid() {
        if(discount_text.contains("Q")) {
            try {
                JSONObject discountJson = new JSONObject(discount_text.replace("Q", ""));
                return discountJson.has("qty") &&
                        discountJson.has("discount") &&
                        isValidDiscountValue(discountJson.getString("discount")) &&
                        NumberTools.isNumber(discountJson.getString("qty"));
            } catch (JSONException e) {
                return false;
            }
        }
        return isValidDiscountValue(discount_text);
    }

    public boolean isValidDiscountValue(String discount_text) {
        return discount_text.matches("@?-?\\d+(\\.(\\d+))?") ||
                discount_text.matches("-?\\d+(\\.(\\d+))?%?") ||
                discount_text.matches("-?\\d+(\\.(\\d+))?");
    }

    public String getSubtotal() {
        BigDecimal retail_price = new BigDecimal(product.getRetail_price());
        BigDecimal qty = new BigDecimal(quantity);
        if(!isValid())
            return "Invalid discount value.";
        if(discount_text.contains("Q")) {
            try {
                JSONObject discountJson = new JSONObject(discount_text.replace("Q", ""));
                BigDecimal whenQuantity = new BigDecimal(discountJson.getDouble("qty"));
                if(qty.compareTo(whenQuantity) == 1 || qty.compareTo(whenQuantity) == 0)
                    return applyDiscount(retail_price, qty, discountJson.getString("discount"));
                else
                    return retail_price.multiply(qty).toPlainString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return applyDiscount(retail_price, qty);
    }

    private String applyDiscount(BigDecimal retail_price, BigDecimal qty) {
        return applyDiscount(retail_price, qty, discount_text);
    }

    private String applyDiscount(BigDecimal retail_price, BigDecimal qty, String discount_text) {
        String subtotal;
        if(discount_text.contains("%")) { // This is for the percentage.
            BigDecimal percentage = new BigDecimal(discount_text.replace("%", ""));
            subtotal = retail_price.multiply(qty).multiply(BigDecimal.ONE.subtract(percentage.divide(ONE_HUNDRED))).toPlainString();
        }
        else if(discount_text.contains("@")) {
            BigDecimal newPrice = new BigDecimal(discount_text.replace("@", ""));
            subtotal = newPrice.multiply(qty).toPlainString();
        }
        else {
            BigDecimal discount = new BigDecimal(discount_text);
            subtotal = retail_price.multiply(qty).subtract(discount).toPlainString();
        }
        return subtotal;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public SalesPromotion getSalesPromotion() {
        return salesPromotion;
    }

    public void setSalesPromotion(SalesPromotion salesPromotion) {
        this.salesPromotion = salesPromotion;
    }

    public String getDiscount_text() {
        return discount_text;
    }

    public void setDiscount_text(String discount_text) {
        this.discount_text = discount_text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDiscount_description() {
        return discount_description;
    }

    public void setDiscount_description(String discount_description) {
        this.discount_description = discount_description;
    }
}
