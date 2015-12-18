package net.nueca.concessioengine.views;

import android.content.Context;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Created by rhymart on 7/27/15.
 * imonggosdk (c)2015
 */
@Deprecated
public class SearchViewEx extends SearchView {

    public interface SearchViewExListener {
        void whenBackPressed();
    }

    private SearchViewExListener searchViewExListener;

    public SearchViewEx(Context context) {
        super(context);
    }

    public SearchViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if(KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
            if(!getQuery().toString().trim().equals(""))
                return super.dispatchKeyEventPreIme(event);
            if(searchViewExListener != null)
                searchViewExListener.whenBackPressed();
            return true;
        }
        return super.dispatchKeyEventPreIme(event);
    }

    public void setSearchViewExListener(SearchViewExListener searchViewExListener) {
        this.searchViewExListener = searchViewExListener;
    }
}
