package com.example.cpu11112_local.testdragvideo.test;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;

import com.example.cpu11112_local.testdragvideo.R;
import com.example.cpu11112_local.testdragvideo.dragVideo.DragVideoYoutubeView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.mainRcv)
    RecyclerView mMainRcv;
    @BindView(R.id.bottom_navigation)
    View mBottomNav;
    @BindView(R.id.dragVideo)
    DragVideoYoutubeView mDragVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mMainRcv.setLayoutManager(new GridLayoutManager(this, 2));
        TestDataAdapter adapter = new TestDataAdapter(new TestDataAdapter.OnAdapterInteract() {
            @Override
            public void onClickVideo(int adapterPosition) {
                mDragVideo.show();
            }
        });
        mMainRcv.setAdapter(adapter);

        mDragVideo.setCallback(new DragVideoYoutubeView.Callback() {
            @Override
            public void onDisappear(int direct) {

            }

            @Override
            public void onOffsetChange(float verticalOffset) {
                int heightBottomNav = mBottomNav.getHeight();
                mBottomNav.setTranslationY(heightBottomNav - verticalOffset * heightBottomNav);
            }
        });

        mBottomNav.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mBottomNav.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mBottomNav.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                mDragVideo.setOffsetBottom(mBottomNav.getHeight());
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
