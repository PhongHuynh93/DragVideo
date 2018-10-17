package com.example.cpu11112_local.testdragvideo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import static com.example.cpu11112_local.testdragvideo.test.MvImageView.VIDEO_THUMBNAIL_RATIO;

/**
 * Created by Phong Huynh on 10/17/2018.
 */
public class DragVideoView extends ViewGroup {
    // The direction of the current drag
    public static final int NONE = 1 << 0;
    public static final int HORIZONTAL = 1 << 1;
    public static final int VERTICAL = 1 << 2;

    // The direction of the last slides
    public static final int SLIDE_RESTORE_ORIGINAL = 1 << 0;
    public static final int SLIDE_TO_LEFT = 1 << 1;
    public static final int SLIDE_TO_RIGHT = 1 << 2;

    // the smallest ratio when drag the player
    private static final float PLAYER_RATIO = 0.5f;
    private static final float ORIGINAL_MIN_OFFSET = 1f / (1f + PLAYER_RATIO);
    private static final float LEFT_DRAG_DISAPPEAR_OFFSET = (4f - PLAYER_RATIO) / (4f + 4f * PLAYER_RATIO);
    private static final float RIGHT_DRAG_DISAPPEAR_OFFSET = (4f + PLAYER_RATIO) / (4f + 4f * PLAYER_RATIO);

    // FIXME: 10/17/2018 change the name of 2 view
    private View mPlayer;
    private View mDesc;

    private boolean mIsFinishInit;
    private int mPlayerMaxWidth;

    private float mVerticalOffset = 1f;
    private int mMinTop;
    private int mPlayerMinWidth;
    private int mHorizontalRange;
    private int mVerticalRange;
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

    public DragVideoView(Context context) {
        this(context, null);
    }

    public DragVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mDragHelper = ViewDragHelper.create(this, 1f, new MyHelperCallback());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 2)
            throw new RuntimeException("this ViewGroup must only contains 2 views");

        mPlayer = getChildAt(0);
        mDesc = getChildAt(1);
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
            mPlayerMinWidth = mPlayer.getMeasuredWidth();
            mHorizontalRange = mPlayerMaxWidth + mPlayerMinWidth;
            mVerticalRange = getMeasuredHeight() - getPaddingTop() - getPaddingBottom() - mPlayer.getMeasuredHeight();
            restorePosition();
            mIsFinishInit = true;
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
        boolean isHit = mDragHelper.isViewUnder(mPlayer, (int) event.getX(), (int) event.getY());
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
        mPlayer.setAlpha(1f);
        this.setAlpha(0f);
        mLeft = mHorizontalRange - mPlayerMinWidth;
        mTop = mVerticalRange;
        mIsMinimum = true;
        mVerticalOffset = 1f;
    }

    private void customMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measurePlayer(widthMeasureSpec, heightMeasureSpec);
        measureDesc(widthMeasureSpec, heightMeasureSpec);
    }

    // FIXME: 10/17/2018 change the name
    private void measurePlayer(int widthMeasureSpec, int heightMeasureSpec) {
        final LayoutParams lp = mPlayer.getLayoutParams();
        if (!mIsFinishInit) {
            mPlayerMaxWidth = MeasureSpec.getSize(getChildMeasureSpec(widthMeasureSpec, getPaddingLeft() + getPaddingRight()
                    , lp.width));
        }
        justMeasurePlayer();
    }

    // FIXME: 10/17/2018 change the name, why the mDesc auto resize
    private void measureDesc(int widthMeasureSpec, int heightMeasureSpec) {
        measureChild(mDesc, widthMeasureSpec, heightMeasureSpec);
    }

    // adjust the player size depend on the factor
    private void justMeasurePlayer() {
        int widthCurSize = (int) (mPlayerMaxWidth * (1f - mVerticalOffset * (1 - PLAYER_RATIO)));
        int heightCurSize = (int) (widthCurSize * VIDEO_THUMBNAIL_RATIO);
        mPlayer.measure(MeasureSpec.makeMeasureSpec(widthCurSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(heightCurSize, MeasureSpec.EXACTLY));
    }

    /**
     * adjust the player and the player info list
     */
    private void onLayoutLightly() {
        // dont move the list under video when we drag horizontal
        if (mDragDirect != HORIZONTAL) {
            mLeft = this.getWidth() - this.getPaddingRight() - this.getPaddingLeft() - mPlayer.getMeasuredWidth();
            mDesc.layout(mLeft, mTop + mPlayer.getMeasuredHeight(), mLeft + mDesc.getMeasuredWidth(),
                    mTop + mPlayer.getMeasuredHeight() + mDesc.getMeasuredHeight());
        }
        mPlayer.layout(mLeft, mTop, mLeft + mPlayer.getMeasuredWidth(), mTop + mPlayer.getMeasuredHeight());
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
        int y = (int) (topBound + slideOffset * mVerticalRange);
        if (mDragHelper.smoothSlideViewTo(mPlayer, mIsMinimum ? (int) (mPlayerMaxWidth * (1 - PLAYER_RATIO))
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
        int leftBound = -mPlayer.getWidth();
        int x = (int) (leftBound + slideOffset * mHorizontalRange);
        if (mDragHelper.smoothSlideViewTo(mPlayer, x, mTop)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private class MyHelperCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            // only allow drag on player view
            return child == mPlayer;
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
            if (child == mPlayer && mDragDirect == VERTICAL) {
                range = mVerticalRange;
            }
            return range;
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            int range = 0;

            if (child == mPlayer && mIsMinimum && mDragDirect == HORIZONTAL) {
                range = mHorizontalRange;
            }
            return range;
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            int newTop = mTop;
            if (child == mPlayer && mDragDirect == VERTICAL) {
                int topBound = mMinTop;
                int bottomBound = topBound + mVerticalRange;
                newTop = Math.min(Math.max(top, topBound), bottomBound);
            }
            return newTop;
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            int newLeft = mLeft;
            if (child == mPlayer && mIsMinimum && mDragDirect == HORIZONTAL) {
                int leftBound = -mPlayer.getWidth();
                int rightBound = leftBound + mHorizontalRange;
                newLeft = Math.min(Math.max(left, leftBound), rightBound);
            }
            return newLeft;
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            if (mDragDirect == VERTICAL) { //垂直方向
                mTop = top;
                mVerticalOffset = (float) (mTop - mMinTop) / mVerticalRange;
            } else if (mIsMinimum && mDragDirect == HORIZONTAL) { // 水平方向
                mLeft = left;
                mHorizontalOffset = Math.abs((float) (mLeft + mPlayerMinWidth) / mHorizontalRange);
            }
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
