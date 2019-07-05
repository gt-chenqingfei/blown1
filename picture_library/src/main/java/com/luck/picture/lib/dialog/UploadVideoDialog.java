package com.luck.picture.lib.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import com.luck.picture.lib.R;

public class UploadVideoDialog extends Dialog {
    public Context context;

    public UploadVideoDialog(Context context) {
        super(context, R.style.picture_alert_dialog);
        this.context = context;
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        window.setWindowAnimations(R.style.DialogWindowStyle);
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_video_dialog);
    }
}