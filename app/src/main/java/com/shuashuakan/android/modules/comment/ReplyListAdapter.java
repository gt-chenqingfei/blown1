package com.shuashuakan.android.modules.comment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.shuashuakan.android.R;
import com.shuashuakan.android.constant.Constants;
import com.shuashuakan.android.data.api.model.comment.ApiComment;
import com.shuashuakan.android.modules.account.AccountManager;
import com.shuashuakan.android.modules.profile.UserProfileActivity;
import com.shuashuakan.android.modules.widget.SpiderAction;
import com.shuashuakan.android.modules.widget.StrickRoundSpan;
import com.shuashuakan.android.utils.TimeUtil;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;


/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/08/11
 * Description:
 */
public class ReplyListAdapter extends RecyclerArrayAdapter<ApiComment> {

    private Long userId;
    private AccountManager accountManager;


    public ReplyListAdapter(Context context, AccountManager accountManager) {
        super(context);
        this.accountManager = accountManager;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new ReplyListVH(parent);
    }

    private class ReplyListVH extends BaseViewHolder<ApiComment> {

        private TextView content, replyTime, replyBtn, praiseTv;
        private SimpleDraweeView reply_avatar;
        private ImageView praiseIv;
        private LinearLayout praiseLl;

        public ReplyListVH(ViewGroup parent) {
            super(parent, R.layout.item_reply);
            content = $(R.id.reply_content);
            replyTime = $(R.id.reply_time);
            replyBtn = $(R.id.reply_btn);
            reply_avatar = $(R.id.reply_avatar);
            praiseIv = $(R.id.reply_praise_iv);
            praiseTv = $(R.id.reply_praise_tv);
            praiseLl = $(R.id.reply_praise_ll);
        }

