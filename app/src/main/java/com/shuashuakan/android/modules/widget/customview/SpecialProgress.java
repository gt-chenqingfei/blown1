package com.shuashuakan.android.modules.widget.customview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.shuashuakan.android.R;
import com.shuashuakan.android.utils.UtilsKt;

/**
 * Author:  liJie
 * Date:   2019/3/1
 * Email:  2607401801@qq.com
 */
public class SpecialProgress extends LinearLayout {

  private Context context;
  public SpecialProgress(Context context) {
    super(context);
    init(context);
  }

  public SpecialProgress(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context){
    this.context=context;
    setOrientation(HORIZONTAL);
  }
  public void setProgressNum(int num){
    removeAllViews();
    for (int i = 0; i < num; i++) {
      LayoutParams layoutParams=new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,20);
      layoutParams.weight=1;
      if(i==num-1){
        layoutParams.rightMargin=0;
      }else {
        layoutParams.rightMargin=10;
      }
      ProgressBar progressBar = new ProgressBar(context,null,R.style.MyProgressBar);
      progressBar.setLayoutParams(layoutParams);
      progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_player));
     // progressBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.progress_player_bg));
      progressBar.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.white_50));//dedede
      progressBar.setMax(100);
      addView(progressBar);
    }
    invalidate();
  }

  public ProgressBar getCurrentProgressBar(int num){
    ProgressBar progress = (ProgressBar)getChildAt(num);
    return progress;
  }

  public void setProgressShow(int currentNum){
    if(currentNum!=0) {
      for (int i = currentNum; i < getChildCount(); i++) {
        ProgressBar progress = (ProgressBar) getChildAt(i);
        progress.setProgress(0);
      }
      for (int i = 0; i < currentNum; i++) {
        ProgressBar progress = (ProgressBar) getChildAt(i);
        if(progress!=null)
          progress.setProgress(100);
      }
    }else{
      for (int i = 0; i < getChildCount(); i++) {
        ProgressBar progress = (ProgressBar) getChildAt(i);
        progress.setProgress(0);
      }
    }
  }
}
