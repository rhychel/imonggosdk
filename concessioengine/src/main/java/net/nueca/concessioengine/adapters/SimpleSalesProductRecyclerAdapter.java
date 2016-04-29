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
import net.nueca.concessioengine.adapters.base.BaseSalesProductRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.concessioengine.objects.SelectedProductItem;
import net.nueca.concessioengine.objects.Values;
import net.nueca.concessioengine.tools.PriceTools;
import net.nueca.imonggosdk.database.ImonggoDBHelper2;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.Unit;
import net.nueca.imonggosdk.objects.customer.Customer;
import net.nueca.imonggosdk.objects.customer.CustomerGroup;
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

    public SimpleSalesProductRecyclerAdapter(Context context, ImonggoDBHelper2 dbHelper, List<Product> productsList,
                                             Customer customer, CustomerGroup customerGroup, Branch branch) {
        super(context, dbHelper, productsList, customer, customerGroup, branch);
    }

    public SimpleSalesProductRecyclerAdapter(Context context, ImonggoDBHelper2 dbHelper, List<Product> productsList) {
        super(context, dbHelper, productsList);
    }
    public SimpleSalesProductRecyclerAdapter(Context context, ImonggoDBHelper2 dbHelper) {
        super(context, dbHelper, new ArrayList<Product>());
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if(listingType == ListingType.BASIC)
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_sales_product_item, parent, false);
        else
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_product_listitem2, parent, false);

        ListViewHolder lvh = new ListViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        Product product = getItem(position);
        Log.e(getClass().getSimpleName(), "PRODUCT_NAME ~ " + product.getName() + " ~ " + listingType.name());

        if(listingType == ListingType.BASIC || listingType == ListingType.SALES) {
            holder.tvInventoryCount.setText(String.format("%1$s %2$s", product.getInStock(), product.getBase_unit_name()));
            if(!hasSubtotal)
                holder.tvSubtotal.setVisibility(View.GONE);
            holder.tvProductName.setText(Html.fromHtml(product.getName() + getSelectedProductItems().getUnitName(product).toLowerCase()));
            double subtotal = product.getRetail_price()*Double.valueOf(getSelectedProductItems().getQuantity(product));
            holder.tvRetailPrice.setText(String.format("P%.2f", product.getRetail_price()));

            if(getSelectedProductItems().hasSelectedProductItem(product)) {
                holder.llQuantity.setVisibility(View.VISIBLE);
                holder.tvQuantity.setText(String.format("%1$s %2$s", NumberTools.separateInSpaceHideZeroDecimals(getSelectedProductItems().getQuantity(product)), getSelectedProductItems().getUnitName(product, false)));
                if(hasSubtotal)
                    holder.tvSubtotal.setText(String.format("P%s", NumberTools.separateInCommas(subtotal)));
            }
        }
        else if(listingType == ListingType.ADVANCED_SALES) {
            Log.e("Product", product.getId()+"---");
            if(!hasSubtotal)
                holder.tvSubtotal2.setVisibility(View.GONE);

            holder.ivStar.setVisibility(View.GONE);
            if(promotionalProducts.indexOf(product) > -1)
                holder.ivStar.setVisibility(View.VISIBLE);
            holder.llQuantity.setVisibility(View.GONE);
            holder.tvProductName.setText(product.getName());

            SelectedProductItem selectedProductItem = getSelectedProductItems().getSelectedProductItem(product);
            Unit unit = null;
            if(selectedProductItem != null && selectedProductItem.getValues() != null && selectedProductItem.getValues().size() > 0) {
                Values values = selectedProductItem.getValues().get(0);
                unit = values.getUnit();
            }
            if(unit == null) {
                try {
                    unit = getHelper().fetchObjects(Unit.class).queryBuilder().where().eq("name", product.getUnit()).and()
                            .eq("product_id", product).queryForFirst();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if(selectedProductItem == null && product.getExtras() != null && product.getExtras().getDefault_selling_unit() != null && product.getExtras()
                    .getDefault_selling_unit().length() > 0 && unit == null) {
                try {
                    Unit t_unit = getHelper().fetchObjects(Unit.class).queryBuilder().where().eq("id", Integer.parseInt(product.getExtras()
                            .getDefault_selling_unit())).queryForFirst();
                    if(t_unit != null)
                        unit = t_unit;
                } catch (SQLException e) { e.printStackTrace(); }
            }
            Log.e("UNITS", product.getName()+" --> "+product.getExtras().getDefault_selling_unit());

            // -------- GET THE DEFAULT SELLING UNIT FOR INVENTORY
            Log.e("hasInStock", hasInStock+"");
            if(hasInStock) {
                if (product.getExtras() != null && product.getExtras().getDefault_selling_unit() != null && !product.getExtras().getDefault_selling_unit().isEmpty()) {
                    Unit defaultSellingUnit = Unit.fetchById(getHelper(), Unit.class, Integer.valueOf(product.getExtras().getDefault_selling_unit()));
                    if (defaultSellingUnit != null)
                        holder.tvInStock.setText(String.format("In Stock: %1$s %2$s", product.getInStock(defaultSellingUnit.getQuantity(),
                                ProductsAdapterHelper.getDecimalPlace()), defaultSellingUnit.getName()));
                    else
                        holder.tvInStock.setText(String.format("In Stock: %1$s %2$s", product.getInStock(), product.getBase_unit_name()));
                } else
                    holder.tvInStock.setText(String.format("In Stock: %1$s %2$s", product.getInStock(), product.getBase_unit_name()));
            }
            else
                holder.tvInStock.setVisibility(View.GONE);
            // -------- GET THE DEFAULT SELLING UNIT FOR INVENTORY

//            Log.e(getClass().getSimpleName(), "unit : " + (unit == null? "null" : unit.getName()) );
//            Log.e(getClass().getSimpleName(), "selectedProductItem isNull? " + (selectedProductItem == null) );

            // Set subtotal
            holder.tvSubtotal2.setText("");
            if(selectedProductItem != null) {
                /*double subtotal = 0d;
                for(Values values : selectedProductItem.getValues()) {
                    subtotal += NumberTools.toDouble(values.getQuantity()) * values.getRetail_price();
                    retail_price = values.getRetail_price();
                    Log.e("values["+selectedProductItem.getValues().indexOf(values)+"] retail_price", retail_price.toString());
                }
                Log.e(getClass().getSimpleName(), "Subtotal : " + subtotal);*/
                Log.e("SSPRA", "subtotal:" + selectedProductItem.getValuesSubtotal() + "  retailprice:" +
                        selectedProductItem.getValuesRetailPrices(';'));
                holder.tvSubtotal2.setText(String.format("P%s", NumberTools.separateInCommas(selectedProductItem.getValuesSubtotal())));
                holder.tvRetailPrice.setText(String.format("P%s/%s", selectedProductItem.getValuesRetailPrices(';'), selectedProductItem.getValuesUnit()));
            }
            // set Retail price
            else {
                Log.e(getClass().getSimpleName(), "calling PriceTools.identifyRetailPrice");
                Double retail_price = PriceTools.identifyRetailPrice(getHelper(), product, branch, customerGroup, customer, unit);

                if(retail_price == null)
                    retail_price = product.getRetail_price();
                Log.e("identified retail_price", retail_price.toString());
                holder.tvRetailPrice.setText(String.format("P%s", NumberTools.separateInCommas(retail_price)));
            }

            // Quantity
            if(getSelectedProductItems().hasSelectedProductItem(product)) {
                holder.llQuantity.setVisibility(View.VISIBLE);
                holder.tvQuantity2.setText(String.format("%1$s %2$s", getSelectedProductItems().getQuantity(product), getSelectedProductItems()
                        .getUnitName(product, false)));
                if(hasSubtotal)
                    holder.tvSubtotal2.setVisibility(View.VISIBLE);
            }
        }

//        Log.e("Thumbnails", product.getThumbnail_url()+" <----");
        if(product.getThumbnail_url() != null && !product.getThumbnail_url().trim().equals("")) {
            String imageUrl = ImonggoTools.buildProductImageUrl(getContext(), ProductsAdapterHelper.getSession().getApiToken(),
                    ProductsAdapterHelper.getSession().getAcctUrlWithoutProtocol(), product.getId() + "", false, false);
            holder.ivProductImage.setImageUrl(imageUrl, ProductsAdapterHelper.getImageLoaderInstance(getContext(), true));
        }
    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {
        public NetworkImageView ivProductImage;
        public AutofitTextView tvProductName, tvSubtotal;
        public TextView tvQuantity, tvRetailPrice, tvInventoryCount;

        // --- for layout2
        public AutofitTextView tvInStock, tvQuantity2;
        public TextView tvSubtotal2;
        public LinearLayout llQuantity;

        public ImageView ivStar;

        public View root;
        public ListViewHolder(View itemView) {
            super(itemView);
            root = itemView;

            ivProductImage = (NetworkImageView) itemView.findViewById(R.id.ivProductImage);
            tvProductName = (AutofitTextView) itemView.findViewById(R.id.tvProductName);

            tvRetailPrice = (TextView) itemView.findViewById(R.id.tvRetailPrice);

            if(listingType == ListingType.BASIC) {
                tvSubtotal = (AutofitTextView) itemView.findViewById(R.id.tvSubtotal);
                tvQuantity = (TextView) itemView.findViewById(R.id.tvQuantity);
                tvInventoryCount = (TextView) itemView.findViewById(R.id.tvInventoryCount);

                ivProductImage.setDefaultImageResId(R.drawable.no_image);
                ivProductImage.setErrorImageResId(R.drawable.no_image);
            }
            else {
                ivStar = (ImageView) itemView.findViewById(R.id.ivStar);
                tvInStock = (AutofitTextView) itemView.findViewById(R.id.tvInStock);
                tvQuantity2 = (AutofitTextView) itemView.findViewById(R.id.tvQuantity);
                tvSubtotal2 = (TextView) itemView.findViewById(R.id.tvSubtotal);
                llQuantity = (LinearLayout) itemView.findViewById(R.id.llQuantity);

                ivProductImage.setDefaultImageResId(R.drawable.ic_tag_grey);
                ivProductImage.setErrorImageResId(R.drawable.ic_tag_grey);
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
