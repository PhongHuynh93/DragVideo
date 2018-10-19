package com.example.cpu11112_local.testdragvideo.test;

import android.content.Context;
import android.content.res.Resources;

/**
 * Created by PhuongHoang on 6/10/16.
 */
public class SystemUtil {
    private static boolean sHasNavBar;

    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getApplicationContext().getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            return resources.getDimensionPixelSize(resourceId);
        return 0;
    }
}
