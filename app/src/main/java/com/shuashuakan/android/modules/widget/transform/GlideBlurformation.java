package com.shuashuakan.android.modules.widget.transform;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static cn.jpush.im.android.api.enums.ContentType.image;
import static com.umeng.socialize.utils.ContextUtil.getContext;


public class GlideBlurformation extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Glide.with(this)
                .load("");
//                .bitmapTransform(new BlurTransformation(getContext(),13))
//                .into(image);
    }
}