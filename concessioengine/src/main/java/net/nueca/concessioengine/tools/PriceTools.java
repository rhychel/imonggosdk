package net.nueca.concessioengine.tools;

import android.util.Log;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchPrice;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.objects.price.PriceList;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by gama on 04/12/2015.
 */
public class PriceTools {
    public static Double identifyRetailPrice(ImonggoDBHelper2 dbHelper2, Product product, Branch branch,
                                             CustomerGroup customerGroup, Customer customer) {
        Double retail_price = product.getRetail_price();
        //Log.e("Product", "retail_price:" + retail_price + " for " + product.getName());
        try {
            // Using BranchPrice
            if (branch != null) {
                BranchPrice branchPrice = dbHelper2.getDao(BranchPrice.class).queryBuilder().where().eq("branch_id", branch.getId()).and().eq
                        ("product_id", product.getId()).queryForFirst();
                if (branchPrice != null) {
                    retail_price = branchPrice.getRetail_price();
                    Log.e("Price-BranchPrice", "retail_price:" + retail_price + " for " + product.getName());
                }
            }

            // Using PriceList
            //Log.e("PriceList", "count : " + dbHelper2.getDao(PriceList.class).countOf() + " for " + product.getName() + "~" + product.getId());
            List<PriceList> priceLists = null;
            String t = "NULL";

            if (customer != null) {
                priceLists = dbHelper2.getDao(PriceList.class).queryBuilder().where().eq("id", customer.getPriceList().getId()).query();
                t = "Customer";
            }
            else if (customerGroup != null) {
                priceLists = dbHelper2.getDao(PriceList.class).queryBuilder().where()
                        .eq("id", customerGroup.getPriceList().getId()).query();
                t = "CustomerGroup";
            }
            else if (branch != null) {
                priceLists = dbHelper2.getDao(PriceList.class).queryBuilder().where()
                        .eq("branch_id", branch.getId()).query();
                t = "Branch";
            }

            if (priceLists != null) {
                Price price = dbHelper2.getDao(Price.class).queryBuilder().where().eq("product_id", product.getId())
                        .and().in("price_list_id", priceLists).queryForFirst();

                if (price != null) {
                    retail_price = price.getRetail_price();
                    Log.e("Price-" + t, "retail_price:" + retail_price + " for " + product.getName());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return product.getRetail_price();
        }

        return retail_price;
    }
}
