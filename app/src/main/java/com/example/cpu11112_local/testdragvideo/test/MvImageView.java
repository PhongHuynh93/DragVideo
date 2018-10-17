package com.example.cpu11112_local.testdragvideo.test;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

public class MvImageView extends AppCompatImageView {
    public static final float VIDEO_THUMBNAIL_RATIO = 9f / 16;

    public MvImageView(Context context) {
        super(context);
    }

    public MvImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MvImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = View.MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(w, (int) (w * VIDEO_THUMBNAIL_RATIO));
    }
}
