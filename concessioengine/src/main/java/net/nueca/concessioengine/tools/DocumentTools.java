package net.nueca.concessioengine.tools;

import android.content.Context;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.objects.ExtendedAttributes;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.enums.DocumentTypeCode;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;

/**
 * Created by rhymartmanchus on 17/05/2016.
 */
public class DocumentTools {

    public static Document generateDocument(Context context, int deviceId, DocumentTypeCode documentTypeCode) {
        return generateDocument(context, deviceId, -1, documentTypeCode);
    }

    public static Document generateDocument(Context context, int deviceId, int targetBranchId, DocumentTypeCode documentTypeCode) {
        Document.Builder pcount = new Document.Builder();

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

                pcount.addDocumentLine(documentLine);
            }
        }
        pcount.customer(ProductsAdapterHelper.getSelectedCustomer());
        pcount.document_type_code(documentTypeCode);
        if(documentTypeCode == DocumentTypeCode.RELEASE_ADJUSTMENT || documentTypeCode == DocumentTypeCode.RELEASE_BRANCH)
            if(ProductsAdapterHelper.getReason() != null)
                pcount.document_purpose_name(ProductsAdapterHelper.getReason().getName());
        if(documentTypeCode == DocumentTypeCode.RELEASE_BRANCH)
            pcount.intransit_status(true);
        if(ProductsAdapterHelper.getParent_document_id() > -1)
            pcount.parent_document_id(ProductsAdapterHelper.getParent_document_id());
        if(targetBranchId > -1)
            pcount.target_branch_id(targetBranchId);
        pcount.generateReference(context, deviceId);
        return pcount.build();
    }

}
