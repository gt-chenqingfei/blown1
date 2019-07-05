package com.luck.picture.lib.tools;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * @author：luck
 * @data：2018/3/28 下午4:10
 * @描述: Toast工具类
 */

public final class ToastManage {

    public static void s(Context mContext, String s) {
        Toast.makeText(mContext.getApplicationContext(), s, Toast.LENGTH_SHORT)
                .show();
    }

    public static void showCenterToast(Context mContext, String msg) {
        int height = mContext.getResources().getDisplayMetrics().heightPixels;
        Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, height / 4 * 3);
        toast.show();
    }
}