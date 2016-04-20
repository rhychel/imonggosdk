package net.nueca.concessioengine.tools;

import android.util.Log;

import com.google.gson.Gson;
import com.j256.ormlite.stmt.Where;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchPrice;
import net.nueca.imonggosdk.objects.BranchProduct;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.objects.price.PriceList;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by gama on 04/12/2015.
 */
public class PriceTools {
    public static final int
            DEFAULT_PRICE = 0,
            PRICELIST_CUSTOMER = 1,
            PRICELIST_CUSTOMERGROUP = 2,
            BRANCHPRODUCT_PRICE = 3;

    public static Double identifyRetailPrice(ImonggoDBHelper2 dbHelper2, Product product, Branch branch,
                                             CustomerGroup customerGroup, Customer customer) {
        return identifyRetailPrice(dbHelper2, product, branch, customerGroup, customer, null);
    }

    public static Double identifyRetailPrice(ImonggoDBHelper2 dbHelper2, Product product, Branch branch,
                                              CustomerGroup customerGroup, Customer customer, final Unit unit) {
        //Log.e("PriceTools", "identifyRetailPrice >>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        //Log.e("UNIT", unit == null? "null" : unit.getName());
        Double retail_price = 0d;

        try {
            retail_price = getBranchPrice(dbHelper2, product, branch, unit);
            //Log.e("BRANCH PRICE", retail_price + "");

            Price selectedPrice = identifyPrice(dbHelper2, product, branch, customerGroup, customer, unit);
            if(selectedPrice != null && selectedPrice.getRetail_price() != null)
                retail_price = selectedPrice.getRetail_price();

            //Log.e("PriceTools", "identifyRetailPrice >>>>>>>>>>> retail_price: " + retail_price);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("PriceTools", "identifyRetailPrice ~ product.Retail_price " +  product.getRetail_price());
            return product.getRetail_price();
        }

        Log.e("PriceTools", "identifyRetailPrice ~ return " +  retail_price);
        return retail_price;
    }

