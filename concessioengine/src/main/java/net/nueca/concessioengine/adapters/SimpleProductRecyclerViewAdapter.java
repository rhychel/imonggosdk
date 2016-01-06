package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseProductsRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.BranchPrice;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.operations.ImonggoTools;

import java.sql.SQLException;
import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public class SimpleProductRecyclerViewAdapter extends BaseProductsRecyclerAdapter<SimpleProductRecyclerViewAdapter.ListViewHolder> {

    public SimpleProductRecyclerViewAdapter(Context context) {
        super(context);
    }

    public SimpleProductRecyclerViewAdapter(Context context, List<Product> productsList) {
        super(context, productsList);
    }

    public SimpleProductRecyclerViewAdapter(Context context, ImonggoDBHelper2 dbHelper, List<Product> productsList) {
        super(context, dbHelper, productsList);
    }

    @Override
    public SimpleProductRecyclerViewAdapter.ListViewHolder onCreateViewHolder(ViewGroup parent, int viewtype) {
        View v;
        if(listingType == ListingType.BASIC)
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_product_listitem, parent, false);
        else
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_product_listitem2, parent, false);


        ListViewHolder lvh = new ListViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(SimpleProductRecyclerViewAdapter.ListViewHolder viewHolder, int position) {
        Product product = getItem(position);

        String imageUrl = ImonggoTools.buildProductImageUrl(getContext(), ProductsAdapterHelper.getSession().getApiToken(), ProductsAdapterHelper.getSession().getAcctUrlWithoutProtocol(), product.getId() + "", false, false);
        viewHolder.ivProductImage.setImageUrl(imageUrl, ProductsAdapterHelper.getImageLoaderInstance(getContext(), true));

        if(listingType == ListingType.BASIC) {
            viewHolder.tvProductName.setText(Html.fromHtml(product.getName() + getSelectedProductItems().getUnitName(product).toLowerCase()));
            viewHolder.tvQuantity.setText(getSelectedProductItems().getQuantity(product));
        }
        else {
            if(!hasSubtotal)
                viewHolder.tvSubtotal.setVisibility(View.GONE);
            viewHolder.llQuantity.setVisibility(View.GONE);
            viewHolder.tvProductName.setText(product.getName());
            viewHolder.tvInStock.setText(String.format("In Stock: %1$s %2$s", product.getInStock(), product.getBase_unit_name()));
            double subtotal = product.getRetail_price()*Double.valueOf(getSelectedProductItems().getQuantity(product));
            viewHolder.tvRetailPrice.setText(String.format("P%.2f", product.getRetail_price()));

            Log.e("selectedItems", getSelectedProductItems().size()+"Size"+ProductsAdapterHelper.getSelectedProductItems().size());
            if(getSelectedProductItems().hasSelectedProductItem(product)) {
                viewHolder.llQuantity.setVisibility(View.VISIBLE);
                viewHolder.tvQuantity.setText(String.format("%1$s %2$s", getSelectedProductItems().getQuantity(product), getSelectedProductItems().getUnitName(product, false)));
                if(hasSubtotal)
                    viewHolder.tvSubtotal.setText(String.format("P%.2f", subtotal));
            }

        }
    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    /**
     * ViewHolder
     */
    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {
        public NetworkImageView ivProductImage;
        public AutofitTextView tvProductName, tvQuantity;

        public AutofitTextView tvInStock;
        public TextView tvRetailPrice, tvSubtotal;
        public LinearLayout llQuantity;

        public View root;

        public ListViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            ivProductImage = (NetworkImageView) itemView.findViewById(R.id.ivProductImage);
            tvProductName = (AutofitTextView) itemView.findViewById(R.id.tvProductName);
            tvQuantity = (AutofitTextView) itemView.findViewById(R.id.tvQuantity);

            if(listingType == ListingType.SALES) {
                tvInStock = (AutofitTextView) itemView.findViewById(R.id.tvInStock);
                tvRetailPrice = (TextView) itemView.findViewById(R.id.tvRetailPrice);
                tvSubtotal = (TextView) itemView.findViewById(R.id.tvSubtotal);
                llQuantity = (LinearLayout) itemView.findViewById(R.id.llQuantity);
            }

            ivProductImage.setDefaultImageResId(R.drawable.ic_tag_grey);
            ivProductImage.setErrorImageResId(R.drawable.ic_tag_grey);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(onItemClickListener != null)
                onItemClickListener.onItemClicked(view, getLayoutPosition());
        }


        @Override
        public boolean onLongClick(View view) {
            if(onItemLongClickListener != null)
                onItemLongClickListener.onItemLongClicked(view, getLayoutPosition());
            return true;
        }
    }

}