        @SuppressLint("CheckResult")
        @Override
        public void setData(ApiComment data) {
            super.setData(data);

            SpannableString author = new SpannableString(data.getAuthor().getNickName() + " ");
            author.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // 粗体
            content.setText(author);

            if (data.getAuthor().getLabels() != null && data.getAuthor().getLabels().size() > 0) {
                SpannableString nameFlag = new SpannableString(data.getAuthor().getLabels().get(0).getContent());
                if (!TextUtils.isEmpty(nameFlag)) {
                    nameFlag.setSpan(new StrickRoundSpan(getContext(), ContextCompat.getColor(getContext(),R.color.comment_send_color),  ContextCompat.getColor(getContext(),R.color.comment_send_color)),
                            0, nameFlag.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    content.append(nameFlag);
                    content.append("  ");
                }
            }

            if (accountManager.hasAccount()) {
                if (accountManager.account().getUserId() == data.getAuthor().getUserId()) {
                    replyBtn.setVisibility(View.GONE);
                } else {
                    replyBtn.setVisibility(View.VISIBLE);
                }
            } else {
                replyBtn.setVisibility(View.VISIBLE);
            }

            reply_avatar.setImageURI(data.getAuthor().getAvatar());

            reply_avatar.setOnClickListener(view ->
                    getContext().startActivity(new Intent(getContext(), UserProfileActivity.class)
                            .putExtra("id", String.valueOf(data.getAuthor().getUserId()))
                            .putExtra("source", SpiderAction.PersonSource.COMMENT.getSource())));

            SpannableString content1 = new SpannableString(" 回复 ");

            content1.setSpan(new StyleSpan(Typeface.NORMAL), 0, content1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            content1.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(),R.color.comment_content_color)), 0, content1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            content.append(content1);


            if (data.getReplyTo() != null) {
                SpannableString reply = new SpannableString(data.getReplyTo().getNickName());
                reply.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, reply.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // 粗体
                reply.setSpan(new ClickableSpan() {
                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(ContextCompat.getColor(getContext(),R.color.color_normal_b6b6b6));
                        ds.setFlags(0);
                    }

                    @Override
                    public void onClick(@NonNull View view) {
                        getContext().startActivity(new Intent(getContext(), UserProfileActivity.class)
                                .putExtra("id", String.valueOf(data.getReplyTo().getUserId()))
                                .putExtra("source", SpiderAction.PersonSource.COMMENT.getSource()));
                    }
                }, 0, reply.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                content.append(reply);

                if (data.getReplyTo().getLabels() != null && data.getReplyTo().getLabels().size() > 0) {
                    SpannableString nameFlag = new SpannableString(data.getReplyTo().getLabels().get(0).getContent());
                    if (!TextUtils.isEmpty(nameFlag)) {
                        nameFlag.setSpan(new StrickRoundSpan(getContext(),  ContextCompat.getColor(getContext(),R.color.comment_send_color),  ContextCompat.getColor(getContext(),R.color.comment_send_color)),
                                0, nameFlag.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        content.append(nameFlag);
                        content.append("  ");
                    }
                }

            }

            SpannableString content = new SpannableString(": " + data.getContent());
            content.setSpan(new ClickableSpan() {
                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(ContextCompat.getColor(getContext(),R.color.enjoy_color_2));
                    ds.setUnderlineText(false);
//          ds.setFlags(0);
                }

                @Override
                public void onClick(@NonNull View view) {
                    if (contentOperationListener != null)
                        contentOperationListener.onContentClick(data, getDataPosition());
                }
            }, 0, content.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            content.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(),R.color.comment_content_color)), 0, content.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            content.setSpan(new StyleSpan(Typeface.NORMAL), 0, content.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            this.content.append(content);

            this.content.setMovementMethod(LinkMovementMethod.getInstance());
            this.content.setLongClickable(false); // TODO

            replyTime.setText(TimeUtil.getTimeFormatText(new Date(data.getCreateAt())));
            replyBtn.setText(replyBtn.getContext().getResources().getString(R.string.string_reply_label));
            praiseTv.setText("" + data.getLikeCount());
            isPraise(data.getLiked(), praiseIv);
            RxView.clicks(praiseLl)
                    .throttleFirst(Constants.DOUBLE_CLICK_INTERVAL, TimeUnit.SECONDS)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> {
                        if (onReplyOperationListener != null && !isDeleted(data) && data.getId() != null) {
                            onReplyOperationListener.onPraise(data, getDataPosition(), new OnListener() {
                                @Override
                                public void onCancelPraise() {
                                    data.setLiked(false);
                                    data.setLikeCount(data.getLikeCount() - 1);
                                    praiseTv.setText("" + data.getLikeCount());
                                    isPraise(data.getLiked(), praiseIv);
                                }

                                @Override
                                public void onPraise() {
                                    data.setLiked(true);
                                    data.setLikeCount(data.getLikeCount() + 1);
                                    praiseTv.setText("" + data.getLikeCount());
                                    isPraise(data.getLiked(), praiseIv);
                                }
                            });
                        }
                    });
        }
    }

    private void isPraise(boolean b, ImageView praiseIv) {
        praiseIv.setImageResource(b ? R.drawable.ic_comment_liked : R.drawable.ic_comment_like);
    }

    public boolean isDeleted(ApiComment data) {
        if (data != null && data.getState() != null) {
            return data.getState().equals("REMOVED");
        }
        return false;
    }

    private OnReplyOperationListener onReplyOperationListener = null;

    public interface OnReplyOperationListener {
        void onPraise(ApiComment data, int position, OnListener onListener);
    }

    public void setOnReplyOperationListener(OnReplyOperationListener onReplyOperationListener) {
        this.onReplyOperationListener = onReplyOperationListener;
    }

    private OnContentOperationListener contentOperationListener = null;

    public interface OnContentOperationListener {
        void onContentClick(ApiComment data, int position);
    }

    public void setContentOperationListener(OnContentOperationListener contentOperationListener) {
        this.contentOperationListener = contentOperationListener;
    }

    @Override
    public void add(ApiComment object) {
        super.add(object);
    }

    public interface OnListener {
        void onCancelPraise();

        void onPraise();
    }
}
