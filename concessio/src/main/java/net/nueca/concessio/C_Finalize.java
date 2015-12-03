package net.nueca.concessio;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.adapters.enums.ListingType;
import net.nueca.concessioengine.fragments.SimpleProductsFragment;
import net.nueca.imonggosdk.fragments.ImonggoFragment;

/**
 * Created by rhymart on 8/22/15.
 * imonggosdk2 (c)2015
 */
public class C_Finalize extends ModuleActivity {

    private Toolbar tbActionBar;
    private TabLayout tlTotal;
    private ViewPager vpReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_review_activity);

        tbActionBar = (Toolbar) findViewById(R.id.tbActionBar);
        setSupportActionBar(tbActionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Review");

        tlTotal = (TabLayout) findViewById(R.id.tlTotal);
        vpReview = (ViewPager) findViewById(R.id.vpReview);

        ReviewAdapter reviewAdapter = new ReviewAdapter(getSupportFragmentManager());
        vpReview.setAdapter(reviewAdapter);

        tlTotal.setupWithViewPager(vpReview);

        tbActionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.simple_review_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mReturn: {
                AlertDialog.Builder addAndReturnDialog = new AlertDialog.Builder(this, R.style.AppCompatDialogStyle_Light);
                addAndReturnDialog.setTitle("Add and Return Items?");
                addAndReturnDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                addAndReturnDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                addAndReturnDialog.show();
            } break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class ReviewAdapter extends FragmentPagerAdapter {

        public ReviewAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            SimpleProductsFragment simpleProductsFragment = SimpleProductsFragment.newInstance();
            simpleProductsFragment.setHelper(getHelper());
            simpleProductsFragment.setListingType(ListingType.SALES);
            simpleProductsFragment.setHasUnits(true);
            simpleProductsFragment.setHasToolBar(false);
            simpleProductsFragment.setHasCategories(false);
            return simpleProductsFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position == 0)
                return "TOTAL SALES";
            return "TOTAL RETURNS";
        }
    }
}
