package net.nueca.imonggosari.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import net.nueca.concessioengine.adapters.base.BaseProductsAdapter;
import net.nueca.concessioengine.adapters.base.BaseProductsRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.imonggosari.R;
import net.nueca.imonggosdk.database.ImonggoDBHelper;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.operations.ImonggoTools;
import net.nueca.imonggosdk.tools.NumberTools;

import java.util.List;

/**
 * Created by gama on 27/10/2015.
 */
public class SariProductsAdapter extends BaseProductsRecyclerAdapter<SariProductsAdapter.ListViewHolder> {

    private boolean isBig = false;
    private Integer itemLayout;
    private SariProductListener listener;
    private Integer maxElement;

    public SariProductsAdapter(Context context) {
        super(context);
    }

    public SariProductsAdapter(Context context, List<Product> productsList) {
        super(context, productsList);
    }

    public SariProductsAdapter(Context context, ImonggoDBHelper dbHelper, List<Product> productsList) {
        super(context, dbHelper, productsList);
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if(itemLayout == null)
            v = LayoutInflater.from(parent.getContext()).inflate(isBig? R.layout.product_tileitem : R.layout.product_listitem,
                    parent, false);
        else
            v = LayoutInflater.from(parent.getContext()).inflate(itemLayout,
                    parent, false);

        ListViewHolder lvh = new ListViewHolder(v);
        return lvh;
    }


    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        Product product = getItem(position%getCount());

        holder.tvProductName.setText(product.getName());

        if(holder.tvRetailPrice != null)
            holder.tvRetailPrice.setText(NumberTools.separateInCommas(product.getRetail_price()));

        if(holder.ivProductImage != null) {
            String imageUrl = ImonggoTools.buildProductImageUrl(getContext(), ProductsAdapterHelper.getSession().getApiToken(),
                    ProductsAdapterHelper.getSession().getAcctUrlWithoutProtocol(), product.getId() + "", isBig, false);

            holder.ivProductImage.setImageUrl(imageUrl, ProductsAdapterHelper.getImageLoaderInstance(getContext(), true));
        }

        if(holder.ibtnDelete != null)
            holder.ibtnDelete.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    public Integer getItemLayout() {
        return itemLayout;
    }

    public void setItemLayout(Integer itemLayout) {
        this.itemLayout = itemLayout;
    }

    public boolean isBig() {
        return isBig;
    }

    public void setIsBig(boolean isBig) {
        this.isBig = isBig;
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {
        TextView tvProductName, tvRetailPrice;
        NetworkImageView ivProductImage;
        ImageButton ibtnDelete;
        View root;

        public ListViewHolder(View itemView) {
            super(itemView);
            root = itemView;

            tvProductName = (TextView) itemView.findViewById(R.id.tvProductName);

            if(!isBig || itemLayout != null)
                tvRetailPrice = (TextView) itemView.findViewById(R.id.tvRetailPrice);

            if(itemLayout == null)
                ivProductImage = (NetworkImageView) itemView.findViewById(R.id.ivProductImage);
            else
                ibtnDelete = (ImageButton) itemView.findViewById(R.id.ibtnDelete);

            if(ivProductImage != null) {
                ivProductImage.setDefaultImageResId(R.drawable.no_image_gray);
                ivProductImage.setErrorImageResId(R.drawable.no_image_gray);
            }

            if(ibtnDelete != null) {
                ibtnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(listener != null)
                            listener.onDelete(getItem(getLayoutPosition()));
                        remove(getLayoutPosition());
                        notifyDataSetChanged();
                    }
                });
            }

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(onItemClickListener != null)
                onItemClickListener.onItemClicked(view, getLayoutPosition());
            if(ibtnDelete != null)
                ibtnDelete.setVisibility(View.GONE);
        }

        @Override
        public boolean onLongClick(View view) {
            if(onItemLongClickListener != null)
                onItemLongClickListener.onItemLongClicked(view, getLayoutPosition());
            if(ibtnDelete != null)
                ibtnDelete.setVisibility(View.VISIBLE);
            return true;
        }
    }

    public interface SariProductListener {
        void onDelete(Product product);
    }

    public void setListener(SariProductListener listener) {
        this.listener = listener;
    }

    public Integer getMaxElement() {
        return maxElement;
    }

    public void setMaxElement(Integer maxElement) {
        this.maxElement = maxElement;
    }

    @Override
    public void add(Product product) {
        if(maxElement != null && getCount() >= maxElement)
            super.remove(0);
        super.add(product);
    }

    @Override
    public boolean updateList(List<Product> products) {
        return super.updateList(products);
    }
}
