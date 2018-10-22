package com.example.cpu11112_local.testdragvideo.test;

import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;

import com.example.cpu11112_local.testdragvideo.R;
import com.example.cpu11112_local.testdragvideo.dragVideo.DragVideoYoutubeView;

import org.salient.artplayer.MediaPlayerManager;
import org.salient.artplayer.ScaleType;
import org.salient.artplayer.VideoView;
import org.salient.artplayer.ui.ControlPanel;

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
    @BindView(R.id.videoView)
    VideoView mVideoView;
    private Rect mMainContentRect = new Rect();

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
                String url = "https://vnso-zn-11-tf-mcloud-bf-s7-mv-zmp3.zadn.vn/bKrForK3IzM/5e0baf84c1c0289e71d1/f5d9ff48d70d3e53671c/480/Thang-Dien.mp4?authen=exp=1540392821~acl=/bKrForK3IzM/*~hmac=631abeaf60db61b4bdc0f1032a6e6651";
//                mVideoView.setUp("https://github.com/moyokoo/Media/blob/master/Azshara.mp4?raw=true");
                mVideoView.setUp(url);
                mVideoView.start();
                MediaPlayerManager.instance().setScreenScale(ScaleType.SCALE_CENTER_CROP);
            }
        });
        mMainRcv.setAdapter(adapter);

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
        mDragVideo.setCallback(mDragVideoCallback);
        mVideoView.setControlPanel(new ControlPanel(this));
    }

    DragVideoYoutubeView.Callback mDragVideoCallback = new DragVideoYoutubeView.Callback() {
        @Override
        public void onDisappear(int direct) {
            MediaPlayerManager.instance().releasePlayerAndView(MainActivity.this);
        }

        @Override
        public void onOffsetChange(float verticalOffset) {
            int heightBottomNav = mBottomNav.getHeight();
            mBottomNav.setTranslationY(heightBottomNav - verticalOffset * heightBottomNav);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                // prevent overdrawing while this view is overlap
                mMainContentRect.set(mMainRcv.getLeft(), mMainRcv.getTop(), mMainRcv.getRight(), (int)
                        (mMainRcv.getBottom() * verticalOffset));
                mMainRcv.setClipBounds(mMainContentRect);
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (MediaPlayerManager.instance().backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaPlayerManager.instance().pause();
    }

    @Override
    protected void onDestroy() {
        mDragVideo.removeCallback(mDragVideoCallback);
        MediaPlayerManager.instance().releasePlayerAndView(this);
        super.onDestroy();
    }
}
