package net.nueca.concessioengine.fragments;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListView;

import net.nueca.concessioengine.fragments.interfaces.ListScrollListener;
import net.nueca.concessioengine.fragments.interfaces.SetupActionBar;
import net.nueca.concessioengine.lists.SelectedProductItemList2;
import net.nueca.imonggosdk.fragments.ImonggoFragment;

import java.util.List;

/**
 * Created by gama on 9/10/15.
 */
public abstract class BaseReviewFragment extends ImonggoFragment {
    protected static final long LIMIT = 15l;
    protected long offset = 0l;
    protected int prevLast = -1;

    protected ListScrollListener listScrollListener;
    protected boolean useRecyclerView = true;

    protected Toolbar tbActionBar;
    protected SetupActionBar setupActionBar;

    protected RecyclerView rvProducts;
    protected ListView lvProducts;

    protected FloatingActionButton fabContinue;

    protected SelectedProductItemList2 selectedProductItemList2;

    protected abstract void whenListEndReached(List objects);
    protected abstract void toggleNoItems(String msg, boolean show);
    protected abstract List getObjects();

    protected abstract int getRecyclerAdapterItemCount();
    protected abstract int getRecyclerAdapterFirstVisibleItemPosition();

    public void setSelectedProductItemList2(SelectedProductItemList2 selectedProductItemList2) {
        this.selectedProductItemList2 = selectedProductItemList2;
    }

    public ListScrollListener getListScrollListener() {
        return listScrollListener;
    }

    public void setListScrollListener(ListScrollListener listScrollListener) {
        this.listScrollListener = listScrollListener;
    }

    public boolean isUseRecyclerView() {
        return useRecyclerView;
    }

    public void setUseRecyclerView(boolean useRecyclerView) {
        this.useRecyclerView = useRecyclerView;
    }

    public void setSetupActionBar(SetupActionBar setupActionBar) {
        this.setupActionBar = setupActionBar;
    }

    protected AbsListView.OnScrollListener lvScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                if (listScrollListener != null)
                    listScrollListener.onScrollStopped();

                if(fabContinue != null) {
                    ViewCompat.animate(fabContinue).translationY(0.0f).setDuration(400)
                            .setInterpolator(new AccelerateDecelerateInterpolator()).start();
                }

            } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                if (listScrollListener != null)
                    listScrollListener.onScrolling();

                if(fabContinue != null) {
                    ViewCompat.animate(fabContinue).translationY(1000.0f).setDuration(400)
                            .setInterpolator(new AccelerateDecelerateInterpolator()).start();
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            int lastItem = firstVisibleItem + visibleItemCount;
            if (lastItem == totalItemCount) {
                if (prevLast != lastItem) {
                    offset += LIMIT;
                    whenListEndReached(getObjects());
                    prevLast = lastItem;
                }
            }
        }
    };

    protected RecyclerView.OnScrollListener rvScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (listScrollListener != null)
                    listScrollListener.onScrollStopped();

                if(fabContinue != null) {
                    ViewCompat.animate(fabContinue).translationY(0.0f).setDuration(400)
                            .setInterpolator(new AccelerateDecelerateInterpolator()).start();
                }
            }
            else if(newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                if(listScrollListener != null)
                    listScrollListener.onScrolling();

                if(fabContinue != null) {
                    ViewCompat.animate(fabContinue).translationY(1000.0f).setDuration(400)
                            .setInterpolator(new AccelerateDecelerateInterpolator()).start();
                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int visibleItemCount = rvProducts.getChildCount();
            int totalItemCount = getRecyclerAdapterItemCount();
            int firstVisibleItem = getRecyclerAdapterFirstVisibleItemPosition();

            int lastItem = firstVisibleItem + visibleItemCount;

            if(lastItem == totalItemCount) {
                if(prevLast != lastItem) {
                    offset += LIMIT;
                    whenListEndReached(getObjects());
                    prevLast = lastItem;
                }
            }
        }
    };
}
