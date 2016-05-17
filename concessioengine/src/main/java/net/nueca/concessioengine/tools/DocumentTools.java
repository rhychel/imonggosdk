package net.nueca.concessioengine.tools;

import android.content.Context;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.objects.ExtendedAttributes;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.enums.DocumentTypeCode;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymartmanchus on 17/05/2016.
 */
public class DocumentTools {

    public static Document generateDocument(Context context, int deviceId, DocumentTypeCode documentTypeCode) {
        return generateDocument(context, deviceId, -1, documentTypeCode);
    }

    public static Document generateDocument(Context context, int deviceId, int targetBranchId, DocumentTypeCode documentTypeCode) {
        Document.Builder document = new Document.Builder();

        for(SelectedProductItem selectedProductItem : ProductsAdapterHelper.getSelectedProductItems()) {
            for(Values value : selectedProductItem.getValues()) {
                DocumentLine.Builder builder = new DocumentLine.Builder()
                        .line_no(value.getLine_no())
                        .product_id(selectedProductItem.getProduct().getId())
                        .quantity(Double.valueOf(value.getActualQuantity()));
                if(value.getExtendedAttributes() != null) {
                    ExtendedAttributes extendedAttributes = value.getExtendedAttributes();
                    builder.extras(extendedAttributes.convertForDocumentLine());
                }

                DocumentLine documentLine = builder.build();
                if(value.isValidUnit()) {
                    documentLine.setUnit_id(value.getUnit().getId());
                    documentLine.setUnit_name(value.getUnit_name());
                    documentLine.setUnit_content_quantity(value.getUnit_content_quantity());
                    documentLine.setUnit_quantity(Double.valueOf(value.getUnit_quantity()));
                    documentLine.setUnit_retail_price(value.getUnit_retail_price());
                }
                else {
                    documentLine.setRetail_price(value.getRetail_price());
                    documentLine.setUnit_retail_price(value.getUnit_retail_price());
                }

                document.addDocumentLine(documentLine);
            }
        }
        document.customer(ProductsAdapterHelper.getSelectedCustomer());
        document.document_type_code(documentTypeCode);
        if(documentTypeCode == DocumentTypeCode.RELEASE_ADJUSTMENT || documentTypeCode == DocumentTypeCode.RELEASE_BRANCH)
            if(ProductsAdapterHelper.getReason() != null)
                document.document_purpose_name(ProductsAdapterHelper.getReason().getName());
        if(documentTypeCode == DocumentTypeCode.RELEASE_BRANCH)
            document.intransit_status(true);
        if(ProductsAdapterHelper.getParent_document_id() > -1)
            document.parent_document_id(ProductsAdapterHelper.getParent_document_id());
        if(targetBranchId > -1)
            document.target_branch_id(targetBranchId);
        document.generateReference(context, deviceId);
        return document.build();
    }

    public static List<Product> generateSelectedItemList(ImonggoDBHelper2 dbHelper, Document document) throws SQLException {
        return generateSelectedItemList(dbHelper, document, false);
    }

    public static List<Product> generateSelectedItemList(ImonggoDBHelper2 dbHelper, Document document, boolean isMultiItem) throws SQLException {
        List<Product> productList = new ArrayList<>();

        List<DocumentLine> documentLines = document.getDocument_lines();
        for(DocumentLine documentLine : documentLines) {
            Product product = dbHelper.fetchIntId(Product.class).queryForId(documentLine.getProduct_id());
            if(productList.indexOf(product) == -1)
                productList.add(product);

            SelectedProductItem selectedProductItem = ProductsAdapterHelper.getSelectedProductItems().initializeItem(product);

            selectedProductItem.setIsMultiline(isMultiItem);

            String quantity = "0";
            Unit unit = null;
            if(documentLine.getUnit_id() != null)
                unit = dbHelper.fetchIntId(Unit.class).queryForId(documentLine.getUnit_id());
            if(unit != null) {
                quantity = documentLine.getUnit_quantity().toString();
                unit.setRetail_price(documentLine.getUnit_retail_price());
            }
            else {
                unit = new Unit();
                unit.setId(-1);
                unit.setName(product.getBase_unit_name());
                unit.setRetail_price(documentLine.getUnit_retail_price());
                quantity = String.valueOf(documentLine.getQuantity());
            }
            Values values = null;
            if(document.getDocument_type_code() == DocumentTypeCode.RECEIVE_BRANCH) {
                ExtendedAttributes extendedAttributes = new ExtendedAttributes(0d, Double.valueOf(quantity));
                values = new Values();
                if(documentLine.getUnit_name() != null)
                    values.setUnit_name(documentLine.getUnit_name());
                if(documentLine.getUnit_content_quantity() != null)
                    values.setUnit_content_quantity(documentLine.getUnit_content_quantity());
                if(documentLine.getUnit_retail_price() != null)
                    values.setUnit_retail_price(documentLine.getUnit_retail_price());
                if(documentLine.getUnit_quantity() != null)
                    values.setUnit_quantity(""+documentLine.getUnit_quantity());

                if(unit.getId() == -1)
                    values.setRetail_price(documentLine.getRetail_price());
                else
                    values.setRetail_price(unit.getRetail_price());

                values.setValue("0.0", unit, extendedAttributes);
            }
            else {
                values = new Values(unit, quantity);
                values.setUnit_retail_price(unit.getRetail_price());
                if(documentLine.getExtras() != null) {
                    ExtendedAttributes extendedAttributes = new ExtendedAttributes(documentLine.getExtras());
                    values.setExtendedAttributes(extendedAttributes);
                }
            }
            values.setLine_no(documentLine.getLine_no());
            selectedProductItem.addValues(values);
            selectedProductItem.setInventory(product.getInventory());
            ProductsAdapterHelper.getSelectedProductItems().add(selectedProductItem);
        }

        return productList;
    }
}
