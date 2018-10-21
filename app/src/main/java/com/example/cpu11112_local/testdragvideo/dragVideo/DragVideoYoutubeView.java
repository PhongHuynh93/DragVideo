package com.example.cpu11112_local.testdragvideo.dragVideo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cpu11112_local.testdragvideo.R;

import java.lang.ref.WeakReference;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.example.cpu11112_local.testdragvideo.test.MvImageView.VIDEO_THUMBNAIL_RATIO;

/**
 * Created by Phong Huynh on 10/17/2018.
 */
public class DragVideoYoutubeView extends ViewGroup {
    private static final String TAG = DragVideoYoutubeView.class.getSimpleName();
    @BindView(R.id.videoView)
    View mVideoPlayer;
    @BindView(R.id.videoWrapper)
    View mVideoWrapper;
    @BindView(R.id.tvTitle)
    TextView mTvTitle;
    @BindView(R.id.tvArtist)
    TextView mTvArtist;
    @BindView(R.id.info)
    LinearLayout mInfo;
    @BindView(R.id.btnPlayPause)
    ImageButton mBtnPlayPause;
    @BindView(R.id.btnNext)
    ImageButton mBtnNext;
    @BindView(R.id.btnClose)
    ImageButton mBtnClose;
    @BindView(R.id.videoInfoRcv)
    View mVideoInfoRcv;
    @BindView(R.id.videoMiniController)
    View mVideoMiniController;

    @BindDimen(R.dimen.spacing_normal)
    int mNormalSpace;
    @BindDimen(R.dimen.playbar_height)
    int mVideoHeight;
    @BindDimen(R.dimen.playbar_icon)
    int mPlaybarIcon;
    int mVideoHeightStartToMinimize;

    // The direction of the current drag
    public static final int NONE = 1 << 0;
    //    public static final int HORIZONTAL = 1 << 1;
    public static final int VERTICAL_EXPAND_COLLAPSE = 1 << 2;
    public static final int VERTICAL_DISMISS = 1 << 3;

    // The direction of the last slides
    private static final int SLIDE_RESTORE_ORIGINAL = 1 << 0;
    private static final int SLIDE_TO_DISMISS = 1 << 1;

    // the smallest ratio when drag the player
    private static final float PLAYER_RATIO = 0.45f;
    private static final float ORIGINAL_MIN_OFFSET = 1f / (1f + PLAYER_RATIO);
    private static final float LEFT_DRAG_DISAPPEAR_OFFSET = (4f - PLAYER_RATIO) / (4f + 4f * PLAYER_RATIO);
    private static final float RIGHT_DRAG_DISAPPEAR_OFFSET = (4f + PLAYER_RATIO) / (4f + 4f * PLAYER_RATIO);

    // we will scale from 0(expand) -> 1(minizie): and at 0.9, width start to scale
    float PERCENT_START_TO_SCALE = 0.93f;
    // from 0 -> PERCENT_START_TO_SCALE: height will start to scale from original height -> mVideoHeightStartToMinimize

    // Minimum zoom ratio
//    float MIN_RATIO_HEIGHT = 0.35f;
//    float MIN_RATIO_HEIGHT_START_TO_SCALE = 0.45f;
//    float MIN_RATIO_WIDTH = 0.45f;

    // FIXME: 10/17/2018 change the name of 2 view
//    private View mVideoPlayer;
//    private View mDesc;

    private boolean mIsFinishInit;
    private int mPlayerMaxWidth;
    private int mPlayerMaxHeight;

    private float mVerticalOffset = 1f;
    private float mVerticalOffsetDismiss = 1f;
    //    private int mHorizontalRange;
//    private int mVerticalRange;
    private int mLeft;
    private int mTop;
    private boolean mIsMinimum = true;
    private int mDragDirect = NONE;
    private ViewDragHelper mDragHelper;
    private int mDownX;
    private int mDownY;
    private WeakReference<Callback> mCallback;
    private int mDisappearDirect = SLIDE_RESTORE_ORIGINAL;
    //    private float mHorizontalOffset;
    private Unbinder mUnbinder;
    private int mRangeScrollY;
//    private int mMaxHeight;

