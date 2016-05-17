package net.nueca.concessio_test;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tonicartos.superslim.GridSLM;
import com.tonicartos.superslim.LayoutManager;
import com.tonicartos.superslim.LinearSLM;

import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

import ca.barrenechea.widget.recyclerview.decoration.DividerDecoration;
import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderAdapter;

public class CustomerList extends AppCompatActivity {

    private static final int VIEW_TYPE_HEADER = 0x01;
    private static final int LINEAR = 0;
    private static final int VIEW_TYPE_CONTENT = 0x00;

    private RecyclerView rvCustomers;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<String> customers = new ArrayList<String>(){{
        add("Allan Macatingrao");
        add("Allan Macatingrao");
        add("Allan Macatingrao");
        add("Allan Macatingrao");
        add("Allan Macatingrao");
        add("Allan Macatingrao");
        add("Allan Macatingrao");
        add("Allan Macatingrao");
        add("Allan Macatingrao");
        add("Keazia Camille Moralina");
        add("Keazia Camille Moralina");
        add("Keazia Camille Moralina");
        add("Keazia Camille Moralina");
        add("Keazia Camille Moralina");
        add("Nujian Den Mark Meralpis");
        add("Nujian Den Mark Meralpis");
        add("Nujian Den Mark Meralpis");
        add("Rhymart Manchus");
        add("Gamaliel dela Cruz");
        add("Gamaliel dela Cruz");
        add("Gamaliel dela Cruz");
        add("Gamaliel dela Cruz");
        add("Gamaliel dela Cruz");
        add("Gamaliel dela Cruz");
        add("John Roger Celada");
        add("Olivia Marie Peñero");
        add("Olivia Marie Peñero");
        add("Olivia Marie Peñero");
        add("Olivia Marie Peñero");
        add("Ronie Amata");
        add("Michelle Basbas");
        add("Michelle Basbas");
        add("Michelle Basbas");
        add("Michelle Basbas");
        add("Michelle Basbas");
    }};

    private List<ArrayList<String>> allItems = new ArrayList<ArrayList<String>>(){{
        add(new ArrayList<String>(){{
            add("Allan Macatingrao");
            add("Allan Macatingrao");
            add("Allan Macatingrao");
            add("Allan Macatingrao");
            add("Allan Macatingrao");
            add("Allan Macatingrao");
            add("Allan Macatingrao");
            add("Allan Macatingrao");
            add("Allan Macatingrao");
        }});

        add(new ArrayList<String>(){{
            add("Keazia Camille Moralina");
            add("Keazia Camille Moralina");
            add("Keazia Camille Moralina");
            add("Keazia Camille Moralina");
            add("Keazia Camille Moralina");
        }});

        add(new ArrayList<String>(){{
            add("Nujian Den Mark Meralpis");
            add("Nujian Den Mark Meralpis");
            add("Nujian Den Mark Meralpis");
        }});

        add(new ArrayList<String>(){{
            add("Rhymart Manchus");
        }});

        add(new ArrayList<String>(){{
            add("Gamaliel dela Cruz");
            add("Gamaliel dela Cruz");
            add("Gamaliel dela Cruz");
            add("Gamaliel dela Cruz");
            add("Gamaliel dela Cruz");
            add("Gamaliel dela Cruz");
        }});

        add(new ArrayList<String>(){{
            add("John Roger Celada");
        }});

        add(new ArrayList<String>(){{
            add("Olivia Marie Peñero");
            add("Olivia Marie Peñero");
            add("Olivia Marie Peñero");
            add("Olivia Marie Peñero");
        }});

        add(new ArrayList<String>(){{
            add("Michelle Basbas");
            add("Michelle Basbas");
            add("Michelle Basbas");
            add("Michelle Basbas");
            add("Michelle Basbas");
        }});
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_list);

        rvCustomers = (RecyclerView) findViewById(R.id.rvCustomers);
        rvCustomers.setHasFixedSize(true);
        mLayoutManager = new LayoutManager(this);
        rvCustomers.setLayoutManager(mLayoutManager);

        ArrayList<LineItem> finalCustomers = new ArrayList<>();

        //Insert headers into list of items.
        String lastHeader = "";
        int headerCount = 0;
        int itemsCount = 0;
        int sectionFirstPosition = 0;
        int i = 1;
        for(ArrayList<String> items : allItems) {
            finalCustomers.add(new LineItem("Header "+i, true, sectionFirstPosition));
            Log.e("customers", "Header "+i+"["+sectionFirstPosition+"]");
            for(String item : items) {
                finalCustomers.add(new LineItem(item, false, sectionFirstPosition));
                Log.e("customers", item+"["+sectionFirstPosition+"]");
                itemsCount++;
            }

            headerCount++;
            sectionFirstPosition = itemsCount+headerCount;
            i++;
        }
//        for (int i = 0; i < customers.size(); i++) {
//            String header = customers.get(i).substring(0, 1);
//            if (!TextUtils.equals(lastHeader, header)) {
//                // Insert new header view and update section data.
//                sectionFirstPosition = i + headerCount;
//                lastHeader = header;
//                headerCount += 1;
//                Log.e("customers", header+"["+sectionFirstPosition+"]");
//                finalCustomers.add(new LineItem(header, true, sectionFirstPosition));
//            }
//            Log.e("customers", customers.get(i)+"["+sectionFirstPosition+"]");
//            finalCustomers.add(new LineItem(customers.get(i), false, sectionFirstPosition));
//        }
        CustomersAdapter customersAdapter = new CustomersAdapter(this, finalCustomers);
        rvCustomers.setAdapter(customersAdapter);
    }

