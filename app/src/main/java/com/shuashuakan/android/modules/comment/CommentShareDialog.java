package com.shuashuakan.android.modules.comment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.shuashuakan.android.R;
import com.shuashuakan.android.utils.Contexts;

/**
 * Created by 18410 on 2017/9/12.
 */

public class CommentShareDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private ShareDialogListener listener;

    public CommentShareDialog(@NonNull Context context,ShareDialogListener listener) {
        super(context,R.style.showCommentShareDialog);
        this.context = context;
        this.listener = listener;
        setContentView(R.layout.dialog_comment_share);
        init();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.showAvatarDialog);
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = Contexts.getScreenSize(context).x;
        attributes.height = Contexts.dip(context,221);
        window.setAttributes(attributes);

    }
    private void init() {
        findViewById(R.id.share_wechat).setOnClickListener(this);
        findViewById(R.id.share_moments).setOnClickListener(this);
        findViewById(R.id.share_qq).setOnClickListener(this);
        findViewById(R.id.share_qzone).setOnClickListener(this);
        findViewById(R.id.share_copy_url).setOnClickListener(this);
        findViewById(R.id.share_open_browser).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_wechat:
            case R.id.share_moments:
            case R.id.share_qq:
            case R.id.share_qzone:
            case R.id.share_copy_url:
            case R.id.share_open_browser:
                dismiss();
                listener.onShareClick(v.getId());
                break;
        }
    }
    public interface ShareDialogListener {
        void onShareClick(int id);
    }
}
