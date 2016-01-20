package net.nueca.concessioengine.tools;

import android.util.Log;

import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.salespromotion.Discount;
import net.nueca.imonggosdk.objects.salespromotion.SalesPromotion;
import net.nueca.imonggosdk.tools.NumberTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by gama on 13/11/2015.
 */
public class DiscountTools {
    private static final String TAG = "DiscountTools";
    public static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    public static BigDecimal applyAllPercentDiscount(Product product) {
        BigDecimal retailPrice = new BigDecimal(product.getRetail_price());
        BigDecimal quantity = new BigDecimal(product.getQuantity());
        BigDecimal subtotal = retailPrice.multiply(quantity);

        List<Discount> discounts = product.getDiscountsList();
        for(Discount discount : discounts) {
            if(!isValid(discount.getDiscount_text())) {
                Log.d(TAG, "applyAllPercentDiscount : applyBestDiscount : isValid? false --- " + discount.getDiscount_text());
                continue;
            }

            if(discount.getSalesPromotion() != null) {
                SalesPromotion salesPromotion = discount.getSalesPromotion();
                Date today = new Date();
                Date endDate = salesPromotion.getToDate();

                if(today.after(endDate))
                    continue;
            }


        }

        return subtotal;
    }

    public static BigDecimal applyBestDiscount(Product product) {
        BigDecimal retailPrice = new BigDecimal(product.getRetail_price());
        BigDecimal quantity = new BigDecimal(product.getQuantity());
        BigDecimal subtotal = retailPrice.multiply(quantity);

        List<Discount> discounts = product.getDiscountsList();
        for(Discount discount : discounts) {
            if(!isValid(discount.getDiscount_text())) {
                Log.d(TAG, "applyAllPercentDiscount : applyBestDiscount : isValid? false --- " + discount.getDiscount_text());
                continue;
            }

            if(discount.getSalesPromotion() != null) {
                SalesPromotion salesPromotion = discount.getSalesPromotion();
                Date today = new Date();
                Date endDate = salesPromotion.getToDate();

                if(today.after(endDate))
                    continue;
            }

            BigDecimal discountedPrice;

            if(discount.getDiscount_text().contains("Q"))
                discountedPrice = applyWhenQuantityDiscount(retailPrice, quantity, discount.getDiscount_text());
            else
                discountedPrice = applyDiscount(retailPrice, quantity, discount.getDiscount_text());

            if(subtotal.compareTo(discountedPrice) > 0)
                subtotal = discountedPrice;
        }

        return subtotal;
    }

    private static BigDecimal applyWhenQuantityDiscount(BigDecimal retail_price, BigDecimal qty, String discount_text) {
        JSONObject discountJson = null;
        try {
            discountJson = new JSONObject(discount_text.replace("Q", ""));
            BigDecimal whenQuantity = new BigDecimal(discountJson.getDouble("qty"));

            if(qty.compareTo(whenQuantity) == 1 || qty.compareTo(whenQuantity) == 0)
                return applyDiscount(retail_price, qty, discountJson.getString("discount"));
            else
                return retail_price.multiply(qty);
        } catch (JSONException e) {
            e.printStackTrace();
            return retail_price.multiply(qty);
        }
    }

    public static BigDecimal applyDiscount(BigDecimal retail_price, BigDecimal qty, String discount_text) {
        BigDecimal subtotal;
        if(discount_text.contains("%")) { // Percent Discount
            BigDecimal percentage = new BigDecimal(discount_text.replace("%", ""));
            subtotal = retail_price.multiply(qty).multiply(BigDecimal.ONE.subtract(percentage.divide(ONE_HUNDRED)));
        }
        else if(discount_text.contains("@")) { // Price Change
            BigDecimal newPrice = new BigDecimal(discount_text.replace("@", ""));
            subtotal = newPrice.multiply(qty);
        }
        else { // Price Fixed Reduction
            BigDecimal discount = new BigDecimal(discount_text);
            subtotal = retail_price.multiply(qty).subtract(discount);
        }
        return subtotal;
    }

