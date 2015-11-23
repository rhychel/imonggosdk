package net.nueca.concessio_test;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SearchViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import net.nueca.concessioengine.activities.module.ModuleActivity;
import net.nueca.concessioengine.fragments.SimpleReceiveFragment;
import net.nueca.concessioengine.fragments.SimpleReceiveReviewFragment;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.lists.ReceivedProductItemList;
import net.nueca.concessioengine.views.SearchViewEx;
import net.nueca.imonggosdk.enums.DocumentTypeCode;
import net.nueca.imonggosdk.objects.Branch;
import net.nueca.imonggosdk.objects.Product;
import net.nueca.imonggosdk.objects.document.Document;
import net.nueca.imonggosdk.objects.document.DocumentLine;
import net.nueca.imonggosdk.objects.deprecated.DocumentLineExtras;

import java.sql.SQLException;
import java.util.List;

public class Receive extends ModuleActivity implements SetupActionBar {

    private FloatingActionButton fab;
    private SimpleReceiveFragment simpleReceiveFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
/*

        try {
            Log.e("Device ID", "" + getSession().getDevice_id());
            List<Document> documents = getHelper().getDocuments().queryForAll();
            for(Document doc : documents)
                if(doc.getDocument_lines().size() >= 30) {
                    Log.e("Doc", doc.getReference());
                    break;
                }
            if(getHelper().getDocuments().queryForAll().size() <= 30) {
                Document document = new Document.Builder()
                        .id(getHelper().getDocuments().queryForAll().size() + 1)
                        .generateReference(this, getSession().getDevice_id())
                        .document_type_code(DocumentTypeCode.RECEIVE_BRANCH)
                        .target_branch_id(357)
                        .build();

                for (Product product : getHelper().getProducts().queryForAll()) {
                    document.addDocumentLine(
                            new DocumentLine.Builder()
                                    .autoLine_no()
                                    .useProductDetails(product)
                                    .quantity((int) (Math.random() * 100) % 1000)
                                    .discount_text("0.0%")
                                    .price(1)
                                    .unit_content_quantity(1)
                                    .unit_name("PC")
                                    .unit_quantity((int) (Math.random() * 10) % 100)
                                    .build());
                    if(document.getDocument_lines().size() == 1)
                        document.addDocumentLine(new DocumentLine.Builder()
                                .autoLine_no()
                                .useProductDetails(product)
                                .quantity(10000)
                                .discount_text("0.0%")
                                .price(1)
                                .unit_content_quantity(1)
                                .unit_name("PC")
                                .extras(new DocumentLineExtras.Builder().batch_no("123").build())
                                .unit_quantity((int) (Math.random() * 10) % 100)
                                .build());

                    if (document.getDocument_lines() != null &&
                            document.getDocument_lines().size() > 20)
                        break;
                }

                document.insertTo(getHelper());
                Log.e("ref no", document.getReference());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
*/

        fab = (FloatingActionButton) findViewById(R.id.fab);

        //toolbar.setBackgroundColor(fetchAccentColor(this));
        //tvDRNo.setBackgroundColor(Color.BLACK);

        simpleReceiveFragment = new SimpleReceiveFragment();
        simpleReceiveFragment.setHelper(getHelper());
        simpleReceiveFragment.setSetupActionBar(this);
        simpleReceiveFragment.setUseRecyclerView(false);
        simpleReceiveFragment.setProductCategories(getProductCategories(true));
        /*simpleReceiveFragment.setMultipleInput(true);
        simpleReceiveFragment.setMultiInputListener(new MultiInputListener() {
            @Override
            public void showInputScreen(Product product) {
                Intent intent = new Intent(Receive.this, TestMultiInput.class);
                intent.putExtra(MultiInputSelectedItemFragment.PRODUCT_ID, product.getId());
                startActivity(intent);
            }
        });*/
        /*simpleReceiveFragment.setListScrollListener(new ListScrollListener() {
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
        });*/
        /*simpleReceiveFragment.setProductsFragmentListener(new BaseProductsFragment.ProductsFragmentListener() {
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
        });*/
        simpleReceiveFragment.setFABListener(new SimpleReceiveFragment.FloatingActionButtonListener() {
            @Override
            public void onClick(ReceivedProductItemList receivedProductItemList, Branch targetBranch, String reference, Integer parentDocumentID) {
                SimpleReceiveReviewFragment simpleReceiveReviewFragment = new SimpleReceiveReviewFragment();
                simpleReceiveReviewFragment.setParentID(parentDocumentID);
                simpleReceiveReviewFragment.setTargetBranch(targetBranch);
                simpleReceiveReviewFragment.setDRNo(reference);
                simpleReceiveReviewFragment.setUseRecyclerView(true);
                simpleReceiveReviewFragment.setHelper(getHelper());
                simpleReceiveReviewFragment.setReceivedProductItemList(receivedProductItemList);
                simpleReceiveReviewFragment.setIsManual(simpleReceiveFragment.isManual());

                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                                R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.flContent, simpleReceiveReviewFragment)
                        .addToBackStack("review_fragment")
                        .commit();
            }
        });

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.flContent, simpleReceiveFragment)
                //.addToBackStack("receive_fragment")
                .commit();
    }
    FragmentManager fragmentManager;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.simple_products_menu, menu);
        mSearch = (SearchViewEx) menu.findItem(R.id.mSearch).getActionView();
        if(mSearch != null) {
            mSearch.setSearchViewExListener(new SearchViewEx.SearchViewExListener() {
                @Override
                public void whenBackPressed() {
                    if(!mSearch.isIconified())
                        mSearch.setIconified(true);
                }
            });
            mSearch.setIconifiedByDefault(true);
            SearchViewCompat.setOnQueryTextListener(mSearch, new SearchViewCompat.OnQueryTextListenerCompat() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    //simpleProductsFragment.updateListWhenSearch(newText);
                    simpleReceiveFragment.updateListWhenSearch(newText);
                    return true;
                }

            });
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //getSupportFragmentManager().popBackStack();
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

    @Override
    public void setupActionBar(Toolbar toolbar) {
        /*ViewGroup parent = (ViewGroup) toolbar.getParent();
        int index = parent.indexOfChild(toolbar);

        View view = getLayoutInflater().inflate(R.layout.receive_toolbar_ext, parent, false);
        view.setBackgroundColor(fetchAccentColor(this));
        tvDRNo = (TextView) view.findViewById(R.id.tvDRNo);

        parent.addView(view, index + 1);*/

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }
}
