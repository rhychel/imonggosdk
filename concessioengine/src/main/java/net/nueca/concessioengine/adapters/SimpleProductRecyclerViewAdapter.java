package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseProductsRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.tools.PriceTools;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.BranchPrice;
import net.nueca.imonggosdk.objects.BranchProduct;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.operations.ImonggoTools;
import net.nueca.imonggosdk.tools.NumberTools;

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
        else if(listingType == ListingType.SALES_GRID)
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_product_tile, parent, false);
        else
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_product_listitem2, parent, false);


        ListViewHolder lvh = new ListViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(SimpleProductRecyclerViewAdapter.ListViewHolder viewHolder, int position) {
        final Product product = getItem(position);


//        Log.e("Thumbnails", product.getThumbnail_url()+" <----");
        if(product.getThumbnail_url() != null && !product.getThumbnail_url().trim().equals("")) {
            String imageUrl = ImonggoTools.buildProductImageUrl(getContext(), ProductsAdapterHelper.getSession().getApiToken(),
                    ProductsAdapterHelper.getSession().getAcctUrlWithoutProtocol(), product.getId() + "", false, false);
            viewHolder.ivProductImage.setImageUrl(imageUrl, ProductsAdapterHelper.getImageLoaderInstance(getContext(), true));
        }

//        String imageUrl = ImonggoTools.buildProductImageUrl(getContext(), ProductsAdapterHelper.getSession().getApiToken(), ProductsAdapterHelper.getSession().getAcctUrlWithoutProtocol(), product.getId() + "", false, false);
//        viewHolder.ivProductImage.setImageUrl(imageUrl, ProductsAdapterHelper.getImageLoaderInstance(getContext(), true));

        if(listingType == ListingType.BASIC) {
            viewHolder.tvProductName.setText(Html.fromHtml(product.getName() + getSelectedProductItems().getUnitName(product).toLowerCase()));
            viewHolder.tvQuantity.setText(getSelectedProductItems().getQuantity(product));
        }
        else if(listingType == ListingType.SALES_GRID) {
            viewHolder.tvProductName.setText(product.getName());
            viewHolder.ivOverlay.setVisibility(getSelectedProductItems().hasSelectedProductItem(product)? View.VISIBLE : View.INVISIBLE);
            viewHolder.tvInventoryCount.setText(String.format("%1$s %2$s", product.getInStock(), product.getBase_unit_name()));
        }
        else {
            if(!hasSubtotal)
                viewHolder.tvSubtotal.setVisibility(View.GONE);
            viewHolder.llQuantity.setVisibility(View.GONE);
            viewHolder.tvProductName.setText(product.getName());
            if(hasInStock) {
                viewHolder.tvInStock.setVisibility(View.VISIBLE);
                viewHolder.tvInStock.setText(String.format("In Stock: %1$s %2$s", product.getInStock(), product.getBase_unit_name()));
            } else
                viewHolder.tvInStock.setVisibility(View.GONE);
            double subtotal = 0.0;
            if(branch != null) {
                try {
                    BranchProduct branchProduct = getHelper().fetchForeignCollection(product.getBranchProducts().closeableIterator(), new ImonggoDBHelper2.Conditional<BranchProduct>() {
                        @Override
                        public boolean validate(BranchProduct obj) {
                            if(obj.getUnit() == null)
                                return true;
                            if(product.getExtras().getDefault_selling_unit() != null && !product.getExtras().getDefault_selling_unit().equals(""))
                                return obj.getUnit().getId() == Integer.parseInt(product.getExtras().getDefault_selling_unit());
//                            return obj.getUnit() == null;
                            return false;
                        }
                    }, 0);
                    Unit unit = null;
                    if(branchProduct != null)
                        unit = branchProduct.getUnit();

                    Log.e(getClass().getSimpleName(), "calling PriceTools.identifyRetailPrice");
                    Double retail_price = PriceTools.identifyRetailPrice(getHelper(), product, branch, null, null, unit);

                    if(retail_price == null)
                        retail_price = product.getRetail_price();
                    Log.e("identified retail_price", retail_price.toString());
                    viewHolder.tvRetailPrice.setText(String.format("P%s", NumberTools.separateInCommas(retail_price)));

                    subtotal = retail_price * Double.valueOf(getSelectedProductItems().getQuantity(product));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else {
                subtotal = product.getRetail_price() * Double.valueOf(getSelectedProductItems().getQuantity(product));
                viewHolder.tvRetailPrice.setText(String.format("P%.2f", product.getRetail_price()));
                //            Log.e("selectedItems", getSelectedProductItems().size()+"Size"+ProductsAdapterHelper.getSelectedProductItems().size());
            }
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
        public TextView tvRetailPrice, tvSubtotal, tvInventoryCount;
        public LinearLayout llQuantity;

        public ImageView ivOverlay;

        public View root;

        public ListViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            ivProductImage = (NetworkImageView) itemView.findViewById(R.id.ivProductImage);
            tvProductName = (AutofitTextView) itemView.findViewById(R.id.tvProductName);

            if(listingType != ListingType.SALES_GRID) {
                tvQuantity = (AutofitTextView) itemView.findViewById(R.id.tvQuantity);
                ivProductImage.setDefaultImageResId(R.drawable.ic_tag_grey);
                ivProductImage.setErrorImageResId(R.drawable.ic_tag_grey);
            }
            else {
                ivProductImage.setDefaultImageResId(R.drawable.ic_image_photo_gray);
                ivProductImage.setErrorImageResId(R.drawable.ic_image_photo_gray);
            }

            if(listingType == ListingType.SALES_GRID) {
                ivOverlay = (ImageView) itemView.findViewById(R.id.ivOverlay);
                tvInventoryCount = (TextView) itemView.findViewById(R.id.tvInventoryCount);
            }
            else {
                tvInStock = (AutofitTextView) itemView.findViewById(R.id.tvInStock);
                tvRetailPrice = (TextView) itemView.findViewById(R.id.tvRetailPrice);
                tvSubtotal = (TextView) itemView.findViewById(R.id.tvSubtotal);
                llQuantity = (LinearLayout) itemView.findViewById(R.id.llQuantity);
            }


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
