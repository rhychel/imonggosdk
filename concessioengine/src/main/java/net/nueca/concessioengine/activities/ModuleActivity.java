package net.nueca.concessioengine.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SearchViewCompat;
import android.util.Log;
import android.widget.SearchView;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.objects.ExtendedAttributes;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.concessioengine.views.SearchViewEx;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.enums.ConcessioModule;
import net.nueca.imonggosdk.enums.DocumentTypeCode;
import net.nueca.imonggosdk.enums.OfflineDataType;
import net.nueca.imonggosdk.objects.AccountSettings;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.OfflineData;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.objects.Session;
import net.nueca.imonggosdk.objects.User;
import net.nueca.imonggosdk.objects.associatives.BranchUserAssoc;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.document.DocumentType;
import net.nueca.imonggosdk.objects.order.Order;
import net.nueca.imonggosdk.objects.order.OrderLine;
import net.nueca.imonggosdk.swable.SwableTools;
import net.nueca.imonggosdk.tools.StringUtilsEx;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 6/3/15.
 * imonggosdk (c)2015
 */
public abstract class ModuleActivity extends ImonggoAppCompatActivity {

    public static final String CONCESSIO_MODULE = "concessio_module";
    protected ConcessioModule concessioModule = ConcessioModule.ORDERS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public List<String> getTransactionTypes() {
        return getTransactionTypes(true);
    }

    /**
     *
     * Get the transaction types the account can access.
     *
     * @param includeAll Include an 'All' filter.
     * @return List of transaction types
     */
    public List<String> getTransactionTypes(boolean includeAll) {
        List<String> transactionTypes = new ArrayList<>();
        if(includeAll)
            transactionTypes.add("All");
        if(AccountSettings.hasOrder(this))
            transactionTypes.add("Order");
        if(AccountSettings.hasCount(this))
            transactionTypes.add("Count");
        if(AccountSettings.hasReceive(this))
            transactionTypes.add("Receive");
        if(AccountSettings.hasPullout(this))
            transactionTypes.add("Pullout");
        return transactionTypes;
    }

    public List<Branch> getBranches() {
        List<Branch> assignedBranches = new ArrayList<>();
        try {
            List<BranchUserAssoc> branchUserAssocs = getHelper().getBranchUserAssocs().queryBuilder().where().eq("user_id", getUser()).query();
            for(BranchUserAssoc branchUser : branchUserAssocs) {
                if(branchUser.getBranch().getId() == getUser().getHome_branch_id())
                    assignedBranches.add(0, branchUser.getBranch());
                else
                    assignedBranches.add(branchUser.getBranch());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return assignedBranches;
    }

    public List<String> getProductCategories(boolean includeAll) {
        List<String> categories = new ArrayList<>();

        try {
            List<ProductTag> productTags = getHelper().getProductTags().queryBuilder().distinct().selectColumns("tag").orderByRaw("tag COLLATE NOCASE ASC").where().like("tag", "#%").query();
            for(ProductTag productTag : productTags) {
                if(productTag.getTag().matches("^#[\\w\\-\\'\\+ ]*")) {
                    String category = StringUtilsEx.ucwords(productTag.getTag().replace("#", ""));
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(includeAll)
            categories.add(0, "All");

        return categories;
    }

    protected void closeSearchField(SearchViewEx searchView) {
        SearchViewCompat.setQuery(searchView, "", false);
        SearchViewCompat.setIconified(searchView, true);
    }

    /**
     *
     * @param context
     * @param servingBranchId --- Pass the serving branch id of the order. Preferably, this should be the warehouse branch id
     * @return
     */
    public Order generateOrder(Context context, int servingBranchId) {
        Order.Builder order = new Order.Builder();
        for(int i = 0;i < ProductsAdapterHelper.getSelectedProductItems().size();i++) {
            SelectedProductItem selectedProductItem = ProductsAdapterHelper.getSelectedProductItems().get(i);
            Values value = selectedProductItem.getValues().get(0);

            OrderLine orderLine = new OrderLine.Builder()
                    .line_no(value.getLine_no())
                    .product_id(selectedProductItem.getProduct().getId())
                    .quantity(Double.valueOf(value.getQuantity()))
                    .build();
            if(value.isValidUnit()) {
                orderLine.setUnit_id(value.getUnit().getId());
                orderLine.setUnit_name(value.getUnit_name());
                orderLine.setUnit_content_quantity(value.getUnit_content_quantity());
                orderLine.setUnit_quantity(Double.valueOf(value.getUnit_quantity()));
                orderLine.setUnit_retail_price(value.getUnit_retail_price());
            }
            order.addOrderLine(orderLine);
        }
        order.order_type_code("stock_request");
        try {
            order.serving_branch_id(servingBranchId);
            order.generateReference(context, getSession().getDevice_id());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return order.build();
    }

    public Document generateDocument(Context context) {
        return generateDocument(context, -1, DocumentTypeCode.PHYSICAL_COUNT);
    }

    public Document generateDocument(Context context, int targetBranchId, DocumentTypeCode documentTypeCode) {
        Document.Builder pcount = new Document.Builder();
        for(int i = 0;i < ProductsAdapterHelper.getSelectedProductItems().size();i++) {
            SelectedProductItem selectedProductItem = ProductsAdapterHelper.getSelectedProductItems().get(i);
            for(Values value : selectedProductItem.getValues()) {
                DocumentLine.Builder builder = new DocumentLine.Builder()
                        .line_no(value.getLine_no())
                        .product_id(selectedProductItem.getProduct().getId())
                        .quantity(Double.valueOf(value.getQuantity()));
                if(value.getExtendedAttributes() != null) {
                    ExtendedAttributes extendedAttributes = value.getExtendedAttributes();
                    // if pcount
//                    extendedAttributes.setBatch_no("0");
                    builder.extended_attributes(extendedAttributes.convertForDocumentLine());
                }

                DocumentLine documentLine = builder.build();
                if(value.isValidUnit()) {
                    documentLine.setUnit_id(value.getUnit().getId());
                    documentLine.setUnit_name(value.getUnit_name());
                    documentLine.setUnit_content_quantity(value.getUnit_content_quantity());
                    documentLine.setUnit_quantity(Double.valueOf(value.getUnit_quantity()));
                    documentLine.setUnit_retail_price(value.getUnit_retail_price());
                }

                pcount.addDocumentLine(documentLine);
            }
        }
        pcount.document_type_code(documentTypeCode);
        if(targetBranchId > -1)
            pcount.target_branch_id(targetBranchId);
        try {
            pcount.generateReference(context, getSession().getDevice_id());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pcount.build();
    }
}
