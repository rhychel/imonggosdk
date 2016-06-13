package net.nueca.concessioengine.adapters.base;

import android.content.Context;
import android.util.Log;

import net.nueca.concessioengine.lists.SelectedProductItemList2;
import net.nueca.concessioengine.objects.ExtendedAttributes;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.document.DocumentLine;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 9/3/15.
 */
public abstract class BaseReceiveAdapter extends BaseAdapter<DocumentLine> {
    private ImonggoDBHelper2 dbHelper;
    protected boolean isManual = false, isReview = false;//, isMultiline = false;
    private int listItemRes;
    protected SelectedProductItemList2 displayProductListItem = new SelectedProductItemList2();
    protected SelectedProductItemList2 receivedProductListItem = new SelectedProductItemList2();

    public BaseReceiveAdapter(Context context, int resource, ImonggoDBHelper2 dbHelper) {
        super(context, resource, new ArrayList<DocumentLine>());
        listItemRes = resource;
        setHelper(dbHelper);
    }

    public SelectedProductItemList2 getDisplayProductListItem() {
        if(displayProductListItem == null)
            displayProductListItem = new SelectedProductItemList2();
        return displayProductListItem;
    }

    public SelectedProductItemList2 getReceivedProductListItem() {
        receivedProductListItem.sort();
        if(isManual)
            receivedProductListItem.removeZeroValue();
        return receivedProductListItem;
    }

    public void setReceivedProductListItem(SelectedProductItemList2 receivedProductListItem) {
        this.receivedProductListItem = receivedProductListItem;
        this.receivedProductListItem.sort();
    }

    public void setListItemResource(int resource) {
        listItemRes = resource;
    }
    public int getListItemResource() {
        return listItemRes;
    }

    public boolean isManual() {
        return isManual;
    }

    public void setIsManual(boolean isManual) {
        this.isManual = isManual;
    }

    public boolean isReview() {
        return isReview;
    }

    public void setIsReview(boolean isReview) {
        this.isReview = isReview;
    }

    /*public boolean isMultiline() {
        return isMultiline;
    }

    public void setIsMultiline(boolean isMultiline) {
        this.isMultiline = isMultiline;
    }*/

    public void addAll(List<DocumentLine> documentLines) {
        super.addAll(documentLines);

        if(displayProductListItem == null)
            displayProductListItem = new SelectedProductItemList2();

        for(DocumentLine documentLine : documentLines) {
            Product product = documentLine.getProduct();

            product.setUnit(documentLine.getUnit_name());
            if(documentLine.getUnit_content_quantity() != null)
                product.setUnit_content_quantity(documentLine.getUnit_content_quantity());
            if(documentLine.getUnit_id() != null)
                product.setUnit_id(documentLine.getUnit_id());
            if(documentLine.getUnit_quantity() != null)
                product.setUnit_quantity(documentLine.getUnit_quantity());

            SelectedProductItem selectedProductItem = receivedProductListItem.getSelectedProductItem(product);

            if(selectedProductItem == null) {
                selectedProductItem = new SelectedProductItem();
                selectedProductItem.setProduct(product);
                if(product.getExtras() != null)
                    selectedProductItem.setIsMultiline(product.getExtras().isBatch_maintained() == null?
                            false : product.getExtras().isBatch_maintained());

                /*Log.e("Adding", product.getName() + " " + displayProductListItem.add(selectedProductItem));
                Log.e("Size", displayProductListItem.size() + "");*/

                displayProductListItem.add(selectedProductItem);
            }
            else {
                displayProductListItem.add(selectedProductItem);
                continue;
            }


            Unit unit = null;
            try {
                unit = getHelper().fetchObjects(Unit.class).queryBuilder().where().eq("id", product.getUnit_id()).queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Values values = new Values();
            ExtendedAttributes extendedAttributes = new ExtendedAttributes(0d, documentLine.getQuantity());
            if(documentLine.getExtras() != null) {
                extendedAttributes.setBatch_no(documentLine.getExtras().getBatch_no());
            }
            if(documentLine.getExtras() != null) {
                extendedAttributes.setDelivery_date(documentLine.getExtras().getDelivery_date());
                extendedAttributes.setBrand(documentLine.getExtras().getBrand());
            }
            Log.e("BASE_RECEIVE_ADAPTER", "values.setValue(\"0\", unit, extendedAttributes)");
            values.setValue("0", unit, extendedAttributes);

            values.setLine_no(documentLine.getLine_no());
            selectedProductItem.addValues(values);
        }
    }
    public void addAllReceived(List<SelectedProductItem> receivedProductItemList) {
        //if(!displayProductListItem.containsAll(selectedProductItemList2))
        displayProductListItem.addAll(receivedProductItemList);
    }

    public void clear() {
        super.clear();
        if(displayProductListItem == null)
            displayProductListItem = new SelectedProductItemList2();
        else
            displayProductListItem.clear();
    }

    public void updateSelectedProduct(List<DocumentLine> documentLines) {
        clear();
        addAll(documentLines);
    }
    public void updateReceivedProduct(List<SelectedProductItem> receivedProductItemList) {
        clear();
        addAllReceived(receivedProductItemList);
    }

    @Override
    public int getCount() {
        if(displayProductListItem == null)
            return 0;
        return displayProductListItem.size();
    }

    public ImonggoDBHelper2 getHelper() {
        return dbHelper;
    }

    public void setHelper(ImonggoDBHelper2 dbHelper) {
        this.dbHelper = dbHelper;
    }


}
