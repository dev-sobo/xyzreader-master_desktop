package com.example.xyzreader.ui;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.view.View;

/**
 * Created by ian on 2/1/2016.
 */
public class ObserableNestedScrollView extends NestedScrollView {
    public ObserableNestedScrollView(Context context) {
        super(context);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

    }


}
