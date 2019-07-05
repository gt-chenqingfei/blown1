package com.shuashuakan.android.modules.profile.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.luck.picture.lib.tools.ScreenUtils;
import com.shuashuakan.android.R;
import com.shuashuakan.android.commons.cache.Storage;
import com.shuashuakan.android.data.api.model.account.FocusModel;
import com.shuashuakan.android.modules.account.AccountManager;
import com.shuashuakan.android.modules.widget.FollowButton;
import com.shuashuakan.android.utils.UtilsKt;

/**
 * Created by lijie on 2018/10/24 下午6:57
 */
public class FocusListAdapter extends RecyclerArrayAdapter<FocusModel> {

    private BtnClick click;
    private String selfUserId;

    private AccountManager accountManager;

    public FocusListAdapter(Context context, BtnClick click, String adapterUserId) {
        super(context);
        this.click = click;
        accountManager = new AccountManager(new Storage(context));
        selfUserId = adapterUserId;
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new FocusListViewHolder(parent);
    }

    public class FocusListViewHolder extends BaseViewHolder<FocusModel> {

        private SimpleDraweeView avatar;
        private TextView nameTv, contentTv;
        private FollowButton followButton;

        public FocusListViewHolder(ViewGroup parent) {
            super(parent, R.layout.item_focus_list);
            avatar = $(R.id.focus_avatar);
            nameTv = $(R.id.focus_name);
            contentTv = $(R.id.focus_content);
            followButton = $(R.id.follow_btn);
        }

        @Override
        public void setData(FocusModel data) {
            super.setData(data);
            String avatarUrl = UtilsKt.imageUrl2WebP2(data.getAvatar(), ScreenUtils.dip2px(getContext(), 45f),
                    ScreenUtils.dip2px(getContext(), 45f));
            avatar.setImageURI(avatarUrl);
            nameTv.setText(data.getNickName());
            contentTv.setText(data.getBio());

            if (accountManager.hasAccount()) {
                if (!selfUserId.equals(data.getUserId())) {
                    followButton.setVisibility(View.VISIBLE);
                } else {
                    followButton.setVisibility(View.GONE);
                }
            }

            if (data.getFollow() != null) {
                if (data.getFollow()) {
                    followButton.setFollowStatus(true, data.is_fans());
                } else {
                    followButton.setFollowStatus(false, data.is_fans());
                }
            } else {
                followButton.setFollowStatus(false, data.is_fans());
            }
            followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    click.theClick(data, followButton, data.getUserId());
                }
            });
            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    click.avatarClick(data.getUserId());
                }
            });
        }
    }

    public interface BtnClick {
        void theClick(FocusModel data, FollowButton followButton, String userId);

        void avatarClick(String userId);
    }
}
