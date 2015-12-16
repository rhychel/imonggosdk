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
                BranchPrice branchPrice = dbHelper2.fetchObjects(BranchPrice.class).queryBuilder().where().eq("branch_id", branch.getId()).and().eq
                        ("product_id", product.getId()).queryForFirst();
                if (branchPrice != null) {
                    retail_price = branchPrice.getRetail_price();
                    //Log.e("Price-BranchPrice", "retail_price:" + retail_price + " for " + product.getName());
                }
            }

            // Using PriceList
            //Log.e("PriceList", "count : " + dbHelper2.fetchObjects(PriceList.class).countOf() + " for " + product.getName() + "~" + product.getId());
            String type = "DEFAULT";
            Price customer_price = null, customergroup_price = null, branch_price = null;

            if (customer != null && customer.getPriceList() != null) {
                List<PriceList> t_priceLists = dbHelper2.fetchObjects(PriceList.class).queryBuilder().where().eq("id", customer.getPriceList().getId())
                        .query();
                if(t_priceLists != null && t_priceLists.size() > 0) {
                    customer_price = dbHelper2.fetchObjects(Price.class).queryBuilder().where().eq("product_id", product.getId())
                            .and().in("price_list_id", t_priceLists).queryForFirst();
                }
            }
            if (customerGroup != null && customerGroup.getPriceList() != null) {
                List<PriceList> t_priceLists = dbHelper2.fetchObjects(PriceList.class).queryBuilder().where()
                        .eq("id", customerGroup.getPriceList().getId()).query();

                if(t_priceLists != null && t_priceLists.size() > 0) {
                    customergroup_price = dbHelper2.fetchObjects(Price.class).queryBuilder().where().eq("product_id", product.getId())
                            .and().in("price_list_id", t_priceLists).queryForFirst();
                }
            }
            if (branch != null) {
                List<PriceList> t_priceLists = dbHelper2.fetchObjects(PriceList.class).queryBuilder().where()
                        .eq("branch_id", branch.getId()).query();

                if(t_priceLists != null && t_priceLists.size() > 0) {
                    branch_price = dbHelper2.fetchObjects(Price.class).queryBuilder().where().eq("product_id", product.getId())
                            .and().in("price_list_id", t_priceLists).queryForFirst();
                }
            }

            if(branch_price != null) {
                retail_price = branch_price.getRetail_price();
                type = "BRANCH";
            }
            if(customergroup_price != null) {
                retail_price = customergroup_price.getRetail_price();
                type = "CUSTOMER_GROUP";
            }
            if(customer_price != null) {
                retail_price = customer_price.getRetail_price();
                type = "CUSTOMER";
            }

            Log.e("Price-" + type, "retail_price:" + retail_price + " for " + product.getName());
        } catch (SQLException e) {
            e.printStackTrace();
            return product.getRetail_price();
        }

        return retail_price;
    }
}
