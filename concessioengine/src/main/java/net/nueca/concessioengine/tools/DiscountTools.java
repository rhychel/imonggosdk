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

    public static BigDecimal applyDiscount(BigDecimal retail_price, BigDecimal qty, String discount_text, String separator) {
        List<String> discountStrs = Arrays.asList(discount_text.split(separator));

        BigDecimal discountedPrice = BigDecimal.ZERO;
        for(String discountStr : discountStrs) {
            if(discountStrs.indexOf(discountStr) == 0)
                discountedPrice = applyDiscount(retail_price, BigDecimal.ONE, discountStr);
            else
                discountedPrice = applyDiscount(discountedPrice, BigDecimal.ONE, discountStr);
        }
        return discountedPrice.multiply(qty);
    }
}
