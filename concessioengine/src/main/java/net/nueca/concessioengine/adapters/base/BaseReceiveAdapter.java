package net.nueca.concessioengine.adapters.base;

import android.content.Context;
import android.util.Log;

import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.document.ExtendedAttributes;
import net.nueca.imonggosdk.tools.NumberTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gama on 9/3/15.
 */
public abstract class BaseReceiveAdapter extends BaseAdapter<DocumentLine> {
    private ImonggoDBHelper dbHelper;
    protected List<Product> productList;
    protected boolean isManual = false;
    private int listItemRes;

    public BaseReceiveAdapter(Context context, int resource, ImonggoDBHelper dbHelper, List<DocumentLine> objects) {
        super(context, resource, objects);
        listItemRes = resource;
        setHelper(dbHelper);
        updateProductList(objects);
    }

    public abstract List<DocumentLine> generateDocumentLines();

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

    public void addAll(List<DocumentLine> documentLines) {
        super.addAll(documentLines);
        if(productList == null)
            productList = new ArrayList<>();

        //try {
            for(DocumentLine documentLine : documentLines) {
                Product t_product = documentLine.getProduct();

                if(productList.contains(t_product)) {
                    t_product = productList.get(productList.indexOf(t_product));

                    double orig_qty = NumberTools.toDouble(t_product.getOrig_quantity());
                    double dsc_qty = NumberTools.toDouble(t_product.getDsc_quantity());

                    t_product.setOrig_quantity("" + ( orig_qty + documentLine.getQuantity() ));
                    t_product.setRcv_quantity("0");
                    t_product.setRet_quantity("0");
                    t_product.setDsc_quantity("" + ( dsc_qty + documentLine.getQuantity() ));

                    continue;
                }

                if(t_product == null)
                    continue;

                if(documentLine.getUnit_name() != null)
                    t_product.setUnit(documentLine.getUnit_name());

                if(documentLine.getUnit_id() != null)
                    t_product.setUnit_id(documentLine.getUnit_id());

                if(documentLine.getUnit_quantity() != null)
                    t_product.setUnit_quantity(documentLine.getUnit_quantity());

                if(documentLine.getUnit_content_quantity() != null)
                    t_product.setUnit_content_quantity(documentLine.getUnit_content_quantity());

                t_product.setOrig_quantity( NumberTools.separateInCommasHideZeroDecimals(documentLine.getQuantity()) );
                t_product.setRcv_quantity("0");
                t_product.setRet_quantity("0");
                t_product.setDsc_quantity( NumberTools.separateInCommasHideZeroDecimals(documentLine.getQuantity()) );

                productList.add(t_product);
            }
        //} catch (SQLException e) {
        //    e.printStackTrace();
        //}
    }

    @Override
    public void clear() {
        super.clear();
        productList = new ArrayList<>();
    }

    public void updateProductList(List<DocumentLine> documentLines) {
        clear();
        addAll(documentLines);
    }

    public Product getProductItem(int position) {
        if(productList == null || productList.size() <= position)
            return null;
        return productList.get(position);
    }

    @Override
    public int getCount() {
        if(productList == null)
            return 0;
        return productList.size();
    }

    public ImonggoDBHelper getHelper() {
        return dbHelper;
    }

    public void setHelper(ImonggoDBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }


}
