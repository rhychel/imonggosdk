package net.nueca.concessio_test;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import net.nueca.concessioengine.activities.ModuleActivity;
import net.nueca.concessioengine.adapters.tools.ProductsAdapterHelper;
import net.nueca.concessioengine.dialogs.SearchDRDialog;
import net.nueca.concessioengine.fragments.BaseProductsFragment;
import net.nueca.concessioengine.fragments.MultiInputSelectedItemFragment;
import net.nueca.concessioengine.fragments.SimpleProductsFragment;
import net.nueca.concessioengine.fragments.interfaces.ListScrollListener;
import net.nueca.concessioengine.fragments.interfaces.MultiInputListener;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.views.SimpleReceiveToolbarExt;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Product;

import java.sql.SQLException;

public class Receive extends ModuleActivity implements SetupActionBar {

    private FloatingActionButton fab;
    private TextView tvDRNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        //toolbar.setBackgroundColor(fetchAccentColor(this));
        //tvDRNo.setBackgroundColor(Color.BLACK);

        SimpleProductsFragment simpleProductsFragment = new SimpleProductsFragment();
        simpleProductsFragment.setHelper(getHelper());
        simpleProductsFragment.setSetupActionBar(this);
        simpleProductsFragment.setProductCategories(getProductCategories(true));
        simpleProductsFragment.setMultipleInput(true);
        simpleProductsFragment.setMultiInputListener(new MultiInputListener() {
            @Override
            public void showInputScreen(Product product) {
                Intent intent = new Intent(Receive.this, TestMultiInput.class);
                intent.putExtra(MultiInputSelectedItemFragment.PRODUCT_ID, product.getId());
                startActivity(intent);
            }
        });
        simpleProductsFragment.setListScrollListener(new ListScrollListener() {
            @Override
            public void onScrolling() {
                ViewCompat.animate(fab).translationY(1000.0f).setDuration(400).setInterpolator(new
                        AccelerateDecelerateInterpolator()).start();
            }

            @Override
            public void onScrollStopped() {
                ViewCompat.animate(fab).translationY(0.0f).setDuration(400).setInterpolator(
                        new AccelerateDecelerateInterpolator()).start();
            }
        });
        simpleProductsFragment.setProductsFragmentListener(new BaseProductsFragment.ProductsFragmentListener() {
            @Override
            public void whenItemsSelectedUpdated() {
                if (ProductsAdapterHelper.getSelectedProductItems().size() > 0)
                    ViewCompat.animate(fab).translationY(0.0f).setDuration(400).setInterpolator(
                            new AccelerateDecelerateInterpolator()).start();
                else {
                    if (ProductsAdapterHelper.getSelectedProductItems().size() > 0)
                        ViewCompat.animate(fab).translationY(1000.0f).setDuration(400).setInterpolator(
                                new AccelerateDecelerateInterpolator()).start();
                }
            }
        });

        getSupportFragmentManager().beginTransaction()
                .add(R.id.flContent, simpleProductsFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.simple_products_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private int fetchAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    @Override
    public void setupActionBar(Toolbar toolbar) {
        /*ViewGroup parent = (ViewGroup) toolbar.getParent();
        int index = parent.indexOfChild(toolbar);

        View view = getLayoutInflater().inflate(R.layout.receive_toolbar_ext, parent, false);
        view.setBackgroundColor(fetchAccentColor(this));
        tvDRNo = (TextView) view.findViewById(R.id.tvDRNo);

        parent.addView(view, index + 1);*/

        final SimpleReceiveToolbarExt simpleReceiveToolbarExt = new SimpleReceiveToolbarExt();
        simpleReceiveToolbarExt.attachAfter(this, toolbar, false);
        simpleReceiveToolbarExt.setOnClickListener(new SimpleReceiveToolbarExt.OnToolbarClickedListener() {
            @Override
            public void onClick() {
                SearchDRDialog searchDRDialog = null;
                try {
                    searchDRDialog = new SearchDRDialog(Receive.this, getHelper(), getUser());

                    searchDRDialog.setDialogListener(new SearchDRDialog.SearchDRDialogListener() {
                        @Override
                        public void onCancel() {

                        }

                        @Override
                        public void onSearch(String deliveryReceiptNo, Branch branch) {
                            if(tvDRNo == null)
                                tvDRNo = (TextView) simpleReceiveToolbarExt.getToolbarExtensionView()
                                    .findViewById(R.id.tvDRNo);
                            tvDRNo.setText(deliveryReceiptNo);
                            Toast.makeText(Receive.this,branch != null? branch.toString() : "",Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                    searchDRDialog.show();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }
}
