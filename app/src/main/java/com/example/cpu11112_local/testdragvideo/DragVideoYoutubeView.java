package com.example.cpu11112_local.testdragvideo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cpu11112_local.testdragvideo.test.DensityUtil;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Phong Huynh on 10/17/2018.
 */
public class DragVideoYoutubeView extends ViewGroup {
    @BindView(R.id.videoWrapper)
    View mVideoPlayer;
    @BindView(R.id.tvTitle)
    TextView mTvTitle;
    @BindView(R.id.tvSubTitle)
    TextView mTvSubTitle;
    @BindView(R.id.titleWrapper)
    LinearLayout mTitleWrapper;
    @BindView(R.id.imgvPause)
    ImageView mImgvPause;
    @BindView(R.id.pauseWrapper)
    FrameLayout mPauseWrapper;
    @BindView(R.id.imgvClose)
    ImageView mImgvClose;
    @BindView(R.id.closeWrapper)
    FrameLayout mCloseWrapper;
    @BindView(R.id.videoInfoRcv)
    RecyclerView mVideoInfoRcv;
    @BindView(R.id.dragVideo)
    DragVideoYoutubeView mDragVideo;

    // The direction of the current drag
    public static final int NONE = 1 << 0;
    public static final int HORIZONTAL = 1 << 1;
    public static final int VERTICAL = 1 << 2;

    // The direction of the last slides
    public static final int SLIDE_RESTORE_ORIGINAL = 1 << 0;
    public static final int SLIDE_TO_LEFT = 1 << 1;
    public static final int SLIDE_TO_RIGHT = 1 << 2;

    // the smallest ratio when drag the player
    private static final float PLAYER_RATIO = 0.45f;
    private static final float ORIGINAL_MIN_OFFSET = 1f / (1f + PLAYER_RATIO);
    private static final float LEFT_DRAG_DISAPPEAR_OFFSET = (4f - PLAYER_RATIO) / (4f + 4f * PLAYER_RATIO);
    private static final float RIGHT_DRAG_DISAPPEAR_OFFSET = (4f + PLAYER_RATIO) / (4f + 4f * PLAYER_RATIO);

    // we will scale from 0(expand) -> 1(minizie):
    float PERCENT_START_TO_SCALE = 0.9f;
    // Minimum zoom ratio
    float MIN_RATIO_HEIGHT = 0.35f;
    float MIN_RATIO_HEIGHT_START_TO_SCALE = 0.45f;
    float MIN_RATIO_WIDTH = 0.45f;

    // FIXME: 10/17/2018 change the name of 2 view
//    private View mVideoPlayer;
//    private View mDesc;

    private boolean mIsFinishInit;
    private int mPlayerMaxWidth;
    private int mPlayerMaxHeight;

