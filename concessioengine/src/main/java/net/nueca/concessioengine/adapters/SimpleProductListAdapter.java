package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.lists.SelectedProductItemList;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.operations.ImonggoTools;

import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public class SimpleProductListAdapter extends BaseProductsAdapter {

    public SimpleProductListAdapter(Context context, List<Product> objects) {
        super(context, R.layout.simple_product_listitem, objects);
    }

    public SimpleProductListAdapter(Context context, ImonggoDBHelper dbHelper, List<Product> objects) {
        super(context, R.layout.simple_product_listitem, objects);
        setDbHelper(dbHelper);
    }

    public void setDbHelper(ImonggoDBHelper dbHelper) {
        ProductsAdapterHelper.setDbHelper(dbHelper);
    }

    private static class ListViewHolder {
        NetworkImageView ivProductImage;
        AutofitTextView tvProductName, tvQuantity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ListViewHolder lvh = null;

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.simple_product_listitem, null);
            lvh = new ListViewHolder();

            lvh.ivProductImage = (NetworkImageView) convertView.findViewById(R.id.ivProductImage);
            lvh.tvProductName = (AutofitTextView) convertView.findViewById(R.id.tvProductName);
            lvh.tvQuantity = (AutofitTextView) convertView.findViewById(R.id.tvQuantity);

            lvh.ivProductImage.setDefaultImageResId(R.drawable.no_image);
            lvh.ivProductImage.setErrorImageResId(R.drawable.no_image);

            convertView.setTag(lvh);
        }
        else
            lvh = (ListViewHolder) convertView.getTag();

        Product product = getItem(position);

        lvh.tvProductName.setText(product.getName());
        lvh.tvQuantity.setText(getSelectedProductItems().getQuantity(product.getId()));
        String imageUrl = ImonggoTools.buildProductImageUrl(getContext(), ProductsAdapterHelper.getSession().getApiToken(), ProductsAdapterHelper.getSession().getAcctUrlWithoutProtocol(), product.getId()+"", false, false);
        lvh.ivProductImage.setImageUrl(imageUrl, ProductsAdapterHelper.getImageLoaderInstance(getContext(), true));

        return convertView;
    }
}
