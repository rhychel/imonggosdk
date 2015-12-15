package net.nueca.concessioengine.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.StateListDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tonicartos.superslim.GridSLM;
import com.tonicartos.superslim.LayoutManager;
import com.tonicartos.superslim.LinearSLM;

import net.nueca.concessioengine.R;
import net.nueca.concessioengine.adapters.base.BaseCustomersRecyclerAdapter;
import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;
import net.nueca.concessioengine.enums.ListingType;
import net.nueca.imonggosdk.objects.customer.Customer;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by gama on 8/13/15.
 */
public class SimpleCustomerRecyclerViewAdapter extends
        BaseCustomersRecyclerAdapter<SimpleCustomerRecyclerViewAdapter.ListViewHolder> {
    /**
     * These are for the sticky header letter.
     */
    private int headerDisplay;
    private boolean marginsFixed;
    private static final int VIEW_TYPE_HEADER = 0x01;
    private static final int VIEW_TYPE_CONTENT = 0x00;

    private int highlightColor;
    private int circleColor = Color.WHITE;

    public SimpleCustomerRecyclerViewAdapter(Context context, List<Customer> customers, boolean isMultiSelect,
                                             Integer highlightColor) {
        super(context, customers, isMultiSelect);
        if(highlightColor != null)
            this.highlightColor = highlightColor;
        else
            this.highlightColor = Color.parseColor("#22000000");
    }

    public void setCircleColor(int color) {
        circleColor = color;
        notifyDataSetChanged();
    }
    public void setHighlightColor(int color) {
        highlightColor = color;
        notifyDataSetChanged();
    }

    @Override
    public void setListingType(ListingType listingType) {
        super.  setListingType(listingType);
        if(listingType == ListingType.LETTER_HEADER) {
            headerDisplay = getContext().getResources().getInteger(R.integer.default_header_display);
            marginsFixed = getContext().getResources().getBoolean(R.bool.default_margins_fixed);
        }
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        if(listingType == ListingType.BASIC)
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.simple_customer_listitem, viewGroup,
                false);
        else {
            if(viewType == VIEW_TYPE_HEADER)
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.simple_customer_header_letter, viewGroup, false);
            else
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.simple_customer_listitem2, viewGroup, false);
        }
        Log.e("onCreateViewHolder", "called");

        ListViewHolder lvh = new ListViewHolder(v);
        return lvh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder listViewHolder, int position) {
        Customer customer = getItem(position);
        listViewHolder.setCustomer(customer);

        String name;
        if (customer.getName() != null && customer.getName().length() > 0)
            name = customer.getName();
        else
            name = customer.getFirst_name() + " " + customer.getLast_name();
        name = name.trim();

        if(listingType == ListingType.BASIC) {
            if (name.length() > 0 && StringUtils.isAlphanumeric(name.substring(0, 1)))
                listViewHolder.tvFirstLetter.setText(StringUtils.upperCase(name).charAt(0) + "");
            else
                listViewHolder.tvFirstLetter.setText("@");

            listViewHolder.tvCustomerName.setText(name);
            listViewHolder.tvAlternateId.setText(customer.getAlternate_code());
            listViewHolder.tvAddress.setText(customer.getFullAddress());

            if (customer.getAlternate_code() != null && !customer.getAlternate_code().isEmpty())
                listViewHolder.tvAlternateId.setVisibility(View.VISIBLE);
            else
                listViewHolder.tvAlternateId.setVisibility(View.GONE);

            if (customer.getFullAddress() != null && !customer.getFullAddress().isEmpty())
                listViewHolder.tvAddress.setVisibility(View.VISIBLE);
            else
                listViewHolder.tvAddress.setVisibility(View.GONE);

            StateListDrawable bgShape = (StateListDrawable) listViewHolder.tvFirstLetter.getBackground();
            bgShape.setColorFilter(circleColor, PorterDuff.Mode.SRC);

            Log.e("SelectedCustomer", selectedCustomer.size() + "");
            if (selectedCustomer.contains(customer))
                listViewHolder.llCustomerItem.setBackgroundColor(highlightColor);
            else
                listViewHolder.llCustomerItem.setBackgroundColor(Color.TRANSPARENT);
        }
        else {
            final View itemView = listViewHolder.itemView;
            if(!customer.isHeader()) {
                listViewHolder.setCustomer(customer);

                listViewHolder.tvCustomerName.setText(name);
                listViewHolder.tvAddress.setText(customer.getFullAddress());

                if (customer.getFullAddress() != null && !customer.getFullAddress().isEmpty())
                    listViewHolder.tvAddress.setVisibility(View.VISIBLE);
                else
                    listViewHolder.tvAddress.setVisibility(View.GONE);

                Log.e("SelectedCustomer", selectedCustomer.size() + "");
                if (selectedCustomer.contains(customer))
                    listViewHolder.llCustomerItem.setBackgroundColor(highlightColor);
                else
                    listViewHolder.llCustomerItem.setBackgroundColor(Color.TRANSPARENT);
            }
            else {
                listViewHolder.tvLetterHeader.setText(customer.getLetterHeader());
            }
            final GridSLM.LayoutParams lp = GridSLM.LayoutParams.from(itemView.getLayoutParams());
            if(customer.isHeader()) {
                lp.headerDisplay = headerDisplay;
                if (lp.isHeaderInline() || (marginsFixed && !lp.isHeaderOverlay())) {
                    lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                } else {
                    lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                }

                lp.headerEndMarginIsAuto = !marginsFixed;
                lp.headerStartMarginIsAuto = !marginsFixed;
            }
            lp.setSlm(LinearSLM.ID);
            lp.setColumnWidth(getContext().getResources().getDimensionPixelSize(R.dimen.grid_column_width));
            Log.e("sectionFirstPosition", customer.getSectionFirstPosition()+"");
            lp.setFirstPosition(customer.getSectionFirstPosition());
            listViewHolder.itemView.setLayoutParams(lp);
            Log.e("onBindViewHolder", "---");
        }
    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        if(listingType ==  ListingType.BASIC)
            return super.getItemViewType(position);
        else {
            Log.e("isHeader", getItem(position).getName()+" || "+getItem(position).isHeader());
            return getItem(position).isHeader() ? VIEW_TYPE_HEADER : VIEW_TYPE_CONTENT;
        }
    }

    @Override
    public void initializeRecyclerView(Context context, RecyclerView rvProducts) {
        if(listingType == ListingType.BASIC)
            super.initializeRecyclerView(context, rvProducts);
        else {
            Log.e("initializeRecyclerView", "LETTER_HEADER");
            layoutManager = new LayoutManager(context);
            rvProducts.setLayoutManager(layoutManager);
            rvProducts.setHasFixedSize(true);
        }
    }

    public class ListViewHolder extends BaseRecyclerAdapter.ViewHolder {
        public TextView tvFirstLetter;
        public TextView tvCustomerName, tvAlternateId, tvAddress;
        public LinearLayout llCustomerItem;
        private Customer customer;

        public View itemView;
        public TextView tvLetterHeader;

        public ListViewHolder(View itemView) {
            super(itemView);
            if(listingType == ListingType.BASIC) {
                llCustomerItem = (LinearLayout) itemView.findViewById(R.id.llCustomerItem);
                tvFirstLetter = (TextView) itemView.findViewById(R.id.tvFirstLetter);
                tvCustomerName = (TextView) itemView.findViewById(R.id.tvCustomerName);
                tvAlternateId = (TextView) itemView.findViewById(R.id.tvAlternateId);
                tvAddress = (TextView) itemView.findViewById(R.id.tvAddress);
            }
            else {
                this.itemView = itemView;
                if(itemView.findViewById(R.id.tvLetterHeader) != null)
                    tvLetterHeader = (TextView) itemView.findViewById(R.id.tvLetterHeader);
                else {
                    llCustomerItem = (LinearLayout) itemView.findViewById(R.id.llCustomerItem);
                    tvCustomerName = (TextView) itemView.findViewById(R.id.tvCustomerName);
                    tvAddress = (TextView) itemView.findViewById(R.id.tvAddress);
                }
            }
            itemView.setOnClickListener(this);
        }

        public void setCustomer(Customer customer) {
            this.customer = customer;
        }

        @Override
        public void onClick(View view) {
            if(isMultiSelect) {
                if(selectedCustomer.contains(customer))
                    selectedCustomer.remove(customer);
                else
                    selectedCustomer.add(customer);
            }
            else {
                if(selectedCustomer.contains(customer))
                    selectedCustomer.remove(customer);
                else {
                    selectedCustomer.clear();
                    selectedCustomer.add(customer);
                }
            }

            if(onItemClickListener != null)
                onItemClickListener.onItemClicked(view, getLayoutPosition());

            notifyDataSetChanged();
        }


        @Override
        public boolean onLongClick(View view) {
            if(onItemLongClickListener != null)
                onItemLongClickListener.onItemLongClicked(view, getLayoutPosition());
            return true;
        }
    }
}