    private float mVerticalOffset = 1f;
    private int mMinTop;
    private int mPlayerMinWidth;
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
    private float mHorizontalOffset;
    private Unbinder mUnbinder;
    private int mRangeScrollY;

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

        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                resolveSizeAndState(maxHeight, heightMeasureSpec, 0));

        if (!mIsFinishInit) {
            mMinTop = getPaddingTop();
            mPlayerMinWidth = mVideoPlayer.getMeasuredWidth();
            // mPlayerMaxWidth: mPlayerMaxWidth: range for video to go out completely to the left
            // mPlayerMinWidth: range for video to go out completely to the right
//            mHorizontalRange = mPlayerMaxWidth + mPlayerMinWidth;
//            mVerticalRange = getMeasuredHeight() - getPaddingTop() - getPaddingBottom() - mVideoPlayer.getMeasuredHeight();
            restorePosition();
            mIsFinishInit = true;

            // FIXME: 10/18/2018 the margin of minimize video from bottom, change this
            int marginBottom = DensityUtil.dip2px(getContext(), 60);
            mRangeScrollY = (int) (this.getMeasuredHeight() - getPaddingTop() - getPaddingBottom() - MIN_RATIO_HEIGHT *
                    mVideoPlayer
                            .getMeasuredHeight() - marginBottom);
//            mRangeNodeScrollY = (int) (this.getMeasuredHeight() - getPaddingTop() - getPaddingBottom() - MIN_RATIO_HEIGHT_NODE *
//                    mVideoPlayer.getMeasuredHeight() - marginBottom);
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
        boolean isHit = mDragHelper.isViewUnder(mVideoPlayer, (int) event.getX(), (int) event.getY());
        if (isHit) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = (int) event.getX();
                    mDownY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    // scroll back to suitable position
                    if (mDragDirect == NONE) {
                        int dx = Math.abs(mDownX - (int) event.getX());
                        int dy = Math.abs(mDownY - (int) event.getY());
                        int slop = mDragHelper.getTouchSlop();

                        if (Math.sqrt(dx * dx + dy * dy) < slop) {
                            mDragDirect = VERTICAL;

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
                            if (dy >= dx)
                                mDragDirect = VERTICAL;
                            else
                                mDragDirect = HORIZONTAL;
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
        mDragDirect = VERTICAL;
        maximize();
    }

    public interface Callback {
        void onDisappear(int direct);
    }

    // hide this view, make the player small so when show the video, it will grow bigger
    private void restorePosition() {
        mVideoPlayer.setAlpha(1f);
        this.setAlpha(0f);
//        mLeft = mHorizontalRange - mPlayerMinWidth;
        mTop = mRangeScrollY;
        mIsMinimum = true;
        mVerticalOffset = 1f;
    }

    private void customMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measurePlayer(widthMeasureSpec, heightMeasureSpec);
        measureDesc(widthMeasureSpec, heightMeasureSpec);
    }

    // FIXME: 10/17/2018 change the name
    private void measurePlayer(int widthMeasureSpec, int heightMeasureSpec) {
        final LayoutParams lp = mVideoPlayer.getLayoutParams();
        if (!mIsFinishInit) {
            mPlayerMaxWidth = MeasureSpec.getSize(getChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight()
                    , lp.width));
            mPlayerMaxHeight = MeasureSpec.getSize(getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom()
                    , lp.height));
        }
        justMeasurePlayer();
    }

    // FIXME: 10/17/2018 change the name, why the mDesc auto resize
    private void measureDesc(int widthMeasureSpec, int heightMeasureSpec) {
//        measureChild(mDesc, widthMeasureSpec, heightMeasureSpec);
    }

    // adjust the player size depend on the factor
    private void justMeasurePlayer() {
//        int widthCurSize = (int) (mPlayerMaxWidth * (1f - mVerticalOffset * (1 - PLAYER_RATIO)));
//        int heightCurSize = (int) (widthCurSize * VIDEO_THUMBNAIL_RATIO);
//        mVideoPlayer.measure(MeasureSpec.makeMeasureSpec(widthCurSize, MeasureSpec.EXACTLY),
//                MeasureSpec.makeMeasureSpec(heightCurSize, MeasureSpec.EXACTLY));


        if (mVerticalOffset >= PERCENT_START_TO_SCALE) {
            // start to scale x
            int widthCurSize = (int) (mPlayerMaxWidth - (mVerticalOffset - PERCENT_START_TO_SCALE) / (1 - PERCENT_START_TO_SCALE)
                    * (mPlayerMaxWidth - mPlayerMaxWidth * MIN_RATIO_WIDTH));
//            int heightCurSize = (int) (widthCurSize * VIDEO_THUMBNAIL_RATIO);
            int heightCurSize = (int) (mPlayerMaxHeight * MIN_RATIO_HEIGHT_START_TO_SCALE - (mVerticalOffset - PERCENT_START_TO_SCALE) / (1 - PERCENT_START_TO_SCALE)
                    * (mPlayerMaxHeight * MIN_RATIO_HEIGHT_START_TO_SCALE - mPlayerMaxHeight * MIN_RATIO_HEIGHT));
            mVideoPlayer.measure(MeasureSpec.makeMeasureSpec(widthCurSize, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(heightCurSize, MeasureSpec.EXACTLY));
        } else {
            int heightCurSize = (int) (mPlayerMaxHeight - (mVerticalOffset) / (PERCENT_START_TO_SCALE)
                    * (mPlayerMaxHeight - mPlayerMaxHeight * MIN_RATIO_HEIGHT_START_TO_SCALE));
            mVideoPlayer.measure(MeasureSpec.makeMeasureSpec(mPlayerMaxWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(heightCurSize, MeasureSpec.EXACTLY));
        }
    }

    /**
     * adjust the player and the player info list
     */
    private void onLayoutLightly() {
        // dont move the list under video when we drag horizontal
        if (mDragDirect != HORIZONTAL) {
//            mLeft = this.getWidth() - this.getPaddingRight() - this.getPaddingLeft() - mVideoPlayer.getMeasuredWidth();
//            mLeft = this.getPaddingLeft();
//            mDesc.layout(mLeft, mTop + mVideoPlayer.getMeasuredHeight(), mLeft + mDesc.getMeasuredWidth(),
//                    mTop + mVideoPlayer.getMeasuredHeight() + mDesc.getMeasuredHeight());
        }
        mVideoPlayer.layout(mLeft, mTop, mLeft + mVideoPlayer.getMeasuredWidth(), mTop + mVideoPlayer.getMeasuredHeight());
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
        int topBound = mMinTop;
        int y = (int) (topBound + slideOffset * mRangeScrollY);
        if (mDragHelper.smoothSlideViewTo(mVideoPlayer, mIsMinimum ? (int) (mPlayerMaxWidth * (1 - PLAYER_RATIO))
                : getPaddingLeft(), y)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void requestLayoutLightly() {
        justMeasurePlayer();
        onLayoutLightly();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void slideToLeft() {
        slideHorizontalTo(0f);
        mDisappearDirect = SLIDE_TO_LEFT;
    }

    private void slideToRight() {
        slideHorizontalTo(1f);
        mDisappearDirect = SLIDE_TO_RIGHT;
    }

    private void slideToOriginalPosition() {
        slideHorizontalTo(ORIGINAL_MIN_OFFSET);
        mDisappearDirect = SLIDE_RESTORE_ORIGINAL;
    }

    private void slideHorizontalTo(float slideOffset) {
//        int leftBound = -mVideoPlayer.getWidth();
//        int x = (int) (leftBound + slideOffset * mHorizontalRange);
//        if (mDragHelper.smoothSlideViewTo(mVideoPlayer, x, mTop)) {
//            ViewCompat.postInvalidateOnAnimation(this);
//        }
    }

    private class MyHelperCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            // only allow drag on player view
            return child == mVideoPlayer;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (state == ViewDragHelper.STATE_IDLE) {
                // time to remove the dragview
                if (mIsMinimum && mDragDirect == HORIZONTAL && mDisappearDirect != SLIDE_RESTORE_ORIGINAL) {
                    if (mCallback != null && mCallback.get() != null)
                        mCallback.get().onDisappear(mDisappearDirect);

                    mDisappearDirect = SLIDE_RESTORE_ORIGINAL;
                    restorePosition();
                    requestLayoutLightly();
                }
                mDragDirect = NONE;
            }
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            int range = 0;
            if (child == mVideoPlayer && mDragDirect == VERTICAL) {
                range = mRangeScrollY;
            }
            return range;
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            int range = 0;

//            if (child == mVideoPlayer && mIsMinimum && mDragDirect == HORIZONTAL) {
//                range = mHorizontalRange;
//            }
            return range;
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            int newTop = mTop;
            if (child == mVideoPlayer && mDragDirect == VERTICAL) {
                int topBound = mMinTop;
                int bottomBound = topBound + (int) mRangeScrollY;
                newTop = Math.min(Math.max(top, topBound), bottomBound);
            }
            return newTop;
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
//            int newLeft = mLeft;
//            if (child == mVideoPlayer && mIsMinimum && mDragDirect == HORIZONTAL) {
//                int leftBound = -mVideoPlayer.getWidth();
//                int rightBound = leftBound + mHorizontalRange;
//                newLeft = Math.min(Math.max(left, leftBound), rightBound);
//            }
//            return newLeft;

            return left;
        }

        // calling when drag
        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            if (mDragDirect == VERTICAL) { //垂直方向
                mTop = top;
                mVerticalOffset = (float) (mTop - mMinTop) / mRangeScrollY;
            }
//            else if (mIsMinimum && mDragDirect == HORIZONTAL) {
//                mLeft = left;
//                mHorizontalOffset = Math.abs((float) (mLeft + mPlayerMinWidth) / mHorizontalRange);
//            }
            requestLayoutLightly();
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            if (mDragDirect == VERTICAL) {
                if (yvel > 0 || (yvel == 0 && mVerticalOffset >= 0.5f))
                    minimize();
                else if (yvel < 0 || (yvel == 0 && mVerticalOffset < 0.5f))
                    maximize();
            } else if (mIsMinimum && mDragDirect == HORIZONTAL) {
                if ((mHorizontalOffset < LEFT_DRAG_DISAPPEAR_OFFSET && xvel < 0))
                    slideToLeft();
                else if ((mHorizontalOffset > RIGHT_DRAG_DISAPPEAR_OFFSET && xvel > 0))
                    slideToRight();
                else
                    slideToOriginalPosition();
            }
        }
    }
}
