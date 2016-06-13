package net.nueca.concessioengine.lists;

import android.util.Log;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import net.nueca.concessioengine.objects.ReceivedProductItem;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.ProductTag;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by gama on 31/05/2016.
 */
public class ReceivedProductItemList extends HashMap<Integer, ReceivedProductItem> {
    public ReceivedProductItem put(Product product) {
        return super.put(product.getId(), new ReceivedProductItem(product));
    }

    @Override
    public boolean containsKey(Object key) {
        if(key instanceof Product)
            return super.containsKey(((Product) key).getId());
        return super.containsKey(key);
    }

    @Override
    public ReceivedProductItem get(Object key) {
        if(key instanceof Product)
            return super.get(((Product) key).getId());
        return super.get(key);
    }

    public ReceivedProductItem getItemAt(int index) {
        return toList().get(index);
    }

    public List<ReceivedProductItem> toList(ImonggoDBHelper2 helper, String searchkey, String category) throws SQLException {
        boolean hasSearchkey = searchkey != null && !searchkey.isEmpty();
        boolean hasCategory = category != null && !category.isEmpty() && !category.toLowerCase().equals("all");

        QueryBuilder<ProductTag, Integer> whereTag = helper.fetchIntId(ProductTag.class).queryBuilder();
        whereTag.selectColumns("product_id").where().like("searchKey", "%" + category.toLowerCase() + "%");
        List<Product> productListWithTag = helper.fetchIntId(Product.class).queryBuilder().where().in("id", whereTag).query();

        List<ReceivedProductItem> itemList = new ArrayList<>();
        for(ReceivedProductItem item : values()) {
            boolean containsKey = hasSearchkey && item.getProduct().getSearchKey().toLowerCase().contains(searchkey.toLowerCase());
            boolean containsCategory = hasCategory && productListWithTag.contains(item.getProduct());

            if(hasSearchkey && hasCategory && containsKey && containsCategory)
                itemList.add(item);
            else if(hasSearchkey && !hasCategory && containsKey)
                itemList.add(item);
            else if(!hasSearchkey && hasCategory && containsCategory)
                itemList.add(item);
            else if(!hasSearchkey && !hasCategory)
                itemList.add(item);
        }

        return itemList;
    }

    public List<ReceivedProductItem> toList() {
        return new ArrayList<>(values());
    }

    public double getSubtotal() {
        double subtotal = 0d;
        for(ReceivedProductItem item : values())
            subtotal += item.getSubtotal();
        return subtotal;
    }
}
