package com.shuashuakan.android.modules.share;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.shuashuakan.android.data.api.model.Complain;

import java.util.List;

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/09/17
 * Description:
 */
public class ComplainArrayAdapter extends ArrayAdapter<Complain> {
  private int resourceId;

  public ComplainArrayAdapter(@NonNull Context context, int resource, @NonNull List<Complain> objects) {
    super(context, resource, objects);
    this.resourceId = resource;
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    Complain complain = getItem(position);
    View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
    TextView text1 = view.findViewById(android.R.id.text1);
    text1.setText(complain.getDesc());
    return view;
  }
}