    // range to start revealing the menu button
    private int mRange1;
    private int mRange2;
    private int mRange3;
    private int mRange4;
    private int mRangeScrollToDismiss;
//    private int mDescMaxHeight;

    public DragVideoYoutubeView(Context context) {
        this(context, null);
    }

    public DragVideoYoutubeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragVideoYoutubeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mDragHelper = ViewDragHelper.create(this, 1f, new MyHelperCallback());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mUnbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        mUnbinder.unbind();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        customMeasure(widthMeasureSpec, heightMeasureSpec);

//        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
//        mMaxHeight = MeasureSpec.getSize(heightMeasureSpec);
//                + SystemUtil.getStatusBarHeight(getContext());

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
//                             resolveSizeAndState(mMaxHeight, heightMeasureSpec, 0));


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (!mIsFinishInit) {
            mIsFinishInit = true;
            restorePosition();
            mRange1 = (int) (getMeasuredWidth() - mNormalSpace * 2 - mPlaybarIcon * 3 - mVideoHeight /
                    VIDEO_THUMBNAIL_RATIO);
            mRange2 = (int) (getMeasuredWidth() - mNormalSpace * 2 - mPlaybarIcon * 2 - mVideoHeight /
                    VIDEO_THUMBNAIL_RATIO);
            mRange3 = (int) (getMeasuredWidth() - mNormalSpace * 2 - mPlaybarIcon - mVideoHeight /
                    VIDEO_THUMBNAIL_RATIO);
            mRange4 = (int) (getMeasuredWidth() - mNormalSpace * 2 - mVideoHeight / VIDEO_THUMBNAIL_RATIO);
            mRangeScrollY = (int) (getMeasuredHeight() - getPaddingTop() - getPaddingBottom() - mVideoHeight);
            mRangeScrollToDismiss = (int) (mVideoHeight);
            mVideoHeightStartToMinimize = (int) (getMeasuredHeight() - mRangeScrollY * PERCENT_START_TO_SCALE);
            Log.e(TAG, "onSizeChanged: mVideoHeightStartToMinimize " + mVideoHeightStartToMinimize);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        onLayoutLightly();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // only allow drag the player
        boolean isHit = mDragHelper.isViewUnder(mVideoWrapper, (int) event.getX(), (int) event.getY());
        if (isHit) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = (int) event.getX();
                    mDownY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    // scroll back to suitable position when we press the video
                    if (mDragDirect == NONE) {
                        int dx = Math.abs(mDownX - (int) event.getX());
                        int dy = Math.abs(mDownY - (int) event.getY());
                        int slop = mDragHelper.getTouchSlop();

                        // detect a click, not a drag
                        if (Math.sqrt(dx * dx + dy * dy) < slop) {
                            mDragDirect = VERTICAL_EXPAND_COLLAPSE;

                            if (mIsMinimum)
                                maximize();
                            else
                                minimize();
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mDragDirect == NONE) {
                        int dx = Math.abs(mDownX - (int) event.getX());
                        int dy = Math.abs(mDownY - (int) event.getY());
                        int slop = mDragHelper.getTouchSlop();

                        if (Math.sqrt(dx * dx + dy * dy) >= slop) {
                            if (dy >= dx) {
                                if (mIsMinimum && mDownY < ((int) event.getY())) {
                                    mDragDirect = VERTICAL_DISMISS;
                                } else {
                                    mDragDirect = VERTICAL_EXPAND_COLLAPSE;
                                }
                            } else {
                                // dont intercept the horizontal touch
                                return false;
                            }
                        }
                    }
                    break;
            }
        }

        mDragHelper.processTouchEvent(event);
        return isHit;
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setCallback(Callback callback) {
        mCallback = new WeakReference<>(callback);
    }

    public void show() {
        this.setAlpha(1f);
        mDragDirect = VERTICAL_EXPAND_COLLAPSE;
        maximize();
    }

    public interface Callback {
        void onDisappear(int direct);
    }

