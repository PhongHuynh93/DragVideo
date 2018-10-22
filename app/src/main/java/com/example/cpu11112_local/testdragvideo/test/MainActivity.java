package com.example.cpu11112_local.testdragvideo.test;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Surface;
import android.view.TextureView;
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
    @BindView(R.id.videoView)
    TextureView mVideoView;
    private Rect mMainContentRect = new Rect();
    private MediaPlayer mMediaPlayer;

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
                if (mMediaPlayer.isPlaying())
                    return;
                try {
                    mMediaPlayer.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mMediaPlayer.start();
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

        mMediaPlayer = MediaPlayer.create(this, R.raw.test_4);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayer.setLooping(true);
            }
        });

        mVideoView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mMediaPlayer.setSurface(new Surface(surface));
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                finish();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    DragVideoYoutubeView.Callback mDragVideoCallback = new DragVideoYoutubeView.Callback() {
        @Override
        public void onDisappear(int direct) {
            mMediaPlayer.pause();
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
    protected void onDestroy() {
        mDragVideo.removeCallback(mDragVideoCallback);
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