    public class CustomersAdapter extends BaseRecyclerAdapter<CustomersAdapter.CVHolder, LineItem> {

        private int mHeaderDisplay;
        private boolean mMarginsFixed;

        public CustomersAdapter(Context context, List<LineItem> list) {
            super(context, list);

            mHeaderDisplay = getContext().getResources().getInteger(R.integer.default_header_display);
            mMarginsFixed = getContext().getResources().getBoolean(R.bool.default_margins_fixed);
        }

        @Override
        public CVHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;
            if (viewType == VIEW_TYPE_HEADER) {
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.section_header, parent, false);
            } else {
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.section_items, parent, false);
            }

            CVHolder cvh = new CVHolder(v);
            return cvh;
        }

        @Override
        public void onBindViewHolder(CVHolder holder, int position) {
            final LineItem item = getItem(position);
            final View itemView = holder.itemView;

            holder.tvTitle.setText("["+position+"]"+item.text);
            final GridSLM.LayoutParams lp = GridSLM.LayoutParams.from(itemView.getLayoutParams());
            // Overrides xml attrs, could use different layouts too.
            if (item.isHeader) {
//                lp.headerDisplay = mHeaderDisplay;
                if (lp.isHeaderInline() || (mMarginsFixed && !lp.isHeaderOverlay())) {
                    lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                } else {
                    lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                }

                lp.headerEndMarginIsAuto = !mMarginsFixed;
                lp.headerStartMarginIsAuto = !mMarginsFixed;
            }
            lp.setSlm(LinearSLM.ID);
//            lp.setColumnWidth(getContext().getResources().getDimensionPixelSize(R.dimen.grid_column_width));
            lp.setFirstPosition(item.sectionFirstPosition);
            itemView.setLayoutParams(lp);
        }

        @Override
        public int getItemCount() {
            return getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).isHeader ? VIEW_TYPE_HEADER : VIEW_TYPE_CONTENT;
        }

        public class CVHolder extends BaseRecyclerAdapter.ViewHolder {

            TextView tvTitle;

            public CVHolder(View itemView) {
                super(itemView);

                tvTitle = (TextView) itemView.findViewById(R.id.text);
            }

            @Override
            public void onClick(View v) {

            }

            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        }

    }


    public class LineItem {

        public int sectionFirstPosition;
        public boolean isHeader = false;
        public String text;

        public LineItem(String text, boolean isHeader, int sectionFirstPosition) {
            this.isHeader = isHeader;
            this.text = text;
            this.sectionFirstPosition = sectionFirstPosition;
        }
    }
}
