package net.nueca.concessio_test;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.nueca.concessioengine.adapters.base.BaseRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

public class CardViewTests extends AppCompatActivity {

    private RecyclerView rvModules;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_view_tests);

        rvModules = (RecyclerView) findViewById(R.id.rvModules);
        rvModules.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(this, 2);
        rvModules.setLayoutManager(mLayoutManager);

        CardViewAdapter cardViewAdapter = new CardViewAdapter(this, new ArrayList<String>(){{
            add("Booking");
            add("Customers");
            add("Receiving");
            add("Pullout");
            add("MSO");
            add("History");
            add("Pullout Request");
            add("Pullout Confirmation");
        }});
        rvModules.setAdapter(cardViewAdapter);
    }

    public class CardViewAdapter extends BaseRecyclerAdapter<CardViewAdapter.CVHolder, String> {

        public CardViewAdapter(Context context, List<String> list) {
            super(context, list);
        }

        @Override
        public CVHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.the_card, parent, false);

            CVHolder cvh = new CVHolder(v);
            return cvh;
        }

        @Override
        public void onBindViewHolder(CVHolder holder, int position) {

            holder.tvTitle.setText(getItem(position));

        }

        @Override
        public int getItemCount() {
            return getCount();
        }

        public class CVHolder extends BaseRecyclerAdapter.ViewHolder {

            TextView tvTitle;

            public CVHolder(View itemView) {
                super(itemView);

                tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
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
}
