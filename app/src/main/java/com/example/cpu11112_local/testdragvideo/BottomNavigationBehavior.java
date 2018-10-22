package com.example.cpu11112_local.testdragvideo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Phong Huynh on 10/22/2018.
 */
public class BottomNavigationBehavior extends CoordinatorLayout.Behavior<View> {
    private boolean mIsFinishOffset;

    public BottomNavigationBehavior() {
    }

    public BottomNavigationBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        // if already set the offset, return false to prevent calling onDependentViewChanged()
        if (mIsFinishOffset) {
            return false;
        } else {
            return dependency instanceof BottomNavigationView;
        }
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        if (!mIsFinishOffset) {
            // only do it one time, because changing margin calls requestlayout()
            mIsFinishOffset = true;
            setMargins(child, 0, 0, 0, child.getBottom() - dependency.getTop());
            return true;
        }
        return false;
    }

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }
}
