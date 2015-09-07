package net.nueca.concessioengine.adapters.base;

import android.content.Context;
import android.util.Log;

import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.lists.SelectedProductItemList;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.tools.ProductListTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by gama on 9/3/15.
 */
public abstract class BaseReceiveAdapter extends BaseAdapter<DocumentLine> {
    private ImonggoDBHelper dbHelper;
    private List<Product> productList;

    public BaseReceiveAdapter(Context context, int resource, ImonggoDBHelper dbHelper, List<DocumentLine> objects) {
        super(context, resource, objects);
        setHelper(dbHelper);
        updateProductList(objects);
    }

    public void addAll(List<DocumentLine> documentLines) {
        super.addAll(documentLines);
        if(productList == null)
            productList = new ArrayList<>();

        try {
            for(DocumentLine documentLine : documentLines) {
                Product t_product = getHelper().getProducts().queryBuilder().where()
                        .eq("id", documentLine.getProduct_id()).queryForFirst();

                t_product.setUnit(documentLine.getUnit_name());

                if(documentLine.getUnit_id() != null)
                    t_product.setUnit_id(documentLine.getUnit_id());

                if(documentLine.getUnit_quantity() != null)
                    t_product.setUnit_quantity(documentLine.getUnit_quantity());

                if(documentLine.getUnit_content_quantity() != null)
                    t_product.setUnit_content_quantity(documentLine.getUnit_content_quantity());

                t_product.setOrig_quantity(""+documentLine.getQuantity());
                t_product.setRcv_quantity("0");
                t_product.setRet_quantity("0");
                t_product.setDsc_quantity(""+documentLine.getQuantity());

                productList.add(t_product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    public ImonggoDBHelper getHelper() {
        return dbHelper;
    }

    public void setHelper(ImonggoDBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

}