    // hide this view at the bottom of the screen, make the player small so when show the video, it will grow bigger
    private void restorePosition() {
        mVideoWrapper.setAlpha(1f);
        this.setAlpha(0f);
        mLeft = mNormalSpace;
        // TODO: 10/20/2018 add padding bottom to this top
        mTop = mRangeScrollY + mRangeScrollToDismiss;
        mIsMinimum = true;
        mVerticalOffset = 1f;
        mVerticalOffsetDismiss = 1;
    }

    private void customMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measurePlayer(widthMeasureSpec, heightMeasureSpec);
        measureDesc(widthMeasureSpec, heightMeasureSpec);
    }

    private void measurePlayer(int widthMeasureSpec, int heightMeasureSpec) {
        final LayoutParams lp = mVideoPlayer.getLayoutParams();
        if (!mIsFinishInit) {
            mPlayerMaxWidth = MeasureSpec.getSize(
                    getChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight(), lp.width));
            mPlayerMaxHeight = (int) (mPlayerMaxWidth * VIDEO_THUMBNAIL_RATIO);
        }
        justMeasureVideoWrapper();
        justMeasurePlayer();
    }

    private void measureDesc(int widthMeasureSpec, int heightMeasureSpec) {
        measureChild(mVideoInfoRcv, widthMeasureSpec, heightMeasureSpec);
//        if (!mIsFinishInit) {
//            mDescMaxHeight = getMeasuredHeight() - mVideoPlayer.getMeasuredHeight();
//        }
    }

    // adjust the player size depend on the factor
    private void justMeasurePlayer() {
        int newPlayerWidth = (int) (mPlayerMaxWidth - (mNormalSpace * 2 * mVerticalOffset));
        mLeft = (int) (mNormalSpace * mVerticalOffset);

        if (mVerticalOffset >= PERCENT_START_TO_SCALE) {
            // start to scale x
            int heightCurSize = (int) (mVideoHeightStartToMinimize - (mVerticalOffset - PERCENT_START_TO_SCALE) / (1
                    - PERCENT_START_TO_SCALE)
                    * (mVideoHeightStartToMinimize - mVideoHeight));
            int widthCurSize = (int) (newPlayerWidth - (mVerticalOffset - PERCENT_START_TO_SCALE) / (1 -
                    PERCENT_START_TO_SCALE)
                    * (newPlayerWidth - mVideoHeight / VIDEO_THUMBNAIL_RATIO));
            mVideoPlayer.measure(MeasureSpec.makeMeasureSpec(widthCurSize, MeasureSpec.EXACTLY),
                                 MeasureSpec.makeMeasureSpec(heightCurSize, MeasureSpec.EXACTLY));
            Log.e(TAG, String.format("justMeasurePlayer: đã vượt qua 0.9 %d", widthCurSize));
            mVideoPlayer.layout(0, 0, widthCurSize, heightCurSize);

            mVideoMiniController.measure(MeasureSpec.makeMeasureSpec(newPlayerWidth - widthCurSize,
                                                                     MeasureSpec.EXACTLY),
                                         MeasureSpec.makeMeasureSpec(heightCurSize, MeasureSpec.EXACTLY));
            mVideoMiniController.layout(widthCurSize, 0, newPlayerWidth, heightCurSize);

            int widthOffset = (int) (widthCurSize - mVideoHeight / VIDEO_THUMBNAIL_RATIO);
            int infoWidth = Math.max(0, mRange1 - widthOffset);
            int playPauseWidth = mPlaybarIcon;
            int nextWidth = mPlaybarIcon;
            int closeWidth = mPlaybarIcon;

            int playPauseOffset = mRange1 - widthOffset;
            int nextOffset = mRange2 - widthOffset;
            int closeOffset = mRange3 - widthOffset;

            if (widthOffset >= mRange1) {
                infoWidth = 0;
//                playPauseWidth = Math.max(Math.min(mPlaybarIcon, mRange2 - widthOffset), 0);
                playPauseOffset = 0;
            }

            if (widthOffset >= mRange2) {
                playPauseWidth = 0;
//                nextWidth = Math.max(Math.min(mRange3 - widthOffset, mPlaybarIcon), 0);
                nextOffset = 0;
            }

            if (widthOffset >= mRange3) {
                nextWidth = 0;
//                closeWidth = Math.max(Math.min(mRange4 - widthOffset, mPlaybarIcon), 0);
                closeOffset = 0;
            }


            mInfo.measure(MeasureSpec.makeMeasureSpec(infoWidth, MeasureSpec.EXACTLY),
                          MeasureSpec.makeMeasureSpec(heightCurSize, MeasureSpec.EXACTLY));
            mBtnPlayPause.measure(MeasureSpec.makeMeasureSpec(playPauseWidth, MeasureSpec.EXACTLY),
                                  MeasureSpec.makeMeasureSpec(heightCurSize, MeasureSpec.EXACTLY));
            mBtnNext.measure(MeasureSpec.makeMeasureSpec(nextWidth, MeasureSpec.EXACTLY),
                             MeasureSpec.makeMeasureSpec(heightCurSize, MeasureSpec.EXACTLY));
            mBtnClose.measure(MeasureSpec.makeMeasureSpec(closeWidth, MeasureSpec.EXACTLY),
                              MeasureSpec.makeMeasureSpec(heightCurSize, MeasureSpec.EXACTLY));
            Log.e(TAG, String.format("justMeasurePlayer: btnclose width %d height %d", closeWidth, heightCurSize));

            mInfo.layout(0, 0, infoWidth, heightCurSize);
            mBtnPlayPause.layout(playPauseOffset, 0, playPauseOffset + playPauseWidth, heightCurSize);
            mBtnNext.layout(nextOffset, 0, nextOffset + nextWidth, heightCurSize);
            mBtnClose.layout(closeOffset, 0, closeOffset + closeWidth, heightCurSize);
            Log.e(TAG, String.format("justMeasurePlayer: btnclose %d %d", closeOffset, closeOffset + closeWidth));
        } else {
            int heightCurSize = (int) (mPlayerMaxHeight - (mVerticalOffset) / (PERCENT_START_TO_SCALE)
                    * (mPlayerMaxHeight - mVideoHeightStartToMinimize));
            mVideoPlayer.measure(MeasureSpec.makeMeasureSpec(newPlayerWidth, MeasureSpec.EXACTLY),
                                 MeasureSpec.makeMeasureSpec(heightCurSize, MeasureSpec.EXACTLY));
            Log.e(TAG, String.format("justMeasurePlayer: %d", newPlayerWidth));
            mVideoPlayer.layout(0, 0, newPlayerWidth, heightCurSize);
            mVideoMiniController.layout(0, 0, 0, 0);
        }
    }

    private void justMeasureVideoWrapper() {
        int newPlayerWidth = (int) (mPlayerMaxWidth - (mNormalSpace * 2 * mVerticalOffset));
        int heightCurSize;

        if (mVerticalOffset >= PERCENT_START_TO_SCALE) {
            // start to scale x
            heightCurSize = (int) (mVideoHeightStartToMinimize - (mVerticalOffset - PERCENT_START_TO_SCALE) / (1 -
                    PERCENT_START_TO_SCALE)
                    * (mVideoHeightStartToMinimize - mVideoHeight));
        } else {
            heightCurSize = (int) (mPlayerMaxHeight - (mVerticalOffset) / (PERCENT_START_TO_SCALE)
                    * (mPlayerMaxHeight - mVideoHeightStartToMinimize));
        }

//        Log.e(TAG, String.format("justMeasureVideoWrapper: %d", newPlayerWidth));
        mVideoWrapper.measure(MeasureSpec.makeMeasureSpec(newPlayerWidth, MeasureSpec.EXACTLY),
                              MeasureSpec.makeMeasureSpec(heightCurSize, MeasureSpec.EXACTLY));
        mVideoWrapper.layout(mLeft,
                             mTop,
                             mLeft + mVideoWrapper.getMeasuredWidth(),
                             mTop + mVideoWrapper.getMeasuredHeight());
    }

    private void justMeasureDesc() {
        int newPlayerWidth = (int) (mPlayerMaxWidth - (mNormalSpace * 2 * mVerticalOffset));
        int heightVideoSize;
        if (mVerticalOffset >= PERCENT_START_TO_SCALE) {
            // start to scale x
            heightVideoSize = (int) (mVideoHeightStartToMinimize - (mVerticalOffset - PERCENT_START_TO_SCALE) / (1 -
                    PERCENT_START_TO_SCALE)
                    * (mVideoHeightStartToMinimize - mVideoHeight));
        } else {
            heightVideoSize = (int) (mPlayerMaxHeight - (mVerticalOffset) / (PERCENT_START_TO_SCALE)
                    * (mPlayerMaxHeight - mVideoHeightStartToMinimize));
        }
        int heightCurSize = getMeasuredHeight() - mTop - heightVideoSize;
        mVideoInfoRcv.measure(MeasureSpec.makeMeasureSpec(newPlayerWidth, MeasureSpec.EXACTLY),
                              MeasureSpec.makeMeasureSpec(heightCurSize, MeasureSpec.EXACTLY));
        mVideoInfoRcv.layout(mLeft, mTop + mVideoWrapper.getMeasuredHeight(), mLeft + mVideoWrapper.getMeasuredWidth(),
                             mTop + mVideoWrapper.getMeasuredHeight() + heightCurSize);
    }

