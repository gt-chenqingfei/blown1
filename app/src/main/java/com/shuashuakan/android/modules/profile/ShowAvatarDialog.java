package com.shuashuakan.android.modules.profile;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.facebook.drawee.view.SimpleDraweeView;
import com.shuashuakan.android.R;

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/07/31
 * Description:
 */
public class ShowAvatarDialog extends Dialog {
    private String avatarUrl;
    private Context mContext;
    private SimpleDraweeView mSimpleDraweeView;

    public ShowAvatarDialog(Context context, String avatarUrl) {
        // 设置自定义样式
        super(context, R.style.showAvatarDialog);
        this.mContext = context;
        this.avatarUrl = avatarUrl;
        initImageView(avatarUrl);
    }

    //直接使用imageview展示头像图片
    private void initImageView(String avatarUrl) {
        //重点在于用setContentView()加载自定义布局
        setContentView(R.layout.dialog_show_avatar);

        mSimpleDraweeView = findViewById(R.id.simple_image);

        mSimpleDraweeView.setImageURI(Uri.parse(avatarUrl));

        mSimpleDraweeView.setOnClickListener(v -> dismiss());

        setParams();
    }

    //设置对话框的宽高适应全屏
    private void setParams() {
        Window window = this.getWindow();
        if (window != null) {
            ViewGroup.LayoutParams lay = window.getAttributes();
            DisplayMetrics dm = new DisplayMetrics();
            window.getWindowManager().getDefaultDisplay().getMetrics(dm);
            Rect rect = new Rect();
            View view = window.getDecorView();
            view.getWindowVisibleDisplayFrame(rect);

            lay.height = dm.heightPixels - rect.top;
            lay.width = dm.widthPixels;
        }
    }
}
