package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseProductsRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.operations.ImonggoTools;
import net.nueca.imonggosdk.tools.NumberTools;

import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by gama on 22/10/2015.
 */
public class SimpleSalesProductRecyclerAdapter extends BaseProductsRecyclerAdapter
        <SimpleSalesProductRecyclerAdapter.ListViewHolder> {

    public SimpleSalesProductRecyclerAdapter(Context context, ImonggoDBHelper2 dbHelper, List<Product> productsList) {
        super(context, dbHelper, productsList);
    }

    public SimpleSalesProductRecyclerAdapter(Context context) {
        super(context);
    }

    public SimpleSalesProductRecyclerAdapter(Context context, List<Product> productsList) {
        super(context, productsList);
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_sales_product_item, parent, false);
        ListViewHolder lvh = new ListViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        Product product = getItem(position);

        holder.tvProductName.setText(Html.fromHtml(product.getName() + getSelectedProductItems().getUnitName(product).toLowerCase()));

        SelectedProductItem selectedProductItem = getSelectedProductItems().getSelectedProductItem(product);
        Double retail_price = product.getRetail_price();
        holder.tvSubtotal.setText("");
        if(selectedProductItem != null) {
            retail_price = selectedProductItem.getRetail_price() == null ? retail_price : selectedProductItem.getRetail_price();
            double subtotal = NumberTools.toDouble(selectedProductItem.getQuantity()) * retail_price;
            holder.tvSubtotal.setText(NumberTools.separateInCommas(subtotal));
        }
        holder.tvRetailPrice.setText(NumberTools.separateInCommas(retail_price));
        holder.tvInventoryCount.setText("0 pcs");

        holder.tvQuantity.setText(NumberTools.separateInSpaceHideZeroDecimals(getSelectedProductItems().getQuantity(product)) + " pcs");

        String imageUrl = ImonggoTools.buildProductImageUrl(getContext(), ProductsAdapterHelper.getSession().getApiToken(),
                ProductsAdapterHelper.getSession().getAcctUrlWithoutProtocol(), product.getId() + "", false, false);

        holder.ivProductImage.setImageUrl(imageUrl, ProductsAdapterHelper.getImageLoaderInstance(getContext(), true));
    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {
        public NetworkImageView ivProductImage;
        public AutofitTextView tvProductName, tvSubtotal;
        public TextView tvQuantity, tvRetailPrice, tvInventoryCount;
        public View root;

        public ListViewHolder(View itemView) {
            super(itemView);
            root = itemView;

            ivProductImage = (NetworkImageView) itemView.findViewById(R.id.ivProductImage);
            tvProductName = (AutofitTextView) itemView.findViewById(R.id.tvProductName);
            tvSubtotal = (AutofitTextView) itemView.findViewById(R.id.tvSubtotal);

            tvQuantity = (TextView) itemView.findViewById(R.id.tvQuantity);
            tvRetailPrice = (TextView) itemView.findViewById(R.id.tvRetailPrice);
            tvInventoryCount = (TextView) itemView.findViewById(R.id.tvInventoryCount);

            ivProductImage.setDefaultImageResId(R.drawable.no_image);
            ivProductImage.setErrorImageResId(R.drawable.no_image);

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