//    private void justMeasureDesc() {
//        int widthCurSize = mVideoPlayer.getMeasuredWidth();
//        int heightCurSize = 0;
//        if (mVerticalOffset >= PERCENT_START_TO_SCALE) {
//        } else {
//            heightCurSize = (int) (mDescMaxHeight - mVerticalOffset * mDescMaxHeight);
//        }
//
//        mVideoPlayer.measure(MeasureSpec.makeMeasureSpec(widthCurSize, MeasureSpec.EXACTLY),
//                MeasureSpec.makeMeasureSpec(heightCurSize, MeasureSpec.EXACTLY));
//    }

    /**
     * adjust the player and the player info list
     */
    private void onLayoutLightly() {
        mVideoWrapper.layout(mLeft,
                             mTop,
                             mLeft + mVideoWrapper.getMeasuredWidth(),
                             mTop + mVideoWrapper.getMeasuredHeight());
        mVideoPlayer.layout(0, 0, mVideoPlayer.getMeasuredWidth(), mVideoPlayer.getMeasuredHeight());
//        Log.e(TAG, String.format("onLayoutLightly: mVideoPlayer %d %d %d %d", 0, 0, mVideoPlayer.getMeasuredWidth()
// , mVideoPlayer.getMeasuredHeight()));
        mVideoInfoRcv.layout(mLeft, mTop + mVideoWrapper.getMeasuredHeight(), mLeft + mVideoWrapper.getMeasuredWidth(),
                             getMeasuredHeight());
    }

    private void minimize() {
        mIsMinimum = true;
        slideVerticalTo(1f);
    }

    private void maximize() {
        mIsMinimum = false;
        slideVerticalTo(0f);
    }

    private void slideVerticalTo(float slideOffset) {
        int y = (int) (slideOffset * mRangeScrollY);
        if (mDragHelper.smoothSlideViewTo(mVideoWrapper, mIsMinimum ? (int) (mPlayerMaxWidth * (1 - PLAYER_RATIO))
                : getPaddingLeft(), y)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void slideToOriginalPosition() {
        slideVerticalDismissTo(0f);
        mDisappearDirect = SLIDE_RESTORE_ORIGINAL;
    }

    private void slideToDismiss() {
        slideVerticalDismissTo(1f);
        mDisappearDirect = SLIDE_TO_DISMISS;
    }

    private void slideVerticalDismissTo(float slideOffset) {
        int y = (int) (mRangeScrollY + slideOffset * mRangeScrollToDismiss);
        if (mDragHelper.smoothSlideViewTo(mVideoWrapper, mLeft, y)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void requestLayoutLightly() {
        int rootWidth;
        int heightVideoWrapper;
        mLeft = (int) (mNormalSpace * mVerticalOffset);

        if (mVerticalOffset >= PERCENT_START_TO_SCALE) {
            // start to scale x
            mLeft = mNormalSpace;
            rootWidth = (int) (mPlayerMaxWidth - (mNormalSpace * 2));
            heightVideoWrapper = (int) (mVideoHeightStartToMinimize - (mVerticalOffset - PERCENT_START_TO_SCALE) / (1 -
                    PERCENT_START_TO_SCALE)
                    * (mVideoHeightStartToMinimize - mVideoHeight));
        } else {
            mLeft = (int) ((mVerticalOffset) / (PERCENT_START_TO_SCALE) * (mNormalSpace));
            rootWidth = (int) (mPlayerMaxWidth - (mVerticalOffset) / (PERCENT_START_TO_SCALE) * (mNormalSpace * 2));
            heightVideoWrapper = (int) (mPlayerMaxHeight - (mVerticalOffset) / (PERCENT_START_TO_SCALE)
                    * (mPlayerMaxHeight - mVideoHeightStartToMinimize));
        }

        mVideoWrapper.measure(MeasureSpec.makeMeasureSpec(rootWidth, MeasureSpec.EXACTLY),
                              MeasureSpec.makeMeasureSpec(heightVideoWrapper, MeasureSpec.EXACTLY));
        mVideoWrapper.layout(mLeft,
                             mTop,
                             mLeft + mVideoWrapper.getMeasuredWidth(),
                             mTop + mVideoWrapper.getMeasuredHeight());

        if (mVerticalOffset >= PERCENT_START_TO_SCALE) {
            // start to scale x
            int widthVideo = (int) (rootWidth - (mVerticalOffset - PERCENT_START_TO_SCALE) / (1 -
                    PERCENT_START_TO_SCALE)
                    * (rootWidth - mVideoHeight / VIDEO_THUMBNAIL_RATIO));
            mVideoPlayer.measure(MeasureSpec.makeMeasureSpec(widthVideo, MeasureSpec.EXACTLY),
                                 MeasureSpec.makeMeasureSpec(heightVideoWrapper, MeasureSpec.EXACTLY));
            mVideoPlayer.layout(0, 0, widthVideo, heightVideoWrapper);

            mVideoMiniController.measure(MeasureSpec.makeMeasureSpec(rootWidth - widthVideo,
                                                                     MeasureSpec.EXACTLY),
                                         MeasureSpec.makeMeasureSpec(heightVideoWrapper, MeasureSpec.EXACTLY));
            mVideoMiniController.layout(widthVideo, 0, rootWidth, heightVideoWrapper);

            int widthOffset = (int) (widthVideo - mVideoHeight / VIDEO_THUMBNAIL_RATIO);
            int infoWidth = Math.max(0, mRange1 - widthOffset);
            int playPauseWidth = mPlaybarIcon;
            int nextWidth = mPlaybarIcon;
            int closeWidth = mPlaybarIcon;

            int playPauseOffset = mRange1 - widthOffset;
            int nextOffset = mRange2 - widthOffset;
            int closeOffset = mRange3 - widthOffset;

            if (widthOffset >= mRange1) {
                infoWidth = 0;
                playPauseOffset = 0;
            }

            if (widthOffset >= mRange2) {
                playPauseWidth = 0;
                nextOffset = 0;
            }

            if (widthOffset >= mRange3) {
                nextWidth = 0;
                closeOffset = 0;
            }


            mInfo.measure(MeasureSpec.makeMeasureSpec(infoWidth, MeasureSpec.EXACTLY),
                          MeasureSpec.makeMeasureSpec(heightVideoWrapper, MeasureSpec.EXACTLY));
            mBtnPlayPause.measure(MeasureSpec.makeMeasureSpec(playPauseWidth, MeasureSpec.EXACTLY),
                                  MeasureSpec.makeMeasureSpec(heightVideoWrapper, MeasureSpec.EXACTLY));
            mBtnNext.measure(MeasureSpec.makeMeasureSpec(nextWidth, MeasureSpec.EXACTLY),
                             MeasureSpec.makeMeasureSpec(heightVideoWrapper, MeasureSpec.EXACTLY));
            mBtnClose.measure(MeasureSpec.makeMeasureSpec(closeWidth, MeasureSpec.EXACTLY),
                              MeasureSpec.makeMeasureSpec(heightVideoWrapper, MeasureSpec.EXACTLY));
            mInfo.layout(0, 0, infoWidth, heightVideoWrapper);
            mBtnPlayPause.layout(playPauseOffset, 0, playPauseOffset + playPauseWidth, heightVideoWrapper);
            mBtnNext.layout(nextOffset, 0, nextOffset + nextWidth, heightVideoWrapper);
            mBtnClose.layout(closeOffset, 0, closeOffset + closeWidth, heightVideoWrapper);
        } else {
            mVideoPlayer.measure(MeasureSpec.makeMeasureSpec(rootWidth, MeasureSpec.EXACTLY),
                                 MeasureSpec.makeMeasureSpec(heightVideoWrapper, MeasureSpec.EXACTLY));
            mVideoPlayer.layout(0, 0, rootWidth, heightVideoWrapper);
            mVideoMiniController.layout(0, 0, 0, 0);
        }

        int heightCurSize = Math.max(0, getMeasuredHeight() - mTop - heightVideoWrapper);
        mVideoInfoRcv.measure(MeasureSpec.makeMeasureSpec(rootWidth, MeasureSpec.EXACTLY),
                              MeasureSpec.makeMeasureSpec(heightCurSize, MeasureSpec.EXACTLY));
        mVideoInfoRcv.layout(mLeft, mTop + mVideoWrapper.getMeasuredHeight(), mLeft + mVideoWrapper.getMeasuredWidth(),
                             mTop + mVideoWrapper.getMeasuredHeight() + heightCurSize);

        ViewCompat.postInvalidateOnAnimation(this);
    }

    private class MyHelperCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            // only allow drag on player view
            return child == mVideoWrapper;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (state == ViewDragHelper.STATE_IDLE) {
                // time to remove the dragview
                if (mIsMinimum && mDragDirect == VERTICAL_DISMISS && mDisappearDirect != SLIDE_RESTORE_ORIGINAL) {
                    if (mCallback != null && mCallback.get() != null)
                        mCallback.get().onDisappear(mDisappearDirect);

                    mDisappearDirect = SLIDE_RESTORE_ORIGINAL;
                    restorePosition();
                    requestLayoutLightly();
                }
                // return the NONE state if idle to detect a click
                mDragDirect = NONE;
            }
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            int range = 0;
            if (child == mVideoWrapper) {
                if (mDragDirect == VERTICAL_EXPAND_COLLAPSE) {
                    range = mRangeScrollY;
                } else if (mDragDirect == VERTICAL_DISMISS) {
                    range = mRangeScrollToDismiss;
                }
            }
            return range;
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            Log.e(TAG, String.format("clampViewPositionVertical: top %d dy %d", top, dy));
            int newTop = mTop;
            if (child == mVideoWrapper) {
                if (mDragDirect == VERTICAL_EXPAND_COLLAPSE) {
                    int bottomBound = (int) mRangeScrollY;
                    newTop = Math.min(Math.max(top, 0), bottomBound);
                } else if (mDragDirect == VERTICAL_DISMISS) {
                    int bottomBound = (int) (mRangeScrollY + mRangeScrollToDismiss);
                    newTop = Math.min(Math.max(top, 0), bottomBound);
                }
            }
            return newTop;
        }

        // calling when drag
        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            mTop = top;
            if (mDragDirect == VERTICAL_EXPAND_COLLAPSE) { //垂直方向
                mVerticalOffset = (float) mTop / mRangeScrollY;
            } else if (mIsMinimum && mDragDirect == VERTICAL_DISMISS) {
                mVerticalOffsetDismiss = (float) (mTop - mRangeScrollY) / mRangeScrollToDismiss;
            }
            requestLayoutLightly();
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            if (mDragDirect == VERTICAL_EXPAND_COLLAPSE) {
                if (yvel > 0 || (yvel == 0 && mVerticalOffset >= 0.5f))
                    minimize();
                else if (yvel < 0 || (yvel == 0 && mVerticalOffset < 0.5f))
                    maximize();
            } else if (mIsMinimum && mDragDirect == VERTICAL_DISMISS) {
                if (yvel > 0 || (yvel == 0 && mVerticalOffsetDismiss >= 0.5f))
                    slideToDismiss();
                else if (yvel < 0 || (yvel == 0 && mVerticalOffsetDismiss < 0.5f))
                    slideToOriginalPosition();
            }
        }
    }
}
