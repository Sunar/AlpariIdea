package com.alpari.avia_kos.alpidea;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by Avia-Kos on 18.05.16.
 */
public class MySwipeRefreshLayout extends SwipeRefreshLayout {
    ListView mListView;
    public MySwipeRefreshLayout(Context context) {
        super(context);
    }

    public MySwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListView(ListView mListView) {
        this.mListView = mListView;
    }

    @Override
    public boolean canChildScrollUp() {
        if(mListView == null)
            return super.canChildScrollUp();
        else
            return mListView.getFirstVisiblePosition() != 0;
    }
}
