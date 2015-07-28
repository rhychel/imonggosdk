package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.NetworkImageView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.lists.ProductsList;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.operations.ImonggoTools;

import me.grantland.widget.AutofitTextView;

/**
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public class SimpleProductRecyclerViewAdapter extends BaseProductsRecyclerAdapter<SimpleProductRecyclerViewAdapter.ListViewHolder> {

    public SimpleProductRecyclerViewAdapter(Context context) {
        super(context);
    }

    public SimpleProductRecyclerViewAdapter(Context context, ProductsList productsList) {
        super(context, productsList);
    }


    public class ListViewHolder extends BaseProductsRecyclerAdapter.ViewHolder {
        public NetworkImageView ivProductImage;
        public AutofitTextView tvProductName, tvQuantity;
        public View root;

        public ListViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            ivProductImage = (NetworkImageView) itemView.findViewById(R.id.ivProductImage);
            tvProductName = (AutofitTextView) itemView.findViewById(R.id.tvProductName);
            tvQuantity = (AutofitTextView) itemView.findViewById(R.id.tvQuantity);

            ivProductImage.setDefaultImageResId(R.drawable.no_image);
            ivProductImage.setErrorImageResId(R.drawable.no_image);

            itemView.setOnClickListener(this);
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
            return false;
        }
    }

    @Override
    public SimpleProductRecyclerViewAdapter.ListViewHolder onCreateViewHolder(ViewGroup parent, int viewtype) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_product_listitem, parent, false);

        ListViewHolder lvh = new ListViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(SimpleProductRecyclerViewAdapter.ListViewHolder viewHolder, int position) {
        Product product = productsList.get(position);

        viewHolder.tvProductName.setText(product.getName());
        viewHolder.tvQuantity.setText(getSelectedProductItems().getQuantity(product.getId()));
        String imageUrl = ImonggoTools.buildProductImageUrl(getContext(), ProductsAdapterHelper.getSession().getApiToken(), ProductsAdapterHelper.getSession().getAcctUrlWithoutProtocol(), product.getId() + "", false, false);
        viewHolder.ivProductImage.setImageUrl(imageUrl, ProductsAdapterHelper.getImageLoaderInstance(getContext(), true));
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

}