    /**
     * 10%|10|@10 -- When no Q, check value if it is a valid discount value;
     * Q{qty:<q>,discount:<10%|10|@10>}
     */
    public static boolean isValid(String discount_text) {
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

    public static boolean isValidDiscountValue(String discount_text) {
        return discount_text.matches("@?-?\\d+(\\.(\\d+))?") ||
                discount_text.matches("-?\\d+(\\.(\\d+))?%?") ||
                discount_text.matches("-?\\d+(\\.(\\d+))?");
    }

    public static BigDecimal applyMultipleDiscounts(BigDecimal retail_price, final BigDecimal qty, String discount_text, String separator) {
        if(discount_text == null || discount_text.length() == 0)
            return retail_price.multiply(qty);

        List<String> discountStrs = Arrays.asList(discount_text.split(separator));

        BigDecimal discountedPrice = retail_price;
        for(String discountStr : discountStrs) {
            if(!isValid(discountStr))
                continue;
            discountedPrice = applyDiscount(discountedPrice, BigDecimal.ONE, discountStr);
        }
        return discountedPrice.multiply(qty);
    }

    public static BigDecimal applyMultipleDiscounts(BigDecimal retail_price, final BigDecimal qty, List<Double> discounts,String discount_text, String
            separator) {
        if(discount_text == null || discount_text.length() == 0)
            return retail_price.multiply(qty);

        List<String> discountStrs = Arrays.asList(discount_text.split(separator));

        BigDecimal discountedPrice = retail_price;
        for(String discountStr : discountStrs) {
            if(!isValid(discountStr))
                continue;
            BigDecimal incomingDiscount = applyDiscount(discountedPrice, BigDecimal.ONE, discountStr);
            discounts.add(discountedPrice.subtract(incomingDiscount).doubleValue() * qty.doubleValue());
            if(incomingDiscount.doubleValue() >= 0d)
                discountedPrice = incomingDiscount;
        }
        return discountedPrice.multiply(qty);
    }

    public static BigDecimal applyMultipleDiscounts(BigDecimal retail_price, final BigDecimal qty, List<Double> product_discounts, List<Double>
            company_discounts, String discount_text, String discount_group_separator, String discount_separator, List<Double>
            customer_discounts, String customer_discount_text) {
        if(discount_text == null || discount_text.length() == 0)
            return retail_price.multiply(qty);

        List<String> discountGroup = Arrays.asList(discount_text.split(discount_group_separator));

        List<String> p_discountStrs = Arrays.asList(discountGroup.get(0).split(discount_separator));
        List<String> co_discountStrs = Arrays.asList(discountGroup.get(1).split(discount_separator));
        List<String> cu_discountStrs = new ArrayList<>();

        if(customer_discount_text != null && customer_discount_text.length() > 0)
            cu_discountStrs = Arrays.asList(customer_discount_text.split(discount_separator));

        BigDecimal discountedPrice = retail_price;

        Log.e("PRODUCT_DISCOUNT_STR", discountGroup.get(0));
        for(String discountStr : p_discountStrs) {
            if(!isValid(discountStr))
                continue;
            BigDecimal incomingDiscount = applyDiscount(discountedPrice, BigDecimal.ONE, discountStr);
            product_discounts.add(discountedPrice.subtract(incomingDiscount).doubleValue() * qty.doubleValue());
            if(incomingDiscount.doubleValue() >= 0d)
                discountedPrice = incomingDiscount;
            Log.e("P Discount " + product_discounts.size(), product_discounts.get(product_discounts.size()-1)+"");
        }

        Log.e("COMPANY_DISCOUNT_STR", discountGroup.get(1));
        for(String discountStr : co_discountStrs) {
            if(!isValid(discountStr))
                continue;
            BigDecimal incomingDiscount = applyDiscount(discountedPrice, BigDecimal.ONE, discountStr);
            company_discounts.add(discountedPrice.subtract(incomingDiscount).doubleValue() * qty.doubleValue());
            if(incomingDiscount.doubleValue() >= 0d)
                discountedPrice = incomingDiscount;
            Log.e("Co Discount " + company_discounts.size(), company_discounts.get(company_discounts.size()-1)+"");
        }

        Log.e("CUSTOMER_DISCOUNT_STR", customer_discount_text);
        for(String discountStr : cu_discountStrs) {
            if(!isValid(discountStr))
                continue;
            BigDecimal incomingDiscount = applyDiscount(discountedPrice, BigDecimal.ONE, discountStr);
            customer_discounts.add(discountedPrice.subtract(incomingDiscount).doubleValue() * qty.doubleValue());
            if(incomingDiscount.doubleValue() >= 0d)
                discountedPrice = incomingDiscount;
            Log.e("Cu Discount " + customer_discounts.size(), customer_discounts.get(customer_discounts.size()-1)+"");
        }

        Log.e("FINAL DISCOUNTED PRICE", discountedPrice.multiply(qty).doubleValue() + "");
        return discountedPrice.multiply(qty);
    }
}
