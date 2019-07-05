package com.shuashuakan.android.modules.profile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.shuashuakan.android.R;
import com.shuashuakan.android.data.api.model.account.UserAccount;
import com.shuashuakan.android.data.api.model.account.UserTag;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mr_Long on 16/1/14.
 */
public class UserTagAdapter extends BaseAdapter {

    private Context mContext;
    private List<UserTag> mList = new ArrayList<>();

    public UserTagAdapter(Context context) {
        this.mContext = context;
    }

    public void updateData(List<UserTag> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public UserTag getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_user_tag, null);
            holder = new ViewHolder();
            holder.tagTv = convertView.findViewById(R.id.tag_tv);
            holder.tagIv = convertView.findViewById(R.id.tag_iv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        UserTag userTag = getItem(position);
        if (userTag.getImage() != null) {
            holder.tagIv.setVisibility(View.VISIBLE);
            holder.tagIv.setImageURI(userTag.getImage());
        } else {
            holder.tagIv.setVisibility(View.GONE);
        }
        if (userTag.getContent() != null)
            holder.tagTv.setText(userTag.getContent());
        else
            holder.tagTv.setText("");
        return convertView;
    }

    static class ViewHolder {
        TextView tagTv;
        SimpleDraweeView tagIv;
    }
}