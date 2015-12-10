package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseSalesProductRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.tools.PriceTools;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.operations.ImonggoTools;
import net.nueca.imonggosdk.tools.NumberTools;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.grantland.widget.AutofitTextView;

/**
 * Created by gama on 22/10/2015.
 */
public class SimpleSalesProductRecyclerAdapter extends BaseSalesProductRecyclerAdapter
        <SimpleSalesProductRecyclerAdapter.ListViewHolder> {

    public SimpleSalesProductRecyclerAdapter(Context context, ImonggoDBHelper2 dbHelper, List<Product> productsList) {
        super(context, dbHelper, productsList);
    }
    public SimpleSalesProductRecyclerAdapter(Context context, ImonggoDBHelper2 dbHelper) {
        super(context, dbHelper, new ArrayList<Product>());
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
        Double retail_price = PriceTools.identifyRetailPrice(getHelper(),product,branch,customerGroup,customer);

        holder.tvSubtotal.setText("");
        if(selectedProductItem != null) {
            selectedProductItem.setRetail_price(retail_price);
            //retail_price = selectedProductItem.getRetail_price() == null ? retail_price : selectedProductItem.getRetail_price();
            double subtotal = NumberTools.toDouble(selectedProductItem.getQuantity()) * retail_price;
            holder.tvSubtotal.setText(NumberTools.separateInCommas(subtotal));
        }
        holder.tvRetailPrice.setText(NumberTools.separateInCommas(retail_price));
        holder.tvInventoryCount.setText("? pcs");
        holder.tvInventoryCount.setVisibility(View.GONE);

        if(selectedProductItem != null && selectedProductItem.getQuantity() != null &&
                NumberTools.toDouble(selectedProductItem.getQuantity()) != 0d) {
            double qty = NumberTools.toDouble(selectedProductItem.getQuantity());
            holder.tvQuantity.setText(NumberTools.separateInSpaceHideZeroDecimals(qty) + (qty == 1? " pc" : " pcs"));
            holder.tvQuantity.setVisibility(View.VISIBLE);
            holder.tvSubtotal.setVisibility(View.VISIBLE);
        } else {
            holder.tvQuantity.setVisibility(View.GONE);
            holder.tvSubtotal.setVisibility(View.GONE);
        }


        /*String imageUrl = ImonggoTools.buildProductImageUrl(getContext(), ProductsAdapterHelper.getSession().getApiToken(),
                ProductsAdapterHelper.getSession().getAcctUrlWithoutProtocol(), product.getId() + "", false, false);

        holder.ivProductImage.setImageUrl(imageUrl, ProductsAdapterHelper.getImageLoaderInstance(getContext(), true));*/
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

    /*public Double identifyRetailPrice(Product product) {
        Double retail_price = product.getRetail_price();
        //Log.e("Product", "retail_price:" + retail_price + " for " + product.getName());
        try {
            // Using BranchPrice
            if(branch != null) {
                BranchPrice branchPrice = getHelper().getDao(BranchPrice.class).queryBuilder().where().eq("branch_id", branch.getId()).and().eq
                        ("product_id", product.getId()).queryForFirst();
                if(branchPrice != null) {
                    retail_price = branchPrice.getRetail_price();
                    Log.e("BranchPrice", "retail_price:" + retail_price + " for " + product.getName());
                }
            }

            // Using PriceList
            Log.e("PriceList", "count : " + getHelper().getDao(PriceList.class).countOf() + " for " + product.getName() + "~" + product.getId());
            List<PriceList> priceLists = null;
            String t = "NULL";
            if(branch != null) {
                priceLists = getHelper().getDao(PriceList.class).queryBuilder().where()
                        .eq("branch_id", branch.getId()).query();
                t = "Branch";
            }
            if(customerGroup != null) {
                priceLists = getHelper().getDao(PriceList.class).queryBuilder().where()
                        .eq("customer_group_id", customerGroup.getId()).query();
                t = "CustomerGroup";
            }
            if(customer != null) {
                priceLists = getHelper().getDao(PriceList.class).queryBuilder().where().eq("customer_id", customer.getId()).query();
                t = "Customer";
            }

            if(priceLists != null) {
                Price price = getHelper().getDao(Price.class).queryBuilder().where().eq("product_id", product.getId())
                        .and().in("price_list_id", priceLists).queryForFirst();

                if (price != null) {
                    retail_price = price.getRetail_price();
                    Log.e(t, "retail_price:" + retail_price + " for " + product.getName());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return retail_price;
    }*/
}
