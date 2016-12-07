package com.example.neb.radarviewdemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Neb on 2016/12/7.
 */

public class FloatView extends LinearLayout {
    private TextView poiName;
    private TextView poiDistance;
    private int left = 0;
    private int top = 0;

    public FloatView(Context context, int left, int top) {
        super(context);
        this.left = left;
        this.top = top;
        init();
    }

    private void init() {
        poiName = new TextView(getContext());
        poiDistance = new TextView(getContext());
/*        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);*/
        //params.gravity = Gravity.CENTER_VERTICAL;
        poiName.setText("---店名----");
        poiDistance.setText("距离");
        poiName.setGravity(Gravity.CENTER);
        poiDistance.setGravity(Gravity.CENTER);
        //设置悬浮view的初始位置
        FrameLayout.LayoutParams llparams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        llparams.setMargins(left, top, 50, 50);
        this.setLayoutParams(llparams);
        setOrientation(VERTICAL);
        addView(poiName);
        addView(poiDistance);
    }

    public FloatView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FloatView setName(String poiName) {
        this.poiName.setText(poiName);
        return this;
    }

    public FloatView setDistance(int distance) {
        this.poiDistance.setText(distance + "米");
        return this;
    }
}