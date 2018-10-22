package com.example.cpu11112_local.testdragvideo.dragVideo;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.FloatRange;
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

import java.util.ArrayList;
import java.util.List;

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
    private static final float MAX_BACKGROUND_ALPHA = 0.5f;
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
    @BindView(R.id.bgView)
    View mBgView;

    int mOffsetLeftRight = 0;
    @BindDimen(R.dimen.spacing_large)
    int mOffsetBottom;
    @BindDimen(R.dimen.playbar_height)
    int mVideoHeight;
    @BindDimen(R.dimen.playbar_icon)
    int mPlaybarSize;
    int mVideoHeightStartToMinimize;

    // The direction of the current drag
    public static final int NONE = 1 << 0;
    //    public static final int HORIZONTAL = 1 << 1;
    public static final int VERTICAL_EXPAND_COLLAPSE = 1 << 2;
    public static final int VERTICAL_DISMISS = 1 << 3;

    // The direction of the last slides
    private static final int SLIDE_RESTORE_ORIGINAL = 1 << 0;
    private static final int SLIDE_TO_DISMISS = 1 << 1;

    // we will scale from 0(expand) -> 1(minizie): and at PERCENT_START_TO_SCALE, width start to scale
    float PERCENT_START_TO_SCALE = 0.93f;

    private boolean mIsFinishCalculate;
    private int mPlayerMaxWidth;
    private int mPlayerMaxHeight;

    private float mVerticalOffset = 1f;
    private float mVerticalOffsetDismiss = 1f;
    private int mLeft;
    private int mTop;
    private boolean mIsMinimum = true;
    private int mDragDirect = NONE;
    private ViewDragHelper mDragHelper;
    private int mDownX;
    private int mDownY;
    private List<Callback> mCallbacks = new ArrayList<>();
    private int mDisappearDirect = SLIDE_RESTORE_ORIGINAL;
    private Unbinder mUnbinder;
    private int mRangeScrollY;

    // range to start revealing the menu button
    private int mRange1;
    private int mRange2;
    private int mRange3;
    private int mRangeScrollToDismiss;
    private int mMarginIcon;
    private int mMarginVideoInfo;

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
        mCallbacks.clear();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculate();
    }

    private void calculate() {
        if (!mIsFinishCalculate) {
            mIsFinishCalculate = true;

            mPlayerMaxWidth = getMeasuredWidth();
            mPlayerMaxHeight = (int) (mPlayerMaxWidth * VIDEO_THUMBNAIL_RATIO);
            mRange1 = (int) (getMeasuredWidth() - mOffsetLeftRight * 2 - mPlaybarSize * 3 - mVideoHeight /
                    VIDEO_THUMBNAIL_RATIO);
            mRange2 = (int) (getMeasuredWidth() - mOffsetLeftRight * 2 - mPlaybarSize * 2 - mVideoHeight /
                    VIDEO_THUMBNAIL_RATIO);
            mRange3 = (int) (getMeasuredWidth() - mOffsetLeftRight * 2 - mPlaybarSize - mVideoHeight /
                    VIDEO_THUMBNAIL_RATIO);
            mRangeScrollY = (int) (getMeasuredHeight() - getPaddingTop() - getPaddingBottom() - mVideoHeight - mOffsetBottom);
            mRangeScrollToDismiss = (int) (mVideoHeight);
            mVideoHeightStartToMinimize = (int) (getMeasuredHeight() - mRangeScrollY * PERCENT_START_TO_SCALE - mOffsetBottom);
            mMarginIcon = (int) ((mVideoHeight - mPlaybarSize) / 2f);
            restorePosition();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e(TAG, "onMeasure: ");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.e(TAG, "onLayout: ");
        requestLayoutLightly();
        mMarginVideoInfo = (int) ((mVideoHeight - mInfo.getMeasuredHeight()) / 2f);
        mBgView.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
        mBgView.layout(0, 0, mBgView.getMeasuredWidth(), mBgView.getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e(TAG, "onDraw: ");
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // only allow drag the player and the video info
        boolean isHit = mDragHelper.isViewUnder(mVideoWrapper, (int) event.getX(), (int) event.getY()) ||
                mDragHelper.isViewUnder(mVideoInfoRcv, (int) event.getX(), (int) event.getY());
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
        mCallbacks.add(callback);
        // notify the observer the current state
        callback.onOffsetChange(mVerticalOffset);
    }

    public void removeCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    public void show() {
        this.setAlpha(1f);
        mDragDirect = VERTICAL_EXPAND_COLLAPSE;
        maximize();
    }

    public void setOffsetBottom(int offsetBottom) {
        mOffsetBottom = offsetBottom;
        mIsFinishCalculate = false;
        calculate();
    }

    public interface Callback {
        void onDisappear(int direct);

        void onOffsetChange(@FloatRange(from = 0f, to = 1f) float verticalOffset);
    }

    // hide this view at the bottom of the screen, make the player small so when show the video, it will grow bigger
    private void restorePosition() {
        mVideoWrapper.setAlpha(1f);
        this.setAlpha(0f);
        mLeft = mOffsetLeftRight;
        mTop = mRangeScrollY + mRangeScrollToDismiss;
        mIsMinimum = true;
        mVerticalOffset = 1f;
        mVerticalOffsetDismiss = 1f;
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
        if (mDragHelper.smoothSlideViewTo(mVideoWrapper, mIsMinimum ? mOffsetLeftRight : 0, y)) {
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
        mLeft = (int) (mOffsetLeftRight * mVerticalOffset);

        if (mVerticalOffset >= PERCENT_START_TO_SCALE) {
            // start to scale x
            mLeft = mOffsetLeftRight;
            rootWidth = (int) (mPlayerMaxWidth - (mOffsetLeftRight * 2));
            heightVideoWrapper = (int) (mVideoHeightStartToMinimize - (mVerticalOffset - PERCENT_START_TO_SCALE) / (1 -
                    PERCENT_START_TO_SCALE)
                    * (mVideoHeightStartToMinimize - mVideoHeight));
        } else {
            mLeft = (int) ((mVerticalOffset) / (PERCENT_START_TO_SCALE) * (mOffsetLeftRight));
            rootWidth = (int) (mPlayerMaxWidth - (mVerticalOffset) / (PERCENT_START_TO_SCALE) * (mOffsetLeftRight * 2));
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
            int playPauseWidth = mPlaybarSize;
            int nextWidth = mPlaybarSize;
            int closeWidth = mPlaybarSize;

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

            int heightInfo = mInfo.getMeasuredHeight();
            mInfo.measure(MeasureSpec.makeMeasureSpec(infoWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(heightInfo, MeasureSpec.EXACTLY));
            mBtnPlayPause.measure(MeasureSpec.makeMeasureSpec(playPauseWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(playPauseWidth, MeasureSpec.EXACTLY));
            mBtnNext.measure(MeasureSpec.makeMeasureSpec(nextWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(nextWidth, MeasureSpec.EXACTLY));
            mBtnClose.measure(MeasureSpec.makeMeasureSpec(closeWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(closeWidth, MeasureSpec.EXACTLY));
            mInfo.layout(0, mMarginVideoInfo, infoWidth, mMarginVideoInfo + heightInfo);
            mBtnPlayPause.layout(playPauseOffset, mMarginIcon, playPauseOffset + playPauseWidth, mMarginIcon +
                    playPauseWidth);
            mBtnNext.layout(nextOffset, mMarginIcon, nextOffset + nextWidth, mMarginIcon + nextWidth);
            mBtnClose.layout(closeOffset, mMarginIcon, closeOffset + closeWidth, mMarginIcon + closeWidth);
            adjustVideoInfoAlpha(mInfo, mBtnPlayPause, mBtnNext, mBtnClose);
        } else {
            mVideoPlayer.measure(MeasureSpec.makeMeasureSpec(rootWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(heightVideoWrapper, MeasureSpec.EXACTLY));
            mVideoPlayer.layout(0, 0, rootWidth, heightVideoWrapper);
            mVideoMiniController.layout(0, 0, 0, 0);
        }

        int heightCurSize = Math.max(0, getMeasuredHeight() - mOffsetBottom - mTop - heightVideoWrapper);
        mVideoInfoRcv.measure(MeasureSpec.makeMeasureSpec(rootWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(heightCurSize, MeasureSpec.EXACTLY));
        int bottomVideoInfoRcv = (int) (mRangeScrollY + mVideoHeight + mOffsetBottom - mVerticalOffset * mOffsetBottom);

        mVideoInfoRcv.layout(mLeft, mTop + mVideoWrapper.getMeasuredHeight(), mLeft + mVideoWrapper.getMeasuredWidth(),
                bottomVideoInfoRcv);

        adjustBackgroundOpa();
        // FIXME: 10/22/2018 do we need to invalidate here
//        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void adjustVideoInfoAlpha(View... views) {
        for (View view : views) {
            view.setAlpha((mVerticalOffset - PERCENT_START_TO_SCALE) / (1 - PERCENT_START_TO_SCALE));
        }
    }

    private void adjustBackgroundOpa() {
        // FIXME: 10/22/2018 this method make onmeasure run multiple time
        float alpha = MAX_BACKGROUND_ALPHA * (1 - mVerticalOffset);
        mBgView.setAlpha(alpha);
        if (alpha == 0 || alpha == MAX_BACKGROUND_ALPHA) {
            if (mBgView.getVisibility() != GONE) {
                mBgView.setVisibility(GONE);
            }
        } else {
            if (mBgView.getVisibility() != VISIBLE) {
                mBgView.setVisibility(VISIBLE);
            }
        }
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
                    for (Callback callback : mCallbacks) {
                        callback.onDisappear(mDisappearDirect);
                    }

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
                for (Callback callback : mCallbacks) {
                    callback.onOffsetChange(mVerticalOffset);
                }
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
