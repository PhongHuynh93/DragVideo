package com.example.cpu11112_local.testdragvideo.test;

import android.content.Context;
import android.util.AttributeSet;

import org.salient.artplayer.VideoView;

public class MvTextureView extends VideoView {
    public static final float VIDEO_THUMBNAIL_RATIO = 9f / 16;

    public MvTextureView(Context context) {
        super(context);
    }

    public MvTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MvTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(w, (int) (w * VIDEO_THUMBNAIL_RATIO));
    }
}