    public static Price identifyPrice(ImonggoDBHelper2 dbHelper2, Product product, Branch branch,
                                             CustomerGroup customerGroup, Customer customer, final Unit unit) {
        //Log.e("PriceTools", "identifyPrice <<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        if(branch == null)
            Log.e("identifyPrice", "branch is NULL");
        if(customerGroup == null)
            Log.e("identifyPrice", "customerGroup is NULL");
        if(customer == null)
            Log.e("identifyPrice", "customer is NULL");
        if(unit == null)
            Log.e("identifyPrice", "unit is NULL");

        /*Log.e("CustomerGroup", customerGroup == null? "null" : customerGroup.toJSONString());
        Log.e("Customer", customer == null? "null" : customer.toJSONString());
        Log.e("CustomerPriceList", customer == null || customer.getPriceList() == null? "null" :
                customer.getPriceList().getId() + " ~ " + customer.getPriceList().toJSONString());*/

        Unit defaultUnit = null;
        if(product.getExtras() != null && product.getExtras().getDefault_selling_unit() != null && product.getExtras()
                .getDefault_selling_unit().length() > 0) {
            try {
                defaultUnit = dbHelper2.fetchObjects(Unit.class).queryBuilder().where().eq("id", Integer.parseInt(product.getExtras()
                        .getDefault_selling_unit())).queryForFirst();
            } catch (SQLException e) { e.printStackTrace(); }
        }
        Log.e("PriceTools", product.getName() + " : " + (defaultUnit != null? "["+defaultUnit.getId()+"] "+defaultUnit.getName() : "null" ) + " " +
                (unit != null? "["+unit.getId()+"] "+unit.getName() : "null"));

        Price selectedPrice = null;
        try {
            int type = DEFAULT_PRICE;

            /** Using PriceList **/
            Price customer_price = null, customergroup_price = null;

            if (customer != null && customer.getPriceList() != null) {
                List<PriceList> t_priceLists = dbHelper2.fetchObjects(PriceList.class).queryBuilder().where().eq("id", customer.getPriceList().getId())
                        .query();
                if(t_priceLists != null && t_priceLists.size() > 0) {
                    Where<Price, ?> where = dbHelper2.fetchObjects(Price.class).queryBuilder().where().eq("product_id", product.getId())
                            .and().in("price_list_id", t_priceLists);
                    Where<Price, ?> whereNullUnit = dbHelper2.fetchObjects(Price.class).queryBuilder().where().eq("product_id", product.getId())
                            .and().in("price_list_id", t_priceLists).and().isNull("unit_id");
                    if(unit != null && unit.getId() != -1) {
                        where.and().eq("unit_id", unit);
                        customer_price = where.queryForFirst();
                    }
                    else
                        customer_price = whereNullUnit.queryForFirst();
                }
            }
            if (customerGroup != null && customerGroup.getPriceList() != null && !customerGroup.getStatus().equals("D")) {
                List<PriceList> t_priceLists = dbHelper2.fetchObjects(PriceList.class).queryBuilder().where()
                        .eq("id", customerGroup.getPriceList().getId()).query();

                if(t_priceLists != null && t_priceLists.size() > 0) {
                    Where<Price, ?> where = dbHelper2.fetchObjects(Price.class).queryBuilder().where().eq("product_id", product.getId())
                            .and().in("price_list_id", t_priceLists);
                    Where<Price, ?> whereNullUnit = dbHelper2.fetchObjects(Price.class).queryBuilder().where().eq("product_id", product.getId())
                            .and().in("price_list_id", t_priceLists).and().isNull("unit_id");
                    if(unit != null && unit.getId() != -1) {
                        where.and().eq("unit_id", unit);
                        customergroup_price = where.queryForFirst();
                    }
                    else
                        customergroup_price = whereNullUnit.queryForFirst();
                }
            }

            Log.e("CUSTOMER-GROUP-PRICE", "isNull? " + (customergroup_price == null));
            if(customergroup_price != null && customergroup_price.getRetail_price() != null) {
                selectedPrice = customergroup_price;
                type = PRICELIST_CUSTOMERGROUP;
            }
            Log.e("CUSTOMER-PRICE", "isNull? " + (customer_price == null));
            if(customer_price != null) {
                if(selectedPrice == null)
                    selectedPrice = customer_price;
                else {
                    if(customer_price.getRetail_price() != null)
                        selectedPrice.setRetail_price(customer_price.getRetail_price());
                    selectedPrice.setDiscount_text(customer_price.getDiscount_text());
                }
                type = PRICELIST_CUSTOMER;
            }

            //if(type == DEFAULT_PRICE && unit == null && defaultUnit != null)
            //    return identifyPrice(dbHelper2, product, branch, customerGroup, customer, defaultUnit);

            //Log.e("identifyRetailPrice", "type="+type);

            Log.e("Price-" + type, selectedPrice == null? "null" : "retail_price:" + selectedPrice.getRetail_price() + " for " + product.getName());
//
//            if(selectedPrice != null)
//                Log.e("Price ("+selectedPrice.getId()+") ~ " + selectedPrice.getPriceList().getId(), selectedPrice.toJSONString());
            Log.e("PriceList", new Gson().toJson(selectedPrice));
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return selectedPrice;
    }

    public static double getBranchPrice(ImonggoDBHelper2 dbHelper2, Product product, Branch branch, Unit unit) throws SQLException {
        Log.e("PriceTools", "getBranchPrice " + (dbHelper2 == null) + " " + product.getName() + " " + (branch == null) + " " + (unit == null));

        BranchProduct branchProduct = null;

        if(unit == null || unit.getId() == -1) {
            branchProduct = null;
            if(branch != null) {
                Where<BranchProduct, ?> branchProductWhere = dbHelper2.fetchObjects(BranchProduct.class).queryBuilder().where()
                        .eq("product_id", product).and().eq("branch_id", branch)/*.and().eq("isBaseUnitSellable", true)*/
                        .and().isNull("unit_id");
                branchProduct = branchProductWhere.queryForFirst();
            }
        } else {
            branchProduct = null;
            if(branch != null) {
                Where<BranchProduct, ?> branchProductWhere = dbHelper2.fetchObjects(BranchProduct.class).queryBuilder().where()
                        .eq("product_id", product).and().eq("branch_id", branch).and().eq("unit_id", unit);
                branchProduct = branchProductWhere.queryForFirst();
            }
        }

        if(branchProduct != null) {
            Log.e("PriceTools", "getBranchPrice ~ returning branchProduct.Unit_retail_price " +  branchProduct.getUnit_retail_price());
            return branchProduct.getUnit_retail_price();
        }

        if(unit != null && unit.getId() != -1) {
            Log.e("PriceTools", "getBranchPrice ~ returning unit.Retail_price " +  unit.getRetail_price());
            return unit.getRetail_price();
        }

        Log.e("PriceTools", "getBranchPrice ~ returning product.Retail_price " +  product.getRetail_price());
        return product.getRetail_price();
    }
}
