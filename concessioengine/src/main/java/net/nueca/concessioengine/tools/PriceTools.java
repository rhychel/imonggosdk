package net.nueca.concessioengine.tools;

import android.util.Log;

import com.j256.ormlite.stmt.Where;

import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.BranchPrice;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.branchentities.BranchProduct;
import net.nueca.imonggosdk.objects.branchentities.BranchUnit;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
import net.nueca.imonggosdk.objects.price.Price;
import net.nueca.imonggosdk.objects.price.PriceList;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
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
                                             CustomerGroup customerGroup, Customer customer, Unit unit) {

        if(branch == null)
            Log.e("identifyRetailPrice", "branch is null");
        if(customerGroup == null)
            Log.e("identifyRetailPrice", "customerGroup is null");
        if(customer == null)
            Log.e("identifyRetailPrice", "customer is null");

        Log.e("CustomerGroup", customerGroup.toJSONString());
        Log.e("Customer", customer.toJSONString());
        Log.e("CustomerPriceList", customer.getPriceList() == null? "null" :
                customer.getPriceList().getId() + " ~ " + customer.getPriceList().toJSONString());

        Unit defaultUnit = null;
        if(product.getExtras() != null && product.getExtras().getDefault_selling_unit() != null && product.getExtras()
                .getDefault_selling_unit().length() > 0) {
            try {
                defaultUnit = dbHelper2.fetchObjects(Unit.class).queryBuilder().where().eq("id", Integer.parseInt(product.getExtras()
                        .getDefault_selling_unit())).queryForFirst();
            } catch (SQLException e) { e.printStackTrace(); }
        }
        Log.e("PriceTools", product.getName() + " : " + (defaultUnit != null? defaultUnit.getName() : "null" ) + " " +
                (unit != null? unit.getName() : "null"));

        Double retail_price = product.getRetail_price();
        //Log.e("Product", "retail_price:" + retail_price + " for " + product.getName());
        try {
            int type = DEFAULT_PRICE;
            BranchProduct branchProduct = getBranchProduct(dbHelper2, product, branch);
            if(branchProduct != null) {
                retail_price = branchProduct.getRetail_price();
                type = BRANCHPRODUCT_PRICE;
                Log.e("BRANCH PRICE", retail_price + " " + branchProduct.toString());
                /*if(branchProduct.getBranchUnits() != null) {
                    List<BranchUnit> units = new ArrayList<>(branchProduct.getBranchUnits());
                    for (int i = 0; i < units.size(); i++) {
                        BranchUnit bu = units.get(i);
                        Log.e("BRANCH_UNIT", bu.getUnit().getName() + " " + bu.getRetail_price());
                    }
                }*/
            }

            /** Using PriceList **/
            //Log.e("PriceList", "count : " + dbHelper2.fetchObjects(PriceList.class).countOf() + " for " + product.getName() + "~" + product.getId());
            Price customer_price = null, customergroup_price = null;// branch_price = null;

            if (customer != null && customer.getPriceList() != null) {
                List<PriceList> t_priceLists = dbHelper2.fetchObjects(PriceList.class).queryBuilder().where().eq("id", customer.getPriceList().getId())
                        .query();
                if(t_priceLists != null && t_priceLists.size() > 0) {
                    Where<Price, ?> where = dbHelper2.fetchObjects(Price.class).queryBuilder().where().eq("product_id", product.getId())
                            .and().in("price_list_id", t_priceLists);
                    Where<Price, ?> whereNullUnit = dbHelper2.fetchObjects(Price.class).queryBuilder().where().eq("product_id", product.getId())
                            .and().in("price_list_id", t_priceLists).and().isNull("unit_id");
                    if(unit != null)
                        where.and().eq("unit_id", unit);

                    customer_price = where.queryForFirst();
                    if(customer_price == null)
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
                    if(unit != null)
                        where.and().eq("unit_id", unit);

                    customergroup_price = where.queryForFirst();
                    if(customergroup_price == null)
                        customergroup_price = whereNullUnit.queryForFirst();
                }
            }
            /*if (branch != null) {
                List<PriceList> t_priceLists = dbHelper2.fetchObjects(PriceList.class).queryBuilder().where()
                        .eq("branch_id", branch.getId()).query();

                if(t_priceLists != null && t_priceLists.size() > 0) {
                    Where<Price, ?> where = dbHelper2.fetchObjects(Price.class).queryBuilder().where().eq("product_id", product.getId())
                            .and().in("price_list_id", t_priceLists);
                    Where<Price, ?> whereNullUnit = dbHelper2.fetchObjects(Price.class).queryBuilder().where().eq("product_id", product.getId())
                            .and().in("price_list_id", t_priceLists).and().isNull("unit_id");
                    if(unit != null)
                        where.and().eq("unit_id", unit);

                    branch_price = where.queryForFirst();
                    if(branch_price == null)
                        branch_price = whereNullUnit.queryForFirst();
                }
            }*/

            Price selectedPrice = null;
            /*if(branch_price != null) {
                selectedPrice = branch_price;
                if(branch_price.getRetail_price() != null)
                    retail_price = branch_price.getRetail_price();
                type = PRICELIST_BRANCH;
            }*/
            Log.e("CUSTOMER-GROUP-PRICE", "isNull? " + (customergroup_price == null));
            if(customergroup_price != null) {
                selectedPrice = customergroup_price;
                if(customergroup_price.getRetail_price() != null)
                    retail_price = customergroup_price.getRetail_price();
                type = PRICELIST_CUSTOMERGROUP;
            }
            Log.e("CUSTOMER-PRICE", "isNull? " + (customer_price == null));
            if(customer_price != null) {
                selectedPrice = customer_price;
                if(customer_price.getRetail_price() != null)
                    retail_price = customer_price.getRetail_price();
                type = PRICELIST_CUSTOMER;
            }

            if(type == DEFAULT_PRICE && unit == null && defaultUnit != null)
                return identifyRetailPrice(dbHelper2, product, branch, customerGroup, customer, defaultUnit);

            Log.e("identifyRetailPrice", "type="+type);

            Log.e("Price-" + type, "retail_price:" + retail_price + " for " + product.getName());
            if(selectedPrice != null)
                Log.e("Price ("+selectedPrice.getId()+") ~ " + selectedPrice.getPriceList().getId(), selectedPrice.toJSONString());
        } catch (SQLException e) {
            e.printStackTrace();
            return product.getRetail_price();
        }

        return retail_price;

        /*Price price = identifyPrice(dbHelper2, product, branch, customerGroup, customer, unit);
        if(price == null)
            return product.getRetail_price();
        return price.getRetail_price();*/
    }

    public static Price identifyPrice(ImonggoDBHelper2 dbHelper2, Product product, Branch branch,
                                             CustomerGroup customerGroup, Customer customer, Unit unit) {
        Unit defaultUnit = null;
        if(product.getExtras() != null && product.getExtras().getDefault_selling_unit() != null && product.getExtras()
                .getDefault_selling_unit().length() > 0) {
            try {
                defaultUnit = dbHelper2.fetchObjects(Unit.class).queryBuilder().where().eq("id", Integer.parseInt(product.getExtras()
                        .getDefault_selling_unit())).queryForFirst();
            } catch (SQLException e) { e.printStackTrace(); }
        }
        Log.e("PriceTools", product.getName() + " : " + (defaultUnit != null? defaultUnit.getName() : "null" ) + " " +
                (unit != null? unit.getName() : "null"));

        Price price = null; // product.getRetail_price();
        //Log.e("Product", "retail_price:" + retail_price + " for " + product.getName());
        try {

            /** Using PriceList **/
            //Log.e("PriceList", "count : " + dbHelper2.fetchObjects(PriceList.class).countOf() + " for " + product.getName() + "~" + product.getId());
            int type = DEFAULT_PRICE;
            Price customer_price = null, customergroup_price = null; // branch_price = null;

            if (customer != null && customer.getPriceList() != null) {
                List<PriceList> t_priceLists = dbHelper2.fetchObjects(PriceList.class).queryBuilder().where().eq("id", customer.getPriceList().getId())
                        .query();
                if(t_priceLists != null && t_priceLists.size() > 0) {
                    Where<Price, ?> where = dbHelper2.fetchObjects(Price.class).queryBuilder().where().eq("product_id", product.getId())
                            .and().in("price_list_id", t_priceLists);
                    Where<Price, ?> whereNullUnit = dbHelper2.fetchObjects(Price.class).queryBuilder().where().eq("product_id", product.getId())
                            .and().in("price_list_id", t_priceLists).and().isNull("unit_id");
                    if(unit != null)
                        where.and().eq("unit_id", unit);

                    customer_price = where.queryForFirst();
                    if(customer_price == null)
                        customer_price = whereNullUnit.queryForFirst();
                }
            }
            if (customerGroup != null && customerGroup.getPriceList() != null) {
                List<PriceList> t_priceLists = dbHelper2.fetchObjects(PriceList.class).queryBuilder().where()
                        .eq("id", customerGroup.getPriceList().getId()).query();

                if(t_priceLists != null && t_priceLists.size() > 0) {
                    Where<Price, ?> where = dbHelper2.fetchObjects(Price.class).queryBuilder().where().eq("product_id", product.getId())
                            .and().in("price_list_id", t_priceLists);
                    Where<Price, ?> whereNullUnit = dbHelper2.fetchObjects(Price.class).queryBuilder().where().eq("product_id", product.getId())
                            .and().in("price_list_id", t_priceLists).and().isNull("unit_id");
                    if(unit != null)
                        where.and().eq("unit_id", unit);

                    customergroup_price = where.queryForFirst();
                    if(customergroup_price == null)
                        customergroup_price = whereNullUnit.queryForFirst();
                }
            }
            /*if (branch != null) {
                List<PriceList> t_priceLists = dbHelper2.fetchObjects(PriceList.class).queryBuilder().where()
                        .eq("branch_id", branch.getId()).query();

                if(t_priceLists != null && t_priceLists.size() > 0) {
                    Where<Price, ?> where = dbHelper2.fetchObjects(Price.class).queryBuilder().where().eq("product_id", product.getId())
                            .and().in("price_list_id", t_priceLists);
                    Where<Price, ?> whereNullUnit = dbHelper2.fetchObjects(Price.class).queryBuilder().where().eq("product_id", product.getId())
                            .and().in("price_list_id", t_priceLists).and().isNull("unit_id");
                    if(unit != null)
                        where.and().eq("unit_id", unit);

                    branch_price = where.queryForFirst();
                    if(branch_price == null)
                        branch_price = whereNullUnit.queryForFirst();
                }
            }*/

            /*if(branch_price != null) {
                price = branch_price;
                type = PRICELIST_BRANCH;
            }*/
            if(customergroup_price != null) {
                price = customergroup_price;
                type = PRICELIST_CUSTOMERGROUP;
            }
            if(customer_price != null) {
                price = customer_price;
                type = PRICELIST_CUSTOMER;
            }

            Log.e("PRICE_TOOLS", "type = " + type);

            if(type == DEFAULT_PRICE && unit == null && defaultUnit != null)
                return identifyPrice(dbHelper2, product, branch, customerGroup, customer, defaultUnit);

            //Log.e("Price-" + type, "retail_price:" + (price!=null? price.getRetail_price() : "null") + " for " + product.getName());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return price;
    }

    private static BranchProduct getBranchProduct(ImonggoDBHelper2 dbHelper2, Product product, Branch branch) throws SQLException {
        return dbHelper2.fetchObjects(BranchProduct.class).queryBuilder().where()
                .eq("product_id", product).and().eq("branch_id", branch).queryForFirst();
    }
}
